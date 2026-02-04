package org.interview.tecalliance.exception;

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

        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(errorMessage, body.get("message"));
    }

    @Test
    void testHandleIllegalArgumentException_SanitizesXSS() {
        String maliciousMessage = "Error: <script>alert('XSS')</script>";
        IllegalArgumentException ex = new IllegalArgumentException(maliciousMessage);

        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/api/articles");
        WebRequest request = new ServletWebRequest(servletRequest);

        ResponseEntity<Object> response = exceptionHandler.handleIllegalArgumentException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> body = (Map<String, Object>) response.getBody();

        String sanitizedMessage = (String) body.get("message");
        assertTrue(sanitizedMessage.contains("&lt;script&gt;"));
        assertFalse(sanitizedMessage.contains("<script>"));
    }

    @Test
    void testHandleGlobalException_Returns500() {
        Exception ex = new RuntimeException("Unexpected error");

        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/api/articles/1");
        WebRequest request = new ServletWebRequest(servletRequest);

        ResponseEntity<Object> response = exceptionHandler.handleGlobalException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(500, body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals("An unexpected error occurred", body.get("message"));
        assertTrue(body.get("path").toString().contains("/api/articles/1"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void testResponseBodyStructure() {
        IllegalArgumentException ex = new IllegalArgumentException("Test error");
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/api/test");
        WebRequest request = new ServletWebRequest(servletRequest);

        ResponseEntity<Object> response = exceptionHandler.handleIllegalArgumentException(ex, request);

        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertNotNull(body);
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body.containsKey("status"));
        assertTrue(body.containsKey("error"));
        assertTrue(body.containsKey("message"));
        assertTrue(body.containsKey("path"));
    }
}
