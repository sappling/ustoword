package org.appling.ustoword;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.GetRequest;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by sappling on 9/5/2016.
 */
public class RallySortedTreeWalker {
    private RallyRestApi restApi;
    private Fetch standardFetch = new Fetch("FormattedID", "Name", "Children","Feature", "ObjectID", "DragAndDropRank", "Description", "DirectChildrenCount", "_ref", "_type", "UserStories");

    public RallySortedTreeWalker(RallyRestApi restApi) {
        this.restApi = restApi;
    }

    public void walk(JsonObject parent, WalkAction action, int depth)  {
        action.act(parent, depth);
        if (parent.getAsJsonPrimitive("DirectChildrenCount").getAsInt() > 0) {
            QueryRequest queryRequest;
            String parentType = parent.get("_type").getAsString();
            String id = parent.get("ObjectID").getAsString();

            // Unfortunately rally denotes parentage differently for different types
            if (parentType.equals("PortfolioItem/Feature")) {
                queryRequest = new QueryRequest("HierarchicalRequirement");
                // Has this feature, but no parent.  Only want the direct feature children, not grandchildren
                queryRequest.setQueryFilter(new QueryFilter("Feature.ObjectID", "=", id)
                        .and(new QueryFilter("Parent", "=", "null")));
            }
            else if (parentType.equals("HierarchicalRequirement")) {
                queryRequest = new QueryRequest("HierarchicalRequirement");
                queryRequest.setQueryFilter(new QueryFilter("Parent.ObjectID", " = ", id));
            }
            else { // must be an Initiative
                queryRequest = new QueryRequest("PortfolioItem/Feature");
                queryRequest.setQueryFilter(new QueryFilter("Parent.ObjectID", "=", id));
            }
            queryRequest.setOrder("DragAndDropRank ASC");
            queryRequest.setFetch(standardFetch);
            QueryResponse response = null;
            try {
                response = restApi.query(queryRequest);
                if (response.wasSuccessful()) {
                    JsonArray jsonArray = response.getResults();
                    Iterator<JsonElement> it = jsonArray.iterator();
                    while (it.hasNext()) {
                        walk(it.next().getAsJsonObject(), action, depth+1);
                    }
                }
            } catch (IOException e) {
                System.err.println("Exception getting children from: "+queryRequest.toUrl());
                e.printStackTrace();
            }
        }
    }
}
