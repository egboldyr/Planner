package neos.planner.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import neos.planner.R;
import neos.planner.adapters.DbEventAdapter;
import neos.planner.adapters.DbNoteAdapter;
import neos.planner.entity.DbEvent;
import neos.planner.entity.DbNote;
import neos.planner.sqlite.ORMLiteOpenHelper;

/**
 * Created by IEvgen Boldyr on 30.03.16.
 * Project: Planner
 *
 * Активити в которой реализованы функции поиска по приложению.
 * Поддерживается:
 *  - Поиск по пользовательским заметкам
 *  - Поиск по пользовательским событиям
 */

public class PlannerSearchEventOrNoteActivity
        extends AppCompatActivity implements View.OnClickListener {

    /*Данные в которых происходит поиск и данные по результатам*/
    private List<DbNote> notes;
    private List<DbEvent> events;
    private List<DbNote> resultsNotes;
    private List<DbEvent> resultsEvents;

    /*Переменные для достуба к БД*/
    private ORMLiteOpenHelper helper;
    private Dao<DbNote, Long> notesDAO;
    private Dao<DbEvent, Long> eventsDAO;

    /*Элементы активити с поисковым запросом и кнопкой поиск*/
    @Bind(R.id.barSearch) Toolbar toolbar;
    @Bind(R.id.mSearchRequest) EditText mSearchRequest;
    @Bind(R.id.bthStartSearch) ImageButton btnStartSearch;
    @Bind(R.id.mSearchResults) RecyclerView mSearchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        toolbar.setTitle(R.string.search_header);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnStartSearch.setOnClickListener(this);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        mSearchResults.setLayoutManager(llm);
    }

    /*Метод обрабатывающий запуск поиска*/
    @Override
    public void onClick(View v) {
        try {
            helper = OpenHelperManager.getHelper(this, ORMLiteOpenHelper.class);
            notesDAO = helper.getNotesDao();
            eventsDAO = helper.getEventsDao();

            notes = notesDAO.queryForAll();
            events = eventsDAO.queryForAll();

            resultsNotes = new ArrayList<>();
            resultsEvents = new ArrayList<>();

            callSelectSearchTypeDialog(v.getContext());
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    /*Диалог выбора места поиска информации (Заметки или События)*/
    private void callSelectSearchTypeDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        CharSequence[] actions = {
                context.getString(R.string.search_on_notes),
                context.getString(R.string.search_on_events)
        };
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0 : {
                        ThreadGroup searching = new ThreadGroup("Searching");
                        callSearchOnNotes(searching);
                        searchProgress(searching);
                        showNoteResults();
                        break;
                    }
                    case 1 : {
                        ThreadGroup searching = new ThreadGroup("Searching");
                        callSearchOnEvents(searching);
                        searchProgress(searching);
                        showEventsResults();
                        break;
                    }
                }
            }
        });
        builder.show();
    }

    /*Метод выполняющий поиск по заметкам*/
    private void callSearchOnNotes(ThreadGroup searching) {
        final String[] criterias = mSearchRequest.getText().toString().split(" ");

        for (final DbNote note : notes) {
            Thread thread = new Thread(searching, new Runnable() {
                @Override
                public void run() {
                    String[] title = note.getTitle().split(" ");
                    String[] text = note.getNoteText().split(" ");
                    for (String criteria : criterias) {
                        for (String word : title) {
                            if (word.equalsIgnoreCase(criteria)) {
                                addNoteToResult(note);
                                break;
                            }
                        }

                        for (String word : text) {
                            if (word.equalsIgnoreCase(criteria)) {
                                addNoteToResult(note);
                                break;
                            }
                        }
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    /*Метод выполняющий поиск по событиям*/
    private void callSearchOnEvents(ThreadGroup searching) {
        final String[] criterias = mSearchRequest.getText().toString().split(" ");

        for (final DbEvent event : events) {
            Thread thread = new Thread(searching, new Runnable() {
                @Override
                public void run() {
                    String[] text = event.getEvent().split(" ");
                    for (String criteria : criterias) {
                        for (String word : text) {
                            if (word.equalsIgnoreCase(criteria)) {
                                addEventToResult(event);
                                break;
                            }
                        }
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    /*Метод добавляющий найденую заметку к результатам*/
    private synchronized void addNoteToResult(DbNote note) {
        resultsNotes.add(note);
    }

    /*Метод добавляющий найденое событие к результатам*/
    private synchronized void addEventToResult(DbEvent event) {
        resultsEvents.add(event);
    }

    /*Метод отображающтий прогресс в процессе поиска*/
    private void searchProgress(ThreadGroup group) {
        ProgressDialog progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage(getBaseContext().getString(R.string.searching_progress));
        while (group.activeCount() > 0) {
            progress.show();
        }
        progress.cancel();
    }

    /*Метод отображающий все найденные заметки*/
    private void showNoteResults() {
        if (resultsNotes.size() != 0) {
            DbNoteAdapter adapter = new DbNoteAdapter(resultsNotes);
            mSearchResults.setAdapter(adapter);
            YoYo.with(Techniques.FadeIn).duration(500).playOn(mSearchResults);
        }
    }

    /*Метод отображающий все найденные события*/
    private void showEventsResults() {
        if (resultsEvents.size() != 0) {
            DbEventAdapter adapter = new DbEventAdapter(resultsEvents);
            mSearchResults.setAdapter(adapter);
            YoYo.with(Techniques.FadeIn).duration(500).playOn(mSearchResults);
        }
    }
}
