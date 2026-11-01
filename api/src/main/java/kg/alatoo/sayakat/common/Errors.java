package kg.alatoo.sayakat.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class Errors {

    private Errors() {
    }

    public static ResponseStatusException notFound(String what, Object id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " " + id + " not found");
    }

    public static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    public static ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    public static ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }
}
