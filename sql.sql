drop database if exists starschema;
create database starschema;
use starschema;
DROP TABLE IF EXISTS Fact_Transactions;
DROP TABLE IF EXISTS Dim_Product;
DROP TABLE IF EXISTS Dim_Customer;
CREATE TABLE Dim_Customer (
    customer_id INT PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    gender VARCHAR(10) NOT NULL
);
CREATE TABLE Dim_Product (
    product_id INT PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

CREATE TABLE Fact_Transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    order_date DATE NOT NULL,
    quantity_ordered INT NOT NULL,
    customer_id INT NOT NULL,
    product_id INT NOT NULL,
    total_sales_value DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES Dim_Customer(customer_id),
    FOREIGN KEY (product_id) REFERENCES Dim_Product(product_id)
);




