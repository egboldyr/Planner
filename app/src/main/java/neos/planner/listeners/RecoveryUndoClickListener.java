package neos.planner.listeners;

import android.view.View;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.logger.LocalLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.sql.SQLException;
import java.util.List;

import neos.planner.annotation.About;
import neos.planner.entity.DbEvent;
import neos.planner.entity.DbNote;
import neos.planner.entity.DbRecoveryFile;
import neos.planner.sqlite.ORMLiteOpenHelper;

/**
 * Created by IEvgen Boldyr on 15.10.16.
 *
 * Слушатель события для кнопки RecoveryUndoButton
 */

@About(author = "IEvgen_Boldyr", version = "0.1.0")
public class RecoveryUndoClickListener implements View.OnClickListener {

    private List<DbRecoveryFile> files;
    private int position;

    public RecoveryUndoClickListener(List<DbRecoveryFile> files, int position) {
        this.files = files;
        this.position = position;
    }

    @Override
    public void onClick(View v) {
        try {
            DbRecoveryFile recovery = files.get(position);
            ORMLiteOpenHelper helper =
                    OpenHelperManager.getHelper(v.getContext(), ORMLiteOpenHelper.class);
            Dao<DbNote, Long> notesDao = helper.getNotesDao();
            Dao<DbEvent, Long> eventsDao = helper.getEventsDao();

            FileInputStream fisNotes = v.getContext().openFileInput(recovery.getNotesFile());
            ObjectInputStream oisNotes = new ObjectInputStream(fisNotes);
            List<DbNote> notes = (List<DbNote>) oisNotes.readObject();

            for (DbNote note : notes) {
                notesDao.createOrUpdate(note);
            }

            FileInputStream fisEvents = v.getContext().openFileInput(recovery.getEventsFile());
            ObjectInputStream oisEvents = new ObjectInputStream(fisEvents);
            List<DbEvent> events = (List<DbEvent>) oisEvents.readObject();

            for (DbEvent event : events) {
                eventsDao.createOrUpdate(event);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
