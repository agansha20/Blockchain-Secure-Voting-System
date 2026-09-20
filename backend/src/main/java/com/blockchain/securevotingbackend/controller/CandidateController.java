package com.blockchain.securevotingbackend.controller;

import org.springframework.web.bind.annotation.*;

import com.blockchain.securevotingbackend.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidates")
@CrossOrigin(origins = "*")
public class CandidateController {

    @GetMapping
    public List<Map<String, Object>> getCandidates() {

        List<Map<String, Object>> candidates =
                new ArrayList<>();

        String sql =
                "SELECT candidate_id, candidate_name " +
                "FROM candidates";

        try (
            Connection connection =
                    java.sql.DriverManager.getConnection(
                            "jdbc:mysql://localhost:3306/secure_voting",
                            "root",
                            DatabaseConfig.DB_PASSWORD
                    );

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            ResultSet resultSet =
                    statement.executeQuery()
        ) {

            while (resultSet.next()) {

                candidates.add(
                    Map.of(
                        "candidateId",
                        resultSet.getInt("candidate_id"),

                        "candidateName",
                        resultSet.getString("candidate_name")
                    )
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return candidates;
    }
}