package maksim.booksservice.utils.enums;

import lombok.Getter;

@Getter
public enum BookStatusScope {
    OVERALL("Overall"),
    LAST_YEAR("LastYear"),
    LAST_MONTH("LastMonth"),
    LAST_WEEK("LastWeek");

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
        throw new IllegalArgumentException("Unknown book status scope: " + value);
    }
}
