package at.fhtw;

import at.fhtw.httpserver.server.Server;
import at.fhtw.httpserver.utils.Router;

import at.fhtw.MTCG.service.echo.EchoService;
import at.fhtw.MTCG.service.user.UserService;
import at.fhtw.MTCG.service.packages.PackagesService;
import at.fhtw.MTCG.service.card.CardsService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(10001, configureRouter());
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Router configureRouter()
    {
        Router router = new Router();
        router.addService("/echo", new EchoService());
        router.addService("/users", new UserService());
        router.addService("/sessions", new UserService());
        router.addService("/packages", new PackagesService());
        router.addService("/transactions", new PackagesService());
        router.addService("/cards", new CardsService());

        return router;
    }
}
