package at.fhtw.MTCG.service.deck;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;

import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;

import at.fhtw.MTCG.controller.Controller;

import at.fhtw.MTCG.dal.repository.DeckRepository;

import at.fhtw.MTCG.dal.DataAccessException;
import at.fhtw.MTCG.dal.UnitOfWork;

import at.fhtw.MTCG.model.Package;
import at.fhtw.MTCG.model.Card;
import at.fhtw.MTCG.model.User;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.ObjectCodec;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DeckController extends Controller{
    private final DeckRepository deckRepository;

    public DeckController(){
        UnitOfWork unitOfWork = new UnitOfWork();
        this.deckRepository = new DeckRepository(unitOfWork);
    }

    public Response view_deck(Request request) {
        try {
            String authorizationHeader = request.getHeaderMap().getHeader("Authorization");
            if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"message\":\"Unauthorized access\"}");
            }
            String username = request.getHeaderMap().getHeader("Authorization").replace("Bearer ", "").replace("-mtcgToken", "");
            Map<String, Map<String,Object>> deck = this.deckRepository.viewDeck(username);

            String params = request.getParams();
            if (params != null && params.contains("format=plain")) {
                // Format the deck as plain text
                StringBuilder plainTextDeck = new StringBuilder();
                for (Map.Entry<String, Map<String, Object>> entry : deck.entrySet()) {
                    plainTextDeck.append(entry.getKey()).append(": ");
                    entry.getValue().forEach((key, value) -> {
                        plainTextDeck.append(value).append(", ");
                    });
                }
                return new Response(HttpStatus.OK, ContentType.PLAIN_TEXT, plainTextDeck.toString().trim());
            }

            String deckJson = getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(deck);
            // Return success response
            return new Response(HttpStatus.OK, ContentType.JSON, deckJson);
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

    public Response configure_deck(Request request) {
        try {
            String authorizationHeader = request.getHeaderMap().getHeader("Authorization");
            if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"message\":\"Unauthorized access\"}");
            }
            // Extract the username from the token
            String username = request.getHeaderMap().getHeader("Authorization").replace("Bearer ", "").replace("-mtcgToken", "");
            // Parse the request body to get the deck card IDs
            List<String> cardIds = this.getObjectMapper().readValue(request.getBody(), new TypeReference<List<String>>() {});

            // Validate that exactly 4 card IDs are provided
            if (cardIds.size() != 4) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"message\":\"Deck must contain exactly 4 cards\"}");
            }

            this.deckRepository.configureDeck(username, cardIds);

            // Return success response
            return new Response(HttpStatus.OK, ContentType.JSON, "{\"message\":\"Deck configured successfully\"}");
        } catch (DataAccessException e) {
            if ("User not found".equals(e.getMessage())) {
                // Return 404 Not Found if no packages are available
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"message\":\"User not found\"}");
            } else if (e.getMessage().startsWith("Card does not belong to the user")) {
                // Return 400 Bad Request for unauthorized card usage
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, String.format("{\"message\":\"%s\"}", e.getMessage()));
            } else if (e.getMessage().startsWith("Card not found")) {
                // Return 400 Bad Request for card not found
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, String.format("{\"message\":\"%s\"}", e.getMessage()));
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
}
