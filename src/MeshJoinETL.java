package src;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class MeshJoinETL {
    // Database credentials
    static final String DB_URL = "jdbc:mysql://localhost:3306/starschema";
    static final String USER = "root";
    static final String PASS = "123456moeez";

    // File paths
    static final String TRANSACTIONS_FILE = "C:\\Users\\Moeez Khan Yousafzai\\Desktop\\project DW\\transactions.csv";
    static final String CUSTOMERS_FILE = "C:\\Users\\Moeez Khan Yousafzai\\Desktop\\project DW\\customers_data.csv";
    static final String PRODUCTS_FILE = "C:\\Users\\Moeez Khan Yousafzai\\Desktop\\project DW\\products_data.csv";

    // Thread pool for parallel execution
    static ExecutorService executor = Executors.newFixedThreadPool(3);

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            System.out.println("Database connected successfully!");

            // Step 1: Load data from CSV files
            Future<List<Transaction>> transactionsFuture = executor.submit(MeshJoinETL::loadTransactions);
            Future<List<Customer>> customersFuture = executor.submit(MeshJoinETL::loadCustomers);
            Future<List<Product>> productsFuture = executor.submit(MeshJoinETL::loadProducts);

            List<Transaction> transactions = transactionsFuture.get();
            List<Customer> customers = customersFuture.get();
            List<Product> products = productsFuture.get();

            // Step 2: Insert Customers and Products into Dimension Tables
            insertCustomers(conn, customers);
            insertProducts(conn, products);

            // Step 3: Perform MESHJOIN and Load Data into Fact Table
            meshJoinAndLoad(conn, transactions, customers, products);

            executor.shutdown();
            System.out.println("ETL process completed!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Load Transactions
    public static List<Transaction> loadTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTIONS_FILE))) {
            String line;
            br.readLine(); // Skip header row
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                try {
                    int orderId = Integer.parseInt(fields[0].trim());
                    String orderDateTime = fields[1].trim();
                    String orderDate = orderDateTime.split(" ")[0]; // Extract date only
                    int customerId = Integer.parseInt(fields[2].trim());
                    int productId = Integer.parseInt(fields[3].trim());
                    int quantity = Integer.parseInt(fields[4].trim());
                    transactions.add(new Transaction(orderId, orderDate, quantity, customerId, productId));
                } catch (Exception e) {
                    System.out.println("Skipping invalid row: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    // Load Customers
    public static List<Customer> loadCustomers() {
        List<Customer> customers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(CUSTOMERS_FILE))) {
            String line;
            br.readLine(); // Skip header row
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                try {
                    int customerId = Integer.parseInt(fields[0].trim());
                    String customerName = fields[1].trim();
                    String gender = fields[2].trim();
                    customers.add(new Customer(customerId, customerName, gender));
                } catch (Exception e) {
                    System.out.println("Skipping invalid row: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return customers;
    }

    // Load Products
    public static List<Product> loadProducts() {
        List<Product> products = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(PRODUCTS_FILE))) {
            String line;
            br.readLine(); // Skip header row
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                try {
                    int productId = Integer.parseInt(fields[0].trim());
                    String productName = fields[1].trim();
                    String priceString = fields[2].trim().replaceAll("[^\\d.]", ""); // Remove $ sign
                    BigDecimal productPrice = new BigDecimal(priceString);
                    products.add(new Product(productId, productName, productPrice));
                } catch (Exception e) {
                    System.out.println("Skipping invalid row: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return products;
    }

    // Insert Customers into Database
    public static void insertCustomers(Connection conn, List<Customer> customers) throws SQLException {
        String query = "INSERT INTO Dim_Customer (customer_id, customer_name, gender) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (Customer customer : customers) {
                stmt.setInt(1, customer.getId());
                stmt.setString(2, customer.getName());
                stmt.setString(3, customer.getGender());
                stmt.executeUpdate();
            }
        }
    }

    // Insert Products into Database
    public static void insertProducts(Connection conn, List<Product> products) throws SQLException {
        String query = "INSERT INTO Dim_Product (product_id, product_name, price) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (Product product : products) {
                stmt.setInt(1, product.getId());
                stmt.setString(2, product.getName());
                stmt.setBigDecimal(3, product.getPrice());
                stmt.executeUpdate();
            }
        }
    }

    // Perform MESHJOIN and Load Data into Fact Table
    public static void meshJoinAndLoad(Connection conn, List<Transaction> transactions, List<Customer> customers, List<Product> products) throws SQLException {
        Map<Integer, Customer> customerMap = new HashMap<>();
        for (Customer customer : customers) {
            customerMap.put(customer.getId(), customer);
        }

        Map<Integer, Product> productMap = new HashMap<>();
        for (Product product : products) {
            productMap.put(product.getId(), product);
        }

        String query = "INSERT INTO Fact_Transactions (order_id, order_date, quantity_ordered, customer_id, product_id, total_sales_value) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (Transaction transaction : transactions) {
                if (!customerMap.containsKey(transaction.getCustomerId()) || !productMap.containsKey(transaction.getProductId())) {
                    System.out.println("Skipping invalid transaction: " + transaction);
                    continue;
                }

                Product product = productMap.get(transaction.getProductId());
                BigDecimal totalSalesValue = product.getPrice().multiply(new BigDecimal(transaction.getQuantity()));

                stmt.setInt(1, transaction.getOrderId());
                stmt.setString(2, transaction.getOrderDate());
                stmt.setInt(3, transaction.getQuantity());
                stmt.setInt(4, transaction.getCustomerId());
                stmt.setInt(5, transaction.getProductId());
                stmt.setBigDecimal(6, totalSalesValue);
                stmt.executeUpdate();
            }
        }
    }
}

// Customer Class
class Customer {
    private int id;
    private String name;
    private String gender;

    public Customer(int id, String name, String gender) {
        this.id = id;
        this.name = name;
        this.gender = gender;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }
}

// Product Class
class Product {
    private int id;
    private String name;
    private BigDecimal price;

    public Product(int id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }
}

// Transaction Class
class Transaction {
    private int orderId;
    private String orderDate;
    private int quantity;
    private int customerId;
    private int productId;

    public Transaction(int orderId, String orderDate, int quantity, int customerId, int productId) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.quantity = quantity;
        this.customerId = customerId;
        this.productId = productId;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getProductId() {
        return productId;
    }
}
