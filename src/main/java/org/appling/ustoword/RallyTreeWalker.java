package org.appling.ustoword;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.GetRequest;
import com.rallydev.rest.response.GetResponse;
import com.rallydev.rest.util.Fetch;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by sappling on 9/5/2016.
 */
public class RallyTreeWalker {
    private RallyRestApi restApi;
    private Fetch standardFetch = new Fetch("FormattedID", "Name", "Children", "DragAndDropRank", "Description", "DirectChildrenCount", "_ref", "_type", "UserStories");

    public RallyTreeWalker(RallyRestApi restApi) {
        this.restApi = restApi;
    }

    public void walk(JsonObject parent, WalkAction action, int depth)  {
        action.act(parent, depth);
        if (parent.getAsJsonPrimitive("DirectChildrenCount").getAsInt() > 0) {
            GetRequest getRequest;
            String ref;
            if (parent.get("_type").getAsString().equals("PortfolioItem/Feature")) {
                ref = parent.get("UserStories").getAsJsonObject().get("_ref").getAsString();
                getRequest = new GetRequest(ref);
            }
            else {
                ref = parent.get("Children").getAsJsonObject().get("_ref").getAsString();
                getRequest = new GetRequest(ref);
            }
            getRequest.setFetch(standardFetch);

            GetResponse getResponse = null;
            try {
                getResponse = restApi.get(getRequest);
                if (getResponse.wasSuccessful()) {
                    JsonArray jsonArray = getResponse.getObject().getAsJsonArray("Results");
                    Iterator<JsonElement> it = jsonArray.iterator();
                    while (it.hasNext()) {
                        walk(it.next().getAsJsonObject(), action, depth+1);
                    }
                }
            } catch (IOException e) {
                System.err.println("Exception getting children from: "+ref);
                e.printStackTrace();
            }
        }
    }
}
