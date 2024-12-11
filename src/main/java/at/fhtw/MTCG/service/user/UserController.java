package at.fhtw.MTCG.service.user;

import at.fhtw.MTCG.dal.DataAccessException;
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

            // Assuming you have a method in UserRepository to validate the user
            User user = userRepository.findUserByUsername(username);
            if (user != null && user.get_password().equals(password)) {
                // Create the token in the format "username-mtcgToken"
                String token = username + "-mtcgToken";

                // Return success response with the generated token
                String jsonResponse = String.format("{\"message\":\"Login successful\", \"token\":\"%s\"}", token);
                return new Response(HttpStatus.OK, ContentType.JSON, jsonResponse);
            } else {
                // Return unauthorized response if credentials are incorrect
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"message\":\"Invalid username or password\"}");
            }
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

}
