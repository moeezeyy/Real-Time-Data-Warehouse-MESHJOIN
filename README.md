# Real-Time Data Warehouse with MESHJOIN Algorithm

This project implements a near-real-time Data Warehouse solution for METRO stores using the MESHJOIN algorithm for stream-relation joins in the ETL process.

## Project Overview
The system enables online analysis of shopping behavior by:
- Processing transactional data in near-real-time
- Enriching transactions with master data using MESHJOIN
- Providing OLAP capabilities for business intelligence
- Supporting strategic decisions like promotions and inventory management

## Key Features
- **Star Schema Design**: Fact and dimension tables for retail analysis
- **MESHJOIN Implementation**: Efficient stream-relation joins in Java
- **OLAP Queries**: 10 analytical queries for business insights
- **End-to-End Pipeline**: From raw data to analytical insights

## System Architecture


## Technology Stack
- **Database**: MySQL (for Data Warehouse)
- **ETL Processing**: Java (MESHJOIN implementation)
- **Analysis**: SQL OLAP queries
- **Documentation**: Project report with implementation details

## Repository Structure


## Installation & Setup
1. **Database Setup**:
```bash
mysql -u username -p < sql/star_schema.sql
