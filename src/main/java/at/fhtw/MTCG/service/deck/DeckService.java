package at.fhtw.MTCG.service.deck;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.http.Method;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class DeckService implements Service{
    private final DeckController deckController;

    public DeckService() { this.deckController = new DeckController(); }

    @Override
    public Response handleRequest(Request request) {
        if (request.getMethod() == Method.GET && request.getPathParts().contains("deck")) {
            // Handle package creation
            return this.deckController.view_deck(request);
        }
        if (request.getMethod() == Method.PUT && request.getPathParts().contains("deck")) {
            // Handle package creation
            return this.deckController.configure_deck(request);
        }
        // If the request doesn't match creation or acquisition, return a bad request
        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }
}
