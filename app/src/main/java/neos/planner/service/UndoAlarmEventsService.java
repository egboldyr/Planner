package neos.planner.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import neos.planner.entity.DbEvent;
import neos.planner.receiver.EventRemindReceiver;
import neos.planner.sqlite.ORMLiteOpenHelper;

/**
 * Created by IEvgen Boldyr on 30.03.16.
 * Project: Planner
 *
 * Сервис для востановления событий в памяти устройства,
 * в случае если устройство было перезагружено
 */

public class UndoAlarmEventsService extends Service {

    private ORMLiteOpenHelper helper;
    private Dao<DbEvent, Long> eventsDAO;
    private Calendar calendar;

    private List<DbEvent> events;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            helper = OpenHelperManager.getHelper(this, ORMLiteOpenHelper.class);
            eventsDAO = helper.getEventsDao();
            events = eventsDAO.queryForAll();
            calendar = Calendar.getInstance();
            addEventsToAlarmManager();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*Метод проверяющий актуальность всех событий и добавляющий
    * прошедшие проверку события в AlarmManager*/
    private void addEventsToAlarmManager() {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        for (DbEvent event : events) {
            try {
                if (checkEventDay(format.parse(event.getDate()))) {
                    addEventToAlarmManager(event, event.getId());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    /*Метод проверяющий больше ли дата указанная в событии чем текущая дата
    * @param Date date - Параметр передающий дату запланированного события*/
    private boolean checkEventDay(Date date) {
        if (date.getTime() > calendar.getTimeInMillis()) {
            return true;
        } else {
            return false;
        }
    }

    /*Метод помещающий созданное пользовательское событие в AlarmManager
   * @param DbEvent event - Параметр передающий событие для обработки
   * @param Long eventId  - Параметр передающий идентефикатор события*/
    private void addEventToAlarmManager(DbEvent event, Long eventId) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        Calendar calendar = Calendar.getInstance();
        try {
            Date currDate = sdf.parse(event.getDate());
            calendar.setTime(currDate);

            Integer day = calendar.get(Calendar.DAY_OF_MONTH);
            Integer month = calendar.get(Calendar.MONTH);
            Integer year = calendar.get(Calendar.YEAR);

            Scanner scanner = new Scanner(event.getTime());
            scanner.useDelimiter("\\:");

            Integer hours = scanner.nextInt();
            Integer minutes = scanner.nextInt();

            calendar.set(year, month, day, hours, minutes);

            Intent intent = new Intent(this, EventRemindReceiver.class);
            intent.putExtra("BODY", event.getEvent());

            String id = Long.toString(eventId);

            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(this, Integer.parseInt(id),
                            intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            switch (event.getRemind()) {
                case "00:15" : {
                    Long remind = calendar.getTimeInMillis();
                    remind -= 1000 * 60 * 15;
                    manager.set(AlarmManager.RTC_WAKEUP, remind, pendingIntent);
                    break;
                }
                case "00:30" : {
                    Long remind = calendar.getTimeInMillis();
                    remind -= 1000 * 60 * 30;
                    manager.set(AlarmManager.RTC_WAKEUP, remind, pendingIntent);
                    break;
                }
                case "01:00" : {
                    Long remind = calendar.getTimeInMillis();
                    remind -= 1000 * 60 * 60;
                    manager.set(AlarmManager.RTC_WAKEUP, remind, pendingIntent);
                    break;
                }
                case "03:00" : {
                    Long remind = calendar.getTimeInMillis();
                    remind -= 1000 * 60 * 180;
                    manager.set(AlarmManager.RTC_WAKEUP, remind, pendingIntent);
                    break;
                }
                case "06:00" : {
                    Long remind = calendar.getTimeInMillis();
                    remind -= 1000 * 60 * 360;
                    manager.set(AlarmManager.RTC_WAKEUP, remind, pendingIntent);
                    break;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
