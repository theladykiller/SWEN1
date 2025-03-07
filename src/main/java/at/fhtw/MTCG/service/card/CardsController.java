package at.fhtw.MTCG.service.card;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;

import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;

import at.fhtw.MTCG.controller.Controller;

import at.fhtw.MTCG.dal.repository.CardsRepository;

import at.fhtw.MTCG.dal.DataAccessException;
import at.fhtw.MTCG.dal.UnitOfWork;

import at.fhtw.MTCG.model.User;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.ObjectCodec;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CardsController extends Controller {
    private final CardsRepository cardsRepository;

    public CardsController() {
        UnitOfWork unitOfWork = new UnitOfWork();
        this.cardsRepository = new CardsRepository(unitOfWork);
    }

    public Response delete_cards(Request request) {
        try {
            String authorizationHeader = request.getHeaderMap().getHeader("Authorization");
            if (authorizationHeader == null || !authorizationHeader.equals("Bearer admin-mtcgToken")) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"message\":\"Unauthorized access\"}");
            }
            this.cardsRepository.deleteCards(); // Call the repository to register the user

            // Return success response
            return new Response(HttpStatus.OK, ContentType.JSON, "{\"message\":\"Cards deleted successfully\"}");
        } catch (DataAccessException e) {
            // For other database-related exceptions, return 500 Internal Server Error
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Error deleting cards\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Error deleting cards\"}");
        }
    }

    public Response show_cards(Request request) {
        try {
            String authorizationHeader = request.getHeaderMap().getHeader("Authorization");
            if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"message\":\"Unauthorized access\"}");
            }
            String username = request.getHeaderMap().getHeader("Authorization").replace("Bearer ", "").replace("-mtcgToken", "");
            Map<String, Map<String,Object>> cards = this.cardsRepository.showCards(username); // Call the repository to register the user
            String cardsJson = getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(cards);
            // Return success response
            return new Response(HttpStatus.OK, ContentType.JSON, cardsJson);
        } catch (DataAccessException e) {
            if ("User not found".equals(e.getMessage())) {
                // Return 404 Not Found if no packages are available
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"message\":\"User not found\"}");
            }
            if ("Error retrieving user information".equals(e.getMessage())) {
                // Return 404 Not Found if no packages are available
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Unknown error\"}");
            }
            if ("Error retrieving cards".equals(e.getMessage())) {
                // Return 404 Not Found if no packages are available
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Unknown error\"}");
            }
            // For other database-related exceptions, return 500 Internal Server Error
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Error deleting cards\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"message\":\"Error deleting cards\"}");
        }
    }
}
