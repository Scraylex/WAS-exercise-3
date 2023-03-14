package farm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;


class FarmKGTest {

    private static final String USERNAME = "was-students";
    private static final String PASSWORD = "knowledge-representation-is-fun";

    private String repoLocation;

    private static String PREFIXES = "PREFIX was: <https://was-course.interactions.ics.unisg.ch/farm-ontology#>\n"+
            "PREFIX hmas: <https://purl.org/hmas/>\n" +
            "PREFIX td: <https://www.w3.org/2019/wot/td#>\n";


    @Test
    void testthisshit() {
        final var section = "https://sandbox-graphdb.interactions.ics.unisg.ch/was-exercise-3-lukas#section-1";

        String queryString = "SELECT ?coordinates WHERE {\n" +
                "GRAPH <https://sandbox-graphdb.interactions.ics.unisg.ch/was-exercise-3-lukas#> {\n" +
                "   bind (<"+section+"> as ?section)\n" +
                "   ?section a was:Section. \n" +
                "   ?section was:hasCoordinates ?coordinates\n" +
                " }\n" +
                "}";
        String queryStr = PREFIXES + queryString;

        JsonArray farmBindings = executeQuery(queryStr);

        JsonObject firstBinding = farmBindings.get(0).getAsJsonObject();

        JsonObject tdBinding = firstBinding.getAsJsonObject("coordinates");
        final var tdValue = tdBinding.getAsJsonPrimitive("value").getAsString();
        String[] split = tdValue.split(",");

        // sets the value of interest to the OpFeedbackParam
        Object[] t = Arrays.stream(split).map(Integer::parseInt).toArray();
    }

    private JsonArray executeQuery(String queryStr) {
        String queryUrl = "https://sandbox-graphdb.interactions.ics.unisg.ch/repositories/was-exercise-3-lukas" + "?query=" +  URLEncoder.encode(queryStr, StandardCharsets.UTF_8);

        try {
            URI uri = new URI(queryUrl);
            String authString = USERNAME + ":" + PASSWORD;
            byte[] authBytes = authString.getBytes(StandardCharsets.UTF_8);
            String encodedAuth = Base64.getEncoder().encodeToString(authBytes);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Accept", "application/sparql-results+json")
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    throw new RuntimeException("HTTP error code : " + response.statusCode());
                }
                String responseBody = response.body();
                JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
                JsonArray bindingsArray = jsonObject.getAsJsonObject("results").getAsJsonArray("bindings");
                return bindingsArray;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return new JsonArray();
    }
}