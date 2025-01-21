package at.fhtw.MTCG.dal.repository;

import at.fhtw.MTCG.dal.DataAccessException;
import at.fhtw.MTCG.dal.UnitOfWork;

import at.fhtw.MTCG.model.Package;
import at.fhtw.MTCG.model.Card;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.util.*;

public class DeckRepository {
    private UnitOfWork unitOfWork;

    public DeckRepository(UnitOfWork unitOfWork) {this.unitOfWork = unitOfWork;}

    public Map<String, Map<String,Object>> viewDeck(String username) {
        this.unitOfWork.beginTransaction();
        Map<String, Map<String, Object>> deck = new LinkedHashMap<>();
        try {
            // Step 1: Retrieve the d_id of the user based on the username
            int deckId;
            try (PreparedStatement selectDeckIdStmt = this.unitOfWork.prepareStatement("""
            SELECT d_id
            FROM "User"
            WHERE username = ?
        """)) {
                selectDeckIdStmt.setString(1, username);
                ResultSet userResultSet = selectDeckIdStmt.executeQuery();

                if (userResultSet.next()) {
                    deckId = userResultSet.getInt("d_id");
                } else {
                    // No user found, return an empty map
                    this.unitOfWork.commitTransaction();
                    return deck;
                }
            }

            // Step 2: Retrieve the entries from the deck table using the d_id
            try (PreparedStatement selectDeckStmt = this.unitOfWork.prepareStatement("""
            SELECT c1_id, c2_id, c3_id, c4_id
            FROM deck
            WHERE d_id = ?
        """)) {
                selectDeckStmt.setInt(1, deckId);
                ResultSet deckResultSet = selectDeckStmt.executeQuery();

                while (deckResultSet.next()) {
                    for (int i = 1; i <= 4; i++) {
                        String cardId = deckResultSet.getString("c" + i + "_id");
                        if (cardId != null) {
                            Map<String, Object> cardDetails = new LinkedHashMap<>();
                            cardDetails.put("card_id", cardId); // Add any other card properties if needed
                            deck.put("card" + i, cardDetails);
                        }
                    }
                }
            }

            // Commit transaction
            this.unitOfWork.commitTransaction();

        } catch (SQLException e) {
            e.printStackTrace();
            this.unitOfWork.rollbackTransaction();
            throw new RuntimeException("Error retrieving deck for user " + username, e);
        }

        // Return the deck map, which will be empty if no deck was found
        return deck;
    }

    public void configureDeck(String username, List<String> cardIds) {
        this.unitOfWork.beginTransaction();

        try {
            // Step 1: Retrieve the user's u_id from the "User" table
            int userId;
            try (PreparedStatement selectUserIdStmt = this.unitOfWork.prepareStatement("""
            SELECT u_id
            FROM "User"
            WHERE username = ?
        """)) {
                selectUserIdStmt.setString(1, username);
                ResultSet userResultSet = selectUserIdStmt.executeQuery();
                if (userResultSet.next()) {
                    userId = userResultSet.getInt("u_id");
                } else {
                    throw new DataAccessException("User not found");
                }
            }

            // Step 2: Verify that all cardIds belong to the user
            for (String cardId : cardIds) {
                try (PreparedStatement selectCardUserIdStmt = this.unitOfWork.prepareStatement("""
                SELECT u_id
                FROM Card
                WHERE c_id = ?
            """)) {
                    selectCardUserIdStmt.setString(1, cardId);
                    ResultSet cardResultSet = selectCardUserIdStmt.executeQuery();
                    if (cardResultSet.next()) {
                        int cardUserId = cardResultSet.getInt("u_id");
                        if (cardUserId != userId) {
                            throw new DataAccessException("Card does not belong to the user: " + cardId);
                        }
                    } else {
                        throw new DataAccessException("Card not found: " + cardId);
                    }
                }
            }

            // Step 3: Update the deck table with the provided cardIds
            try (PreparedStatement updateDeckStmt = this.unitOfWork.prepareStatement("""
                UPDATE deck
                SET c1_id = ?, c2_id = ?, c3_id = ?, c4_id = ?
                WHERE d_id = ?
            """)) {
                updateDeckStmt.setString(1, cardIds.get(0)); // c1_id
                updateDeckStmt.setString(2, cardIds.get(1)); // c2_id
                updateDeckStmt.setString(3, cardIds.get(2)); // c3_id
                updateDeckStmt.setString(4, cardIds.get(3)); // c4_id
                updateDeckStmt.setInt(5, userId); // d_id matches the user's u_id
                int rowsUpdated = updateDeckStmt.executeUpdate();

                // If no rows were updated, throw an exception
                if (rowsUpdated == 0) {
                    throw new DataAccessException("Failed to update deck for user: " + username);
                }
            }

            // Commit the transaction
            this.unitOfWork.commitTransaction();

        } catch (SQLException e) {
            e.printStackTrace();
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException("Error configuring deck", e);
        }
    }

}
