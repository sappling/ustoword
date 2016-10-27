package org.appling.ustoword;

import com.google.gson.*;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.GetRequest;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.GetResponse;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by sappling on 9/5/2016.
 */
public class Main {
    static public void main(String args[]) {
        boolean useProxy = true;

        if (args.length < 2) {
            System.err.println("Syntax is ustoword.bat <Initiative ID like I101> <output file name> [noproxy optional]");
            System.exit(-1);
        }

        if (args.length == 3) {
            if (args[2].equalsIgnoreCase("noproxy")) {
                useProxy = false;
            }
        }

        String rally_key = System.getenv("RALLY_KEY");
        if (rally_key == null) {
            System.err.println("Error:  environment variable RALLY_KEY not defined.  This must be set to the Rally API-Key for read only web services.");
            System.exit(-1);
        }

        String ID = args[0];
        String outName = args[1];
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
                StringWriter html = new StringWriter();
                HTMLWriter hwriter = new HTMLWriter(html);
                hwriter.writeHeader();
                //RallyTreeWalker walker = new RallyTreeWalker(restApi);
                RallySortedTreeWalker walker = new RallySortedTreeWalker (restApi);
                walker.walk(jsonInitiative.getAsJsonObject(), hwriter, 1);
                hwriter.writeFooter();

                //System.out.println(prettyPrintJSON(jsonInitiative));

                InputStream stream = Doctest.class.getResourceAsStream("/styles.docx");
                WordprocessingMLPackage wordMLPackage = Docx4J.load(stream);
                MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

                /*
                PrintWriter w = new PrintWriter(new File("out.html"));
                w.append(html.toString());
                w.close();
                */

                documentPart.addAltChunk(AltChunkType.Html, html.toString().getBytes());
                wordMLPackage.save(new File(outName));

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
        } catch (Docx4JException e) {
            e.printStackTrace();
        }
    }

    static String prettyPrintJSON(JsonElement element) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(element);
    }
}
