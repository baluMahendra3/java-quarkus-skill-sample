package org.acme.common.error;

import java.util.List;

public class ErrorResponse {
    public String error;
    public int status;
    public String path;
    public String timestamp;
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
