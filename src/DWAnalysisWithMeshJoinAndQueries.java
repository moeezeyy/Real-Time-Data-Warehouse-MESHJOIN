package src;

import java.sql.*;

public class DWAnalysisWithMeshJoinAndQueries {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/starschema";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "123456moeez";

    public static void main(String[] args) throws SQLException {
        // Establish a connection to the database
        Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

        // Load data using MESHJOIN (this will call the method that performs the MESHJOIN operation)
        // Assuming meshJoin() is implemented elsewhere and loads data into tables
        performMeshJoinAndLoadData(conn);

        // Perform OLAP Queries (execute all queries in sequence)
        query1(conn);
        query2(conn);
        query3(conn);
        query4(conn);
        query5(conn);
        query6(conn);
        query7(conn);
        query8(conn);
        query9(conn);
        query10(conn);

        // Close the connection
        conn.close();
    }

    // Method to perform MESHJOIN data loading (using your meshjoin logic)
    public static void performMeshJoinAndLoadData(Connection conn) {
        // This method assumes that the MESHJOIN logic (loading data into tables) is implemented here.
        // You can either use multi-threading to load data or process it sequentially.
        System.out.println("Loading data using MESHJOIN...");

        // Assuming meshJoin() is a method that executes the MESHJOIN logic and loads data into tables.
        // meshJoin(conn); 
    }

    // Q1: Top Revenue-Generating Products on Weekdays and Weekends with Monthly Drill-Down
    public static void query1(Connection conn) throws SQLException {
        String queryQ1 = "SELECT " +
                             "product_name, " +
                             "CASE WHEN DAYOFWEEK(order_date) IN (1, 7) THEN 'Weekend' ELSE 'Weekday' END AS day_type, " +
                             "MONTH(order_date) AS month, " +
                             "SUM(total_sales_value) AS total_sales " +
                          "FROM Fact_Transactions ft " +
                          "JOIN Dim_Product dp ON ft.product_id = dp.product_id " +
                          "GROUP BY product_name, day_type, month " +
                          "ORDER BY month, total_sales DESC";

        executeQuery(conn, queryQ1);
    }

    // Q2: Trend Analysis of Store Revenue Growth Rate Quarterly for 2017
    public static void query2(Connection conn) throws SQLException {
        String queryQ2 = "WITH QuarterlyRevenue AS (" +
                             "SELECT " +
                                 "QUARTER(order_date) AS quarter, " +
                                 "customer_id, " +
                                 "SUM(total_sales_value) AS total_revenue " +
                             "FROM Fact_Transactions " +
                             "WHERE YEAR(order_date) = 2017 " +
                             "GROUP BY customer_id, QUARTER(order_date) " +
                          "), QuarterlyGrowth AS (" +
                             "SELECT " +
                                 "quarter, " +
                                 "customer_id, " +
                                 "total_revenue, " +
                                 "LAG(total_revenue) OVER (PARTITION BY customer_id ORDER BY quarter) AS prev_quarter_revenue, " +
                                 "CASE " +
                                     "WHEN LAG(total_revenue) OVER (PARTITION BY customer_id ORDER BY quarter) IS NOT NULL " +
                                     "THEN ROUND(((total_revenue - LAG(total_revenue) OVER (PARTITION BY customer_id ORDER BY quarter)) " +
                                                 "/ LAG(total_revenue) OVER (PARTITION BY customer_id ORDER BY quarter)) * 100, 2) " +
                                     "ELSE 0 " +
                                 "END AS growth_rate " +
                             "FROM QuarterlyRevenue " +
                          ") " +
                          "SELECT " +
                              "quarter, " +
                              "customer_id, " +
                              "total_revenue, " +
                              "growth_rate " +
                          "FROM QuarterlyGrowth " +
                          "ORDER BY customer_id, quarter";

        executeQuery(conn, queryQ2);
    }

    // Q3: Detailed Supplier Sales Contribution by Store and Product Name
    public static void query3(Connection conn) throws SQLException {
        String queryQ3 = "SELECT " +
                             "store_id, " +
                             "supplier_name, " +
                             "product_name, " +
                             "SUM(total_sales_value) AS total_sales " +
                          "FROM Fact_Transactions ft " +
                          "JOIN Dim_Product dp ON ft.product_id = dp.product_id " +
                          "JOIN Dim_Supplier ds ON dp.supplier_id = ds.supplier_id " +
                          "GROUP BY store_id, supplier_name, product_name " +
                          "ORDER BY store_id, supplier_name, product_name";

        executeQuery(conn, queryQ3);
    }

    // Q4: Seasonal Analysis of Product Sales Using Dynamic Drill-Down
    public static void query4(Connection conn) throws SQLException {
        String queryQ4 = "SELECT " +
                             "product_name, " +
                             "CASE " +
                                 "WHEN MONTH(order_date) IN (3, 4, 5) THEN 'Spring' " +
                                 "WHEN MONTH(order_date) IN (6, 7, 8) THEN 'Summer' " +
                                 "WHEN MONTH(order_date) IN (9, 10, 11) THEN 'Fall' " +
                                 "ELSE 'Winter' " +
                             "END AS season, " +
                             "SUM(total_sales_value) AS total_sales " +
                          "FROM Fact_Transactions ft " +
                          "JOIN Dim_Product dp ON ft.product_id = dp.product_id " +
                          "GROUP BY product_name, season " +
                          "ORDER BY season, total_sales DESC";

        executeQuery(conn, queryQ4);
    }

