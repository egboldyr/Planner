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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import neos.planner.R;
import neos.planner.adapters.DbEventAdapter;
import neos.planner.adapters.DbNoteAdapter;
import neos.planner.entity.DbEvent;
import neos.planner.entity.DbNote;
import neos.planner.listeners.DbEventItemClickListener;
import neos.planner.listeners.DbNoteItemClickListener;
import neos.planner.sqlite.ORMLiteOpenHelper;

public class PlannerMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Блок описания констант
    private static final int ADD_NOTE_ACTIVITY_ACTION = 1000;
    private static final int EDIT_NOTE_ACTIVITY_ACTION = 2000;
    private static final int ADD_EVENT_ACTIVITY_ACTION = 3000;
    private static final int EDIT_EVENT_ACTIVITY_ACTION = 4000;

    //Блок переменных для сбора, хранения и обработки пользовательских данных
    private Toolbar toolbar;
    private RecyclerView view;
    private List<DbNote> notes;
    private List<DbEvent> events;

    //Блок переменных для работы с БД
    private ORMLiteOpenHelper helper;
    private Dao<DbNote, Long> notesDAO;
    private Dao<DbEvent, Long> eventsDAO;

    /*Слушатели события OnTouchItem для двух разных списков*/
    private DbNoteItemClickListener noteItemClickListener;
    private DbEventItemClickListener eventItemClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.barMainToolbar);
        setSupportActionBar(toolbar);

        try {
            helper = OpenHelperManager.getHelper(getApplicationContext(), ORMLiteOpenHelper.class);
            notesDAO = helper.getNotesDao();
            eventsDAO = helper.getEventsDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        view = (RecyclerView) findViewById(R.id.mRecyclerView);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        view.setLayoutManager(llm);

        createOnTouchListeners();
        getEventsList();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabMain);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                final CharSequence[] actions = {
                    getBaseContext().getString(R.string.fabMain_new_note_dialog),
                    getBaseContext().getString(R.string.fabMain_new_event_dialog)
                    /*getBaseContext().getString(R.string.fabMain_new_contact_dialog),*/
                };
                builder.setItems(actions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: {
                                Intent intent = new Intent(
                                        PlannerMainActivity.this, PlannerAddNoteActivity.class);
                                startActivityForResult(intent, ADD_NOTE_ACTIVITY_ACTION);
                                break;
                            }
                            case 1: {
                                Intent intent = new Intent(
                                        PlannerMainActivity.this, PlannerAddEventActivity.class);
                                startActivityForResult(intent, ADD_EVENT_ACTIVITY_ACTION);
                                break;
                            }
                            case 2: {
                                //Место для вызова активности создания нового контакта
                                break;
                            }
                        }
                    }
                });
                builder.show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
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
            case R.id.action_settings : {
                //В будущем добавить настройки сюда
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
                toolbar.setTitle(R.string.nav_drawer_events_today);
                //События за текущий день
                break;
            }
            case R.id.nav_important_events : {
                toolbar.setTitle(R.string.nav_drawer_events_important);
                //Важные события
                break;
            }
            /*case R.id.nav_contacts : {
                toolbar.setTitle(R.string.nav_drawer_contacts);
                //Контакты
                break;
            }*/
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*Метод обрабатывающий результаты которые вернули другие активити
    * @param requstCode  - Параметр передающий код запроса
    * @param resultCode* - Параметр передающий код ответа
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
                    break;
                }
                case EDIT_EVENT_ACTIVITY_ACTION : {
                    getEventsList();
                    break;
                }
            }
        }
    }

    /*Метод подготавливающий и вызывающий SearchEventOrNoteActivity*/
    private void callSearchActivity() {
        Intent intent =
                new Intent(PlannerMainActivity.this, PlannerSearchEventOrNoteActivity.class);
        startActivity(intent);
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
}
