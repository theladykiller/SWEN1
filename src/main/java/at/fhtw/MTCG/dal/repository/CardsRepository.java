package at.fhtw.MTCG.dal.repository;

import at.fhtw.MTCG.dal.DataAccessException;
import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.model.Card;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.util.LinkedHashMap;
import java.util.Map;


public class CardsRepository {
    private UnitOfWork unitOfWork;

    public CardsRepository(UnitOfWork unitOfWork) { this.unitOfWork = unitOfWork; }

    public void deleteCards() {
        this.unitOfWork.beginTransaction();
        // Delete all cards
        try (PreparedStatement deleteStatement  = this.unitOfWork.prepareStatement("""
        DELETE FROM "card"
        """)) {
            deleteStatement.executeUpdate();
            this.unitOfWork.commitTransaction();
        } catch (SQLException e) {
            // Rollback in case of error
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException("Error deleting cards", e);
        }
    }

    public Map<String, Map<String,Object>> showCards(String username) {
        this.unitOfWork.beginTransaction();
        // Step 1: Retrieve the U_ID of the user based on the username
        int userId;
        try (PreparedStatement selectUserStmt = this.unitOfWork.prepareStatement("""
        SELECT u_id
        FROM "User"
        WHERE username = ?
    """)) {
            selectUserStmt.setString(1, username);
            ResultSet userResultSet = selectUserStmt.executeQuery();
            if (userResultSet.next()) {
                userId = userResultSet.getInt("u_id");
            } else {
                throw new DataAccessException("User not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException("Error retrieving user information");
        }

        // Step 2: Retrieve cards for the user using the U_ID
        Map<String, Map<String, Object>> cardsMap = new LinkedHashMap<>();
        try (PreparedStatement selectCardsStmt = this.unitOfWork.prepareStatement("""
        SELECT C_ID, name, damage, element_type, card_type, trait
        FROM Card
        WHERE u_id = ?
    """)) {
            selectCardsStmt.setInt(1, userId);
            ResultSet resultSet = selectCardsStmt.executeQuery();
            while (resultSet.next()) {
                Map<String, Object> cardDetails = new LinkedHashMap<>();
                cardDetails.put("name", resultSet.getString("name"));
                cardDetails.put("damage", resultSet.getInt("damage"));
                cardDetails.put("element_type", resultSet.getString("element_type"));
                cardDetails.put("card_type", resultSet.getString("card_type"));
                cardDetails.put("trait", resultSet.getString("trait"));

                cardsMap.put(resultSet.getString("C_ID"), cardDetails);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException("Error retrieving cards");
        }

        // Commit the transaction
        this.unitOfWork.commitTransaction();

        // Convert cards map to JSON
        return cardsMap;
    }
}
