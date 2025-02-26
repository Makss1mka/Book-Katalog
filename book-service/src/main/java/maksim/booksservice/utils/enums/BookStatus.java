package maksim.booksservice.utils.enums;

import lombok.Getter;

@Getter
public enum BookStatus {
    DROP("drop"),
    READ("read"),
    READING("reading");

    private final String value;

    BookStatus(String value) {
        this.value = value;
    }

    public static BookStatus fromValue(String value) {
        for (BookStatus field : values()) {
            if (field.value.equalsIgnoreCase(value)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Unknown sort dir field: " + value);
    }
}
