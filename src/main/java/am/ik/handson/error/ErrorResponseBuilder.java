package am.ik.handson.error;

import am.ik.yavi.core.ConstraintViolations;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

public class ErrorResponseBuilder {

    private List<ErrorResponse.Detail> details;

    private String error;

    private String message;

    private int status;

    public ErrorResponse createErrorResponse() {
        return new ErrorResponse(status, error, message, details);
    }

    public ErrorResponseBuilder withDetails(List<ErrorResponse.Detail> details) {
        this.details = details;
        return this;
    }

    public ErrorResponseBuilder withDetails(ConstraintViolations violations) {
        this.details = violations.details().stream()
            .map(d -> new ErrorResponse.Detail((String) d.getArgs()[0], d.getDefaultMessage()))
            .collect(Collectors.toList());
        return this;
    }

    public ErrorResponseBuilder withMessage(String message) {
        this.message = message;
        return this;
    }

    public ErrorResponseBuilder withStatus(HttpStatus status) {
        this.status = status.value();
        this.error = status.getReasonPhrase();
        return this;
    }
}