package neos.planner.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import neos.planner.R;
import neos.planner.adapters.DbEventAdapter;
import neos.planner.adapters.DbNoteAdapter;
import neos.planner.decorator.CalendarDayDecorator;
import neos.planner.entity.DbEvent;
import neos.planner.entity.DbNote;
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
    @Bind(R.id.mMaterialCalendar) MaterialCalendarView materialCalendar;
    @Bind(R.id.mRecyclerView) RecyclerView view;
    @Bind(R.id.fabMain) FloatingActionButton fab;
    @Bind(R.id.drawer_layout) DrawerLayout drawer;
    @Bind(R.id.nav_view) NavigationView navigationView;

    //Блок переменных для работы с БД
    private ORMLiteOpenHelper helper;
    private Dao<DbNote, Long> notesDAO;
    private Dao<DbEvent, Long> eventsDAO;

    //Блок переменных для хранения даных полученных с БД
    private List<DbNote> notes;
    private List<DbEvent> events;
    private CalendarDayDecorator decorator;

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
        } catch (SQLException e) {
            e.printStackTrace();
        }

        LinearLayoutManager llm = new LinearLayoutManager(this);
        view.setLayoutManager(llm);

        createOnTouchListeners();
        getTodayEventsList();
        List<CalendarDay> days = getDecorateAllEventsOnCalendar();
        decorator = new CalendarDayDecorator(days);

        materialCalendar.setSelectedDate(CalendarDay.today());
        materialCalendar.setCurrentDate(CalendarDay.today());
        materialCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        materialCalendar.setCalendarDisplayMode(CalendarMode.WEEKS);
        materialCalendar.setOnDateChangedListener(this);
        materialCalendar.addDecorator(decorator);

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
            case R.id.nav_notes : {
                toolbar.setTitle(R.string.nav_drawer_notes);
                getNotesList();
                break;
            }
            case R.id.nav_all_events : {
                toolbar.setTitle(R.string.nav_drawer_events_all);
                getEventsList();
                break;
            }
            case R.id.nav_today_events : {
                getTodayEventsList();
                toolbar.setTitle(R.string.nav_drawer_events_today);
                break;
            }
            case R.id.nav_search : {
                callSearchActivity();
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
                    getEventsList();
                    materialCalendar.removeDecorator(decorator);
                    materialCalendar.addDecorator(updateEventsOnCalendar());
                    break;
                }
                case EDIT_EVENT_ACTIVITY_ACTION : {
                    getEventsList();
                    break;
                }
            }
        }
    }

    /*Метод обрабатывающий выбор определенного дня в календаре*/
    @Override
    public void onDateSelected(MaterialCalendarView widget, CalendarDay date, boolean selected) {
        events = getTodayEventsFromDatabase(date.getDate());
        DbEventAdapter adapter = new DbEventAdapter(events);
        deleteAllRecyclerViewOnTouchListeners();
        view.addOnItemTouchListener(eventItemClickListener);
        view.setAdapter(adapter);
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

    /*Метод подготавливающий полный список со всеми пользовательскими
     *заметками для дальнейшего отображения на экране*/
    private void getNotesList() {
        notes = getAllNotesFromDataBase();
        DbNoteAdapter adapter = new DbNoteAdapter(notes);
        deleteAllRecyclerViewOnTouchListeners();
        view.addOnItemTouchListener(noteItemClickListener);
        view.setAdapter(adapter);
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

    /*Метод подготавливающий события для отметки в календаре*/
    private List<CalendarDay> getDecorateAllEventsOnCalendar() {
        List<CalendarDay> days = new ArrayList<>();
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

        return days;
    }

    private CalendarDayDecorator updateEventsOnCalendar() {
        List<CalendarDay> days = getDecorateAllEventsOnCalendar();
        decorator = new CalendarDayDecorator(days);
        return decorator;
    }
}
