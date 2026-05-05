package org.acme.common.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionMapperTest {

    @Test
    void toResponseShouldMapApiException() throws Exception {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();

        UriInfo uriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfo.getPath()).thenReturn("api/test");

        Field field = GlobalExceptionMapper.class.getDeclaredField("uriInfo");
        field.setAccessible(true);
        field.set(mapper, uriInfo);

        Response response = mapper.toResponse(new ApiException(404, "Not found"));
        ErrorResponse body = (ErrorResponse) response.getEntity();

        assertEquals(404, response.getStatus());
        assertNotNull(body);
        assertEquals("Not found", body.error);
        assertEquals("api/test", body.path);
        assertEquals("application/json", response.getMediaType().toString());
    }

    @Test
    void toResponseShouldMapIllegalArgumentToBadRequest() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();
        Response response = mapper.toResponse(new IllegalArgumentException("bad input"));
        ErrorResponse body = (ErrorResponse) response.getEntity();

        assertEquals(400, response.getStatus());
        assertEquals("bad input", body.error);
    }

    @Test
    void toResponseShouldMapConstraintViolationsToValidationFailed() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();
        ConstraintViolation<?> violation = Mockito.mock(ConstraintViolation.class);
        Path path = Mockito.mock(Path.class);

        Mockito.when(path.toString()).thenReturn("request.name");
        Mockito.when(violation.getPropertyPath()).thenReturn(path);
        Mockito.when(violation.getMessage()).thenReturn("must not be blank");

        Response response = mapper.toResponse(new ConstraintViolationException(Set.of(violation)));
        ErrorResponse body = (ErrorResponse) response.getEntity();

        assertEquals(400, response.getStatus());
        assertEquals("Validation failed", body.error);
        assertEquals(1, body.details.size());
        assertEquals("request.name: must not be blank", body.details.get(0));
    }

    @Test
    void toResponseShouldMapWebAndSecurityExceptions() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();

        assertEquals(401, mapper.toResponse(new NotAuthorizedException("auth")).getStatus());
        assertEquals(403, mapper.toResponse(new ForbiddenException("forbidden")).getStatus());
        assertEquals(404, mapper.toResponse(new NotFoundException("missing")).getStatus());
        assertEquals(405, mapper.toResponse(new NotAllowedException("POST", new String[]{"GET"})).getStatus());
        assertEquals(400, mapper.toResponse(new BadRequestException("bad")).getStatus());
        assertEquals(418, mapper.toResponse(new WebApplicationException(Response.status(418).build())).getStatus());
    }

    @Test
    void toResponseShouldFallbackForUnexpectedErrors() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();

        Response response = mapper.toResponse(new RuntimeException());
        ErrorResponse body = (ErrorResponse) response.getEntity();

        assertEquals(500, response.getStatus());
        assertEquals("Internal Server Error", body.error);
    }
}
