package maksim.reviewsservice.utils.enums;

import lombok.Getter;

@Getter
public enum SortField {
    RATING("rating"),
    LIKES("likes");

    private final String value;

    SortField(String value) {
        this.value = value;
    }

    public static SortField fromValue(String value) {
        for (SortField field : values()) {
            if (field.value.equalsIgnoreCase(value)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Unknown sort field: " + value);
    }
}
