package maksim.review_service.utils.enums;

import lombok.Getter;

@Getter
public enum SortDirection {
    ASC("asc"),
    DESC("desc");

    private final String value;

    SortDirection(String value) {
        this.value = value;
    }

    public static SortDirection fromValue(String value) {
        for (SortDirection field : values()) {
            if (field.value.equalsIgnoreCase(value)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Unknown sort dir field: " + value);
    }
}
