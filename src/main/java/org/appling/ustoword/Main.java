package org.appling.ustoword;

import com.google.gson.*;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.GetRequest;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.GetResponse;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by sappling on 9/5/2016.
 */
public class Main {
    static public void main(String args[]) {
        boolean useProxy = true;

        if (args.length < 1) {
            System.err.println("Syntax is ustoword.bat <Initiative ID like I101> [noproxy optional]");
            System.exit(-1);
        }
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("noproxy")) {
                useProxy = false;
            }
        }

        String rally_key = System.getenv("RALLY_KEY");
        if (rally_key == null) {
            System.err.println("Error:  environment variable RALLY_KEY not defined.  This must be set to the Rally API-Key for read only web services.");
            System.exit(-1);
        }

        String ID = args[0];
        try {
            RallyRestApi restApi = new RallyRestApi(new URI("https://rally1.rallydev.com"), rally_key);
            if (useProxy) {
                restApi.setProxy(new URI("http://comproxy.utc.com:8080"), "foo", "bar");
            }
            QueryRequest iRequest = new QueryRequest("PortfolioItem/Initiative");

            iRequest.setFetch(new Fetch("FormattedID", "Name", "Children", "Description", "ObjectID", "DirectChildrenCount", "_ref", "_type"));

            iRequest.setQueryFilter(new QueryFilter("FormattedID", "=", ID));

            iRequest.setPageSize(1);
            iRequest.setLimit(1);

            QueryResponse queryResponse = restApi.query(iRequest);
            if (queryResponse.wasSuccessful()) {
                JsonArray resultArray = queryResponse.getResults();
                JsonElement jsonInitiative = resultArray.get(0);
                HTMLWriter hwriter = new HTMLWriter(System.out);
                hwriter.writeHeader();
                //RallyTreeWalker walker = new RallyTreeWalker(restApi);
                RallySortedTreeWalker walker = new RallySortedTreeWalker (restApi);
                walker.walk(jsonInitiative.getAsJsonObject(), hwriter, 1);
                hwriter.writeFooter();

                //System.out.println(prettyPrintJSON(jsonInitiative));
            } else {
                System.out.println("Error:");
                String[] errors = queryResponse.getErrors();
                for (String error : errors) {
                    System.out.println(error);
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String prettyPrintJSON(JsonElement element) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(element);
    }
}
