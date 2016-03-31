package neos.planner.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.List;

import neos.planner.R;
import neos.planner.entity.DbNote;

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
    private List<DbNote> results;

    /*Элементы активити с поисковым запросом и кнопкой поиск*/
    private EditText mSearchRequest;
    private ImageButton btnStartSearch;
    private RecyclerView mSearchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchRequest = (EditText) findViewById(R.id.mSearchRequest);
        btnStartSearch = (ImageButton) findViewById(R.id.bthStartSearch);
        btnStartSearch.setOnClickListener(this);
        mSearchResults = (RecyclerView) findViewById(R.id.mSearchResults);
    }

    /*Метод обрабатывающий запуск поиска*/
    @Override
    public void onClick(View v) {
        //Реализовать логику поиска
    }
}
