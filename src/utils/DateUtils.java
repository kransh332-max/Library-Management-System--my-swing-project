package utils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.sql.Date;

public class DateUtils {

    // returns SQL Date for today
    public static Date today() {
        return Date.valueOf(LocalDate.now());
    }

    // returns SQL Date for today + days
    public static Date plusDays(int days) {
        return Date.valueOf(LocalDate.now().plusDays(days));
    }

    // calculate fine: ₹10 per day late (customize)
    public static double calculateFine(Date dueDate, Date returnDate) {
        if (returnDate == null) return 0.0;
        LocalDate d1 = dueDate.toLocalDate();
        LocalDate d2 = returnDate.toLocalDate();
        long daysLate = java.time.temporal.ChronoUnit.DAYS.between(d1, d2);
        if (daysLate > 0) {
            return daysLate * 10.0; // ₹10 per day
        } else {
            return 0.0;
        }
    }

    // check overdue (today > dueDate)
    public static boolean isOverdue(Date dueDate) {
        return LocalDate.now().isAfter(dueDate.toLocalDate());
    }
}
