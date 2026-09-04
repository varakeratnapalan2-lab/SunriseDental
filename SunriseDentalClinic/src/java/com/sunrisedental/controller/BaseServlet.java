package com.sunrisedental.controller;

import com.sunrisedental.util.JsonUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseServlet extends HttpServlet {

    protected String readBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    protected <T> T readJson(HttpServletRequest request, Class<T> type) throws IOException {
        return JsonUtil.gson().fromJson(readBody(request), type);
    }
}
