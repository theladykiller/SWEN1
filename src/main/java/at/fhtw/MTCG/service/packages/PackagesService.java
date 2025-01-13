package at.fhtw.MTCG.service.packages;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.http.Method;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class PackagesService implements Service{
    private final PackagesController packagesController;


    public PackagesService() { this.packagesController = new PackagesController(); }

    @Override
    public Response handleRequest(Request request) {
        if (request.getMethod() == Method.POST && request.getPathParts().contains("packages") && !request.getPathParts().contains("transactions")) {
            // Handle package creation
            return this.packagesController.create_package(request);
        } else if (request.getMethod() == Method.POST && request.getPathParts().contains("transactions")) {
            // Handle package acquisition
            return this.packagesController.acquire_package(request);
        }

        // If the request doesn't match creation or acquisition, return a bad request
        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }
}
