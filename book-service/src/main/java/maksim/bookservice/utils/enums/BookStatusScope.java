package maksim.bookservice.utils.enums;

import lombok.Getter;

@Getter
public enum BookStatusScope {
    OVERALL("overall"),
    LAST_YEAR("year"),
    LAST_MONTH("month"),
    LAST_WEEK("week");

    private final String value;

    BookStatusScope(String value) {
        this.value = value;
    }

    public static BookStatusScope fromValue(String value) {
        for (BookStatusScope field : values()) {
            if (field.value.equalsIgnoreCase(value)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Unknown sort dir field: " + value);
    }
}