    // Q5: Store-Wise and Supplier-Wise Monthly Revenue Volatility
    public static void query5(Connection conn) throws SQLException {
        String queryQ5 = "WITH MonthlyRevenue AS (" +
                             "SELECT " +
                                 "store_id, " +
                                 "supplier_name, " +
                                 "MONTH(order_date) AS month, " +
                                 "SUM(total_sales_value) AS total_sales " +
                             "FROM Fact_Transactions ft " +
                             "JOIN Dim_Product dp ON ft.product_id = dp.product_id " +
                             "JOIN Dim_Supplier ds ON dp.supplier_id = ds.supplier_id " +
                             "GROUP BY store_id, supplier_name, month " +
                          "), RevenueVolatility AS (" +
                             "SELECT " +
                                 "store_id, " +
                                 "supplier_name, " +
                                 "month, " +
                                 "total_sales, " +
                                 "LAG(total_sales) OVER (PARTITION BY store_id, supplier_name ORDER BY month) AS prev_month_sales, " +
                                 "CASE " +
                                     "WHEN LAG(total_sales) OVER (PARTITION BY store_id, supplier_name ORDER BY month) IS NOT NULL " +
                                     "THEN ROUND(((total_sales - LAG(total_sales) OVER (PARTITION BY store_id, supplier_name ORDER BY month)) " +
                                                 "/ LAG(total_sales) OVER (PARTITION BY store_id, supplier_name ORDER BY month)) * 100, 2) " +
                                     "ELSE NULL " +
                                 "END AS volatility " +
                             "FROM MonthlyRevenue " +
                          ") " +
                          "SELECT " +
                              "store_id, " +
                              "supplier_name, " +
                              "month, " +
                              "volatility " +
                          "FROM RevenueVolatility " +
                          "ORDER BY store_id, supplier_name, month";

        executeQuery(conn, queryQ5);
    }

    // Q6: Top 5 Products Purchased Together Across Multiple Orders
    public static void query6(Connection conn) throws SQLException {
        String queryQ6 = "SELECT " +
                             "t1.product_id AS product1, " +
                             "t2.product_id AS product2, " +
                             "COUNT(*) AS frequency " +
                          "FROM Fact_Transactions ft1 " +
                          "JOIN Fact_Transactions ft2 ON ft1.order_id = ft2.order_id " +
                          "WHERE ft1.product_id < ft2.product_id " +
                          "GROUP BY product1, product2 " +
                          "ORDER BY frequency DESC " +
                          "LIMIT 5";

        executeQuery(conn, queryQ6);
    }

    // Q7: Yearly Revenue Trends by Store, Supplier, and Product with ROLLUP
    public static void query7(Connection conn) throws SQLException {
        String queryQ7 = "SELECT " +
                             "store_id, " +
                             "supplier_name, " +
                             "product_name, " +
                             "SUM(total_sales_value) AS total_sales " +
                          "FROM Fact_Transactions ft " +
                          "JOIN Dim_Product dp ON ft.product_id = dp.product_id " +
                          "JOIN Dim_Supplier ds ON dp.supplier_id = ds.supplier_id " +
                          "GROUP BY store_id, supplier_name, product_name WITH ROLLUP " +
                          "ORDER BY store_id, supplier_name, product_name";

        executeQuery(conn, queryQ7);
    }

    // Q8: Revenue and Volume-Based Sales Analysis for Each Product for H1 and H2
    public static void query8(Connection conn) throws SQLException {
        String queryQ8 = "SELECT " +
                             "product_name, " +
                             "SUM(CASE WHEN MONTH(order_date) <= 6 THEN total_sales_value ELSE 0 END) AS H1_revenue, " +
                             "SUM(CASE WHEN MONTH(order_date) > 6 THEN total_sales_value ELSE 0 END) AS H2_revenue " +
                          "FROM Fact_Transactions ft " +
                          "JOIN Dim_Product dp ON ft.product_id = dp.product_id " +
                          "GROUP BY product_name " +
                          "ORDER BY H1_revenue DESC";

        executeQuery(conn, queryQ8);
    }

    // Q9: Identify High Revenue Spikes in Product Sales and Highlight Outliers
    public static void query9(Connection conn) throws SQLException {
        String queryQ9 = "SELECT " +
                             "product_id, " +
                             "order_date, " +
                             "SUM(total_sales_value) AS total_sales, " +
                             "AVG(total_sales_value) OVER (PARTITION BY product_id ORDER BY order_date ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING) AS avg_sales " +
                          "FROM Fact_Transactions " +
                          "GROUP BY product_id, order_date " +
                          "HAVING total_sales > 2 * avg_sales";

        executeQuery(conn, queryQ9);
    }

    // Q10: Create View for Store Quarterly Sales Analysis
    public static void query10(Connection conn) throws SQLException {
        String queryQ10 = "CREATE VIEW STORE_QUARTERLY_SALES AS " +
                          "SELECT " +
                              "store_id, " +
                              "QUARTER(order_date) AS quarter, " +
                              "SUM(total_sales_value) AS total_sales " +
                          "FROM Fact_Transactions " +
                          "GROUP BY store_id, quarter " +
                          "ORDER BY store_id, quarter";

        executeQuery(conn, queryQ10);
    }

    // Helper method to execute a query and print the results
    private static void executeQuery(Connection conn, String query) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        
        // Display the query result
        ResultSetMetaData rsMetaData = rs.getMetaData();
        int columnCount = rsMetaData.getColumnCount();
        
        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(rs.getString(i) + "\t");
            }
            System.out.println();
        }

        rs.close();
        stmt.close();
    }
}
