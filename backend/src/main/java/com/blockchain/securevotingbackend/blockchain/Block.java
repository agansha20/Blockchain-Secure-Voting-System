package com.blockchain.securevotingbackend.blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Block {

    private int index;
    private String timestamp;
    private String voterId;
    private int candidateId;
    private String previousHash;
    private String hash;

    public Block(
            int index,
            String timestamp,
            String voterId,
            int candidateId,
            String previousHash) {

        this.index = index;
        this.timestamp = timestamp;
        this.voterId = voterId;
        this.candidateId = candidateId;
        this.previousHash = previousHash;

        this.hash = calculateHash();
    }

    public String calculateHash() {

        String data =
                index +
                timestamp +
                voterId +
                candidateId +
                previousHash;

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hashBytes =
                    digest.digest(
                            data.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder hexString =
                    new StringBuilder();

            for (byte b : hashBytes) {

                String hex =
                        Integer.toHexString(
                                0xff & b
                        );

                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    public int getIndex() {
        return index;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getVoterId() {
        return voterId;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getHash() {
        return hash;
    }
}