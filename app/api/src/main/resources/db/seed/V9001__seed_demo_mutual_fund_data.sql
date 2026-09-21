-- DEMO / DEVELOPMENT ONLY seed data for the mutual fund platform.
-- Applied only when the "dev" Spring profile is active (application-dev.properties adds
-- classpath:db/seed to spring.flyway.locations). MUST NOT run against production databases.
-- All AMC names, scheme names, ISINs, holdings and fund manager names below are entirely
-- fictional. NAV history and returns are synthetically generated for UI/API development and
-- testing purposes only and must never be presented to end users as real market data.

INSERT INTO mutual_fund_amcs (id, name, short_name, code, website_url, description, status)
VALUES
    (gen_random_uuid(), 'Nilgiri Mutual Fund', 'Nilgiri MF', 'NILGIRI', 'https://example.com/nilgiri', 'Fictional demo AMC used for development and testing only.', 'ACTIVE'),
    (gen_random_uuid(), 'Sundara Asset Management', 'Sundara AMC', 'SUNDARA', 'https://example.com/sundara', 'Fictional demo AMC used for development and testing only.', 'ACTIVE'),
    (gen_random_uuid(), 'Vaidik Capital AMC', 'Vaidik AMC', 'VAIDIK', 'https://example.com/vaidik', 'Fictional demo AMC used for development and testing only.', 'ACTIVE'),
    (gen_random_uuid(), 'Triveni Investment Managers', 'Triveni IM', 'TRIVENI', 'https://example.com/triveni', 'Fictional demo AMC used for development and testing only.', 'ACTIVE'),
    (gen_random_uuid(), 'Kaveri Fund House', 'Kaveri FH', 'KAVERI', 'https://example.com/kaveri', 'Fictional demo AMC used for development and testing only.', 'ACTIVE');

INSERT INTO mutual_funds (
    id, scheme_code, isin, amc_id, scheme_name, short_name, category, sub_category, plan_type, option_type,
    asset_class, investment_objective, risk_level, benchmark, expense_ratio, exit_load, minimum_lumpsum,
    minimum_sip, aum, nav, nav_date, inception_date, fund_manager, status
)
SELECT gen_random_uuid(), v.scheme_code, v.isin, a.id, v.scheme_name, v.short_name, v.category, v.sub_category,
       v.plan_type, v.option_type, v.asset_class, v.investment_objective, v.risk_level, v.benchmark,
       v.expense_ratio, v.exit_load, v.minimum_lumpsum, v.minimum_sip, v.aum,
       10.0000, CURRENT_DATE - INTERVAL '3 years', CURRENT_DATE - INTERVAL '3 years', v.fund_manager, 'ACTIVE'
