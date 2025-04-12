package maksim.booksservice.utils.enums;

import lombok.Getter;

@Getter
public enum LogRequestStatus {
    IN_PROCESS("in_process"),
    READY("ready"),
    NOT_REQUESTED("not_requested");

    private final String value;

    LogRequestStatus(String value) {
        this.value = value;
    }

    public static LogRequestStatus fromValue(String value) {
        for (LogRequestStatus field : values()) {
            if (field.value.equalsIgnoreCase(value)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Unknown log request status: " + value);
    }
}
