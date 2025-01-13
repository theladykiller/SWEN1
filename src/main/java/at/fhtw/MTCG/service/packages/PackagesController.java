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
            String authorizationHeader = request.getHeaderMap().getHeader("Authorization");
            if (authorizationHeader == null || !authorizationHeader.equals("Bearer admin-mtcgToken")) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"message\":\"Unauthorized access\"}");
            }

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
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            // Return 400 Bad Request for invalid JSON
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"message\":\"Invalid argument count\"}");
        }  catch (Exception e) {
            e.printStackTrace();
            // Catch all other exceptions
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Unexpected error occurred\"}");
        }
    }

    public Response acquire_package(Request request) {
        try {
            // Extract the user token from the Authorization header
            String username = request.getHeaderMap().getHeader("Authorization").replace("Bearer ", "").replace("-mtcgToken", "");
            // Call the repository to acquire a package for the user
            Package acquiredPackage = this.packagesRepository.acquirePackage(username);
            // Make response readable
            String response = getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(acquiredPackage);
            // Return the acquired package details
            return new Response(HttpStatus.CREATED, ContentType.JSON, response);
        } catch (DataAccessException e) {
            if ("No packages available".equals(e.getMessage())) {
                // Return 404 Not Found if no packages are available
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"message\":\"No packages available\"}");
            }
            if ("User not found".equals(e.getMessage())) {
                // Return 404 Not Found if no user match
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"message\":\"User does not exist\"}");
            }
            if ("Not enough coins".equals(e.getMessage())) {
                // Return 403 if user does not have enough coins
                return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "{\"message\":\"User does not have enough coins\"}");
            }
            e.printStackTrace();
            // Return 500 Internal Server Error for other database issues
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, String.format("{\"message\": \"%s\"}", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            // Catch all other exceptions
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Unexpected error occurred\"}");
        }
    }
}
