package finadvisor.service.impl;

import finadvisor.dto.mutualfund.SyncResultResponse;
import finadvisor.entity.FundStatus;
import finadvisor.entity.MutualFund;
import finadvisor.entity.MutualFundAmc;
import finadvisor.entity.MutualFundDataSync;
import finadvisor.entity.MutualFundHolding;
import finadvisor.entity.MutualFundManager;
import finadvisor.entity.MutualFundReturn;
import finadvisor.entity.OptionType;
import finadvisor.entity.PlanType;
import finadvisor.entity.ReturnPeriod;
import finadvisor.entity.SyncStatus;
import finadvisor.entity.SyncType;
import finadvisor.provider.MutualFundDataProvider;
import finadvisor.provider.ProviderCallExecutor;
import finadvisor.provider.ProviderException;
import finadvisor.provider.ProviderFundData;
import finadvisor.provider.ProviderHoldingData;
import finadvisor.provider.ProviderManagerData;
import finadvisor.provider.ProviderNavPoint;
import finadvisor.provider.ProviderResponse;
import finadvisor.provider.ProviderReturnData;
import finadvisor.repository.MutualFundAmcRepository;
import finadvisor.repository.MutualFundDataSyncRepository;
import finadvisor.repository.MutualFundHoldingRepository;
import finadvisor.repository.MutualFundManagerRepository;
import finadvisor.repository.MutualFundNavHistoryRepository;
import finadvisor.repository.MutualFundRepository;
import finadvisor.repository.MutualFundReturnRepository;
import finadvisor.service.MutualFundDataSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class MutualFundDataSyncServiceImpl implements MutualFundDataSyncService {

    private static final Logger log = Logger.getLogger(MutualFundDataSyncServiceImpl.class.getName());

    private final MutualFundDataProvider provider;
    private final ProviderCallExecutor providerCallExecutor;
    private final MutualFundAmcRepository amcRepository;
    private final MutualFundRepository fundRepository;
    private final MutualFundNavHistoryRepository navHistoryRepository;
    private final MutualFundHoldingRepository holdingRepository;
    private final MutualFundManagerRepository managerRepository;
    private final MutualFundReturnRepository returnRepository;
    private final MutualFundDataSyncRepository syncLogRepository;

    @Override
    @Transactional
    public SyncResultResponse syncFunds() {
        Instant startedAt = Instant.now();
        int processed = 0;
        int failed = 0;
        String errorMessage = null;

        try {
            ProviderResponse<List<ProviderFundData>> response =
                    providerCallExecutor.executeWithRetry("syncFunds", provider::syncFunds);
            if (!response.success()) {
                return persistLog(SyncType.FULL_SYNC, startedAt, 0, 0, SyncStatus.FAILED, response.errorMessage());
            }
            for (ProviderFundData data : response.data()) {
                try {
                    upsertFund(data);
                    processed++;
                } catch (Exception ex) {
                    failed++;
                    log.log(Level.WARNING, "Failed to upsert fund " + data.schemeCode(), ex);
                }
            }
        } catch (ProviderException ex) {
            errorMessage = ex.getMessage();
            log.log(Level.SEVERE, "Full fund sync failed", ex);
        }

        SyncStatus status = resolveStatus(processed, failed, errorMessage);
        return persistLog(SyncType.FULL_SYNC, startedAt, processed, failed, status, errorMessage);
    }

    @Override
    @Transactional
    public SyncResultResponse syncNav() {
        Instant startedAt = Instant.now();
        int processed = 0;
        int failed = 0;

        for (MutualFund fund : fundRepository.findByStatusNot(FundStatus.INACTIVE)) {
            try {
                ProviderResponse<ProviderNavPoint> response = providerCallExecutor.executeWithRetry(
                        "getCurrentNav:" + fund.getSchemeCode(), () -> provider.getCurrentNav(fund.getSchemeCode()));
                if (!response.success()) {
                    failed++;
                    continue;
                }
                ProviderNavPoint point = response.data();
                if (!navHistoryRepository.existsByMutualFund_IdAndNavDate(fund.getId(), point.navDate())) {
                    navHistoryRepository.save(finadvisor.entity.MutualFundNavHistory.builder()
                            .mutualFund(fund).nav(point.nav()).navDate(point.navDate()).build());
                }
                if (fund.getNavDate() == null || !point.navDate().isBefore(fund.getNavDate())) {
                    fund.setNav(point.nav());
                    fund.setNavDate(point.navDate());
                    fundRepository.save(fund);
                }
                processed++;
            } catch (ProviderException ex) {
                failed++;
                log.log(Level.WARNING, "NAV sync failed for " + fund.getSchemeCode(), ex);
            }
        }

        return persistLog(SyncType.NAV_SYNC, startedAt, processed, failed, resolveStatus(processed, failed, null), null);
    }

    @Override
    @Transactional
    public SyncResultResponse syncHoldings() {
        Instant startedAt = Instant.now();
        int processed = 0;
        int failed = 0;

        for (MutualFund fund : fundRepository.findByStatusNot(FundStatus.INACTIVE)) {
            try {
                ProviderResponse<List<ProviderHoldingData>> response = providerCallExecutor.executeWithRetry(
                        "getHoldings:" + fund.getSchemeCode(), () -> provider.getHoldings(fund.getSchemeCode()));
                if (!response.success() || response.data().isEmpty()) {
                    continue;
                }
                var asOfDate = response.data().get(0).asOfDate();
                holdingRepository.deleteByMutualFund_IdAndAsOfDate(fund.getId(), asOfDate);
                for (ProviderHoldingData holding : response.data()) {
                    holdingRepository.save(MutualFundHolding.builder()
                            .mutualFund(fund)
                            .securityName(holding.securityName())
                            .isin(holding.isin())
                            .sector(holding.sector())
                            .assetType(finadvisor.entity.HoldingAssetType.valueOf(holding.assetType()))
                            .weightPercentage(holding.weightPercentage())
                            .quantity(holding.quantity())
                            .marketValue(holding.marketValue())
                            .asOfDate(holding.asOfDate())
                            .build());
                }
                processed++;
            } catch (ProviderException ex) {
                failed++;
                log.log(Level.WARNING, "Holdings sync failed for " + fund.getSchemeCode(), ex);
            }
        }

        return persistLog(SyncType.HOLDINGS_SYNC, startedAt, processed, failed, resolveStatus(processed, failed, null), null);
    }

    @Override
    @Transactional
    public SyncResultResponse syncReturns() {
        Instant startedAt = Instant.now();
        int processed = 0;
        int failed = 0;

        for (MutualFund fund : fundRepository.findByStatusNot(FundStatus.INACTIVE)) {
            try {
                ProviderResponse<List<ProviderReturnData>> response = providerCallExecutor.executeWithRetry(
                        "getFundReturns:" + fund.getSchemeCode(), () -> provider.getFundReturns(fund.getSchemeCode()));
                if (!response.success()) {
                    failed++;
                    continue;
                }
                for (ProviderReturnData returnData : response.data()) {
                    ReturnPeriod period = ReturnPeriod.fromCode(returnData.period());
                    MutualFundReturn entity = returnRepository.findByMutualFund_IdAndReturnPeriod(fund.getId(), period)
                            .orElseGet(() -> MutualFundReturn.builder().mutualFund(fund).returnPeriod(period).build());
                    entity.setReturnPercentage(returnData.returnPercentage());
                    entity.setAnnualized(returnData.annualized());
                    entity.setCalculatedAsOf(returnData.calculatedAsOf());
                    returnRepository.save(entity);
                }
                processed++;
            } catch (ProviderException ex) {
                failed++;
                log.log(Level.WARNING, "Returns sync failed for " + fund.getSchemeCode(), ex);
            }
        }

        return persistLog(SyncType.RETURNS_SYNC, startedAt, processed, failed, resolveStatus(processed, failed, null), null);
    }

    @Override
    @Transactional
    public SyncResultResponse syncManagers() {
        Instant startedAt = Instant.now();
        int processed = 0;
        int failed = 0;

        for (MutualFund fund : fundRepository.findByStatusNot(FundStatus.INACTIVE)) {
            try {
                ProviderResponse<List<ProviderManagerData>> response = providerCallExecutor.executeWithRetry(
                        "getFundManagers:" + fund.getSchemeCode(), () -> provider.getFundManagers(fund.getSchemeCode()));
                if (!response.success()) {
                    failed++;
                    continue;
                }
                managerRepository.deleteByMutualFund_Id(fund.getId());
                for (ProviderManagerData manager : response.data()) {
                    managerRepository.save(MutualFundManager.builder()
                            .mutualFund(fund)
                            .name(manager.name())
                            .designation(manager.designation())
                            .experienceYears(manager.experienceYears())
                            .joiningDate(manager.joiningDate())
                            .bio(manager.bio())
                            .active(manager.active())
                            .build());
                }
                processed++;
            } catch (ProviderException ex) {
                failed++;
                log.log(Level.WARNING, "Manager sync failed for " + fund.getSchemeCode(), ex);
            }
        }

        return persistLog(SyncType.MANAGER_SYNC, startedAt, processed, failed, resolveStatus(processed, failed, null), null);
    }

    @Override
    public List<SyncResultResponse> syncAll() {
        return List.of(syncFunds(), syncNav(), syncHoldings(), syncReturns(), syncManagers());
    }

    private void upsertFund(ProviderFundData data) {
        MutualFundAmc amc = amcRepository.findByCode(data.amcCode())
                .orElseGet(() -> amcRepository.save(MutualFundAmc.builder()
                        .code(data.amcCode()).name(data.amcName()).status(FundStatus.ACTIVE).build()));

        MutualFund fund = fundRepository.findBySchemeCode(data.schemeCode())
                .orElseGet(() -> MutualFund.builder().schemeCode(data.schemeCode()).status(FundStatus.ACTIVE).build());

        fund.setIsin(data.isin());
        fund.setAmc(amc);
        fund.setSchemeName(data.schemeName());
        fund.setShortName(data.shortName());
        fund.setCategory(data.category());
        fund.setSubCategory(data.subCategory());
        fund.setPlanType(PlanType.valueOf(data.planType()));
        fund.setOptionType(OptionType.valueOf(data.optionType()));
        fund.setAssetClass(finadvisor.entity.AssetClass.valueOf(data.assetClass()));
        fund.setInvestmentObjective(data.investmentObjective());
        fund.setRiskLevel(finadvisor.entity.FundRiskLevel.valueOf(data.riskLevel()));
        fund.setBenchmark(data.benchmark());
        fund.setExpenseRatio(data.expenseRatio());
        fund.setExitLoad(data.exitLoad());
        fund.setMinimumLumpsum(data.minimumLumpsum());
        fund.setMinimumSip(data.minimumSip());
        fund.setAum(data.aum());
        fund.setNav(data.nav());
        fund.setNavDate(data.navDate());
        fund.setInceptionDate(data.inceptionDate());
        fund.setFundManager(data.fundManager());
        fundRepository.save(fund);
    }

    private SyncStatus resolveStatus(int processed, int failed, String errorMessage) {
        if (errorMessage != null && processed == 0) {
            return SyncStatus.FAILED;
        }
        if (failed == 0) {
            return SyncStatus.SUCCESS;
        }
        return processed > 0 ? SyncStatus.PARTIAL_FAILURE : SyncStatus.FAILED;
    }

    private SyncResultResponse persistLog(SyncType type, Instant startedAt, int processed, int failed,
                                           SyncStatus status, String errorMessage) {
        Instant completedAt = Instant.now();
        MutualFundDataSync entity = syncLogRepository.save(MutualFundDataSync.builder()
                .provider(provider.getProviderName())
                .syncType(type)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .recordsProcessed(processed)
                .recordsFailed(failed)
                .status(status)
                .errorMessage(errorMessage)
                .build());
        return new SyncResultResponse(entity.getProvider(), entity.getSyncType().name(), entity.getStatus().name(),
                entity.getRecordsProcessed(), entity.getRecordsFailed(), entity.getStartedAt(), entity.getCompletedAt(),
                entity.getErrorMessage());
    }
}
