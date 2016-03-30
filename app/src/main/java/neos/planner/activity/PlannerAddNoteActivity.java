package neos.planner.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import neos.planner.R;
import neos.planner.entity.DbNote;
import neos.planner.sqlite.ORMLiteOpenHelper;

/**
 * Created by IEvgen Boldyr on 18.03.16.
 * Project: Planner
 *
 * Активити для создания новой заметки в приложении
 */

public class PlannerAddNoteActivity extends AppCompatActivity {

    //Блок переменных для работы с БД
    private ORMLiteOpenHelper helper;
    private Dao<DbNote, Long> notesDAO;

    //Блок пременных для работы с пользовательскими данными
    private DbNote note = new DbNote();;
    private Spinner group;
    private EditText title;
    private EditText text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        try {
            helper = OpenHelperManager.getHelper(this, ORMLiteOpenHelper.class);
            notesDAO = helper.getNotesDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.barNoteDetails);
        toolbar.setTitle(R.string.add_note_header);
        setSupportActionBar(toolbar);

        group = (Spinner) findViewById(R.id.mSingleNoteGroup);
        String[] groups = {
            getBaseContext().getString(R.string.note_group_general),
            getBaseContext().getString(R.string.note_group_favorites)
        };
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, groups);
        group.setAdapter(adapter);

        title = (EditText) findViewById(R.id.mSingleNoteTitle);
        text = (EditText) findViewById(R.id.mSingleNoteText);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabEditNote);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    note.setTitle(title.getText().toString());
                    note.setNoteText(text.getText().toString());
                    note.setGroup(group.getSelectedItem().toString());
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    note.setCreationDate(sdf.format(new Date()));
                    note.setUpdateDate(sdf.format(new Date()));
                    notesDAO.create(note);
                    setResult(RESULT_OK, new Intent());
                    finish();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (title.getText().toString().equals("")
                && text.getText().toString().equals("")) {
            setResult(RESULT_CANCELED, new Intent());
            finish();
        } else {
            callSavingDialog();
        }
    }

    /*Метод в котором происходит вызов Диалога сохранения*/
    private void callSavingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setMessage(R.string.save_changes_dialog_message);
        builder.setPositiveButton(
                R.string.save_changes_dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            note.setTitle(title.getText().toString());
                            note.setNoteText(text.getText().toString());
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            note.setCreationDate(sdf.format(new Date()));
                            note.setUpdateDate(sdf.format(new Date()));
                            note.setGroup(group.getSelectedItem().toString());
                            notesDAO.create(note);
                            setResult(RESULT_OK, new Intent());
                            finish();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
        builder.setNegativeButton(
                R.string.save_changes_dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED, new Intent());
                        finish();
                    }
                });
        builder.setCancelable(false);
        builder.show();
    }
}
