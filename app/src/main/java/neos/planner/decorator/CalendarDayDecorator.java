package neos.planner.decorator;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.style.BackgroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by IEvgen Boldyr on 04.04.16.
 * Project: Planner
 */

public class CalendarDayDecorator implements DayViewDecorator {

    private HashSet<CalendarDay> days;

    public CalendarDayDecorator(Collection<CalendarDay> days) {
        this.days = new HashSet<>(days);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return days.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new StyleSpan(Typeface.BOLD));
        view.addSpan(new DotSpan(10, Color.parseColor("#2196F3")));
    }

    public void addNewDay(CalendarDay day) {
        if ( !days.contains(day) ) days.add(day);
    }
}