FROM (VALUES
    ('DEMO001','INE000A01011','NILGIRI','Nilgiri Bluechip Equity Fund','Nilgiri Bluechip','Equity','Large Cap','DIRECT','GROWTH','EQUITY','Long-term capital appreciation by investing predominantly in large-cap companies.','VERY_HIGH','Nifty 100 TRI',1.05,'1% if redeemed within 365 days',5000,500,12450.75,'Ananya Rao'),
    ('DEMO002','INE000A01029','NILGIRI','Nilgiri Flexi Cap Fund','Nilgiri Flexi Cap','Equity','Flexi Cap','DIRECT','GROWTH','EQUITY','Long-term capital growth across market capitalisations with flexible allocation.','VERY_HIGH','Nifty 500 TRI',0.85,'1% if redeemed within 365 days',5000,500,9820.40,'Karthik Subramaniam'),
    ('DEMO003','INE000A01037','SUNDARA','Sundara Midcap Growth Fund','Sundara Midcap','Equity','Mid Cap','DIRECT','GROWTH','EQUITY','Capital appreciation by investing predominantly in mid-cap companies.','VERY_HIGH','Nifty Midcap 150 TRI',1.15,'1% if redeemed within 365 days',5000,500,6210.60,'Priya Deshmukh'),
    ('DEMO004','INE000A01045','SUNDARA','Sundara Smallcap Opportunities Fund','Sundara Smallcap','Equity','Small Cap','DIRECT','GROWTH','EQUITY','Long-term capital appreciation by investing predominantly in small-cap companies.','VERY_HIGH','Nifty Smallcap 250 TRI',1.35,'1% if redeemed within 365 days',5000,500,3125.90,'Rohan Mehta'),
    ('DEMO005','INE000A01052','VAIDIK','Vaidik Tax Saver Fund','Vaidik Tax Saver','Equity','ELSS','DIRECT','GROWTH','EQUITY','Long-term capital growth with tax benefits under Section 80C and a 3-year lock-in.','VERY_HIGH','Nifty 50 TRI',0.95,'Nil (lock-in of 3 years)',500,500,4520.30,'Meera Iyer'),
    ('DEMO006','INE000A01060','VAIDIK','Vaidik Liquid Fund','Vaidik Liquid','Debt','Liquid','DIRECT','GROWTH','DEBT','Reasonable returns with high liquidity by investing in money market instruments.','LOW','CRISIL Liquid Fund Index',0.20,'Nil',5000,1000,15840.00,'Arjun Nair'),
    ('DEMO007','INE000A01078','TRIVENI','Triveni Short Duration Debt Fund','Triveni Short Duration','Debt','Short Duration','DIRECT','GROWTH','DEBT','Income generation by investing in debt and money market instruments of short maturity.','MODERATE','CRISIL Short Duration Debt Index',0.45,'0.25% if redeemed within 30 days',5000,1000,4380.20,'Divya Krishnan'),
    ('DEMO008','INE000A01086','TRIVENI','Triveni Balanced Advantage Fund','Triveni BAF','Hybrid','Balanced Advantage','DIRECT','GROWTH','HYBRID','Long-term growth with lower volatility via dynamic equity and debt allocation.','MODERATELY_HIGH','NIFTY 50 Hybrid Composite Debt 50:50 Index',1.10,'1% if redeemed within 365 days',5000,500,7650.50,'Vikram Chandran'),
    ('DEMO009','INE000A01094','KAVERI','Kaveri Aggressive Hybrid Fund','Kaveri Aggressive Hybrid','Hybrid','Aggressive Hybrid','DIRECT','GROWTH','HYBRID','Long-term capital appreciation with a predominantly equity-oriented hybrid allocation.','HIGH','CRISIL Hybrid 35+65 Aggressive Index',1.20,'1% if redeemed within 365 days',5000,500,5230.80,'Sneha Bhatt'),
    ('DEMO010','INE000A01102','KAVERI','Kaveri Nifty 50 Index Fund','Kaveri Nifty Index','Equity','Index Funds','DIRECT','GROWTH','EQUITY','Returns that closely track the Nifty 50 TRI, before expenses and tracking error.','VERY_HIGH','Nifty 50 TRI',0.20,'Nil',5000,500,2980.10,'Aditya Menon')
) AS v(scheme_code, isin, amc_code, scheme_name, short_name, category, sub_category, plan_type, option_type,
       asset_class, investment_objective, risk_level, benchmark, expense_ratio, exit_load, minimum_lumpsum,
       minimum_sip, aum, fund_manager)
JOIN mutual_fund_amcs a ON a.code = v.amc_code;

INSERT INTO mutual_fund_managers (id, mutual_fund_id, name, experience_years, joining_date, designation, bio, active)
SELECT gen_random_uuid(), mf.id, mf.fund_manager, v.experience_years, mf.inception_date, 'Fund Manager',
       'Fictional demo fund manager profile used for development and testing only.', true
FROM mutual_funds mf
JOIN (VALUES
    ('DEMO001', 14), ('DEMO002', 11), ('DEMO003', 9), ('DEMO004', 8), ('DEMO005', 16),
    ('DEMO006', 12), ('DEMO007', 10), ('DEMO008', 15), ('DEMO009', 13), ('DEMO010', 7)
) AS v(scheme_code, experience_years) ON v.scheme_code = mf.scheme_code;

