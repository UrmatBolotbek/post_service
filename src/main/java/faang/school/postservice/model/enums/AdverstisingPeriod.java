package faang.school.postservice.model.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AdverstisingPeriod {
    DEY(1, 10),
    WEEK(7, 25),
    MONTH(30, 80);

    private final int days;
    private final int price;

    public static AdverstisingPeriod fromDays(int days) {
        for (AdverstisingPeriod period : AdverstisingPeriod.values()) {
            if (period.getDays() == days) {
                return period;
            }
        }
        throw new IllegalArgumentException("Invalid advertisting period: " + days + "days");
    }
}
