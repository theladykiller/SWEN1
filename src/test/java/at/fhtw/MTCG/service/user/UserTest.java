package at.fhtw.MTCG.service.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
class UserTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Order(1)
    void testUserRegistration() throws Exception {
        // Sample user data for registration
        String userJson = "{\"Username\":\"testuser\",\"Password\":\"testpass\"}";

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
        assertEquals(HttpURLConnection.HTTP_CREATED, responseCode, "Expected response code 201 (Created) or 409 (Conflict), but got " + responseCode);
    }

    @Test
    @Order(2)
    void testUserRegistrationFailsForDuplicate() throws Exception {
        // Sample user data for duplicate registration (same as in BeforeAll)
        String userJson = "{\"Username\":\"testuser\",\"Password\":\"testpass\"}";

        // Set up connection to the registration endpoint
        URL url = new URL("http://localhost:10001/users");
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

        // Assert that the response code is 409 Conflict
        assertEquals(HttpURLConnection.HTTP_CONFLICT, responseCode,
                "Expected response code 409 (Conflict), but got " + responseCode);
    }

    @Test
    @Order(3)
    void testUserLogin() throws Exception {
        // Sample login data
        String loginJson = "{\"Username\":\"testuser\",\"Password\":\"testpass\"}";

        // Set up connection to the login endpoint
        URL url = new URL("http://localhost:10001/sessions"); // Adjust URL as needed
        URLConnection urlConnection = url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.getOutputStream().write(loginJson.getBytes());

        // Check response code
        int responseCode = ((HttpURLConnection) urlConnection).getResponseCode();
        assertEquals(HttpURLConnection.HTTP_OK, responseCode); // Expect 200 OK

        // Read the response
        InputStream inputStream = urlConnection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String response = bufferedReader.readLine();

        // You may want to check the response content here as well
        Map<String, String> responseMap = objectMapper.readValue(response, new TypeReference<Map<String, String>>() {});
        assertEquals("Login successful", responseMap.get("message"));

        // Validate the token
        String token = responseMap.get("token");
        assertNotNull(token, "Token should not be null");
        assertEquals("testuser-mtcgToken", token, "Token should be in the format 'username-mtcgToken'");

        bufferedReader.close();
    }

    @Test
    @Order(4)
    void testUserLoginInvalidCredentials() throws Exception {
        // Sample login data with invalid credentials
        String loginJson = "{\"Username\":\"invaliduser\",\"Password\":\"wrongpass\"}";

        // Set up connection to the login endpoint
        URL url = new URL("http://localhost:10001/sessions"); // Adjust URL as needed
        URLConnection urlConnection = url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/json");

        // Send the request
        try (OutputStream os = urlConnection.getOutputStream()) {
            os.write(loginJson.getBytes());
        }

        // Check response code
        int responseCode = ((HttpURLConnection) urlConnection).getResponseCode();

        // Assert that the response code is 401 Unauthorized
        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, responseCode); // Test is successful if this assertion passes
    }

    @Test
    @Order(5)
    void testDeleteUser() throws Exception {
        // User credentials to delete (same as registered in @BeforeAll)
        String deleteUserJson = "{\"Username\":\"testuser\",\"Password\":\"testpass\"}";

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

        // Verify the response message (optional, based on your API's behavior)
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
            String responseMessage = reader.readLine();
            assertNotNull(responseMessage, "Response message should not be null");
            assertTrue(responseMessage.contains("User deleted successfully"),
                    "Expected response message to confirm successful deletion");
        }
    }

    @Test
    @Order(6)
    void testConcurrentUserRegistration() throws Exception {
        final int THREAD_COUNT = 10;
        Thread[] threads = new Thread[THREAD_COUNT];
        String baseUsername = "concurrentUser";

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    // Prepare user JSON for each thread
                    String userJson = String.format("{\"Username\":\"%s%d\",\"Password\":\"testpass%d\"}", baseUsername, index, index);

                    // Set up connection to the registration endpoint
                    URL url = new URL("http://localhost:10001/users");
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

                    // Assert that the response is either 201 Created or 409 Conflict
                    assertTrue(responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_CONFLICT,
                            "Expected response code 201 (Created) or 409 (Conflict), but got " + responseCode);

                } catch (Exception e) {
                    fail("Exception occurred during concurrent registration: " + e.getMessage());
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to finish
        for (Thread thread : threads) {
            thread.join();
        }
    }

    @Test
    @Order(7)
    void testConcurrentUserLogin() throws Exception {
        final int THREAD_COUNT = 10;
        Thread[] threads = new Thread[THREAD_COUNT];
        String baseUsername = "concurrentUser";

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    // Prepare login JSON
                    String loginJson = String.format("{\"Username\":\"%s%d\",\"Password\":\"testpass%d\"}", baseUsername, index, index);

                    // Set up connection to the login endpoint
                    URL url = new URL("http://localhost:10001/sessions");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/json");

                    // Write the login JSON to the output stream
                    try (OutputStream os = urlConnection.getOutputStream()) {
                        os.write(loginJson.getBytes());
                        os.flush();
                    }

                    // Check response code
                    int responseCode = urlConnection.getResponseCode();
                    assertEquals(HttpURLConnection.HTTP_OK, responseCode, "Expected response code 200 (OK)");

                    // Read and validate the response
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                        String response = reader.readLine();
                        assertNotNull(response, "Response should not be null");

                        Map<String, String> responseMap = objectMapper.readValue(response, new TypeReference<>() {});
                        assertEquals("Login successful", responseMap.get("message"), "Expected successful login message");
                        assertNotNull(responseMap.get("token"), "Token should not be null");
                    }

                } catch (Exception e) {
                    fail("Exception occurred during concurrent login: " + e.getMessage());
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to finish
        for (Thread thread : threads) {
            thread.join();
        }
    }

    @Test
    @Order(8)
    void testConcurrentUserDeletion() throws Exception {
        final int THREAD_COUNT = 10;
        Thread[] threads = new Thread[THREAD_COUNT];
        String baseUsername = "concurrentUser";

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    // Prepare delete JSON for each user
                    String deleteUserJson = String.format("{\"Username\":\"%s%d\",\"Password\":\"testpass%d\"}", baseUsername, index, index);

                    // Set up connection to the delete endpoint
                    URL url = new URL("http://localhost:10001/users");
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
                            "Expected response code 200 (OK) for user deletion");

                    // Verify the response message (optional)
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                        String responseMessage = reader.readLine();
                        assertNotNull(responseMessage, "Response message should not be null");
                        assertTrue(responseMessage.contains("User deleted successfully"),
                                "Expected response message to confirm successful deletion");
                    }

                } catch (Exception e) {
                    fail("Exception occurred during concurrent deletion: " + e.getMessage());
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to finish
        for (Thread thread : threads) {
            thread.join();
        }
    }
}