-- Top holdings (fictional securities only) for equity/hybrid schemes.
INSERT INTO mutual_fund_holdings (id, mutual_fund_id, security_name, isin, sector, asset_type, weight_percentage, quantity, market_value, as_of_date)
SELECT gen_random_uuid(), mf.id, v.security_name, v.isin, v.sector, v.asset_type, v.weight_percentage, v.quantity, v.market_value, CURRENT_DATE - 7
FROM mutual_funds mf
JOIN (VALUES
    ('DEMO001','Alpha Industries Ltd','INE100A01011','Financial Services','EQUITY',9.20,1250000,458000000.00),
    ('DEMO001','Beta Technologies Ltd','INE100A01029','Information Technology','EQUITY',8.10,980000,402000000.00),
    ('DEMO001','Gamma Bank Ltd','INE100A01037','Financial Services','EQUITY',7.40,1100000,367000000.00),
    ('DEMO001','Delta Energy Ltd','INE100A01045','Energy','EQUITY',6.30,860000,312000000.00),
    ('DEMO001','Epsilon Consumer Ltd','INE100A01052','Consumer Goods','EQUITY',5.60,540000,278000000.00),
    ('DEMO001','Zeta Pharma Ltd','INE100A01060','Healthcare','EQUITY',4.90,410000,243000000.00),
    ('DEMO002','Beta Technologies Ltd','INE100A01029','Information Technology','EQUITY',8.70,760000,318000000.00),
    ('DEMO002','Theta Automobiles Ltd','INE100A01078','Automobile','EQUITY',7.30,505000,271000000.00),
    ('DEMO002','Gamma Bank Ltd','INE100A01037','Financial Services','EQUITY',6.80,610000,252000000.00),
    ('DEMO002','Iota Infra Ltd','INE100A01086','Industrials','EQUITY',5.90,455000,219000000.00),
    ('DEMO002','Epsilon Consumer Ltd','INE100A01052','Consumer Goods','EQUITY',5.20,380000,193000000.00),
    ('DEMO002','Zeta Pharma Ltd','INE100A01060','Healthcare','EQUITY',4.60,320000,171000000.00),
    ('DEMO003','Kappa Materials Ltd','INE100A01094','Materials','EQUITY',6.90,720000,187000000.00),
    ('DEMO003','Iota Infra Ltd','INE100A01086','Industrials','EQUITY',6.20,655000,168000000.00),
    ('DEMO003','Theta Automobiles Ltd','INE100A01078','Automobile','EQUITY',5.70,590000,154000000.00),
    ('DEMO003','Lambda Textiles Ltd','INE100A01102','Consumer Goods','EQUITY',5.10,510000,138000000.00),
    ('DEMO003','Mu Chemicals Ltd','INE100A01110','Materials','EQUITY',4.60,470000,124000000.00),
    ('DEMO003','Nu Realty Ltd','INE100A01128','Real Estate','EQUITY',4.10,420000,111000000.00),
    ('DEMO004','Xi Logistics Ltd','INE100A01136','Industrials','EQUITY',5.40,610000,88000000.00),
    ('DEMO004','Omicron Foods Ltd','INE100A01144','Consumer Goods','EQUITY',4.90,540000,80000000.00),
    ('DEMO004','Pi Electronics Ltd','INE100A01151','Information Technology','EQUITY',4.50,505000,73000000.00),
    ('DEMO004','Rho Textiles Ltd','INE100A01169','Consumer Goods','EQUITY',4.10,460000,66000000.00),
    ('DEMO004','Sigma Ports Ltd','INE100A01177','Industrials','EQUITY',3.70,410000,60000000.00),
    ('DEMO004','Tau Specialty Chemicals Ltd','INE100A01185','Materials','EQUITY',3.30,370000,54000000.00),
    ('DEMO005','Alpha Industries Ltd','INE100A01011','Financial Services','EQUITY',8.50,410000,384000000.00),
    ('DEMO005','Gamma Bank Ltd','INE100A01037','Financial Services','EQUITY',7.60,380000,343000000.00),
    ('DEMO005','Beta Technologies Ltd','INE100A01029','Information Technology','EQUITY',6.90,320000,312000000.00),
    ('DEMO005','Zeta Pharma Ltd','INE100A01060','Healthcare','EQUITY',5.80,270000,262000000.00),
    ('DEMO005','Delta Energy Ltd','INE100A01045','Energy','EQUITY',5.10,240000,230000000.00),
    ('DEMO006','182-Day T-Bill 2026','IN002A01234','Government Securities','DEBT',18.50,NULL,2930000000.00),
    ('DEMO006','Upsilon Finance NCD 2026','INF200A01012','Corporate Bond','DEBT',12.30,NULL,1949000000.00),
    ('DEMO006','91-Day T-Bill 2026','IN002A01242','Government Securities','DEBT',10.80,NULL,1711000000.00),
    ('DEMO006','Phi Housing Finance CP 2026','INF200A01020','Corporate Bond','DEBT',9.40,NULL,1489000000.00),
    ('DEMO007','7.26% GOI 2033','IN0020230012','Government Securities','DEBT',15.60,NULL,683000000.00),
    ('DEMO007','Chi Capital NCD 2028','INF210A01018','Corporate Bond','DEBT',11.20,NULL,491000000.00),
    ('DEMO007','Psi Power Finance NCD 2027','INF210A01026','Corporate Bond','DEBT',9.80,NULL,429000000.00),
    ('DEMO007','6.79% GOI 2029','IN0020220045','Government Securities','DEBT',8.50,NULL,372000000.00),
    ('DEMO008','Alpha Industries Ltd','INE100A01011','Financial Services','EQUITY',6.10,320000,467000000.00),
    ('DEMO008','Beta Technologies Ltd','INE100A01029','Information Technology','EQUITY',5.40,270000,413000000.00),
    ('DEMO008','7.26% GOI 2033','IN0020230012','Government Securities','DEBT',14.20,NULL,1087000000.00),
    ('DEMO008','Omega Finance NCD 2027','INF220A01011','Corporate Bond','DEBT',9.60,NULL,735000000.00),
    ('DEMO009','Gamma Bank Ltd','INE100A01037','Financial Services','EQUITY',7.80,360000,408000000.00),
    ('DEMO009','Zeta Pharma Ltd','INE100A01060','Healthcare','EQUITY',6.50,300000,340000000.00),
    ('DEMO009','Theta Automobiles Ltd','INE100A01078','Automobile','EQUITY',5.70,265000,298000000.00),
    ('DEMO009','7.26% GOI 2033','IN0020230012','Government Securities','DEBT',10.40,NULL,544000000.00),
    ('DEMO010','Alpha Industries Ltd','INE100A01011','Financial Services','EQUITY',10.80,3250000,322000000.00),
    ('DEMO010','Beta Technologies Ltd','INE100A01029','Information Technology','EQUITY',9.40,2810000,280000000.00),
    ('DEMO010','Gamma Bank Ltd','INE100A01037','Financial Services','EQUITY',8.60,2560000,256000000.00),
    ('DEMO010','Delta Energy Ltd','INE100A01045','Energy','EQUITY',6.90,2050000,206000000.00),
    ('DEMO010','Epsilon Consumer Ltd','INE100A01052','Consumer Goods','EQUITY',5.80,1720000,173000000.00)
) AS v(scheme_code, security_name, isin, sector, asset_type, weight_percentage, quantity, market_value)
    ON v.scheme_code = mf.scheme_code;

