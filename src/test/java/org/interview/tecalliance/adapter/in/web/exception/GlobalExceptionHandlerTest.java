package org.interview.tecalliance.adapter.in.web.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void testHandleIllegalArgumentException_Returns400() {
        // Arrange
        String errorMessage = "Discounts would cause the article price to go below net price";
        IllegalArgumentException ex = new IllegalArgumentException(errorMessage);

        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/api/articles");
        WebRequest request = new ServletWebRequest(servletRequest);

        ResponseEntity<Object> response = exceptionHandler.handleIllegalArgumentException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals(errorMessage, body.get("message"));
        assertTrue(body.get("path").toString().contains("/api/articles"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void testHandleIllegalArgumentException_WithOverlappingDiscounts() {
        String errorMessage = "Multiple discounts have overlapping date ranges. Only one discount can be applicable at a time.";
        IllegalArgumentException ex = new IllegalArgumentException(errorMessage);

        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/api/articles");
        WebRequest request = new ServletWebRequest(servletRequest);

        ResponseEntity<Object> response = exceptionHandler.handleIllegalArgumentException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(400, body.get("status"));
        assertEquals(errorMessage, body.get("message"));
    }

    @Test
    void testHandleIllegalArgumentException_EscapesHtmlInMessage() {
        String errorMessage = "<script>alert('xss')</script>";
        IllegalArgumentException ex = new IllegalArgumentException(errorMessage);

        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/api/articles");
        WebRequest request = new ServletWebRequest(servletRequest);

        ResponseEntity<Object> response = exceptionHandler.handleIllegalArgumentException(ex, request);

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        String message = (String) body.get("message");

        // HTML should be escaped
        assertFalse(message.contains("<script>"));
        assertTrue(message.contains("&lt;script&gt;") || message.contains("&amp;lt;script&amp;gt;"));
    }

    @Test
    void testHandleGlobalException_Returns500() {
        Exception ex = new RuntimeException("Unexpected error");

        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/api/articles/123");
        WebRequest request = new ServletWebRequest(servletRequest);

        ResponseEntity<Object> response = exceptionHandler.handleGlobalException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(500, body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals("An unexpected error occurred", body.get("message"));
        assertTrue(body.get("path").toString().contains("/api/articles/123"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void testHandleGlobalException_DoesNotExposeInternalErrorMessage() {
        // Even though the actual exception has a specific message,
        // we return a generic message to avoid leaking internal details
        Exception ex = new RuntimeException("Database connection failed: user=admin, password=secret");

        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/api/articles");
        WebRequest request = new ServletWebRequest(servletRequest);

        ResponseEntity<Object> response = exceptionHandler.handleGlobalException(ex, request);

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        String message = (String) body.get("message");

        // Should not expose the actual error message
        assertEquals("An unexpected error occurred", message);
        assertFalse(message.contains("password"));
        assertFalse(message.contains("Database"));
    }
}
