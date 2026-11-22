package com.grupo8.turnos_app.modules.day_schedule;

import java.time.LocalTime;

import com.grupo8.turnos_app.modules.day_schedule.entity.DaySchedule;

public final class DaySchedules {

    private DaySchedules() {
    }

    // true when the range is inside the opening hours of the day, or when the day has no hours set
    public static boolean fitsOpeningHours(DaySchedule daySchedule, LocalTime start, LocalTime end) {
        if (daySchedule.getDayStart() == null || daySchedule.getDayEnd() == null)
            return true;
        return !start.isBefore(daySchedule.getDayStart()) && !end.isAfter(daySchedule.getDayEnd());
    }
}
