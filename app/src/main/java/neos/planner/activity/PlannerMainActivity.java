package neos.planner.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import neos.planner.R;
import neos.planner.adapters.DbEventAdapter;
import neos.planner.adapters.DbNoteAdapter;
import neos.planner.adapters.DbRecoveryFileAdapter;
import neos.planner.adapters.SubTotalEventsAdapter;
import neos.planner.async.MarkAllEventsOnCalendar;
import neos.planner.decorator.CalendarDaysDecorator;
import neos.planner.decorator.CalendarOneDayDecorator;
import neos.planner.entity.DbEvent;
import neos.planner.entity.DbNote;
import neos.planner.entity.DbRecoveryFile;
import neos.planner.listeners.DbEventItemClickListener;
import neos.planner.listeners.DbNoteItemClickListener;
import neos.planner.sqlite.ORMLiteOpenHelper;

public class PlannerMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnDateSelectedListener {

    //Блок описания констант
    private static final int ADD_NOTE_ACTIVITY_ACTION = 1000;
    private static final int EDIT_NOTE_ACTIVITY_ACTION = 2000;
    private static final int ADD_EVENT_ACTIVITY_ACTION = 3000;
    private static final int EDIT_EVENT_ACTIVITY_ACTION = 4000;

    //Блок переменных для сбора, хранения и обработки пользовательских данных
    @Bind(R.id.barMainToolbar) Toolbar toolbar;
    @Bind(R.id.mCalendarCardView) CardView calendarCard;
    @Bind(R.id.mMaterialCalendar) MaterialCalendarView materialCalendar;
    @Bind(R.id.mRecyclerView) RecyclerView view;
    @Bind(R.id.fabMain) FloatingActionButton fab;
    @Bind(R.id.drawer_layout) DrawerLayout drawer;
    @Bind(R.id.nav_view) NavigationView navigationView;
    @Bind(R.id.mCreateRecovery) Button btnCreateRecovery;

    //Блок переменных для работы с БД
    private ORMLiteOpenHelper helper;
    private Dao<DbNote, Long> notesDAO;
    private Dao<DbEvent, Long> eventsDAO;
    private Dao<DbRecoveryFile, Long> recoveryDAO;

    //Блок переменных для хранения даных полученных с БД
    private List<DbNote> notes;
    private List<DbEvent> events;
    private List<DbRecoveryFile> recoveries;
    private CalendarDaysDecorator decorator;

