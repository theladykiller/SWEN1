package at.fhtw.MTCG.dal.repository;

import at.fhtw.MTCG.dal.DataAccessException;
import at.fhtw.MTCG.dal.UnitOfWork;

import at.fhtw.MTCG.model.Package;
import at.fhtw.MTCG.model.Card;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PackagesRepository {
    private UnitOfWork unitOfWork;

    public PackagesRepository(UnitOfWork unitOfWork) { this.unitOfWork = unitOfWork; }

    public void createPackage(List<Card> cards) {
        if (cards.size() != 5) {
            throw new IllegalArgumentException("A package must contain exactly 5 cards.");
        }

        try {
            // Start a transaction
            this.unitOfWork.beginTransaction();

            // Insert cards into the database
            for (Card card : cards) {
                try (PreparedStatement cardStatement = this.unitOfWork.prepareStatement("""
                INSERT INTO Card (C_ID, name, damage, element_type, card_type, trait, U_ID)
                VALUES (?, ?, ?, ?, ?, ?, NULL)
            """)) {
                    cardStatement.setString(1, card.get_C_ID());
                    cardStatement.setString(2, card.get_name());
                    cardStatement.setInt(3, card.get_damage());
                    // Use `NULL` if optional fields are not provided
                    if (card.get_element_type() != null) {
                        cardStatement.setString(4, card.get_element_type());
                    } else {
                        cardStatement.setNull(4, java.sql.Types.VARCHAR);
                    }

                    if (card.get_card_type() != null) {
                        cardStatement.setString(5, card.get_card_type());
                    } else {
                        cardStatement.setNull(5, java.sql.Types.VARCHAR);
                    }

                    if (card.get_trait() != null) {
                        cardStatement.setString(6, card.get_trait());
                    } else {
                        cardStatement.setNull(6, java.sql.Types.VARCHAR);
                    }

                    cardStatement.executeUpdate();
                }
            }

            // Insert the package with references to the 5 cards
            try (PreparedStatement packageStatement = this.unitOfWork.prepareStatement("""
            INSERT INTO Package (C1_ID, C2_ID, C3_ID, C4_ID, C5_ID)
            VALUES (?, ?, ?, ?, ?)
        """)) {
                packageStatement.setString(1, cards.get(0).get_C_ID());
                packageStatement.setString(2, cards.get(1).get_C_ID());
                packageStatement.setString(3, cards.get(2).get_C_ID());
                packageStatement.setString(4, cards.get(3).get_C_ID());
                packageStatement.setString(5, cards.get(4).get_C_ID());

                packageStatement.executeUpdate();
            }

            // Commit the transaction
            this.unitOfWork.commitTransaction();
        } catch (SQLException e) {
            // Rollback the transaction in case of any error
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException("Error creating package and inserting cards.", e);
        }
    }

    public Package acquirePackage(String username) {
        try {
            // Start a transaction
            this.unitOfWork.beginTransaction();

            // Step 1: Select a random package
            int packageId;
            int packagePrice;
            List<String> cardIds = new ArrayList<>();
            try (PreparedStatement selectPackageStmt = this.unitOfWork.prepareStatement("""
            SELECT P_ID, price, C1_ID, C2_ID, C3_ID, C4_ID, C5_ID 
            FROM Package
            ORDER BY RANDOM() LIMIT 1
        """)) {
                ResultSet resultSet = selectPackageStmt.executeQuery();
                if (resultSet.next()) {
                    packageId = resultSet.getInt("P_ID");
                    packagePrice = resultSet.getInt("price");
                    cardIds.add(resultSet.getString("C1_ID"));
                    cardIds.add(resultSet.getString("C2_ID"));
                    cardIds.add(resultSet.getString("C3_ID"));
                    cardIds.add(resultSet.getString("C4_ID"));
                    cardIds.add(resultSet.getString("C5_ID"));
                } else {
                    throw new DataAccessException("No packages available");
                }
            }

            // Step 2.1: Retrieve the U_ID of the user based on the username
            int userId;
            int userCoins;
            try (PreparedStatement selectUserStmt = this.unitOfWork.prepareStatement("""
            SELECT u_id, coins 
            FROM "User" 
            WHERE username = ?
        """)) {
                selectUserStmt.setString(1, username);
                ResultSet resultSet = selectUserStmt.executeQuery();
                if (resultSet.next()) {
                    userId = resultSet.getInt("U_ID");
                    userCoins = resultSet.getInt("coins");
                } else {
                    throw new DataAccessException("User not found");
                }
            }

            // Step 2.2: Check if the user has enough coins
            if (userCoins < packagePrice) {
                throw new DataAccessException("Not enough coins");
            }

            // Step 2.4: Assign the cards to the user
            try (PreparedStatement updateCardsStmt = this.unitOfWork.prepareStatement("""
            UPDATE Card 
            SET U_ID = ? 
            WHERE C_ID = ?
        """)) {
                for (String cardId : cardIds) {
                    updateCardsStmt.setInt(1, userId);
                    updateCardsStmt.setString(2, cardId);
                    updateCardsStmt.executeUpdate();
                }
            }

            // Step 2.5: Deduct coins from the user's account
            try (PreparedStatement updateUserCoinsStmt = this.unitOfWork.prepareStatement("""
            UPDATE "User" 
            SET coins = coins - ? 
            WHERE u_id = ?
        """)) {
                updateUserCoinsStmt.setInt(1, packagePrice); // Deduct the package price
                updateUserCoinsStmt.setInt(2, userId);       // Use the user's ID
                int rowsUpdated = updateUserCoinsStmt.executeUpdate();

                if (rowsUpdated == 0) {
                    throw new DataAccessException("Failed to update user coins");
                }
            }


            // Step 3: Delete the package
            try (PreparedStatement deletePackageStmt = this.unitOfWork.prepareStatement("""
            DELETE FROM Package 
            WHERE P_ID = ?
        """)) {
                deletePackageStmt.setInt(1, packageId);
                deletePackageStmt.executeUpdate();
            }

            // Step 4: Retrieve the card details for the package
            List<Card> cards = new ArrayList<>();
            try (PreparedStatement selectCardsStmt = this.unitOfWork.prepareStatement("""
            SELECT C_ID, name, damage, element_type, card_type, trait, U_ID
            FROM Card 
            WHERE C_ID = ?
        """)) {
                for (String cardId : cardIds) {
                    selectCardsStmt.setString(1, cardId);
                    ResultSet resultSet = selectCardsStmt.executeQuery();
                    if (resultSet.next()) {
                        Card card = new Card();
                        card.set_C_ID(resultSet.getString("C_ID"));
                        card.set_name(resultSet.getString("name"));
                        card.set_damage(resultSet.getInt("damage"));
                        card.set_element_type(resultSet.getString("element_type"));
                        card.set_card_type(resultSet.getString("card_type"));
                        card.set_trait(resultSet.getString("trait"));
                        card.set_U_ID(resultSet.getInt("U_ID"));
                        cards.add(card);
                    }
                }
            }

            // Step 5: Commit the transaction and return the package
            this.unitOfWork.commitTransaction();

            // Create and return the Package object
            Package acquiredPackage = new Package();
            acquiredPackage.set_P_ID(packageId);
            acquiredPackage.set_price(packagePrice);
            acquiredPackage.set_C1_ID(cards.get(0).get_C_ID());
            acquiredPackage.set_C2_ID(cards.get(1).get_C_ID());
            acquiredPackage.set_C3_ID(cards.get(2).get_C_ID());
            acquiredPackage.set_C4_ID(cards.get(3).get_C_ID());
            acquiredPackage.set_C5_ID(cards.get(4).get_C_ID());
            return acquiredPackage;

        } catch (Exception e) {
            // Rollback the transaction in case of errors
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException(e.getMessage());
        }
    }


}
