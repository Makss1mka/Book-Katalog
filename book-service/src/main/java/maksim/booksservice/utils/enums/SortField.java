package maksim.booksservice.utils.enums;

import lombok.Getter;

@Getter
public enum SortField {
    RATING("rating"),
    NAME("name"),
    AUTHOR("author"),
    RATINGS_COUNT("ratings count"),
    ISSUED_DATE("issued_date"),

    STATUS_DROP_LAST_WEEK("status_drop_last_week"),
    STATUS_DROP_LAST_MONTH("status_drop_last_month"),
    STATUS_DROP_LAST_YEAR("status_drop_last_year"),
    STATUS_DROP_OVERALL("status_drop_overall"),

    STATUS_READ_LAST_WEEK("status_read_last_week"),
    STATUS_READ_LAST_MONTH("status_read_last_month"),
    STATUS_READ_LAST_YEAR("status_read_last_year"),
    STATUS_READ_OVERALL("status_read_overall"),

    STATUS_READING_LAST_WEEK("status_reading_last_week"),
    STATUS_READING_LAST_MONTH("status_reading_last_month"),
    STATUS_READING_LAST_YEAR("status_reading_last_year"),
    STATUS_READING_OVERALL("status_reading_overall");

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
