package am.ik.handson.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public class ErrorResponse {

    private final int status;

    private final String error;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<Detail> details;

    public ErrorResponse(int status, String error, String message, List<Detail> details) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.details = details;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public static class Detail {

        private final String field;

        private final String message;

        public Detail(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }
    }
}
