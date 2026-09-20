package com.blockchain.securevotingbackend.config;

public class DatabaseConfig {

    public static final String DB_URL =
            "jdbc:mysql://localhost:3306/secure_voting";

    public static final String DB_USER =
            "root";

    public static final String DB_PASSWORD =
            System.getenv("DB_PASSWORD");
}