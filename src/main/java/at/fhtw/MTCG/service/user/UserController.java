package at.fhtw.MTCG.service.user;

import at.fhtw.MTCG.dal.DataAccessException;
import at.fhtw.MTCG.model.Card;
import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.MTCG.controller.Controller;
import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.dal.repository.UserRepository;
import at.fhtw.MTCG.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserController extends Controller {
    private final UserRepository userRepository;

    public UserController() {
        UnitOfWork unitOfWork = new UnitOfWork();
        this.userRepository = new UserRepository(unitOfWork);
    }

    public Response register_user(Request request) {
        try {
            // Assuming the user data comes in the request body as JSON
            User newUser = this.getObjectMapper().readValue(request.getBody(), User.class);
            userRepository.registerUser(newUser); // Call the repository to register the user

            // Return success response
            return new Response(HttpStatus.CREATED, ContentType.JSON, "{\"message\":\"User registered successfully\"}");
        } catch (DataAccessException e) {
            if ("User already exists".equals(e.getMessage())) {
                // Return 409 Conflict if the user already exists
                return new Response(HttpStatus.CONFLICT, ContentType.JSON, "{\"message\":\"User already exists\"}");
            } else {
                // For other database-related exceptions, return 500 Internal Server Error
                e.printStackTrace();
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Error registering user\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Error registering user\"}");
        }
    }

    public Response login_user(Request request) {
        try {
            // Assuming the login data comes in the request body as JSON
            Map<String, String> loginData = this.getObjectMapper().readValue(request.getBody(), new TypeReference<Map<String, String>>() {});
            String username = loginData.get("Username");
            String password = loginData.get("Password");

            User user = userRepository.findUserByUsername(username);
            if (!user.get_password().equals(password)) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Wrong password");
            }

            String token = username + "-mtcgToken";

            // Return success response with the generated token
            String jsonResponse = String.format("{\"message\":\"Login successful\", \"token\":\"%s\"}", token);
            return new Response(HttpStatus.OK, ContentType.JSON, jsonResponse);
        }catch (DataAccessException e) {
            if ("User not found".equals(e.getMessage())) {
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, e.getMessage());
            }
            e.printStackTrace();
            // Return 500 Internal Server Error for database issues
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Error saving package\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Error logging in\"}");
        }
    }

    public Response delete_user(Request request) {
        try {
            // Parse the request body for username and password
            Map<String, String> requestData = this.getObjectMapper().readValue(request.getBody(), new TypeReference<Map<String, String>>() {});
            String username = requestData.get("Username");
            String password = requestData.get("Password");

            // Call the repository method to delete the user
            userRepository.deleteUser(username, password);

            // Return success response
            return new Response(HttpStatus.OK, ContentType.JSON, "{\"message\":\"User deleted successfully\"}");
        } catch (DataAccessException e) {
            if ("Invalid username or password".equals(e.getMessage())) {
                // Return 401 Unauthorized if credentials are incorrect
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"message\":\"Invalid username or password\"}");
            } else if ("User does not exist".equals(e.getMessage())) {
                // Return 404 Not Found if the user does not exist
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"message\":\"User not found\"}");
            } else {
                // Return 500 Internal Server Error for other database errors
                e.printStackTrace();
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Error deleting user\"}");
            }
        } catch (Exception e) {
            // Return 500 Internal Server Error for unexpected exceptions
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Error deleting user\"}");
        }
    }

    public Response get_user_data(Request request) {
        try {
            String authorizationHeader = request.getHeaderMap().getHeader("Authorization");
            if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"message\":\"Unauthorized access\"}");
            }
            String username = request.getHeaderMap().getHeader("Authorization").replace("Bearer ", "").replace("-mtcgToken", "");

            //get path
            List<String> pathParts = request.getPathParts();
            //check if token exists && if path matches username
            if (pathParts.isEmpty() || !pathParts.get(pathParts.size() - 1).equals(username)) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"message\":\"Unauthorized access\"}");
            }

            User user = userRepository.findUserByUsername(username);
            // Make response readable
            String response = getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(user);
            // Return success response
            return new Response(HttpStatus.OK, ContentType.JSON, response);
        } catch (DataAccessException e) {
            e.printStackTrace();
            // Return 500 Internal Server Error for database issues
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Error saving package\"}");
        } catch (Exception e) {
            e.printStackTrace();
            // Catch all other exceptions
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Unexpected error occurred\"}");
        }
    }

    public Response update_user_data(Request request) {
        try {
            String authorizationHeader = request.getHeaderMap().getHeader("Authorization");
            if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"message\":\"Unauthorized access\"}");
            }
            String username = request.getHeaderMap().getHeader("Authorization").replace("Bearer ", "").replace("-mtcgToken", "");

            //get path
            List<String> pathParts = request.getPathParts();
            //check if token exists && if path matches username
            if (pathParts.isEmpty() || !pathParts.get(pathParts.size() - 1).equals(username)) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"message\":\"Unauthorized access\"}");
            }
            // Parse JSON payload
            Map<String, Object> updateData;
            try {
                updateData = getObjectMapper().readValue(request.getBody(), new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"message\":\"Invalid JSON payload\"}");
            }
            userRepository.updateUserData(username, updateData);
            return new Response(HttpStatus.OK, ContentType.JSON, "{\"message\":\"User data updated successfully\"}");
        } catch (DataAccessException e) {
            if ("Error updating username".equals(e.getMessage())) {
                // Return 404 Not Found if no packages are available
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, e.getMessage());
            }
            if ("Error updating password".equals(e.getMessage())) {
                // Return 404 Not Found if no packages are available
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, e.getMessage());
            }
            if ("Error updating bio".equals(e.getMessage())) {
                // Return 404 Not Found if no packages are available
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, e.getMessage());
            }
            if ("Error updating image".equals(e.getMessage())) {
                // Return 404 Not Found if no packages are available
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, e.getMessage());
            }

            e.printStackTrace();
            // Return 500 Internal Server Error for database issues
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Error updating user\"}");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            // Return a 400 Bad Request response for invalid input
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"message\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            // Catch all other exceptions
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Unexpected error occurred\"}");
        }
    }

    public Response get_user_stats(Request request) {
        try {
            String authorizationHeader = request.getHeaderMap().getHeader("Authorization");
            if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"message\":\"Unauthorized access\"}");
            }
            String username = request.getHeaderMap().getHeader("Authorization").replace("Bearer ", "").replace("-mtcgToken", "");

            User user = userRepository.findUserByUsername(username);
            // Create a map containing only the required fields
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("name", user.get_name());
            stats.put("elo", user.get_elo());
            stats.put("score", user.get_score());
            stats.put("game_count", user.get_game_count());
            // Make response readable
            String response = getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(stats);
            // Return success response
            return new Response(HttpStatus.OK, ContentType.JSON, response);
        } catch (DataAccessException e) {
            if ("User not found".equals(e.getMessage())) {
                // Return 404 Not Found if no packages are available
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, e.getMessage());
            }

            e.printStackTrace();
            // Return 500 Internal Server Error for database issues
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Error saving package\"}");
        } catch (Exception e) {
            e.printStackTrace();
            // Catch all other exceptions
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Unexpected error occurred\"}");
        }
    }
    public Response show_scoreboard(Request request) {
        try {
            String authorizationHeader = request.getHeaderMap().getHeader("Authorization");
            if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"message\":\"Unauthorized access\"}");
            }
            List<Map<String, Object>> scoreboard = userRepository.showScoreboard();
            String response = getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(scoreboard);
            System.out.println(response);
            return new Response(HttpStatus.OK, ContentType.JSON, response);
        } catch (DataAccessException e) {

            e.printStackTrace();
            // Return 500 Internal Server Error for database issues
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Error saving package\"}");
        } catch (Exception e) {
            e.printStackTrace();
            // Catch all other exceptions
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Unexpected error occurred\"}");
        }
    }
}
