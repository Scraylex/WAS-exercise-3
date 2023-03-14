package farm;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;


public class FarmKG extends Artifact {

    private static final String USERNAME = "was-students";
    private static final String PASSWORD = "knowledge-representation-is-fun";

    private String repoLocation;

    private static String PREFIXES = "PREFIX was: <https://was-course.interactions.ics.unisg.ch/farm-ontology#>\n"+
    "PREFIX hmas: <https://purl.org/hmas/>\n" +
    "PREFIX td: <https://www.w3.org/2019/wot/td#>\n";
    

    public void init(String repoLocation) {
        this.repoLocation = repoLocation;
    }

    @OPERATION
    public void queryFarm(OpFeedbackParam<String> farm){
        // the variable where we will store the result to be returned to the agent
        String farmValue = null;

        // sets your variable name for the farm to be queried
        String farmVariableName = "farm-1";

        // constructs query
        String queryString = "SELECT ?farm WHERE {\n" +
                "GRAPH <https://sandbox-graphdb.interactions.ics.unisg.ch/was-exercise-3-lukas#> {\n" +
                "   bind (<https://sandbox-graphdb.interactions.ics.unisg.ch/was-exercise-3-lukas#"+farmVariableName+"> as ?farm)\n" +
                "   ?farm a was:Farm.\n" +
                "   }\n" +
                "}";
        String queryStr = PREFIXES + queryString;

        /* Example SPARQL query 
         * PREFIX was: <https://was-course.interactions.ics.unisg.ch/farm-ontology#>
         * PREFIX hmas: <https://purl.org/hmas/>
         * PREFIX td: <https://www.w3.org/2019/wot/td#>
         * SELECT ?farm WHERE { ?farm a was:Farm. }
         */

        // executes query
        JsonArray farmBindings = executeQuery(queryStr);
        
        /* Example JSON result
         * [{"farm":
         *  {
         *   "type":"uri",
         *   "value":"https://sandbox-graphdb.interactions.ics.unisg.ch/was-exercise-3-danai#farm-17c04810-567a-4236-b310-611bb4fd2a8c"
         *  }
         * }]
         */

        // handles result as JSON object
        JsonObject firstBinding = farmBindings.get(0).getAsJsonObject();
        JsonObject farmBinding = firstBinding.getAsJsonObject(farmVariableName);
        farmValue = farmBinding.getAsJsonPrimitive("value").getAsString();

        // sets the value of interest to the OpFeedbackParam
        farm.set(farmValue);
    }


    @OPERATION 
    public void queryThing(String farm, String offeredAffordance, OpFeedbackParam<String> thingDescription) {
        // the variable where we will store the result to be returned to the agent
        String tdValue = null; 

        // sets your variable name for the farm to be queried
        String tdVariableName = "td";

        // constructs query
        String queryStr = PREFIXES + "SELECT ?" + tdVariableName + " WHERE {\n" + 
            "<" + farm + "> hmas:contains ?thing.\n" +
            "?thing td:hasActionAffordance ?aff.\n" +
            "?thing hmas:hasProfile ?" + tdVariableName + ".\n" +
            "?aff a <" + offeredAffordance +">.} LIMIT 1";
        
        /* Example SPARQL query 
         * PREFIX was: <https://was-course.interactions.ics.unisg.ch/farm-ontology#>
         * PREFIX hmas: <https://purl.org/hmas/>
         * PREFIX td: <https://www.w3.org/2019/wot/td#>
         * SELECT ?td WHERE {
         *   <https://sandbox-graphdb.interactions.ics.unisg.ch/was-exercise-3-danai#farm-17c04810-567a-4236-b310-611bb4fd2a8c> hmas:contains ?thing.
         *   ?thing td:hasActionAffordance ?aff.
         *   ?thing hmas:hasProfile ?td.
         *   ?aff a <https://was-course.interactions.ics.unisg.ch/farm-ontology#ReadSoilMoistureAffordance>.
         * } LIMIT 1
         */

        // executes query
        JsonArray tdBindings = executeQuery(queryStr);

        /* Example JSON result
         * [{"td":
         *  {
         *   "type":"uri",
         *   "value":"https://raw.githubusercontent.com/Interactions-HSG/example-tds/was/tds/tractor1.ttl"
         *  }
         * }]
         */
        
        // handles result as JSON object
        JsonObject firstBinding = tdBindings.get(0).getAsJsonObject();
        JsonObject tdBinding = firstBinding.getAsJsonObject(tdVariableName);
        tdValue = tdBinding.getAsJsonPrimitive("value").getAsString();

        // sets the value of interest to the OpFeedbackParam
        thingDescription.set(tdValue);
    }

