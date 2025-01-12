package at.fhtw.MTCG.service.packages;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
class PackagesTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Test
    @Order(1)
    void testCreatePackageWithCards() throws Exception {
        String cardsJson = """
        [
            {"C_ID": "card1", "name": "Water Goblin", "damage": 10, "element_type": "Water", "card_type": "Monster", "trait": "Goblin"},
            {"C_ID": "card2", "name": "Dragon", "damage": 50, "element_type": "Fire", "card_type": "Monster", "trait": "Dragon"},
            {"C_ID": "card3", "name": "Ork", "damage": 20, "element_type": "Normal", "card_type": "Monster", "trait": "Ork"},
            {"C_ID": "card4", "name": "Fire Spell", "damage": 25, "element_type": "Fire", "card_type": "Magic", "trait": "Spell"},
            {"C_ID": "card5", "name": "Water Spell", "damage": 30, "element_type": "Water", "card_type": "Magic", "trait": "Spell"}
        ]
    """;

        URL url = new URL("http://localhost:10001/packages");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = urlConnection.getOutputStream()) {
            os.write(cardsJson.getBytes());
            os.flush();
        }

        // Get and assert the response code
        int responseCode = urlConnection.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_CREATED, responseCode, "Expected response code 201 (Created)");
    }

}