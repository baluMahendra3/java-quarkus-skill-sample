package org.acme.common.error;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable exception) {
        int status = resolveStatus(exception);
        String message = resolveMessage(exception, status);
        List<String> details = resolveDetails(exception);

        ErrorResponse body = new ErrorResponse(
                message,
                status,
                uriInfo != null ? uriInfo.getPath() : null,
                OffsetDateTime.now().toString(),
                details
        );

        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    private int resolveStatus(Throwable exception) {
        if (exception instanceof ApiException apiException) {
            return apiException.getStatus();
        }
        if (exception instanceof ConstraintViolationException) {
            return Response.Status.BAD_REQUEST.getStatusCode();
        }
        if (exception instanceof DateTimeParseException || exception instanceof IllegalArgumentException) {
            return Response.Status.BAD_REQUEST.getStatusCode();
        }
        if (exception instanceof NotAuthorizedException) {
            return Response.Status.UNAUTHORIZED.getStatusCode();
        }
        if (exception instanceof ForbiddenException) {
            return Response.Status.FORBIDDEN.getStatusCode();
        }
        if (exception instanceof NotFoundException) {
            return Response.Status.NOT_FOUND.getStatusCode();
        }
        if (exception instanceof NotAllowedException) {
            return Response.Status.METHOD_NOT_ALLOWED.getStatusCode();
        }
        if (exception instanceof BadRequestException) {
            return Response.Status.BAD_REQUEST.getStatusCode();
        }
        if (exception instanceof WebApplicationException webApplicationException
                && webApplicationException.getResponse() != null) {
            return webApplicationException.getResponse().getStatus();
        }
        return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
    }

    private String resolveMessage(Throwable exception, int status) {
        if (exception instanceof ApiException apiException) {
            return apiException.getMessage();
        }
        if (exception instanceof ConstraintViolationException) {
            return "Validation failed";
        }
        if (exception instanceof DateTimeParseException dateTimeParseException) {
            return dateTimeParseException.getMessage();
        }
        if (exception instanceof IllegalArgumentException illegalArgumentException) {
            return illegalArgumentException.getMessage();
        }
        Response.StatusType statusInfo = Response.Status.fromStatusCode(status);
        if (statusInfo != null) {
            return statusInfo.getReasonPhrase();
        }
        return "Unexpected error";
    }

    private List<String> resolveDetails(Throwable exception) {
        if (exception instanceof ConstraintViolationException violationException) {
            return violationException.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .sorted()
                    .toList();
        }
        return null;
    }
}
