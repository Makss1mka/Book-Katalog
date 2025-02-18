package maksim.bookservice.utils;

import lombok.Getter;

@Getter
public enum Operator {
    GREATER("greater"),
    EQUAL("equal"),
    LESS("less");

    private final String value;

    Operator(String value) {
        this.value = value;
    }

    public static Operator fromValue(String value) {
        for (Operator field : values()) {
            if (field.value.equalsIgnoreCase(value)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Unknown sort dir field: " + value);
    }
}
