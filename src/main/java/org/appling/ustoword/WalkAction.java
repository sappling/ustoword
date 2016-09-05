package org.appling.ustoword;

import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;

/**
 * Created by sappling on 9/5/2016.
 */
public interface WalkAction {
    void act(JsonObject obj, int depth);
}
