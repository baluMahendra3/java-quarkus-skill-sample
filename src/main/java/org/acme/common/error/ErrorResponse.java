package org.acme.common.error;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(name = "ErrorResponse", description = "Standard error payload returned for handled API failures")
public class ErrorResponse {
    @Schema(description = "Short, user-safe summary of the error", example = "Validation failed")
    public String error;

    @Schema(description = "HTTP status code for the error response", example = "400")
    public int status;

    @Schema(description = "Request path that produced the error", example = "/api/trips")
    public String path;

    @Schema(description = "Timestamp when the error response was created", example = "2026-05-06T14:25:43.511Z")
    public String timestamp;

    @Schema(description = "Optional field-level or parameter-level validation details", example = "[\"tripDate: must not be null\"]")
    public List<String> details;

    public ErrorResponse() {
    }

    public ErrorResponse(String error, int status, String path, String timestamp, List<String> details) {
        this.error = error;
        this.status = status;
        this.path = path;
        this.timestamp = timestamp;
        this.details = details;
    }
}