    @OPERATION
    public void queryFarmSections(String farm, OpFeedbackParam<Object[]> sections) {
        // the variable where we will store the result to be returned to the agent
        Object[] sectionsValue = new Object[]{ "fakeSection1", "fakeSection2", "fakeSection3", "fakeSection4" };

        String queryString = "SELECT ?section WHERE {\n" +
                "GRAPH <https://sandbox-graphdb.interactions.ics.unisg.ch/was-exercise-3-lukas#> {\n" +
                "   bind (<"+farm+"> as ?farm)\n" +
                "   ?farm a was:Farm.\n" +
                "   ?farm hmas:contains ?section.\n" +
                "   ?section a was:Section. \n" +
                " }\n" +
                "}";
        String queryStr = PREFIXES + queryString;

        JsonArray farmBindings = executeQuery(queryStr);

        JsonObject firstBinding = farmBindings.get(0).getAsJsonObject();
        JsonArray farmBinding = firstBinding.getAsJsonArray("section");

        final var list = new ArrayList<Object>();
        for(int i = 0; i < 4; i++) {
            JsonElement jsonElement = farmBinding.get(i);
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            String value = asJsonObject.getAsJsonPrimitive("value").getAsString();
            list.add(value);
        }



        // sets the value of interest to the OpFeedbackParam
        sections.set(list.toArray());
    }

    @OPERATION
    public void querySectionCoordinates(String section, OpFeedbackParam<Object[]> coordinates) {
        // the variable where we will store the result to be returned to the agent
        Object[] coordinatesValue = new Object[]{ 0, 0, 1, 1 };

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
        Object[] t = Arrays.stream(split).map(Integer::getInteger).toArray();
        coordinates.set(t);
    }

    @OPERATION 
    public void queryCropOfSection(String section, OpFeedbackParam<String> crop) {
        // the variable where we will store the result to be returned to the agent
        String cropValue = "fakeCrop";
        Object[] coordinatesValue = new Object[]{ 0, 0, 1, 1 };

        String queryString = "SELECT ?crop WHERE {\n" +
                "GRAPH <https://sandbox-graphdb.interactions.ics.unisg.ch/was-exercise-3-lukas#> {\n" +
                "   bind (<"+section+"> as ?section)\n" +
                "   ?section a was:Section.\n" +
                "   ?section hmas:contains ?crop.\n" +
                " }\n" +
                "}";
        String queryStr = PREFIXES + queryString;

        JsonArray farmBindings = executeQuery(queryStr);

        JsonObject firstBinding = farmBindings.get(0).getAsJsonObject();

        JsonObject tdBinding = firstBinding.getAsJsonObject("crop");
        final var tdValue = tdBinding.getAsJsonPrimitive("value").getAsString();

        // sets the value of interest to the OpFeedbackParam
        crop.set(tdValue);
    }

    @OPERATION
    public void queryRequiredMoisture(String crop, OpFeedbackParam<Integer> level) {
        // the variable where we will store the result to be returned to the agent
        Integer moistureLevelValue = 120;

        String queryString = "SELECT ?moisture WHERE {\n" +
                "GRAPH <https://sandbox-graphdb.interactions.ics.unisg.ch/was-exercise-3-lukas#> {\n" +
                "   bind (<"+ crop+"> as ?crop)\n" +
                "   ?crop was:hasSoilmoisture ?moisture. \n" +
                " }\n" +
                "}";
        String queryStr = PREFIXES + queryString;

        JsonArray farmBindings = executeQuery(queryStr);

        JsonObject firstBinding = farmBindings.get(0).getAsJsonObject();

        JsonObject tdBinding = firstBinding.getAsJsonObject("moisture");
        final var tdValue = tdBinding.getAsJsonPrimitive("value").getAsString();

        // sets the value of interest to the OpFeedbackParam
        final var value = Integer.valueOf(tdValue);

        // sets the value of interest to the OpFeedbackParam
        level.set(value);
    }

    private JsonArray executeQuery(String queryStr) {
        String queryUrl = this.repoLocation + "?query=" +  URLEncoder.encode(queryStr, StandardCharsets.UTF_8);
        
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
