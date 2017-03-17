package neos.planner.async;

import android.content.Context;
import android.os.AsyncTask;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import neos.planner.annotation.About;
import neos.planner.decorator.CalendarDaysDecorator;
import neos.planner.entity.DbEvent;
import neos.planner.sqlite.ORMLiteOpenHelper;

/**
 * Created by IEvgen Boldyr on 05.04.16.
 * Project: Planner
 *
 * Класс для отметки всех событий в календаре в отдельном потоке
 */

@About(author = "IEvgen_Boldyr", version = "0.1.0")
public class MarkAllEventsOnCalendar extends AsyncTask<Void, Void, Void> {

    /*Переменные и компонеты необходимые для работы*/
    private MaterialCalendarView calendar;
    private ORMLiteOpenHelper helper;
    private Dao<DbEvent, Long> eventsDAO;
    private List<CalendarDay> days;

    /*Конструктор для создания класса
    * @param Context context      - Параметр передающий контекст приложения
    * @param MaterialCalendarView - Параметр передающий ссылку на виджет календаря*/
    public MarkAllEventsOnCalendar(Context context, MaterialCalendarView calendar) {
        try {
            this.calendar = calendar;
            helper = OpenHelperManager.getHelper(context, ORMLiteOpenHelper.class);
            eventsDAO = helper.getEventsDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        days = new ArrayList<>();
        try {
            List<DbEvent> events = eventsDAO.queryForAll();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            for (DbEvent event : events) {
                try {
                    days.add(CalendarDay.from(sdf.parse(event.getDate())));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        CalendarDaysDecorator decorator = new CalendarDaysDecorator(days);
        calendar.addDecorator(decorator);
    }
}
