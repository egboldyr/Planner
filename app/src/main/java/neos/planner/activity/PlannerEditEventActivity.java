package neos.planner.activity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import butterknife.Bind;
import butterknife.ButterKnife;
import neos.planner.R;
import neos.planner.entity.DbEvent;
import neos.planner.entity.DbNote;
import neos.planner.receiver.EventRemindReceiver;
import neos.planner.sqlite.ORMLiteOpenHelper;

/**
 * Created by IEvgen Boldyr on 23.03.16.
 * Project: Planner
 *
 * Активити для редактирования пользовательского события
 */

public class PlannerEditEventActivity extends AppCompatActivity {

    //Параметры переданные из главной активити
    private Bundle extras;

    /*Переменные для работы с элементами активити*/
    @Bind(R.id.barNoteDetails)  Toolbar toolbar;
    @Bind(R.id.mEventDate) TextView mEventDate;
    @Bind(R.id.mEventTime) TextView mEventTime;
    @Bind(R.id.mRemindMeParam) Spinner mRemindMeParam;
    @Bind(R.id.mEventBody) EditText mEventBody;
    @Bind(R.id.fabAddEvent) FloatingActionButton fab;

    /*Переменные для хранения пользовательских данных*/
    private DbEvent event;
    private int day;
    private int month;
    private int year;
    private int hours;
    private int minutes;

    /*Переменные хранящие объекты для доступа к БД*/
    private ORMLiteOpenHelper helper;
    private Dao<DbEvent, Long> eventDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        ButterKnife.bind(this);
        extras = getIntent().getExtras();

        toolbar.setTitle(R.string.edit_event_header);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        try {
            helper = OpenHelperManager.getHelper(this, ORMLiteOpenHelper.class);
            eventDAO = helper.getEventsDao();
            Long id = extras.getLong("ID");
            event = eventDAO.queryForId(id);
            getDate(event.getDate());
            getTime(event.getTime());
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

        mEventDate.setText(event.getDate());
        mEventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog =
                        new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                monthOfYear++;
                                mEventDate.setText(parseDateFromDatePicker(dayOfMonth, monthOfYear, year));
                            }
                        }, year, month, day);
                dialog.show();
            }
        });

        mEventTime.setText(event.getTime());
        mEventTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mEventTime.setText(parseTimeFromTimePicker(hourOfDay, minute));
                    }
                }, hours, minutes, true);
                dialog.show();
            }
        });

        String[] reminds = {
                getBaseContext().getString(R.string.event_remind_15_min),
                getBaseContext().getString(R.string.event_remind_30_min),
                getBaseContext().getString(R.string.event_remind_1_hour),
                getBaseContext().getString(R.string.event_remind_3_hour),
                getBaseContext().getString(R.string.event_remind_6_hour)
        };
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, reminds);
        mRemindMeParam.setAdapter(adapter);
        setRemindOption(event.getRemind());

        mEventBody.setText(event.getEvent());

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    event.setEvent(mEventBody.getText().toString());
                    event.setDate(mEventDate.getText().toString());
                    event.setTime(mEventTime.getText().toString());
                    event.setRemind(parseRemindOption(mRemindMeParam.getSelectedItem().toString()));
                    eventDAO.update(event);
                    addEventToAlarmManager(event, event.getId());
                    setResult(RESULT_OK, new Intent());
                    finish();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
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

    /*Метод помещающий созданное пользовательское событие в AlarmManager*/
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
            intent.putExtra("BODY", mEventBody.getText().toString());

            String id = Long.toString(eventId);

            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(this, Integer.parseInt(id),
                            intent, PendingIntent.FLAG_CANCEL_CURRENT);

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

    /*Метод возвращающий строковое значение даты на основании параметров DatePicker
    * @param Integer dayOfMonth  - Параметр передающий число
    * @param Integer monthOfYear - Параметр передающий месяц
    * @param Integer year        - Параметр передающий год
    * @return String             - Возвращается строковое представление даты*/
    private String parseDateFromDatePicker(Integer dayOfMonth, Integer monthOfYear, Integer year) {
        if (monthOfYear < 10) {
            if (dayOfMonth < 10) {
                return "0" + dayOfMonth + ".0" + monthOfYear + "." + year;
            } else {
                return dayOfMonth + ".0" + monthOfYear + "." + year;
            }
        } else {
            return dayOfMonth + "." + monthOfYear + "." + year;
        }
    }

    /*Метод возвращающий строковое значение времени на основании параметров TimePicker
    * @param Integer hourOfDay - Параметр передающий количество часов
    * @param Integer minute    - Параметр передающий количество минут
    * @return String           - Возвращает строковое представление времени*/
    private String parseTimeFromTimePicker(Integer hourOfDay, Integer minute) {
        if (hourOfDay < 10) {
            if (minute < 10) {
                return "0" + hourOfDay + ":0" + minute;
            } else {
                return "0" + hourOfDay + ":" + minute;
            }
        } else {
            if (minute < 10) {
                return hourOfDay + ":0" + minute;
            }
            return hourOfDay + ":" + minute;
        }
    }

    /*Временное решение для удобства сохранения времени до события в нужном формате*/
    private String parseRemindOption(String remind) {
        if (remind.equals(getBaseContext().getString(R.string.event_remind_15_min))) {
            return "00:15";
        } else if (remind.equals(getBaseContext().getString(R.string.event_remind_30_min))) {
            return "00:30";
        } else if (remind.equals(getBaseContext().getString(R.string.event_remind_1_hour))) {
            return "01:00";
        } else if (remind.equals(getBaseContext().getString(R.string.event_remind_3_hour))) {
            return "03:00";
        } else {
            return "06:00";
        }
    }

    /*Временное решение для удобства сохранения времени до события в нужном формате*/
    private void setRemindOption(String remindOption) {
        if (remindOption.equals("00:15")) {
            mRemindMeParam.setSelection(0);
        } else if (remindOption.equals("00:30")) {
            mRemindMeParam.setSelection(1);
        } else if (remindOption.equals("01:00")) {
            mRemindMeParam.setSelection(2);
        } else if (remindOption.equals("03:00")) {
            mRemindMeParam.setSelection(3);
        } else {
            mRemindMeParam.setSelection(4);
        }
    }
}
