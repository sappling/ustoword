package org.appling.ustoword;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Created by sappling on 9/5/2016.
 */
public class HTMLWriter implements WalkAction {
    private PrintWriter writer;
    Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public HTMLWriter(OutputStream out) {
        writer = new PrintWriter(new BufferedOutputStream(out));
    }

    public void close() {
        writer.close();
    }

    public void writeHeader() {
        writer.println("<html>");
        writer.println("<body>");
    }

    public void writeFooter() {
        writer.println("</body>");
        writer.println("</html>");
        writer.flush();
    }

    private String toHTML(JsonElement element) {
        String json = gson.toJson(element);
        if (json.length()>0 && json.startsWith("\"")) {
            // was a string, need to de-stringify
            String noQuotes = json.substring(1, json.length() - 1);
            return noQuotes.replace("\\","");
        }
        return json;
    }
    @Override
    public void act(JsonObject obj, int depth) {
        writer.print("<h"+depth+">");
        writer.print(obj.get("FormattedID").getAsString());
        writer.print("&nbsp;-&nbsp;");
        writer.print(obj.get("Name").getAsString());
        writer.println("</h" + depth + ">");

        writer.println("<div>");
        writer.println(toHTML(obj.get("Description")));
        writer.println("</div>");

        writer.flush();
    }
}