    /*Слушатели события OnTouchItem для двух разных списков*/
    private DbNoteItemClickListener noteItemClickListener;
    private DbEventItemClickListener eventItemClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.nav_drawer_events_today);
        setSupportActionBar(toolbar);

        try {
            helper = OpenHelperManager.getHelper(getApplicationContext(), ORMLiteOpenHelper.class);
            notesDAO = helper.getNotesDao();
            eventsDAO = helper.getEventsDao();
            recoveryDAO = helper.getRecoveryDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        LinearLayoutManager llm = new LinearLayoutManager(this);
        view.setLayoutManager(llm);

        createOnTouchListeners();
        getTodayEventsList();
        invisibleRecoveryButton();

        materialCalendar.setCurrentDate(CalendarDay.today());
        materialCalendar.setSelectedDate(CalendarDay.today());
        materialCalendar.setTitleMonths(getBaseContext().getResources().getStringArray(R.array.arrMonths));
        materialCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        materialCalendar.setCalendarDisplayMode(CalendarMode.WEEKS);
        materialCalendar.setOnDateChangedListener(this);

        MarkAllEventsOnCalendar marker = new MarkAllEventsOnCalendar(this, materialCalendar);
        marker.execute();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callChooseOperationDialog();
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    /*Метод обрабатывающий нажатие кнопки BACK*/
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*Метод определяющий основное меню на основании XML разметки*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.planner_main, menu);
        return true;
    }

    /*Метод отвечающий за обработку действий пользователя в основном меню
    * @param item - Параметр передающий выбраный пользователем пункт меню */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_find : {
                callSearchActivity();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /*Метод обработки выбора опции с навигационной панели
     * @param item - Параметр передающий выбраный пользователем пункт меню*/
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_all_events : {
                toolbar.setTitle(R.string.nav_drawer_events_all);
                getSubTotalStat();
                materialCalendar.setCalendarDisplayMode(CalendarMode.MONTHS);
                invisibleRecoveryButton();
                visibleCalendarView();
                YoYo.with(Techniques.SlideInDown).duration(500).playOn(calendarCard);
                break;
            }
            case R.id.nav_today_events : {
                toolbar.setTitle(R.string.nav_drawer_events_today);
                invisibleRecoveryButton();
                visibleCalendarView();
                getTodayEventsList();
                materialCalendar.setCalendarDisplayMode(CalendarMode.WEEKS);
                YoYo.with(Techniques.SlideInDown).duration(500).playOn(calendarCard);
                break;
            }
            case R.id.nav_notes : {
                toolbar.setTitle(R.string.nav_drawer_notes);
                invisibleRecoveryButton();
                invisibleCalendarView();
                getNotesList();
                break;
            }
            case R.id.nav_search : {
                callSearchActivity();
                break;
            }
            case R.id.nav_backup : {
                toolbar.setTitle(R.string.nav_drawer_backup);
                invisibleCalendarView();
                visibleRecoveryButton();
                getRecoveryFilesList();
                YoYo.with(Techniques.SlideInDown).duration(500).playOn(btnCreateRecovery);
                break;
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*Метод обрабатывающий результаты которые вернули другие активити
    * @param requstCode  - Параметр передающий код запроса
    * @param resultCode  - Параметр передающий код ответа
    * @param data        - Параметр передающий событие*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ADD_NOTE_ACTIVITY_ACTION : {
                    getNotesList();
                    break;
                }
                case EDIT_NOTE_ACTIVITY_ACTION : {
                    getNotesList();
                    break;
                }
                case ADD_EVENT_ACTIVITY_ACTION : {
                    getTodayEventsList();
                    Bundle bundle = data.getExtras();
                    materialCalendar.addDecorator(updateEventsOnCalendar(bundle.getString("DATE")));
                    break;
                }
                case EDIT_EVENT_ACTIVITY_ACTION : {
                    getTodayEventsList();
                    break;
                }
            }
        }
    }

    /*Метод обрабатывающий выбор определенного дня в календаре*/
    @Override
    public void onDateSelected(MaterialCalendarView widget, CalendarDay date, boolean selected) {
        materialCalendar.setCalendarDisplayMode(CalendarMode.WEEKS);
        events = getTodayEventsFromDatabase(date.getDate());
        DbEventAdapter adapter = new DbEventAdapter(events);
        deleteAllRecyclerViewOnTouchListeners();
        view.addOnItemTouchListener(eventItemClickListener);
        view.setAdapter(adapter);
        YoYo.with(Techniques.SlideInRight).duration(250).playOn(view);
    }

    /*Метод подготавливающий и вызывающий PlannerAddNoteActivity*/
    private void callAddNoteActivity() {
        Intent intent = new Intent(
                PlannerMainActivity.this, PlannerAddNoteActivity.class);
        startActivityForResult(intent, ADD_NOTE_ACTIVITY_ACTION);
    }

    private void callAddEventActivity() {
        Intent intent = new Intent(
                PlannerMainActivity.this, PlannerAddEventActivity.class);
        startActivityForResult(intent, ADD_EVENT_ACTIVITY_ACTION);
    }

    /*Метод подготавливающий и вызывающий PlannerSearchEventOrNoteActivity*/
    private void callSearchActivity() {
        Intent intent =
                new Intent(PlannerMainActivity.this, PlannerSearchEventOrNoteActivity.class);
        startActivity(intent);
    }

    /*Метод подготавливающий и отображающий диалог выбора при создании
    * нового события либо заметки*/
    private void callChooseOperationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        final CharSequence[] actions = {
                getBaseContext().getString(R.string.fabMain_new_note_dialog),
                getBaseContext().getString(R.string.fabMain_new_event_dialog)
        };
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: {
                        callAddNoteActivity();
                        break;
                    }
                    case 1: {
                        callAddEventActivity();
                        break;
                    }
                }
            }
        });
        builder.show();
    }

    /*Метод для получения всех пользовательских заметок находящихся в БД
     *@return List<DbNote> - Возвращает список всех записей из таблицы PLANNER_NOTES */
    private List<DbNote> getAllNotesFromDataBase() {
        try {
            List<DbNote> list = notesDAO.queryForAll();
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*Метод для получения всех пользовательских событий находящихся в БД
     *@return List<DbEvent> - Возвращает список всех событий из таблицы PLANNER_EVENTS */
    private List<DbEvent> getAllEventsFromDatabase() {
        try {
            List<DbEvent> list = eventsDAO.queryForAll();
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*Метод для получения пользовательских событий запланированных на
    * определенный день из БД
    * @param Date currDate  - Параметр передающий дату для выборки запланированных событий
    * @return List<DbEvent> - Возвращает список запланированных событий на
    *                         дату системного времени*/
    private List<DbEvent> getTodayEventsFromDatabase(Date currDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            QueryBuilder<DbEvent, Long> builder = eventsDAO.queryBuilder();
            builder.where().eq("EVENTS_DATE", format.format(currDate));
            toolbar.setTitle(format.format(currDate));
            List<DbEvent> list = eventsDAO.query(builder.prepare());
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*Метод для получения всех сохраненых резервных копий данных
    * в приложении из БД
    * @return List<DbRecoveryFile> - Взозвращает список всех имеющихся
    *                                резервных копий в приложении*/
    private List<DbRecoveryFile> getRecoveryFilesFromDatabase() {
        try {
            List<DbRecoveryFile> list = recoveryDAO.queryForAll();
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*Метод подготавливающий полный список со всеми пользовательскими
     *заметками для дальнейшего отображения на экране*/
    private void getNotesList() {
        notes = getAllNotesFromDataBase();
        DbNoteAdapter adapter = new DbNoteAdapter(notes);
        deleteAllRecyclerViewOnTouchListeners();
        view.addOnItemTouchListener(noteItemClickListener);
        view.setAdapter(adapter);
        YoYo.with(Techniques.FadeIn).duration(400).playOn(view);
    }

    /*Метод подготавливающий полный список со всеми пользовательскими
     *событиями для дальнейшего отображения на экране*/
    private void getEventsList() {
        events = getAllEventsFromDatabase();
        DbEventAdapter adapter = new DbEventAdapter(events);
        deleteAllRecyclerViewOnTouchListeners();
        view.addOnItemTouchListener(eventItemClickListener);
        view.setAdapter(adapter);
    }

    /*Метод подготавливающий все резервные копии которые есть в
    * приложении для дальнейшего отображения на экране */
    private void getRecoveryFilesList() {
        recoveries = getRecoveryFilesFromDatabase();
        DbRecoveryFileAdapter adapter = new DbRecoveryFileAdapter(recoveries);
        deleteAllRecyclerViewOnTouchListeners();
        /*Добавить слушатель событий*/
        view.setAdapter(adapter);
        YoYo.with(Techniques.SlideInRight).duration(400).playOn(view);
    }

    /*Метод подготавливающий полный список событий запланированных
    * на текущий день для дальнейшеко отображения на экране*/
    private void getTodayEventsList() {
        Calendar calendar = Calendar.getInstance();
        Date currDate = calendar.getTime();
        materialCalendar.setSelectedDate(calendar.getTime());
        events = getTodayEventsFromDatabase(currDate);
        DbEventAdapter adapter = new DbEventAdapter(events);
        deleteAllRecyclerViewOnTouchListeners();
        view.addOnItemTouchListener(eventItemClickListener);
        view.setAdapter(adapter);
        YoYo.with(Techniques.SlideInRight).duration(250).playOn(view);
    }

    /*Метод подготавливающий данные для вывода статистики по событиям*/
    private void getSubTotalStat() {
        try {
            events = eventsDAO.queryForAll();
            Integer allEvents = events.size();
            Integer activeEvents = 0;
            Integer finishEvents = 0;
            for (DbEvent event : events) {
                if (event.getStatus().equals(
                        getBaseContext().getResources().getString(R.string.event_status_active))) {
                    activeEvents++;
                } else {
                    finishEvents++;
                }
            }
            SubTotalEventsAdapter adapter =
                    new SubTotalEventsAdapter(this, allEvents, activeEvents, finishEvents);
            deleteAllRecyclerViewOnTouchListeners();
            view.setAdapter(adapter);
            YoYo.with(Techniques.SlideInUp).duration(500).playOn(view);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*Метод создающий необходимые слушатели событий OnTouch для компонента RecyclerView */
    private void createOnTouchListeners() {
        noteItemClickListener =
                new DbNoteItemClickListener(this, new DbNoteItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(PlannerMainActivity.this, PlannerEditNoteActivity.class);
                intent.putExtra("ID", notes.get(position).getId());
                startActivityForResult(intent, EDIT_NOTE_ACTIVITY_ACTION);
            }
        });
        eventItemClickListener =
                new DbEventItemClickListener(this, new DbEventItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(PlannerMainActivity.this, PlannerEditEventActivity.class);
                intent.putExtra("ID", events.get(position).getId());
                startActivityForResult(intent, EDIT_EVENT_ACTIVITY_ACTION);
            }
        });
    }

    /*Метод удаляющий все подключенные Listeners к RecyclerView*/
    private void deleteAllRecyclerViewOnTouchListeners() {
        view.removeOnItemTouchListener(eventItemClickListener);
        view.removeOnItemTouchListener(noteItemClickListener);
    }

    /*Метод создающий декоатор для отметки в календаре нового события
    * @param String date - Параметр передающий строковое значение даты*/
    private CalendarOneDayDecorator updateEventsOnCalendar(String date) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            Date addDate = format.parse(date);
            CalendarDay day = CalendarDay.from(addDate);
            CalendarOneDayDecorator oneDayDecorator = new CalendarOneDayDecorator(day);
            return oneDayDecorator;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*Метод для создания резервной копии пользовательских данных*/
    @OnClick(R.id.mCreateRecovery)
    public void createRecoveryOnClick() {
        getNotesList();
        getEventsList();
        SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy-HHmm");

        DbRecoveryFile recovery = new DbRecoveryFile(
                "Recovery copy",
                "notes" + format.format(new Date()) + ".bkp",
                "events" + format.format(new Date()) + ".bkp",
                new Date()
        );

        try {
            final FileOutputStream outNotes = openFileOutput(
                    recovery.getNotesFile(), Context.MODE_PRIVATE);
            final FileOutputStream outEvents = openFileOutput(
                    recovery.getEventsFile(), Context.MODE_PRIVATE);

            final Thread notesThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(outNotes);
                        oos.writeObject(notes);
                        oos.flush();
                        oos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            notesThread.start();

            Thread eventsThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(outEvents);
                        oos.writeObject(events);
                        oos.flush();
                        oos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            eventsThread.start();

            recoveryDAO.create(recovery);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        getRecoveryFilesList();
    }

    /*Метод скрывающий MaterialCalendarView*/
    private void invisibleCalendarView() {
        calendarCard.setVisibility(View.GONE);
        materialCalendar.setVisibility(View.GONE);
    }

    /*Метод отображающий MaterialCalendarView*/
    private void visibleCalendarView() {
        calendarCard.setVisibility(View.VISIBLE);
        materialCalendar.setVisibility(View.VISIBLE);
    }

    /*Метод скрывающий отображение Button (Для создания резервной копии)*/
    private void invisibleRecoveryButton() {
        btnCreateRecovery.setVisibility(View.GONE);
    }

    /*Метод скрывающий отображение Button (Для создания резервной копии)*/
    private void visibleRecoveryButton() {
        btnCreateRecovery.setVisibility(View.VISIBLE);
    }
}
