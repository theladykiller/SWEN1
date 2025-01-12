package at.fhtw.MTCG.service.packages;

import at.fhtw.MTCG.dal.DataAccessException;
import at.fhtw.MTCG.model.User;
import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.MTCG.controller.Controller;
import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.dal.repository.PackagesRepository;
import at.fhtw.MTCG.model.Package;
import at.fhtw.MTCG.model.Card;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PackagesController extends Controller{
    private final PackagesRepository packagesRepository;

    public PackagesController() {
        UnitOfWork unitOfWork = new UnitOfWork();
        this.packagesRepository = new PackagesRepository(unitOfWork);
    }

    public Response create_package(Request request) {
        try {
            // Parse the JSON array of packages from the request body
            List<Card> cards = this.getObjectMapper().readValue(request.getBody(), new TypeReference<List<Card>>() {});

            // Call the repository to save the packages
            this.packagesRepository.createPackage(cards);

            // Return success response
            return new Response(HttpStatus.CREATED, ContentType.JSON, "{\"message\":\"Package created successfully\"}");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            // Return 400 Bad Request for invalid JSON
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"message\":\"Invalid package data\"}");
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

    /*public Response acquire_package(Request request) {
        try {
            // Extract the user token from the Authorization header
            String token = request.getHeaders().get("Authorization").replace("Bearer ", "");
            User user = this.getUserFromToken(token); // Method to validate and fetch user by token

            // Call the repository to acquire a package for the user
            Package acquiredPackage = this.packagesRepository.acquirePackage(user);

            // Return the acquired package details
            String responseContent = this.getObjectMapper().writeValueAsString(acquiredPackage);
            return new Response(HttpStatus.OK, ContentType.JSON, responseContent);
        } catch (DataAccessException e) {
            if ("No packages available".equals(e.getMessage())) {
                // Return 404 Not Found if no packages are available
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"message\":\"No packages available\"}");
            } else {
                e.printStackTrace();
                // Return 500 Internal Server Error for other database issues
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Error acquiring package\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Catch all other exceptions
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Unexpected error occurred\"}");
        }
    }*/
}
