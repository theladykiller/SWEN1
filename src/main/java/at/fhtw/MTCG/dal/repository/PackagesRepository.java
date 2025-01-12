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



    /*public void acquirePackage(Package packages){

    }*/

}
