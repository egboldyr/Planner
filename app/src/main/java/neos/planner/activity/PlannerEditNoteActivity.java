package neos.planner.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
 * Created by IEvgen Boldyr on 17.03.16.
 * Project: Planner
 *
 * Активити для простмотра и редактирования выбранной
 * пользователем заметки из уже сохраненных в БД вариантов
 */

public class PlannerEditNoteActivity extends AppCompatActivity {

    //Блок значений по умолчанию
    private static final int MNU_DELETE_NOTE_OPTION = 1000;

    //Параметры переданные из главной активити
    private Bundle extras;

    //Блок переменных для работы с БД
    private ORMLiteOpenHelper helper;
    private Dao<DbNote, Long> notesDAO;

    //Блок пременных для работы с пользовательскими данными
    private DbNote note;
    private EditText title;
    private EditText text;
    private Spinner group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);
        extras = getIntent().getExtras();

        try {
            helper = OpenHelperManager.getHelper(this, ORMLiteOpenHelper.class);
            notesDAO = helper.getNotesDao();

            Long id = extras.getLong("ID");
            note = notesDAO.queryForId(id);

            group = (Spinner) findViewById(R.id.mSingleNoteGroup);
            String[] groups = {
                    getBaseContext().getString(R.string.note_group_general),
                    getBaseContext().getString(R.string.note_group_favorites)
            };
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, groups);
            group.setAdapter(adapter);
            spinnerDefaultItem(group, note);

            title = (EditText) findViewById(R.id.mSingleNoteTitle);
            title.setText(note.getTitle());
            text = (EditText) findViewById(R.id.mSingleNoteText);
            text.setText(note.getNoteText());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.barNoteDetails);
        toolbar.setTitle(R.string.edit_note_header);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabEditNote);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    note.setTitle(title.getText().toString());
                    note.setNoteText(text.getText().toString());
                    note.setGroup(group.getSelectedItem().toString());
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    note.setUpdateDate(sdf.format(new Date()));
                    notesDAO.update(note);
                    setResult(RESULT_OK, new Intent());
                    finish();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MNU_DELETE_NOTE_OPTION, 1, "Delete")
                .setIcon(R.mipmap.ic_action_delete)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MNU_DELETE_NOTE_OPTION : {
                try {
                    notesDAO.delete(note);
                    setResult(RESULT_OK);
                    finish();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (note.getTitle().equals(title.getText().toString())
                && note.getNoteText().equals(text.getText().toString())
                && note.getGroup().equals(group.getSelectedItem().toString())) {
            setResult(RESULT_CANCELED, new Intent());
            finish();
        } else {
            callSavingDialog();
        }
    }

    /*Временное решение для выбора уже сохранненой группы по умолчанию */
    private void spinnerDefaultItem(Spinner group, DbNote note) {
        if (note.getGroup().equals(getBaseContext().getString(R.string.note_group_general))) {
            group.setSelection(0);
        } else {
            group.setSelection(1);
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
                            note.setGroup(group.getSelectedItem().toString());
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            note.setUpdateDate(sdf.format(new Date()));
                            notesDAO.update(note);
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
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
        builder.setCancelable(false);
        builder.show();
    }
}
