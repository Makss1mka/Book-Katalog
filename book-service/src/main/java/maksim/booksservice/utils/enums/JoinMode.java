package maksim.booksservice.utils.enums;

import lombok.Getter;

@Getter
public enum JoinMode {
    WITHOUT_JOIN("without"),
    WITH_JOIN("with");

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
