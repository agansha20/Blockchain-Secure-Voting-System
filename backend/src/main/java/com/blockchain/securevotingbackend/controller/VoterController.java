package com.blockchain.securevotingbackend.controller;

import com.blockchain.securevotingbackend.blockchain.Block;
import com.blockchain.securevotingbackend.blockchain.Blockchain;
import com.blockchain.securevotingbackend.config.DatabaseConfig;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/voter")
@CrossOrigin(origins = "*")
public class VoterController {

    // ==========================================
    // BLOCKCHAIN
    // ==========================================

    private static final Blockchain blockchain =
            new Blockchain();


    // ==========================================
    // DATABASE CONNECTION
    // ==========================================

    private Connection getConnection()
            throws Exception {

        return java.sql.DriverManager.getConnection(
               DatabaseConfig.DB_URL,
               DatabaseConfig.DB_USER,
               DatabaseConfig.DB_PASSWORD
        );
    }


    // ==========================================
    // TEST API
    // ==========================================

    @GetMapping("/test")
    public String test() {

        return "Voter API is working!";
    }


    // ==========================================
    // VOTER LOGIN
    // ==========================================

    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestBody Map<String, String> request) {

        String voterId =
                request.get("voterId");

        String password =
                request.get("password");

        Map<String, Object> response =
                new HashMap<>();

        String sql =
                "SELECT name, has_voted " +
                "FROM voters " +
                "WHERE voter_id = ? " +
                "AND password = ?";

        try (
                Connection connection =
                        getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, voterId);
            statement.setString(2, password);

            ResultSet resultSet =
                    statement.executeQuery();

            if (resultSet.next()) {

                response.put("success", true);
                response.put("message", "Login successful");
                response.put("voterId", voterId);

                response.put(
                        "name",
                        resultSet.getString("name")
                );

                response.put(
                        "hasVoted",
                        resultSet.getBoolean("has_voted")
                );

            } else {

                response.put("success", false);
                response.put(
                        "message",
                        "Invalid Voter ID or Password"
                );
            }

        } catch (Exception e) {

            response.put("success", false);
            response.put(
                    "message",
                    "Database connection error"
            );

            e.printStackTrace();
        }

        return response;
    }


    // ==========================================
    // CAST VOTE
    // ==========================================

    @PostMapping("/vote")
    public Map<String, Object> castVote(
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response =
                new HashMap<>();

        String voterId =
                String.valueOf(
                        request.get("voterId")
                );

        int candidateId;

        try {

            candidateId =
                    Integer.parseInt(
                            String.valueOf(
                                    request.get("candidateId")
                            )
                    );

        } catch (Exception e) {

            response.put("success", false);
            response.put(
                    "message",
                    "Invalid candidate ID"
            );

            return response;
        }


        Connection connection = null;

        try {

            connection = getConnection();

            // ==========================================
            // START TRANSACTION
            // ==========================================

            connection.setAutoCommit(false);


            // ==========================================
            // 1. CHECK VOTER
            // ==========================================

            String voterSql =
                    "SELECT has_voted " +
                    "FROM voters " +
                    "WHERE voter_id = ?";

            try (
                    PreparedStatement voterStatement =
                            connection.prepareStatement(voterSql)
            ) {

                voterStatement.setString(
                        1,
                        voterId
                );

                ResultSet voterResult =
                        voterStatement.executeQuery();

                if (!voterResult.next()) {

                    connection.rollback();

                    response.put(
                            "success",
                            false
                    );

                    response.put(
                            "message",
                            "Voter not found"
                    );

                    return response;
                }

                boolean hasVoted =
                        voterResult.getBoolean(
                                "has_voted"
                        );

                if (hasVoted) {

                    connection.rollback();

                    response.put(
                            "success",
                            false
                    );

                    response.put(
                            "message",
                            "You have already voted"
                    );

                    return response;
                }
            }


            // ==========================================
            // 2. CHECK CANDIDATE
            // ==========================================

            String candidateSql =
                    "SELECT candidate_name " +
                    "FROM candidates " +
                    "WHERE candidate_id = ?";

            String candidateName;

            try (
                    PreparedStatement candidateStatement =
                            connection.prepareStatement(candidateSql)
            ) {

                candidateStatement.setInt(
                        1,
                        candidateId
                );

                ResultSet candidateResult =
                        candidateStatement.executeQuery();

                if (!candidateResult.next()) {

                    connection.rollback();

                    response.put(
                            "success",
                            false
                    );

                    response.put(
                            "message",
                            "Candidate not found"
                    );

                    return response;
                }

                candidateName =
                        candidateResult.getString(
                                "candidate_name"
                        );
            }


            // ==========================================
            // 3. SAVE VOTE TO MYSQL
            // ==========================================

            String voteSql =
                    "INSERT INTO votes " +
                    "(voter_id, candidate_id) " +
                    "VALUES (?, ?)";

            try (
                    PreparedStatement voteStatement =
                            connection.prepareStatement(voteSql)
            ) {

                voteStatement.setString(
                        1,
                        voterId
                );

                voteStatement.setInt(
                        2,
                        candidateId
                );

                voteStatement.executeUpdate();
            }


            // ==========================================
            // 4. CREATE BLOCKCHAIN BLOCK
            // ==========================================

            Block block =
                    blockchain.addVote(
                            voterId,
                            candidateId
                    );


            // ==========================================
            // 5. SAVE BLOCKCHAIN BLOCK TO MYSQL
            // ==========================================

            int blockIndex =
                    block.getIndex();

            String previousHash =
                    block.getPreviousHash();

            String hash =
                    block.getHash();

            String blockTimestamp =
                    LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern(
                                    "yyyy-MM-dd HH:mm:ss"
                            )
                    );


            String blockSql =
                    "INSERT INTO blockchain_blocks " +
                    "(block_index, block_timestamp, voter_id, " +
                    "candidate_id, previous_hash, hash) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            try (
                    PreparedStatement blockStatement =
                            connection.prepareStatement(blockSql)
            ) {

                blockStatement.setInt(
                        1,
                        blockIndex
                );

                blockStatement.setString(
                        2,
                        blockTimestamp
                );

                blockStatement.setString(
                        3,
                        voterId
                );

                blockStatement.setInt(
                        4,
                        candidateId
                );

                blockStatement.setString(
                        5,
                        previousHash
                );

                blockStatement.setString(
                        6,
                        hash
                );

                blockStatement.executeUpdate();
            }


            // ==========================================
            // 6. UPDATE VOTER
            // ==========================================

            String updateVoterSql =
                    "UPDATE voters " +
                    "SET has_voted = TRUE " +
                    "WHERE voter_id = ?";

            try (
                    PreparedStatement updateStatement =
                            connection.prepareStatement(
                                    updateVoterSql
                            )
            ) {

                updateStatement.setString(
                        1,
                        voterId
                );

                updateStatement.executeUpdate();
            }


            // ==========================================
            // 7. COMMIT EVERYTHING
            // ==========================================

            connection.commit();


            // ==========================================
            // 8. SUCCESS RESPONSE
            // ==========================================

            response.put(
                    "success",
                    true
            );

            response.put(
                    "message",
                    "Vote cast successfully"
            );

            response.put(
                    "voterId",
                    voterId
            );

            response.put(
                    "candidateId",
                    candidateId
            );

            response.put(
                    "candidateName",
                    candidateName
            );

            response.put(
                    "blockIndex",
                    blockIndex
            );

            response.put(
                    "blockHash",
                    hash
            );

            response.put(
                    "previousHash",
                    previousHash
            );

        } catch (Exception e) {

            // ==========================================
            // ROLLBACK IF ANYTHING FAILS
            // ==========================================

            try {

                if (connection != null) {
                    connection.rollback();
                }

            } catch (Exception rollbackError) {

                rollbackError.printStackTrace();
            }

            response.put(
                    "success",
                    false
            );

            response.put(
                    "message",
                    "Unable to cast vote"
            );

            e.printStackTrace();

        } finally {

            try {

                if (connection != null) {
                    connection.close();
                }

            } catch (Exception closeError) {

                closeError.printStackTrace();
            }
        }

        return response;
    }


    // ==========================================
    // VIEW BLOCKCHAIN
    // ==========================================

    @GetMapping("/blockchain")
    public Object getBlockchain() {

        return blockchain.getChain();
    }


    // ==========================================
    // VERIFY BLOCKCHAIN
    // ==========================================

    @GetMapping("/blockchain/verify")
    public Map<String, Object> verifyBlockchain() {

        Map<String, Object> response =
                new HashMap<>();

        boolean valid =
                blockchain.isValid();

        response.put(
                "valid",
                valid
        );

        if (valid) {

            response.put(
                    "message",
                    "Blockchain is valid"
            );

        } else {

            response.put(
                    "message",
                    "Blockchain has been tampered with"
            );
        }

        return response;
    }


    // ==========================================
    // ELECTION RESULTS
    // ==========================================

    @GetMapping("/results")
    public List<Map<String, Object>> getResults() {

        List<Map<String, Object>> results =
                new ArrayList<>();

        String sql =
                "SELECT c.candidate_id, " +
                "c.candidate_name, " +
                "COUNT(v.vote_id) AS vote_count " +
                "FROM candidates c " +
                "LEFT JOIN votes v " +
                "ON c.candidate_id = v.candidate_id " +
                "GROUP BY c.candidate_id, c.candidate_name " +
                "ORDER BY vote_count DESC";

        try (
                Connection connection =
                        getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {

                Map<String, Object> candidate =
                        new HashMap<>();

                candidate.put(
                        "candidateId",
                        resultSet.getInt(
                                "candidate_id"
                        )
                );

                candidate.put(
                        "candidateName",
                        resultSet.getString(
                                "candidate_name"
                        )
                );

                candidate.put(
                        "voteCount",
                        resultSet.getInt(
                                "vote_count"
                        )
                );

                results.add(candidate);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return results;
    }
}