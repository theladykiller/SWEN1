package at.fhtw.MTCG.dal.repository;

import at.fhtw.MTCG.dal.DataAccessException;
import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.model.Card;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

}
