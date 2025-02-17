package maksim.review_service.utils;

public enum ReviewLikeTableLinkingMode {
    WITH_LINKING,
    WITHOUT_LINKING;

    public static ReviewLikeTableLinkingMode getFromModeFromInt(int intMode) {
        return switch(intMode) {
            case 0 -> ReviewLikeTableLinkingMode.WITH_LINKING;
            case 1 -> ReviewLikeTableLinkingMode.WITHOUT_LINKING;
            default -> throw new IllegalStateException("Unexpected value: " + intMode);
        };
    }

}
