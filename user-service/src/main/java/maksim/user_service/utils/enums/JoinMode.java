package maksim.user_service.utils.enums;

import lombok.Getter;

@Getter
public enum JoinMode {
    WITHOUT("without"),
    WITH_STATUSES("with_statuses"),
    WITH_STATUSES_AND_BOOKS("with_statuses_and_books");

    private final String value;

    JoinMode(String value) {
        this.value = value;
    }

    public static JoinMode fromValue(String value) {
        for (JoinMode field : values()) {
            if (field.value.equalsIgnoreCase(value)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Unknown join mode: " + value);
    }
}
