package at.fhtw.MTCG.service.user;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.http.Method;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class UserService implements Service  {
    private final UserController userController;

    public UserService() { this.userController = new UserController(); }

    @Override
    public Response handleRequest(Request request) {
        if (request.getMethod() == Method.POST && request.getPathParts().contains("users")) {
            // Handle user registration
            return this.userController.register_user(request);
        } else if (request.getMethod() == Method.DELETE && request.getPathParts().contains("users")) {
            // Handle user deletion
            return this.userController.delete_user(request);
        } else if (request.getMethod() == Method.POST && request.getPathParts().contains("sessions")) {
            // Handle user login
            return this.userController.login_user(request);
        } else if (request.getMethod() == Method.GET && request.getPathParts().size() == 2 && "users".equals(request.getPathParts().get(0))) {
            // Handle fetching user data
            return this.userController.get_user_data(request);
        } else if (request.getMethod() == Method.PUT && request.getPathParts().size() == 2 && "users".equals(request.getPathParts().get(0))) {
            // Handle updating user data
            return this.userController.update_user_data(request);
        } else if (request.getMethod() == Method.GET && request.getPathParts().contains("stats")) {
            // Handle user deletion
            return this.userController.get_user_stats(request);
        }else if (request.getMethod() == Method.GET && request.getPathParts().contains("scoreboard")) {
            // Handle user deletion
            return this.userController.show_scoreboard(request);
        }

        // If the request doesn't match registration, deletion or login, return a bad request
        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }
}
