package finadvisor.repository;

import finadvisor.entity.FundStatus;
import finadvisor.entity.MutualFund;
import finadvisor.entity.MutualFundAmc;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Composable JPA {@link Specification}s backing the mutual fund search/filter API (Part 7). */
public final class MutualFundSpecifications {

    private MutualFundSpecifications() {
    }

    public static Specification<MutualFund> matching(String search, String amc, String category, String subCategory,
                                                       String planType, String optionType, String riskLevel,
                                                       BigDecimal minAum, BigDecimal maxAum,
                                                       BigDecimal minExpenseRatio, BigDecimal maxExpenseRatio) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.notEqual(root.get("status"), FundStatus.INACTIVE));

            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("schemeName")), like),
                        cb.like(cb.lower(root.get("shortName")), like),
                        cb.like(cb.lower(root.get("schemeCode")), like)));
            }
            if (amc != null && !amc.isBlank()) {
                query.distinct(true);
                jakarta.persistence.criteria.Join<MutualFund, MutualFundAmc> amcJoin = root.join("amc");
                predicates.add(cb.equal(cb.upper(amcJoin.get("code")), amc.trim().toUpperCase()));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));
            }
            if (subCategory != null && !subCategory.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("subCategory")), subCategory.trim().toLowerCase()));
            }
            if (planType != null && !planType.isBlank()) {
                predicates.add(cb.equal(root.get("planType"), finadvisor.entity.PlanType.valueOf(planType.trim().toUpperCase())));
            }
            if (optionType != null && !optionType.isBlank()) {
                predicates.add(cb.equal(root.get("optionType"), finadvisor.entity.OptionType.valueOf(optionType.trim().toUpperCase())));
            }
            if (riskLevel != null && !riskLevel.isBlank()) {
                predicates.add(cb.equal(root.get("riskLevel"), finadvisor.entity.FundRiskLevel.valueOf(riskLevel.trim().toUpperCase())));
            }
            if (minAum != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("aum"), minAum));
            }
            if (maxAum != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("aum"), maxAum));
            }
            if (minExpenseRatio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expenseRatio"), minExpenseRatio));
            }
            if (maxExpenseRatio != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expenseRatio"), maxExpenseRatio));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
