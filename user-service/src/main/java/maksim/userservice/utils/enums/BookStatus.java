package maksim.userservice.utils.enums;

import lombok.Getter;

@Getter
public enum BookStatus {
    READING("reading"),
    READ("read"),
    DROP("drop"),
    LIKED("liked"),
    ANY("any");

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
        throw new IllegalArgumentException("Unknown book status: " + value);
    }
}
