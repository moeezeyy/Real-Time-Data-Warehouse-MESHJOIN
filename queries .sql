use starschema;
-- q1 --
SELECT 
    DATE_FORMAT(order_date, '%Y-%m') AS month,
    CASE 
        WHEN DAYOFWEEK(order_date) IN (1, 7) THEN 'Weekend'
        ELSE 'Weekday'
    END AS day_type,
    p.product_name,
    SUM(f.total_sales_value) AS total_revenue
FROM Fact_Transactions f
JOIN Dim_Product p ON f.product_id = p.product_id
GROUP BY month, day_type, p.product_name
ORDER BY month, day_type, total_revenue DESC
LIMIT 5;

WITH QuarterlyRevenue AS (
    SELECT 
        QUARTER(order_date) AS quarter,
        customer_id, 
        SUM(total_sales_value) AS total_revenue
    FROM Fact_Transactions
    WHERE YEAR(order_date) = 2019
    GROUP BY customer_id, QUARTER(order_date)
),
QuarterlyGrowth AS (
    SELECT 
        quarter,
        customer_id,
        total_revenue,
        -- LAG function to get the previous quarter's revenue for each customer
        LAG(total_revenue) OVER (PARTITION BY customer_id ORDER BY quarter) AS prev_quarter_revenue,
        -- Calculate growth rate as the percentage change between current and previous quarter
        CASE 
            WHEN LAG(total_revenue) OVER (PARTITION BY customer_id ORDER BY quarter) IS NOT NULL 
            THEN ROUND(((total_revenue - LAG(total_revenue) OVER (PARTITION BY customer_id ORDER BY quarter)) 
                        / LAG(total_revenue) OVER (PARTITION BY customer_id ORDER BY quarter)) * 100, 2)
            ELSE 0  -- For the first quarter, set the growth rate to 0
        END AS growth_rate
    FROM QuarterlyRevenue
)
SELECT 
    quarter,
    customer_id,
    total_revenue,
    growth_rate
FROM QuarterlyGrowth
ORDER BY customer_id, quarter;


-- q3 --
SELECT 
    p.product_name,
    SUM(f.total_sales_value) AS total_sales
FROM Fact_Transactions f
JOIN Dim_Product p ON f.product_id = p.product_id
GROUP BY p.product_name
ORDER BY total_sales DESC;


-- q4 --
SELECT 
    CASE 
        WHEN MONTH(order_date) IN (3, 4, 5) THEN 'Spring'
        WHEN MONTH(order_date) IN (6, 7, 8) THEN 'Summer'
        WHEN MONTH(order_date) IN (9, 10, 11) THEN 'Fall'
        ELSE 'Winter'
    END AS season,
    p.product_name,
    SUM(f.total_sales_value) AS total_sales
FROM Fact_Transactions f
JOIN Dim_Product p ON f.product_id = p.product_id
GROUP BY season, p.product_name
ORDER BY season, total_sales DESC;

-- q5 --
SELECT 
    DATE_FORMAT(order_date, '%Y-%m') AS month,
    p.product_name,
    SUM(f.total_sales_value) AS monthly_revenue,
    ROUND((SUM(f.total_sales_value) - LAG(SUM(f.total_sales_value)) 
           OVER (PARTITION BY p.product_name ORDER BY DATE_FORMAT(order_date, '%Y-%m'))) / 
           LAG(SUM(f.total_sales_value)) OVER (PARTITION BY p.product_name ORDER BY DATE_FORMAT(order_date, '%Y-%m')) * 100, 2) 
           AS volatility_percentage
FROM Fact_Transactions f
JOIN Dim_Product p ON f.product_id = p.product_id
GROUP BY month, p.product_name;

-- q6 --
SELECT 
    f1.product_id AS product_1, 
    f2.product_id AS product_2, 
    COUNT(*) AS purchase_count
FROM Fact_Transactions f1
JOIN Fact_Transactions f2 ON f1.order_id = f2.order_id 
WHERE f1.product_id < f2.product_id
GROUP BY f1.product_id, f2.product_id
ORDER BY purchase_count DESC
LIMIT 5;


-- q7 --

SELECT 
    YEAR(order_date) AS year,
    p.product_name,
    SUM(f.total_sales_value) AS total_revenue
FROM Fact_Transactions f
JOIN Dim_Product p ON f.product_id = p.product_id
GROUP BY ROLLUP(year, p.product_name)
ORDER BY year, p.product_name;

-- q8 --
SELECT 
    p.product_name,
    CASE 
        WHEN MONTH(order_date) BETWEEN 1 AND 6 THEN 'H1'
        ELSE 'H2'
    END AS half_year,
    SUM(f.total_sales_value) AS total_revenue,
    SUM(f.quantity_ordered) AS total_quantity
FROM Fact_Transactions f
JOIN Dim_Product p ON f.product_id = p.product_id
GROUP BY p.product_name, half_year
ORDER BY p.product_name, half_year;

-- q9 --
WITH DailySales AS (
    SELECT 
        product_id,
        order_date,
        SUM(total_sales_value) AS daily_sales
    FROM Fact_Transactions
    GROUP BY product_id, order_date
),
DailyAvg AS (
    SELECT 
        product_id,
        AVG(daily_sales) AS avg_daily_sales
    FROM DailySales
    GROUP BY product_id
)
SELECT 
    ds.product_id,
    ds.order_date,
    ds.daily_sales,
    da.avg_daily_sales,
    CASE 
        WHEN ds.daily_sales > da.avg_daily_sales * 2 THEN 'Outlier'
        ELSE 'Normal'
    END AS sales_status
FROM DailySales ds
JOIN DailyAvg da ON ds.product_id = da.product_id
ORDER BY ds.product_id, ds.order_date;

-- q10 --

CREATE VIEW STORE_QUARTERLY_SALES AS
WITH QuarterlySales AS (
    SELECT 
        QUARTER(order_date) AS quarter,
        customer_id,
        SUM(total_sales_value) AS total_revenue
    FROM Fact_Transactions
    GROUP BY customer_id, QUARTER(order_date)
)
SELECT 
    quarter,
    customer_id,
    total_revenue
FROM QuarterlySales
ORDER BY customer_id, quarter;

drop view STORE_QUARTERLY_SALES;







