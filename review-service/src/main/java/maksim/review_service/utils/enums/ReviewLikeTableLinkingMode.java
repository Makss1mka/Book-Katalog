package maksim.review_service.utils.enums;

import lombok.Getter;

@Getter
public enum ReviewLikeTableLinkingMode {
    WITH_LINKING("with"),
    WITHOUT_LINKING("without");

    private final String value;

    ReviewLikeTableLinkingMode(String value) {
        this.value = value;
    }

    public static ReviewLikeTableLinkingMode fromValue(String value) {
        for (ReviewLikeTableLinkingMode field : values()) {
            if (field.value.equalsIgnoreCase(value)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Unknown sort dir field: " + value);
    }

}