-- Synthetic daily (business-day) NAV history from each fund's inception date to today, derived from a
-- deterministic drift + oscillation formula per fund so returns computed below stay internally consistent.
WITH fund_params (scheme_code, annual_drift, amplitude, wave_days) AS (
    VALUES
        ('DEMO001', 0.13, 0.020, 47),
        ('DEMO002', 0.14, 0.022, 53),
        ('DEMO003', 0.16, 0.030, 41),
        ('DEMO004', 0.18, 0.040, 37),
        ('DEMO005', 0.13, 0.021, 45),
        ('DEMO006', 0.065, 0.002, 29),
        ('DEMO007', 0.075, 0.004, 31),
        ('DEMO008', 0.10, 0.012, 39),
        ('DEMO009', 0.12, 0.018, 43),
        ('DEMO010', 0.125, 0.019, 49)
),
fund_days AS (
    SELECT mf.id AS mutual_fund_id, fp.annual_drift, fp.amplitude, fp.wave_days, gs.day_offset,
           (mf.inception_date + gs.day_offset) AS nav_date
    FROM mutual_funds mf
    JOIN fund_params fp ON fp.scheme_code = mf.scheme_code
    CROSS JOIN LATERAL generate_series(0, (CURRENT_DATE - mf.inception_date)::int) AS gs(day_offset)
    WHERE EXTRACT(ISODOW FROM (mf.inception_date + gs.day_offset)) < 6
)
INSERT INTO mutual_fund_nav_history (id, mutual_fund_id, nav, nav_date)
SELECT gen_random_uuid(), mutual_fund_id,
       ROUND((10.0 * POWER(1 + annual_drift / 365.0, day_offset) *
              (1 + amplitude * SIN(2 * PI() * day_offset / wave_days)))::numeric, 4),
       nav_date
