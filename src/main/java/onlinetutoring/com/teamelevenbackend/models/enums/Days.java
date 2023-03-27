package onlinetutoring.com.teamelevenbackend.models.enums;

import java.util.Locale;

public enum Days {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    @Override
    public String toString() {
        return this.name().toLowerCase(Locale.ENGLISH);
    }
}
