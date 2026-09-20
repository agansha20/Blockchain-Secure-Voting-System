package com.blockchain.securevotingbackend.config;

public class DatabaseConfig {

    public static final String DB_URL =
            "jdbc:mysql://secure-voting-db-blockchain-voting.j.aivencloud.com:15864/defaultdb?ssl-mode=REQUIRED";

    public static final String DB_USER =
            "avnadmin";

    public static final String DB_PASSWORD =
            System.getenv("DB_PASSWORD");
}