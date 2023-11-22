package dataRepresentation;

import dataModel.AbstractClassroom;
import dataModel.AbstractTerm;

import java.time.LocalDate;
import java.time.LocalTime;

public class WeeklyTerm extends AbstractTerm {

    private DaysOfWeek dayOfWeek;
    private LocalDate activeFrom;
    private LocalDate activeUntil;
    private LocalTime startTime;
    private LocalTime endTime;

    public WeeklyTerm(AbstractClassroom classroom) {
        super(classroom);
        activeFrom = null;
        activeUntil = null;
    }

    public DaysOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DaysOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalDate getActiveFrom() {
        return activeFrom;
    }

    public void setActiveFrom(LocalDate activeFrom) {
        this.activeFrom = activeFrom;
    }

    public LocalDate getActiveUntil() {
        return activeUntil;
    }

    public void setActiveUntil(LocalDate activeUntil) {
        this.activeUntil = activeUntil;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();

        getTermAttributes().forEach((s, termAttribute) -> {
            stringBuilder.append(termAttribute + " ");
        });

        return getClassroom().getName() + " " + getStartTime() + "-" + getEndTime() + " from:" + getActiveFrom() + " until:" + getActiveUntil() + "\n" + stringBuilder + "\n";
    }
}
