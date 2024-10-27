package at.fhtw.MTCG.service.user;

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
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Error registering user\"}");
        }
    }

    public Response login_user(Request request) {
        try {
            // Assuming the login data comes in the request body as JSON
            Map<String, String> loginData = this.getObjectMapper().readValue(request.getBody(), new TypeReference<Map<String, String>>() {});
            String username = loginData.get("username");
            String password = loginData.get("password");

            // Assuming you have a method in UserRepository to validate the user
            User user = userRepository.findUserByUsername(username);
            if (user != null && user.get_password().equals(password)) {
                // Return success response on login
                return new Response(HttpStatus.OK, ContentType.JSON, "{\"message\":\"Login successful\"}");
            } else {
                // Return unauthorized response if credentials are incorrect
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"message\":\"Invalid username or password\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Error logging in\"}");
        }
    }
}
