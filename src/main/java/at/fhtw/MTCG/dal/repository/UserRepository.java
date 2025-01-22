package at.fhtw.MTCG.dal.repository;

import at.fhtw.MTCG.dal.DataAccessException;
import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.model.User;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Types;

import java.util.*;

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
                preparedStatement.setObject(2, null, Types.INTEGER);
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
                        resultSet.getInt("D_ID"),
                        resultSet.getString("bio"),
                        resultSet.getString("image"),
                        resultSet.getString("name")
                );
            }else {
                // User not found
                throw new DataAccessException("User not found");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding user by username", e);
        }
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

    public void updateUserData(String currentUsername, Map<String, Object> updateData) {
        try {
            // Define the valid keys that correspond to the database columns
            Set<String> validKeys = Set.of("password", "Bio", "Image", "Name");

            // Check for invalid keys
            for (String key : updateData.keySet()) {
                if (!validKeys.contains(key)) {
                    throw new IllegalArgumentException("Invalid key: " + key);
                }
            }

            this.unitOfWork.beginTransaction();

            if (updateData.containsKey("password")) {
                try (PreparedStatement preparedStatement = this.unitOfWork.prepareStatement("""
                    UPDATE "User" SET password = ? WHERE username = ?
                """)) {
                    preparedStatement.setString(1, (String) updateData.get("password"));
                    preparedStatement.setString(2, currentUsername);
                    int rowsUpdated = preparedStatement.executeUpdate();
                    if (rowsUpdated == 0) {
                        throw new DataAccessException("Error updating password");
                    }
                }
            }

            if (updateData.containsKey("Bio")) {
                try (PreparedStatement preparedStatement = this.unitOfWork.prepareStatement("""
                    UPDATE "User" SET bio = ? WHERE username = ?
                """)) {
                    preparedStatement.setString(1, (String) updateData.get("Bio"));
                    preparedStatement.setString(2, currentUsername);
                    int rowsUpdated = preparedStatement.executeUpdate();
                    if (rowsUpdated == 0) {
                        throw new DataAccessException("Error updating bio");
                    }
                }
            }

            if (updateData.containsKey("Image")) {
                try (PreparedStatement preparedStatement = this.unitOfWork.prepareStatement("""
                    UPDATE "User" SET image = ? WHERE username = ?
                """)) {
                    preparedStatement.setString(1, (String) updateData.get("Image"));
                    preparedStatement.setString(2, currentUsername);
                    int rowsUpdated = preparedStatement.executeUpdate();
                    if (rowsUpdated == 0) {
                        throw new DataAccessException("Error updating image");
                    }
                }
            }

            // Update fields one by one using separate PreparedStatements
            if (updateData.containsKey("Name")) {
                try (PreparedStatement preparedStatement = this.unitOfWork.prepareStatement("""
                    UPDATE "User" SET name = ? WHERE username = ?
                """)) {
                    preparedStatement.setString(1, (String) updateData.get("Name"));
                    preparedStatement.setString(2, currentUsername);
                    int rowsUpdated = preparedStatement.executeUpdate();
                    if (rowsUpdated == 0) {
                        throw new DataAccessException("Error updating name");
                    }
                }
            }

            this.unitOfWork.commitTransaction();
        } catch (SQLException e) {
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException("Error updating user", e);
        }
    }
    public List<Map<String, Object>> showScoreboard() {
        List<Map<String, Object>> scoreboard = new ArrayList<>();
        try {
            try (PreparedStatement preparedStatement = this.unitOfWork.prepareStatement("""
                SELECT username, elo, score, game_count
                FROM "User"
                ORDER BY score DESC, game_count ASC
            """)) {
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    Map<String, Object> user = new LinkedHashMap<>();
                    user.put("username", resultSet.getString("username"));
                    user.put("elo", resultSet.getString("elo"));
                    user.put("score", resultSet.getInt("score"));
                    scoreboard.add(user);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving scoreboard", e);
        }
        return scoreboard;
    }
}
