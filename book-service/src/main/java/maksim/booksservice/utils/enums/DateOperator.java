package maksim.booksservice.utils.enums;

import lombok.Getter;

@Getter
public enum DateOperator {
    OLDER("older"),
    NEWER("newer");

    private final String value;

    DateOperator(String value) {
        this.value = value;
    }

    public static DateOperator fromValue(String value) {
        for (DateOperator field : values()) {
            if (field.value.equalsIgnoreCase(value)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Unknown date operator: " + value);
    }
}
