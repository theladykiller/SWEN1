package at.fhtw.MTCG.dal.repository;

import at.fhtw.MTCG.dal.DataAccessException;
import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;

public class UserRepository {
    private UnitOfWork unitOfWork;

    public UserRepository(UnitOfWork unitOfWork) { this.unitOfWork = unitOfWork; }

    // Method to register a new user
    public void registerUser(User user) {
        try {
            // Step 0: Check if the user already exists
            boolean userExists;
            try (PreparedStatement preparedStatement = this.unitOfWork.prepareStatement("""
                SELECT 1 FROM "User" WHERE username = ?
            """)) {
                preparedStatement.setString(1, user.get_username());

                ResultSet resultSet = preparedStatement.executeQuery();
                userExists = resultSet.next(); // If there's a result, the user already exists
            }

            if (userExists) {
                throw new DataAccessException("User already exists");
            }

            // Step 1: Insert a new User entry without D_ID
            int generatedUserId;
            try (PreparedStatement preparedStatement = this.unitOfWork.prepareStatement("""
                INSERT INTO "User" (username, password) 
                VALUES (?, ?) RETURNING U_ID
            """)) {
                preparedStatement.setString(1, user.get_username());
                preparedStatement.setString(2, user.get_password());

                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    generatedUserId = resultSet.getInt("U_ID");
                } else {
                    throw new DataAccessException("Failed to generate User ID");
                }
            }

            // Step 2: Insert a new Deck entry with the same D_ID as the generated U_ID
            try (PreparedStatement preparedStatement = this.unitOfWork.prepareStatement("""
                INSERT INTO Deck (D_ID, C1_ID, C2_ID, C3_ID, C4_ID) 
                VALUES (?, ?, ?, ?, ?)
            """)) {
                preparedStatement.setInt(1, generatedUserId); // Set D_ID to be the same as U_ID
                preparedStatement.setObject(2, null, Types.INTEGER); // Example for nullable FK
                preparedStatement.setObject(3, null, Types.INTEGER);
                preparedStatement.setObject(4, null, Types.INTEGER);
                preparedStatement.setObject(5, null, Types.INTEGER);

                preparedStatement.executeUpdate();
            }

            // Step 3: Update the user record with the D_ID
            try (PreparedStatement preparedStatement = this.unitOfWork.prepareStatement("""
                UPDATE "User" 
                SET D_ID = ? 
                WHERE U_ID = ?
            """)) {
                preparedStatement.setInt(1, generatedUserId); // Set the D_ID
                preparedStatement.setInt(2, generatedUserId); // Where U_ID matches

                preparedStatement.executeUpdate();
            }

            // Commit the transaction
            this.unitOfWork.commitTransaction();
        } catch (SQLException e) {
            this.unitOfWork.rollbackTransaction(); // Rollback in case of error
            throw new DataAccessException("Error registering user", e);
        }
    }

    // Method to find a user by username
    public User findUserByUsername(String username) {
        try (PreparedStatement preparedStatement = this.unitOfWork.prepareStatement("""
                SELECT * FROM "User" WHERE username = ?
            """)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new User(
                        resultSet.getInt("U_ID"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getInt("coins"),
                        resultSet.getInt("score"),
                        resultSet.getString("elo"),
                        resultSet.getInt("game_count"),
                        resultSet.getInt("D_ID")
                );
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding user by username", e);
        }
        return null; // Return null if no user is found
    }

    public void deleteUser(String username, String password) {
        try {
            // Validate username and password
            boolean validCredentials;
            try (PreparedStatement preparedStatement = this.unitOfWork.prepareStatement("""
                SELECT 1 FROM "User" WHERE username = ? AND password = ?
            """)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                ResultSet resultSet = preparedStatement.executeQuery();
                validCredentials = resultSet.next();
            }

            if (!validCredentials) {
                throw new DataAccessException("Invalid username or password");
            }

            // Delete the user
            try (PreparedStatement preparedStatement = this.unitOfWork.prepareStatement("""
                DELETE FROM "User" WHERE username = ?
            """)) {
                preparedStatement.setString(1, username);
                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected == 0) {
                    throw new DataAccessException("Failed to delete user");
                }
            }

            // Commit the transaction
            this.unitOfWork.commitTransaction();
        } catch (SQLException e) {
            this.unitOfWork.rollbackTransaction(); // Rollback in case of error
            throw new DataAccessException("Error deleting user", e);
        }
    }
    // Add other methods as needed (e.g., to get all users, etc.)
}
