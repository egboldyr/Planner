package neos.planner.listeners;

import android.content.Intent;
import android.provider.CalendarContract;
import android.view.View;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import neos.planner.annotation.About;
import neos.planner.entity.DbEvent;

/**
 * Created by IEvgen Boldyr on 29.03.16.
 * Project: Planner
 *
 * Слушатель событий для ShareEventButton
 */

@About(author = "IEvgen_Boldyr", version = "0.1.0")
public class EventShareButtonClickListener implements View.OnClickListener {

    private DbEvent event;
    private Calendar calendar;

    private Integer day;
    private Integer month;
    private Integer year;
    private Integer hours;
    private Integer minutes;

    private Long beginMills;
    private Long endMills;

    /*Конструктор для создания Listener'а
    * @param event - параметр передающийсобытие для обработки*/
    public EventShareButtonClickListener(DbEvent event) {
        this.event = event;
    }

    @Override
    public void onClick(View v) {
        getDate(event.getDate());
        getTime(event.getTime());

        calendar = Calendar.getInstance();
        calendar.set(year, month, day, hours, minutes);

        endMills = calendar.getTimeInMillis();
        beginMills = parseBeginMills(event.getRemind());

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, endMills)
                /*.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMills)*/
                .putExtra(CalendarContract.Events.TITLE, event.getEvent());
        v.getContext().startActivity(intent);
    }

    /*Метод разбивающий строковую дату по отдельным целочисленным значениям
    * @param date - Параметр перадающий дату*/
    private void getDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            Date currDate = sdf.parse(date);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currDate);

            day = calendar.get(Calendar.DAY_OF_MONTH);
            month = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /*Метод разбивающий строковую дату по отдельным целочисленным значениям
    * @param time - Параметр перадающий время*/
    private void getTime(String time) {
        Scanner scanner = new Scanner(time);
        scanner.useDelimiter("\\:");
        hours = scanner.nextInt();
        minutes = scanner.nextInt();
    }

    /*Метод обрабатывающий строковое значение напомнить
    * @param String remind - Параметр передающий строковое значение "Напомнить за"
    * @return Long         - Возвращает значение в милисекундах равное времени старта напоминания*/
    private Long parseBeginMills(String remind) {
        switch (remind) {
            case "00:15" : {
               return endMills - 1000 * 60 * 15;
            }
            case "00:30" : {
                return endMills - 1000 * 60 * 30;
            }
            case "01:00" : {
                return endMills - 1000 * 60 * 60;
            }
            case "03:00" : {
                return endMills - 1000 * 60 * 180;
            }
            case "06:00" : {
                return endMills - 1000 * 60 * 360;
            }
        }
        return null;
    }
}
