package vn.edu.iuh.models.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReactionType {
    LIKE("Thích"),
    WOW("Wow"),
    CRY("Khóc"),
    ANGER("Tức giận"),
    LOVE("Yêu thích");
    private final String type;

    public static ReactionType fromString(String text) {
        for (ReactionType reactionType : ReactionType.values()) {
            if (reactionType.type.equalsIgnoreCase(text)) {
                return reactionType;
            }
        }
        throw new IllegalArgumentException("No constant with type " + text + " found");
    }
}
