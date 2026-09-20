
package com.blockchain.securevotingbackend.blockchain;
import com.blockchain.securevotingbackend.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Blockchain {

    private final List<Block> chain = new ArrayList<>();

    private final String url = "jdbc:mysql://localhost:3306/secure_voting";
    private final String username = "root";
    private final String password = DatabaseConfig.DB_PASSWORD;

    public Blockchain() {

        loadBlockchain();

        if (chain.isEmpty()) {

            Block genesisBlock = new Block(
                    0,
                    LocalDateTime.now().toString(),
                    "GENESIS",
                    0,
                    "0"
            );

            chain.add(genesisBlock);

            saveBlock(genesisBlock);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                url,
                username,
                password
        );
    }

    private void loadBlockchain() {

        String sql =
                "SELECT block_index, block_timestamp, voter_id, " +
                "candidate_id, previous_hash, hash " +
                "FROM blockchain_blocks " +
                "ORDER BY block_index ASC";

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {

                int index =
                        resultSet.getInt("block_index");

                String timestamp =
                        resultSet.getString("block_timestamp");

                String voterId =
                        resultSet.getString("voter_id");

                int candidateId =
                        resultSet.getInt("candidate_id");

                String previousHash =
                        resultSet.getString("previous_hash");

                Block block = new Block(
                        index,
                        timestamp,
                        voterId,
                        candidateId,
                        previousHash
                );

                chain.add(block);
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    public Block addVote(
            String voterId,
            int candidateId) {

        Block previousBlock =
                chain.get(chain.size() - 1);

        Block newBlock =
                new Block(
                        chain.size(),
                        LocalDateTime.now().toString(),
                        voterId,
                        candidateId,
                        previousBlock.getHash()
                );

        chain.add(newBlock);

        saveBlock(newBlock);

        return newBlock;
    }

    private void saveBlock(Block block) {

        String sql =
                "INSERT INTO blockchain_blocks " +
                "(block_index, block_timestamp, voter_id, " +
                "candidate_id, previous_hash, hash) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    block.getIndex()
            );

            statement.setString(
                    2,
                    block.getTimestamp()
            );

            statement.setString(
                    3,
                    block.getVoterId()
            );

            statement.setInt(
                    4,
                    block.getCandidateId()
            );

            statement.setString(
                    5,
                    block.getPreviousHash()
            );

            statement.setString(
                    6,
                    block.getHash()
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    public List<Block> getChain() {
        return chain;
    }

    public Block getLatestBlock() {

        return chain.get(
                chain.size() - 1
        );
    }

    public boolean isValid() {

        for (
                int i = 1;
                i < chain.size();
                i++
        ) {

            Block currentBlock =
                    chain.get(i);

            Block previousBlock =
                    chain.get(i - 1);

            if (!currentBlock
                    .getHash()
                    .equals(
                            currentBlock.calculateHash()
                    )) {

                return false;
            }

            if (!currentBlock
                    .getPreviousHash()
                    .equals(
                            previousBlock.getHash()
                    )) {

                return false;
            }
        }

        return true;
    }
}