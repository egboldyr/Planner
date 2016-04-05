package neos.planner.decorator;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.style.StyleSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

/**
 * Created by IEvgen Boldyr on 05.04.16.
 * Project: Planner
 *
 * Класс который отвечает за отметку о событии по одному дню
 */

public class CalendarOneDayDecorator implements DayViewDecorator {

    private CalendarDay day;

    /*Конструктор для создания однодневного декоратора
    * @param CalendarDay day - Параметр передающий дату для отметки*/
    public CalendarOneDayDecorator(CalendarDay day) {
        this.day = day;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return this.day != null && day.equals(this.day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new StyleSpan(Typeface.BOLD));
        view.addSpan(new DotSpan(10, Color.parseColor("#2196F3")));
    }
}
