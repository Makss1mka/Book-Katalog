package maksim.booksservice.utils.enums;

import lombok.Getter;

@Getter
public enum NumberOperator {
    GREATER("greater"),
    EQUAL("equal"),
    LESS("less");

    private final String value;

    NumberOperator(String value) {
        this.value = value;
    }

    public static NumberOperator fromValue(String value) {
        for (NumberOperator field : values()) {
            if (field.value.equalsIgnoreCase(value)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Unknown number operator: " + value);
    }
}
