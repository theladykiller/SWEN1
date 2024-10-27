package at.fhtw.MTCG.service.user;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.net.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserRegistrationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testUserRegistration() throws Exception {
        // Sample user data for registration
        String userJson = "{\"username\":\"testuser\",\"password\":\"testpass\"}";

        // Set up connection to the registration endpoint
        URL url = new URL("http://localhost:10001/register"); // Adjust URL as needed
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
        assertEquals(HttpURLConnection.HTTP_CREATED, responseCode); // Expect 201 Created
    }
}

class UserLoginTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testUserLogin() throws Exception {
        // Sample login data
        String loginJson = "{\"username\":\"testuser\",\"password\":\"testpass\"}";

        // Set up connection to the login endpoint
        URL url = new URL("http://localhost:10001/login"); // Adjust URL as needed
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

        bufferedReader.close();
    }

    @Test
    void testUserLoginInvalidCredentials() throws Exception {
        // Sample login data with invalid credentials
        String loginJson = "{\"username\":\"invaliduser\",\"password\":\"wrongpass\"}";

        // Set up connection to the login endpoint
        URL url = new URL("http://localhost:10001/login"); // Adjust URL as needed
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
}