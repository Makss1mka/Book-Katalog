package maksim.reviewsservice.utils.enums;

import lombok.Getter;

@Getter
public enum JoinMode {
    WITH("with"),
    WITHOUT("without");

    private final String value;

    JoinMode(String value) {
        this.value = value;
    }

    public static JoinMode fromValue(String value) {
        for (JoinMode field : values()) {
            if (field.value.equalsIgnoreCase(value)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Unknown link mode: " + value);
    }

}
