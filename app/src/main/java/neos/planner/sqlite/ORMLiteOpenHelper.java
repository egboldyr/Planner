package neos.planner.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import neos.planner.R;
import neos.planner.annotation.About;
import neos.planner.entity.DbEvent;
import neos.planner.entity.DbNote;
import neos.planner.entity.DbRecoveryFile;

/**
 * Created by IEvgen Boldyr on 16.03.16.
 * Project: Planner
 *
 * После внесения изменений в данный класс необходима обязательная перегенирация
 * ormlite_config.txt с помощью утилиты ORMLiteDataBaseConfigUtil
 */

@About(author = "IEvgen_Boldyr", version = "0.1.0")
public class ORMLiteOpenHelper extends OrmLiteSqliteOpenHelper {


    /*Название и актуальная версия БД*/
    private static final String DATABASE_NAME = "planner";
    private static final int DATABASE_VERSION = 1;

    /*Классы DAO для удобной работы с данными*/
    private Dao<DbNote, Long> notesDao;
    private Dao<DbEvent, Long> eventsDao;
    private Dao<DbRecoveryFile, Long> recoveryDao;

    /*Конструктор для ORMLite-Framework*/
    public ORMLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    /*Метод который будет вызван при первом запуске приложения для создания БД.
    *   - Для добавления дополнительной таблицы достаточно расширить метод
    *   с помощью TableUtils и необходимого класса описания сущности*/
    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, DbNote.class);
            TableUtils.createTable(connectionSource, DbEvent.class);
            TableUtils.createTable(connectionSource, DbRecoveryFile.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*Метод который будет вызван при обновлении структуры данных в таблицах БД.
     *   - Для добавления дополнительной таблицы достаточно расширить метод
     *   с помощью TableUtils и необходимого класса описания сущности*/
    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.createTable(connectionSource, DbNote.class);
            TableUtils.createTable(connectionSource, DbEvent.class);
            TableUtils.createTable(connectionSource, DbRecoveryFile.class);
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*Метод возвращающий DAO для удобной работы с записями в таблице PLANNER_NOTES*/
    public Dao<DbNote, Long> getNotesDao() throws SQLException {
        if (notesDao == null) {
            notesDao = getDao(DbNote.class);
        }
        return notesDao;
    }

    /*Метод возвращающий DAO для удобной работы с записями в таблице PLANNER_EVENTS*/
    public Dao<DbEvent, Long> getEventsDao() throws SQLException {
        if (eventsDao == null) {
            eventsDao = getDao(DbEvent.class);
        }
        return  eventsDao;
    }

    /*Метод возвращающий DAO для удобной работы с записями в таблице PLANNER_BACKUP*/
    public Dao<DbRecoveryFile, Long> getRecoveryDao() throws SQLException {
        if (recoveryDao == null) {
            recoveryDao = getDao(DbRecoveryFile.class);
        }
        return recoveryDao;
    }
}
