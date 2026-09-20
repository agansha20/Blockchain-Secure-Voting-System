package com.blockchain.securevotingbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.blockchain.securevotingbackend.config.DatabaseConfig;

import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private static final String DB_URL =
        DatabaseConfig.DB_URL;

private static final String DB_USER =
        DatabaseConfig.DB_USER;

private static final String DB_PASSWORD =
        DatabaseConfig.DB_PASSWORD;


    // =====================================================
    // ADMIN LOGIN
    // =====================================================

    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(
            @RequestBody Map<String, String> request) {

        String username = request.get("username");
        String password = request.get("password");

        String sql =
                "SELECT * FROM admins " +
                "WHERE username = ? AND password = ?";

        try (
                Connection connection =
                        DriverManager.getConnection(
                                DB_URL,
                                DB_USER,
                                DB_PASSWORD
                        );

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet resultSet =
                    statement.executeQuery();

            if (resultSet.next()) {

                return ResponseEntity.ok(
                        Map.of(
                                "success", true,
                                "message", "Login successful"
                        )
                );
            }

            return ResponseEntity
                    .status(401)
                    .body(
                            Map.of(
                                    "success", false,
                                    "message",
                                    "Invalid username or password"
                            )
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(
                            Map.of(
                                    "success", false,
                                    "message",
                                    "Database error: "
                                            + e.getMessage()
                            )
                    );
        }
    }


    // =====================================================
    // DASHBOARD STATS
    // =====================================================

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {

        Map<String, Object> stats =
                new HashMap<>();

        String candidateSql =
                "SELECT COUNT(*) FROM candidates";

        String voteSql =
                "SELECT COUNT(*) FROM votes";

        String blockSql =
                "SELECT COUNT(*) FROM blockchain_blocks";

        try (
                Connection connection =
                        DriverManager.getConnection(
                                DB_URL,
                                DB_USER,
                                DB_PASSWORD
                        )
        ) {

            // Total Candidates

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    candidateSql
                            );

                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (resultSet.next()) {

                    stats.put(
                            "totalCandidates",
                            resultSet.getInt(1)
                    );
                }
            }


            // Total Votes

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    voteSql
                            );

                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (resultSet.next()) {

                    stats.put(
                            "totalVotes",
                            resultSet.getInt(1)
                    );
                }
            }


            // Blockchain Blocks

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    blockSql
                            );

                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (resultSet.next()) {

                    stats.put(
                            "totalBlocks",
                            resultSet.getInt(1)
                    );
                }
            }


            return ResponseEntity.ok(stats);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(
                            Map.of(
                                    "message",
                                    "Error loading dashboard stats: "
                                            + e.getMessage()
                            )
                    );
        }
    }


    // =====================================================
    // GET ALL CANDIDATES
    // =====================================================

    @GetMapping("/candidates")
    public ResponseEntity<?> getCandidates() {

        String sql =
                "SELECT candidate_id, candidate_name " +
                "FROM candidates " +
                "ORDER BY candidate_id";

        List<Map<String, Object>> candidates =
                new ArrayList<>();

        try (
                Connection connection =
                        DriverManager.getConnection(
                                DB_URL,
                                DB_USER,
                                DB_PASSWORD
                        );

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
                        resultSet.getInt("candidate_id")
                );

                candidate.put(
                        "candidateName",
                        resultSet.getString("candidate_name")
                );

                candidates.add(candidate);
            }

            return ResponseEntity.ok(candidates);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(
                            Map.of(
                                    "message",
                                    "Error loading candidates: "
                                            + e.getMessage()
                            )
                    );
        }
    }


    // =====================================================
    // ADD CANDIDATE
    // =====================================================

    @PostMapping("/candidates")
    public ResponseEntity<?> addCandidate(
            @RequestBody Map<String, String> request) {

        String candidateName =
                request.get("candidateName");

        if (
                candidateName == null ||
                candidateName.trim().isEmpty()
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Candidate name is required."
                            )
                    );
        }

        String sql =
                "INSERT INTO candidates " +
                "(candidate_name) VALUES (?)";

        try (
                Connection connection =
                        DriverManager.getConnection(
                                DB_URL,
                                DB_USER,
                                DB_PASSWORD
                        );

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    candidateName.trim()
            );

            statement.executeUpdate();

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Candidate added successfully."
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(
                            Map.of(
                                    "message",
                                    "Error adding candidate: "
                                            + e.getMessage()
                            )
                    );
        }
    }


    // =====================================================
    // DELETE CANDIDATE
    // =====================================================

    @DeleteMapping("/candidates/{candidateId}")
    public ResponseEntity<?> deleteCandidate(
            @PathVariable int candidateId) {

        Connection connection = null;

        try {

            connection =
                    DriverManager.getConnection(
                            DB_URL,
                            DB_USER,
                            DB_PASSWORD
                    );

            connection.setAutoCommit(false);


            String checkSql =
                    "SELECT candidate_id " +
                    "FROM candidates " +
                    "WHERE candidate_id = ?";


            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    checkSql
                            )
            ) {

                statement.setInt(
                        1,
                        candidateId
                );

                ResultSet resultSet =
                        statement.executeQuery();

                if (!resultSet.next()) {

                    connection.rollback();

                    return ResponseEntity
                            .status(404)
                            .body(
                                    Map.of(
                                            "message",
                                            "Candidate not found."
                                    )
                            );
                }
            }


            // Delete votes of candidate

            String deleteVotesSql =
                    "DELETE FROM votes " +
                    "WHERE candidate_id = ?";


            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    deleteVotesSql
                            )
            ) {

                statement.setInt(
                        1,
                        candidateId
                );

                statement.executeUpdate();
            }


            // Delete candidate

            String deleteCandidateSql =
                    "DELETE FROM candidates " +
                    "WHERE candidate_id = ?";


            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    deleteCandidateSql
                            )
            ) {

                statement.setInt(
                        1,
                        candidateId
                );

                int deleted =
                        statement.executeUpdate();


                if (deleted > 0) {

                    connection.commit();

                    return ResponseEntity.ok(
                            Map.of(
                                    "message",
                                    "Candidate removed successfully."
                            )
                    );
                }


                connection.rollback();

                return ResponseEntity
                        .status(404)
                        .body(
                                Map.of(
                                        "message",
                                        "Candidate not found."
                                )
                        );
            }

        } catch (Exception e) {

            if (connection != null) {

                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                }
            }

            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(
                            Map.of(
                                    "message",
                                    "Error removing candidate: "
                                            + e.getMessage()
                            )
                    );

        } finally {

            if (connection != null) {

                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }


    // =====================================================
    // GET ALL VOTERS
    // =====================================================

    @GetMapping("/voters")
    public ResponseEntity<?> getVoters() {

        String sql =
                "SELECT voter_id, name, has_voted " +
                "FROM voters " +
                "ORDER BY voter_id";

        List<Map<String, Object>> voters =
                new ArrayList<>();

        try (
                Connection connection =
                        DriverManager.getConnection(
                                DB_URL,
                                DB_USER,
                                DB_PASSWORD
                        );

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {

                Map<String, Object> voter =
                        new HashMap<>();

                voter.put(
                        "voter_id",
                        resultSet.getString("voter_id")
                );

                voter.put(
                        "name",
                        resultSet.getString("name")
                );

                voter.put(
                        "has_voted",
                        resultSet.getBoolean("has_voted")
                );

                voters.add(voter);
            }

            return ResponseEntity.ok(voters);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(
                            Map.of(
                                    "message",
                                    "Error loading voters: "
                                            + e.getMessage()
                            )
                    );
        }
    }


    // =====================================================
    // ADD VOTER
    // =====================================================

    @PostMapping("/voters")
    public ResponseEntity<?> addVoter(
            @RequestBody Map<String, String> request) {

        String voterId =
                request.get("voterId");

        String name =
                request.get("name");

        String password =
                request.get("password");


        if (
                voterId == null ||
                voterId.trim().isEmpty()
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Voter ID is required."
                            )
                    );
        }


        if (
                name == null ||
                name.trim().isEmpty()
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Voter name is required."
                            )
                    );
        }


        if (
                password == null ||
                password.trim().isEmpty()
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Password is required."
                            )
                    );
        }


        String sql =
                "INSERT INTO voters " +
                "(voter_id, name, password, has_voted) " +
                "VALUES (?, ?, ?, FALSE)";


        try (
                Connection connection =
                        DriverManager.getConnection(
                                DB_URL,
                                DB_USER,
                                DB_PASSWORD
                        );

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    voterId.trim()
            );

            statement.setString(
                    2,
                    name.trim()
            );

            statement.setString(
                    3,
                    password.trim()
            );

            statement.executeUpdate();

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Voter added successfully."
                    )
            );

        } catch (
                SQLIntegrityConstraintViolationException e
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Voter ID already exists."
                            )
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(
                            Map.of(
                                    "message",
                                    "Error adding voter: "
                                            + e.getMessage()
                            )
                    );
        }
    }


    // =====================================================
    // DELETE VOTER
    // =====================================================

    @DeleteMapping("/voters/{voterId}")
    public ResponseEntity<?> deleteVoter(
            @PathVariable String voterId) {

        Connection connection = null;

        try {

            connection =
                    DriverManager.getConnection(
                            DB_URL,
                            DB_USER,
                            DB_PASSWORD
                    );

            connection.setAutoCommit(false);


            String checkSql =
                    "SELECT voter_id " +
                    "FROM voters " +
                    "WHERE voter_id = ?";


            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    checkSql
                            )
            ) {

                statement.setString(
                        1,
                        voterId
                );

                ResultSet resultSet =
                        statement.executeQuery();

                if (!resultSet.next()) {

                    connection.rollback();

                    return ResponseEntity
                            .status(404)
                            .body(
                                    Map.of(
                                            "message",
                                            "Voter not found."
                                    )
                            );
                }
            }


            // Delete vote

            String deleteVoteSql =
                    "DELETE FROM votes " +
                    "WHERE voter_id = ?";


            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    deleteVoteSql
                            )
            ) {

                statement.setString(
                        1,
                        voterId
                );

                statement.executeUpdate();
            }


            // Delete voter

            String deleteVoterSql =
                    "DELETE FROM voters " +
                    "WHERE voter_id = ?";


            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    deleteVoterSql
                            )
            ) {

                statement.setString(
                        1,
                        voterId
                );

                int deleted =
                        statement.executeUpdate();


                if (deleted > 0) {

                    connection.commit();

                    return ResponseEntity.ok(
                            Map.of(
                                    "message",
                                    "Voter removed successfully."
                            )
                    );
                }


                connection.rollback();

                return ResponseEntity
                        .status(404)
                        .body(
                                Map.of(
                                        "message",
                                        "Voter was not removed."
                                )
                        );
            }

        } catch (Exception e) {

            if (connection != null) {

                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                }
            }

            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(
                            Map.of(
                                    "message",
                                    "Error removing voter: "
                                            + e.getMessage()
                            )
                    );

        } finally {

            if (connection != null) {

                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }


    // =====================================================
    // ELECTION RESULTS
    // =====================================================

    @GetMapping("/results")
    public ResponseEntity<?> getResults() {

        String sql =
                "SELECT " +
                "c.candidate_id, " +
                "c.candidate_name, " +
                "COUNT(v.vote_id) AS vote_count " +
                "FROM candidates c " +
                "LEFT JOIN votes v " +
                "ON c.candidate_id = v.candidate_id " +
                "GROUP BY " +
                "c.candidate_id, " +
                "c.candidate_name " +
                "ORDER BY vote_count DESC, c.candidate_id";


        List<Map<String, Object>> results =
                new ArrayList<>();


        try (
                Connection connection =
                        DriverManager.getConnection(
                                DB_URL,
                                DB_USER,
                                DB_PASSWORD
                        );

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {

                Map<String, Object> result =
                        new HashMap<>();


                result.put(
                        "candidateId",
                        resultSet.getInt(
                                "candidate_id"
                        )
                );


                result.put(
                        "candidateName",
                        resultSet.getString(
                                "candidate_name"
                        )
                );


                result.put(
                        "voteCount",
                        resultSet.getInt(
                                "vote_count"
                        )
                );


                results.add(result);
            }


            return ResponseEntity.ok(results);


        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(
                            Map.of(
                                    "message",
                                    "Error loading results: "
                                            + e.getMessage()
                            )
                    );
        }
    }


    // =====================================================
    // BLOCKCHAIN BLOCKS
    // =====================================================

    @GetMapping("/blockchain/blocks")
    public ResponseEntity<?> getBlockchainBlocks() {

        String sql =
                "SELECT COUNT(*) AS total_blocks " +
                "FROM blockchain_blocks";


        try (
                Connection connection =
                        DriverManager.getConnection(
                                DB_URL,
                                DB_USER,
                                DB_PASSWORD
                        );

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            if (resultSet.next()) {

                return ResponseEntity.ok(
                        Map.of(
                                "totalBlocks",
                                resultSet.getInt(
                                        "total_blocks"
                                )
                        )
                );
            }


            return ResponseEntity.ok(
                    Map.of(
                            "totalBlocks",
                            0
                    )
            );


        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(
                            Map.of(
                                    "message",
                                    "Error loading blockchain blocks: "
                                            + e.getMessage()
                            )
                    );
        }
    }


    // =====================================================
    // GET BLOCKCHAIN DATA
    // =====================================================

    @GetMapping("/blockchain")
    public ResponseEntity<?> getBlockchain() {

        String sql =
                "SELECT " +
                "block_id, " +
                "block_index, " +
                "block_timestamp, " +
                "voter_id, " +
                "candidate_id, " +
                "previous_hash, " +
                "hash " +
                "FROM blockchain_blocks " +
                "ORDER BY block_index";


        List<Map<String, Object>> blocks =
                new ArrayList<>();


        try (
                Connection connection =
                        DriverManager.getConnection(
                                DB_URL,
                                DB_USER,
                                DB_PASSWORD
                        );

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {

                Map<String, Object> block =
                        new HashMap<>();


                block.put(
                        "blockId",
                        resultSet.getInt(
                                "block_id"
                        )
                );


                block.put(
                        "blockIndex",
                        resultSet.getInt(
                                "block_index"
                        )
                );


                block.put(
                        "blockTimestamp",
                        resultSet.getString(
                                "block_timestamp"
                        )
                );


                block.put(
                        "voterId",
                        resultSet.getString(
                                "voter_id"
                        )
                );


                block.put(
                        "candidateId",
                        resultSet.getInt(
                                "candidate_id"
                        )
                );


                block.put(
                        "previousHash",
                        resultSet.getString(
                                "previous_hash"
                        )
                );


                block.put(
                        "hash",
                        resultSet.getString(
                                "hash"
                        )
                );


                blocks.add(block);
            }


            return ResponseEntity.ok(blocks);


        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(
                            Map.of(
                                    "message",
                                    "Error loading blockchain: "
                                            + e.getMessage()
                            )
                    );
        }
    }
}