package at.fhtw.MTCG.service.card;

import at.fhtw.MTCG.service.packages.PackagesController;
import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.http.Method;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class CardsService implements Service{
    private final CardsController cardsController;


    public CardsService() { this.cardsController = new CardsController(); }

    @Override
    public Response handleRequest(Request request) {
        if (request.getMethod() == Method.DELETE && request.getPathParts().contains("cards")){
            // Handle package creation
            return this.cardsController.delete_cards(request);
        }

        // If the request doesn't match creation or acquisition, return a bad request
        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }
}
