package com.ija.student_management_portal.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * Forwards all non-API, non-static-asset routes to index.html so that
 * the React SPA can handle client-side routing.
 *
 * Uses two strategies:
 * 1. Explicit mappings for known top-level SPA routes.
 * 2. Implements {@link ErrorController} so that 404s for unknown paths
 *    (e.g. /students/2025-0001) are forwarded to the SPA instead of
 *    returning a Whitelabel error page.
 */
@Controller
public class SpaForwardingController implements ErrorController {

    /**
     * Known top-level SPA routes that don't clash with /api or static assets.
     */
    @RequestMapping(value = {
            "/login",
            "/register",
            "/home",
            "/add",
            "/admin/**",
            "/student/**",
            "/students/**",
            "/accept/**",
            "/bulk-import/**"
    })
    public String forwardKnownRoutes() {
        return "forward:/index.html";
    }

    /**
     * Catch-all for any route that Spring couldn't resolve.
     * API routes and static files get a proper error response;
     * everything else forwards to the SPA for client-side routing.
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uri = (String) request.getAttribute("jakarta.servlet.error.request_uri");

        if (uri != null && (uri.startsWith("/api/") || uri.contains("."))) {
            // Return a proper JSON error for API endpoints and static file misses
            Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
            int status = statusCode != null ? statusCode : HttpStatus.NOT_FOUND.value();
            response.setStatus(status);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Not found\",\"status\":" + status + "}");
            return null;
        }

        // Forward to the SPA for client-side routing
        return "forward:/index.html";
    }
}