FROM fund_days;

-- Sync each fund's "current" NAV/date to the most recent generated history point.
UPDATE mutual_funds mf
SET nav = h.nav, nav_date = h.nav_date
FROM (
    SELECT DISTINCT ON (mutual_fund_id) mutual_fund_id, nav, nav_date
    FROM mutual_fund_nav_history
    ORDER BY mutual_fund_id, nav_date DESC
) h
WHERE mf.id = h.mutual_fund_id;

-- Trailing returns derived from the generated NAV history (absolute for <1Y, annualized/CAGR for >=1Y).
WITH periods (period_code, days_back, is_annualized) AS (
    VALUES
        ('1D', 1, false), ('1W', 7, false), ('1M', 30, false), ('3M', 91, false),
        ('6M', 182, false), ('1Y', 365, true), ('3Y', 1095, true)
)
INSERT INTO mutual_fund_returns (id, mutual_fund_id, return_period, return_percentage, annualized, calculated_as_of)
SELECT gen_random_uuid(), mf.id, p.period_code,
       ROUND(
           CASE WHEN p.is_annualized THEN
               (POWER((mf.nav / start_nav.nav)::numeric, 365.0 / (mf.nav_date - start_nav.nav_date)) - 1) * 100
           ELSE
               ((mf.nav / start_nav.nav) - 1) * 100
           END, 2),
       p.is_annualized, mf.nav_date
FROM mutual_funds mf
CROSS JOIN periods p
JOIN LATERAL (
    SELECT nav, nav_date FROM mutual_fund_nav_history h
    WHERE h.mutual_fund_id = mf.id AND h.nav_date <= (mf.nav_date - p.days_back)
    ORDER BY h.nav_date DESC
    LIMIT 1
) start_nav ON true
WHERE (mf.nav_date - mf.inception_date) >= p.days_back;

-- Since-inception return (CAGR once the fund has a 1-year+ track record, else absolute).
INSERT INTO mutual_fund_returns (id, mutual_fund_id, return_period, return_percentage, annualized, calculated_as_of)
SELECT gen_random_uuid(), mf.id, 'SINCE_INCEPTION',
       ROUND(
           CASE WHEN (mf.nav_date - mf.inception_date) >= 365 THEN
               (POWER((mf.nav / 10.0)::numeric, 365.0 / GREATEST(mf.nav_date - mf.inception_date, 1)) - 1) * 100
           ELSE
               ((mf.nav / 10.0) - 1) * 100
           END, 2),
       (mf.nav_date - mf.inception_date) >= 365,
       mf.nav_date
FROM mutual_funds mf;

INSERT INTO mutual_fund_data_sync (id, provider, sync_type, started_at, completed_at, records_processed, records_failed, status, error_message)
VALUES (gen_random_uuid(), 'DEMO_SEED', 'FULL_SYNC', now(), now(), 10, 0, 'SUCCESS', NULL);
