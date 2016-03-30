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

import neos.planner.R;
import neos.planner.entity.DbEvent;
import neos.planner.receiver.EventRemindReceiver;
import neos.planner.sqlite.ORMLiteOpenHelper;

/**
 * Created by IEvgen Boldyr on 21.03.16.
 * Project: Planner
 *
 * Активити для создания нового события
 */

public class PlannerAddEventActivity extends AppCompatActivity {

    /*Переменные храняшие данные текущей даты*/
    private Calendar calendar;

    /*Переменные для работы с элементами активити*/
    private TextView mEventDate;
    private TextView mEventTime;
    private Spinner mRemindMeParam;
    private EditText mEventBody;

    /*Переменные хранящие объекты для доступа к БД*/
    private ORMLiteOpenHelper helper;
    private Dao<DbEvent, Long> eventDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.barNoteDetails);
        toolbar.setTitle(R.string.add_event_header);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        calendar = Calendar.getInstance();
        try {
            helper = OpenHelperManager.getHelper(this, ORMLiteOpenHelper.class);
            eventDAO = helper.getEventsDao();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

        mEventDate = (TextView) findViewById(R.id.mEventDate);
        mEventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog dialog =
                        new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        monthOfYear++;
                        if (monthOfYear < 10) {
                            mEventDate.setText(dayOfMonth + ".0" + monthOfYear + "." + year);
                        } else {
                            mEventDate.setText(dayOfMonth + "." + monthOfYear + "." + year);
                        };
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        mEventTime = (TextView) findViewById(R.id.mEventTime);
        mEventTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (minute < 10) {
                            mEventTime.setText(hourOfDay + ":0" + minute);
                        } else {
                            mEventTime.setText(hourOfDay + ":" + minute);
                        }
                    }
                }, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), true);
                dialog.show();
            }
        });

        mRemindMeParam = (Spinner) findViewById(R.id.mRemindMeParam);
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

        mEventBody = (EditText) findViewById(R.id.mEventBody);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAddEvent);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    DbEvent event = new DbEvent(mEventBody.getText().toString(),
                            mEventDate.getText().toString(), mEventTime.getText().toString(),
                            getBaseContext().getString(R.string.event_status_active),mRemindMeParam.getSelectedItem().toString());
                    event.setRemind(parseRemindOption(mRemindMeParam.getSelectedItem().toString()));
                    eventDAO.create(event);
                    addEventToAlarmManager(event, eventDAO.extractId(event));
                    setResult(RESULT_OK, new Intent());
                    finish();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
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


}
