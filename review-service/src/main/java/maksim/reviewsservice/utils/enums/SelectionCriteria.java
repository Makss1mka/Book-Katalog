package maksim.reviewsservice.utils.enums;

import lombok.Getter;

@Getter
public enum SelectionCriteria {
    USER("userId"),
    BOOK("bookId");

    private final String value;

    SelectionCriteria(String value) {
        this.value = value;
    }

    public static SelectionCriteria fromValue(String value) {
        for (SelectionCriteria field : values()) {
            if (field.value.equalsIgnoreCase(value)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Unknown selection criteria field: " + value);
    }
}
