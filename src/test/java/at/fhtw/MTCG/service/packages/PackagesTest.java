package at.fhtw.MTCG.service.packages;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;

import java.util.Map;

@TestMethodOrder(OrderAnnotation.class)
class PackagesTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Test
    @Order(1)
    void testCreateMultiplePackages() throws Exception {

        for (int i = 1; i <= 5; i++) {
            URL url = new URL("http://localhost:10001/packages");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Authorization", "Bearer admin-mtcgToken");

            String cardsJson = String.format("""
            [
                {"C_ID": "card%d1", "name": "Water Goblin", "damage": 10, "element_type": "Water", "card_type": "Monster", "trait": "Goblin"},
                {"C_ID": "card%d2", "name": "Dragon", "damage": 50, "element_type": "Fire", "card_type": "Monster", "trait": "Dragon"},
                {"C_ID": "card%d3", "name": "Ork", "damage": 20, "element_type": "Normal", "card_type": "Monster", "trait": "Ork"},
                {"C_ID": "card%d4", "name": "Fire Spell", "damage": 25, "element_type": "Fire", "card_type": "Magic", "trait": "Spell"},
                {"C_ID": "card%d5", "name": "Water Spell", "damage": 30, "element_type": "Water", "card_type": "Magic", "trait": "Spell"}
            ]
            """, i, i, i, i, i); // Use the iteration variable to make unique card IDs


            try (OutputStream os = urlConnection.getOutputStream()) {
                os.write(cardsJson.getBytes());
                os.flush();
            }

            // Get and assert the response code
            int responseCode = urlConnection.getResponseCode();
            assertEquals(HttpURLConnection.HTTP_CREATED, responseCode, "Expected response code 201 (Created)");
        }
    }

    @Test
    @Order(2)
    void testIncorrectAmountOfCards() throws Exception {
        String cardsJson = """
        [
            {"C_ID": "card6", "name": "Dragon", "damage": 50, "element_type": "Fire", "card_type": "Monster", "trait": "Dragon"},
            {"C_ID": "card7", "name": "Ork", "damage": 20, "element_type": "Normal", "card_type": "Monster", "trait": "Ork"},
            {"C_ID": "card8", "name": "Fire Spell", "damage": 25, "element_type": "Fire", "card_type": "Magic", "trait": "Spell"},
            {"C_ID": "card9", "name": "Water Spell", "damage": 30, "element_type": "Water", "card_type": "Magic", "trait": "Spell"}
        ]
    """;

        URL url = new URL("http://localhost:10001/packages");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Authorization", "Bearer admin-mtcgToken");

        try (OutputStream os = urlConnection.getOutputStream()) {
            os.write(cardsJson.getBytes());
            os.flush();
        }

        // Get and assert the response code
        int responseCode = urlConnection.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, responseCode, "Expected response code 400 (Bad Request)");
    }

    @Test
    @Order(3)
    void testCreatePackageNotAuthorized() throws Exception {
        String cardsJson = """
            [
                {"C_ID": "card10", "name": "Water Goblin", "damage": 10, "element_type": "Water", "card_type": "Monster", "trait": "Goblin"},
                {"C_ID": "card11", "name": "Dragon", "damage": 50, "element_type": "Fire", "card_type": "Monster", "trait": "Dragon"},
                {"C_ID": "card12", "name": "Ork", "damage": 20, "element_type": "Normal", "card_type": "Monster", "trait": "Ork"},
                {"C_ID": "card13", "name": "Fire Spell", "damage": 25, "element_type": "Fire", "card_type": "Magic", "trait": "Spell"},
                {"C_ID": "card14", "name": "Water Spell", "damage": 30, "element_type": "Water", "card_type": "Magic", "trait": "Spell"}
            ]
        """;

        URL url = new URL("http://localhost:10001/packages");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Authorization", "Bearer testUser100-mtcgToken");

        try (OutputStream os = urlConnection.getOutputStream()) {
            os.write(cardsJson.getBytes());
            os.flush();
        }

        // Get and assert the response code
        int responseCode = urlConnection.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, responseCode, "Expected response code 401 (Unauthorized)");
    }

    @Test
    @Order(4)
    void testAcquirePackage() throws Exception {
        //step 1: create user
        {
            String userJson = "{\"Username\":\"testUser100\",\"Password\":\"testpass\"}";

            // Set up connection to the registration endpoint
            URL url = new URL("http://localhost:10001/users"); // Adjust URL as needed
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");

            // Write the JSON string to the output stream
            try (OutputStream os = urlConnection.getOutputStream()) {
                os.write(userJson.getBytes());
                os.flush();
            }
            // Check response code
            int responseCode = urlConnection.getResponseCode();
            // Assert that the response is 201 Created (successful registration)
            assertTrue(responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_CONFLICT,
                    "Expected response code 201 (Created) or 409 (Conflict), but got " + responseCode);
        }

        // Step 2: Acquire a package 5 times
        for (int i = 1; i <= 5; i++) {
            // Set up the HTTP connection to acquire a package
            URL url = new URL("http://localhost:10001/transactions/packages");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Authorization", "Bearer testUser100-mtcgToken");

            // Send the request with an empty payload (as per your curl example)
            try (OutputStream os = urlConnection.getOutputStream()) {
                os.write("".getBytes()); // Empty body
                os.flush();
            }

            // Get the response code
            int responseCode = urlConnection.getResponseCode();
            if (i <= 4) {
                // First 4 iterations should return 201 CREATED
                assertEquals(HttpURLConnection.HTTP_CREATED, responseCode, "Expected response code 201 (CREATED) on iteration " + i);
            } else {
                // Fifth iteration should return 403 Forbidden
                assertEquals(HttpURLConnection.HTTP_FORBIDDEN, responseCode, "Expected response code 403 (Forbidden) on iteration " + i);
            }
        }
    }

    @Test
    @Order(5)
    void testAcquirePackageNoPackagesLeft() throws Exception {
        // Step 1: Create a new user
        {
            String newUserJson = "{\"Username\":\"testUser101\",\"Password\":\"newpass\"}";

            // Set up connection to the registration endpoint
            URL userUrl = new URL("http://localhost:10001/users"); // Adjust URL as needed
            HttpURLConnection userConnection = (HttpURLConnection) userUrl.openConnection();
            userConnection.setDoOutput(true);
            userConnection.setRequestMethod("POST");
            userConnection.setRequestProperty("Content-Type", "application/json");

            // Write the JSON string to the output stream
            try (OutputStream os = userConnection.getOutputStream()) {
                os.write(newUserJson.getBytes());
                os.flush();
            }

            // Check response code for user creation
            int userResponseCode = userConnection.getResponseCode();
            assertTrue(userResponseCode == HttpURLConnection.HTTP_CREATED || userResponseCode == HttpURLConnection.HTTP_CONFLICT,
                    "Expected response code 201 (Created) or 409 (Conflict), but got " + userResponseCode);
        }

        // Step 2 and 3: Acquire packages in a loop
        for (int attempt = 1; attempt <= 2; attempt++) {
            // Set up the HTTP connection to acquire a package
            URL url = new URL("http://localhost:10001/transactions/packages");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Authorization", "Bearer testUser101-mtcgToken");

            // Send the request with an empty payload
            try (OutputStream os = urlConnection.getOutputStream()) {
                os.write("".getBytes());
                os.flush();
            }

            // Get the response code
            int responseCode = urlConnection.getResponseCode();

            if (attempt == 1) {
                // First attempt should succeed
                assertEquals(HttpURLConnection.HTTP_CREATED, responseCode, "Expected response code 201 (Created) on first acquisition");
            } else {
                // Second attempt should fail
                assertEquals(HttpURLConnection.HTTP_NOT_FOUND, responseCode, "Expected response code 404 (Not Found) on second acquisition");
            }
        }
    }

    @Test
    @Order(6)
    void testAcquirePackageUserNotFound() throws Exception {
        URL url = new URL("http://localhost:10001/transactions/packages");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Authorization", "Bearer anonymous-mtcgToken");

        // Send the request with an empty payload
        try (OutputStream os = urlConnection.getOutputStream()) {
            os.write("".getBytes());
            os.flush();
        }

        // Get the response code
        int responseCode = urlConnection.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, responseCode, "Expected response code 404 (Not Found)");
    }

    @Test
    @Order(7)
    void testShowAcquiredCardsAuthorized() throws Exception {
        URL url = new URL("http://localhost:10001/cards");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Authorization", "Bearer testUser100-mtcgToken");

        // Get and assert the response code
        int responseCode = urlConnection.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_OK, responseCode, "Expected HTTP 200 (OK)");
    }

    @Test
    @Order(8)
    void testShowAcquiredCardsUnknownUser() throws Exception {
        URL url = new URL("http://localhost:10001/cards");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Authorization", "Bearer testUser001-mtcgToken");

        // Get and assert the response code
        int responseCode = urlConnection.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, responseCode, "Expected HTTP 404 (NOT_FOUND)");
    }

    @Test
    @Order(9)
    void testShowAcquiredCardsUnauthorized() throws Exception {
        URL url = new URL("http://localhost:10001/cards");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");

        // Get and assert the response code
        int responseCode = urlConnection.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, responseCode, "Expected HTTP 401 (Unauthorized)");
    }

    @Test
    @Order(10)
    void testViewDeck() throws Exception {
        // Setup the HTTP connection
        URL url = new URL("http://localhost:10001/deck");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Authorization", "Bearer testUser100-mtcgToken");

        // Get the response code
        int responseCode = urlConnection.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_OK, responseCode, "Expected HTTP 200 (OK)");
        // Validate the Content-Type header for JSON
        String contentType = urlConnection.getHeaderField("Content-Type");
        assertTrue(contentType.contains("application/json"), "Expected Content-Type to be application/json");
    }

    @Test
    @Order(11)
    void testViewDeckPlainText() throws Exception {
        // Setup the HTTP connection
        URL url = new URL("http://localhost:10001/deck?format=plain");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Authorization", "Bearer testUser100-mtcgToken");

        // Get the response code
        int responseCode = urlConnection.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_OK, responseCode, "Expected HTTP 200 (OK)");
        // Validate the Content-Type header for plain text
        String contentType = urlConnection.getHeaderField("Content-Type");
        assertTrue(contentType.contains("text/plain"), "Expected Content-Type to be text/plain");
    }

    @Test
    @Order(12)
    void testConfigureDeckNotFourCards() throws Exception {
        // Setup the HTTP connection
        URL url = new URL("http://localhost:10001/deck");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true); // Allow writing a request body
        urlConnection.setRequestMethod("PUT");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Authorization", "Bearer testUser100-mtcgToken");

        // Invalid deck payload with only 3 cards
        String invalidDeckJson = """
            ["card31", "card32", "card33"]
        """;

        // Write the invalid payload to the connection
        try (OutputStream os = urlConnection.getOutputStream()) {
            os.write(invalidDeckJson.getBytes());
            os.flush();
        }

        // Get the response code and assert it is 400 Bad Request
        int responseCode = urlConnection.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, responseCode, "Expected HTTP 400 (Bad Request)");
    }

    @Test
    @Order(13)
    void testConfigureDeckUserNotFound() throws Exception {
        // Setup the HTTP connection
        URL url = new URL("http://localhost:10001/deck");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true); // Allow writing a request body
        urlConnection.setRequestMethod("PUT");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Authorization", "Bearer anonymous-mtcgToken");

        // Valid deck payload with 4 cards
        String validDeckJson = """
            ["card31", "card32", "card33", "card34"]
        """;

        // Write the valid payload to the connection
        try (OutputStream os = urlConnection.getOutputStream()) {
            os.write(validDeckJson.getBytes());
            os.flush();
        }

        // Get the response code and assert it is 404 Not Found
        int responseCode = urlConnection.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, responseCode, "Expected HTTP 404 (Not Found)");
    }

    @Test
    @Order(14)
    void testConfigureDeckCardDoesNotBelongToUser() throws Exception {
        // create new package since acquiring cards is randomized
        {
            {
                URL url = new URL("http://localhost:10001/packages");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Authorization", "Bearer admin-mtcgToken");

                String cardsJson = String.format("""
                    [
                        {"C_ID": "card101", "name": "Water Goblin", "damage": 10, "element_type": "Water", "card_type": "Monster", "trait": "Goblin"},
                        {"C_ID": "card102", "name": "Dragon", "damage": 50, "element_type": "Fire", "card_type": "Monster", "trait": "Dragon"},
                        {"C_ID": "card103", "name": "Ork", "damage": 20, "element_type": "Normal", "card_type": "Monster", "trait": "Ork"},
                        {"C_ID": "card104", "name": "Fire Spell", "damage": 25, "element_type": "Fire", "card_type": "Magic", "trait": "Spell"},
                        {"C_ID": "card105", "name": "Water Spell", "damage": 30, "element_type": "Water", "card_type": "Magic", "trait": "Spell"}
                    ]
                    """);

                try (OutputStream os = urlConnection.getOutputStream()) {
                    os.write(cardsJson.getBytes());
                    os.flush();
                }

                // Get and assert the response code
                int responseCode = urlConnection.getResponseCode();
                assertEquals(HttpURLConnection.HTTP_CREATED, responseCode, "Expected response code 201 (Created)");
            }
            // testUser101 acquires the new package
            URL url = new URL("http://localhost:10001/transactions/packages");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Authorization", "Bearer testUser101-mtcgToken");

            // Send the request with an empty payload
            try (OutputStream os = urlConnection.getOutputStream()) {
                os.write("".getBytes()); // Empty body
                os.flush();
            }
            int responseCode = urlConnection.getResponseCode();
            assertEquals(HttpURLConnection.HTTP_CREATED, responseCode, "Expected response code 201 (CREATED)");
        }

        // Setup the HTTP connection
        URL url = new URL("http://localhost:10001/deck");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true); // Allow writing a request body
        urlConnection.setRequestMethod("PUT");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Authorization", "Bearer testUser100-mtcgToken");

        // Payload containing a card not owned by the user
        String invalidDeckJson = """
            ["card101", "card102", "103", "card104"]
        """;

        // Write the payload to the connection
        try (OutputStream os = urlConnection.getOutputStream()) {
            os.write(invalidDeckJson.getBytes());
            os.flush();
        }

        // Get the response code and assert it is 400 Bad Request
        int responseCode = urlConnection.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, responseCode, "Expected HTTP 400 (Bad Request)");

        // Optional: Read the response body for validation
        InputStream responseStream = urlConnection.getErrorStream();
        String jsonResponse = new BufferedReader(new InputStreamReader(responseStream))
                .lines()
                .reduce("", (accumulator, actual) -> accumulator + actual);
        System.out.println("Response: " + jsonResponse);
        assertTrue(jsonResponse.contains("Card does not belong to the user"), "Expected 'Card does not belong to the user' message in response");
    }

    @Test
    @Order(15)
    void testConfigureDeckCardNotFound() throws Exception {
        // Setup the HTTP connection
        URL url = new URL("http://localhost:10001/deck");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true); // Allow writing a request body
        urlConnection.setRequestMethod("PUT");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Authorization", "Bearer testUser100-mtcgToken");

        // Payload containing a non-existent card
        String invalidDeckJson = """
            ["card31", "card32", "nonExistentCard", "card34"]
        """;

        // Write the payload to the connection
        try (OutputStream os = urlConnection.getOutputStream()) {
            os.write(invalidDeckJson.getBytes());
            os.flush();
        }

        // Get the response code and assert it is 400 Bad Request
        int responseCode = urlConnection.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, responseCode, "Expected HTTP 400 (Bad Request)");
    }

    @Test
    @Order(16)
    void testDeleteUserAndCards() throws Exception {
        for (int attempt = 1; attempt <= 2; attempt++) {
            String deleteUserJson = "";

            if (attempt == 1) {
                deleteUserJson = "{\"Username\":\"testUser100\",\"Password\":\"testpass\"}";
            } else if (attempt == 2) {
                deleteUserJson = "{\"Username\":\"testUser101\",\"Password\":\"newpass\"}";
            }


            // Set up connection to the delete endpoint
            URL url = new URL("http://localhost:10001/users"); // Adjust URL as needed
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("DELETE");
            urlConnection.setRequestProperty("Content-Type", "application/json");

            // Write the JSON string to the output stream
            try (OutputStream os = urlConnection.getOutputStream()) {
                os.write(deleteUserJson.getBytes());
                os.flush();
            }

            // Check response code
            int responseCode = urlConnection.getResponseCode();

            // Assert that the response code is 200 OK
            assertEquals(HttpURLConnection.HTTP_OK, responseCode,
                    "Expected response code 200 (OK), but got " + responseCode);
        }
        String deleteCardsJson = "";

        // Set up connection to the delete endpoint
        URL url = new URL("http://localhost:10001/cards"); // Adjust URL as needed
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("DELETE");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Authorization", "Bearer admin-mtcgToken");

        // Write the JSON string to the output stream
        try (OutputStream os = urlConnection.getOutputStream()) {
            os.write(deleteCardsJson.getBytes());
            os.flush();
        }

        // Check response code
        int responseCode = urlConnection.getResponseCode();

        // Assert that the response code is 200 OK
        assertEquals(HttpURLConnection.HTTP_OK, responseCode,
                "Expected response code 200 (OK), but got " + responseCode);
    }
}