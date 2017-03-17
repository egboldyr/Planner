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

import neos.planner.annotation.About;

/**
 * Created by IEvgen Boldyr on 04.04.16.
 * Project: Planner
 *
 * Класс который отвечает за отметки событий в календаре по нескольким дням
 */

@About(author = "IEvgen_Boldyr", version = "0.1.0")
public class CalendarDaysDecorator implements DayViewDecorator {

    /*Коллекция хранящая даты всех событий*/
    private HashSet<CalendarDay> days;

    /*Конструктор для создания множественного декоратора
    * @param Collection<CalendarDay> days - Параметр передающий количество дней для отметки*/
    public CalendarDaysDecorator(Collection<CalendarDay> days) {
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
}
