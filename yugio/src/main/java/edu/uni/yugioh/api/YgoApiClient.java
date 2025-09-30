package edu.uni.ygoduellite.api;

import edu.uni.ygoduellite.model.Card;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class YgoApiClient {
    private static final String RANDOM_CARD_URL = "https://db.ygoprodeck.com/api/v7/cardinfo.php?num=1&offset=0&sort=random&cachebust";
    private final HttpClient client = HttpClient.newHttpClient();

    public Card getRandomMonsterCard() throws IOException, InterruptedException {
        StringBuilder debugInfo = new StringBuilder();

        for (int attempts = 0; attempts < 5; attempts++) {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(RANDOM_CARD_URL))
                    .GET()
                    .build();

            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                debugInfo.append("Intento ").append(attempts + 1)
                        .append(": status ").append(resp.statusCode()).append("\n");
                continue;
            }

            JSONObject json = new JSONObject(resp.body());
            JSONObject cardJson;

            if (json.has("data")) {
                JSONArray dataArr = json.getJSONArray("data");
                if (dataArr.isEmpty()) continue;
                cardJson = dataArr.getJSONObject(0);
            } else {
                cardJson = json;
            }

            String type = cardJson.optString("type", "");
            if (!type.toLowerCase().contains("monster")) continue;

            String name = cardJson.optString("name", "Unknown");
            int atk = cardJson.optInt("atk", 0);
            int def = cardJson.optInt("def", 0);

            String imageUrl = null;
            if (cardJson.has("card_images")) {
                JSONArray imgs = cardJson.getJSONArray("card_images");
                if (!imgs.isEmpty()) {
                    JSONObject img = imgs.getJSONObject(0);
                    imageUrl = img.optString("image_url", null);
                }
            }

            return new Card(name, atk, def, imageUrl);
        }

        // Si no encontramos ninguna Monster, lanzamos la excepción
        throw new IOException("No se obtuvo carta Monster tras varios intentos.");
    }
}
