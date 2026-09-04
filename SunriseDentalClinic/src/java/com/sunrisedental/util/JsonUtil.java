package com.sunrisedental.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public final class JsonUtil {

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private JsonUtil() {
    }

    public static Gson gson() {
        return GSON;
    }

    public static JsonObject parseBody(String body) {
        if (body == null || body.isBlank()) {
            return new JsonObject();
        }
        return JsonParser.parseString(body).getAsJsonObject();
    }

    public static void writeJson(HttpServletResponse resp, int status, Object data) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(status);
        try (PrintWriter out = resp.getWriter()) {
            out.print(GSON.toJson(data));
        }
    }

    public static void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        JsonObject err = new JsonObject();
        err.addProperty("message", message);
        writeJson(resp, status, err);
    }
}
