package neos.planner.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import neos.planner.R;

/**
 * Created by IEvgen Boldyr on 30.03.16.
 * Project: Planner
 *
 * Активити в которой реализованы функции поиска по приложению.
 * Поддерживается:
 *  - Поиск по пользовательским заметкам
 *  - Поиск по пользовательским событиям
 */

public class PlannerSearchEventOrNoteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        //Реализовать логику поиска данных по приложению
    }
}
