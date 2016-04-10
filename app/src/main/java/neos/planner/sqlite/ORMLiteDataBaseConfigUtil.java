package neos.planner.sqlite;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import neos.planner.entity.DbEvent;
import neos.planner.entity.DbNote;
import neos.planner.entity.DbRecoveryFile;

/**
 * Created by IEvgen Boldyr on 16.03.16.
 * Project: Planner
 *
 * Утилита для автоматической генерации кофигурационного файла ormlite_config.txt
 */

public class ORMLiteDataBaseConfigUtil extends OrmLiteConfigUtil{
    /**
     * Здесь необходимо описать все сущности которые будут использоватся
     * при работе с БД, в случае изменений внесенных в ORMLiteOpenHelper,
     * добавить только те классы на которые были сделаны расшиоения
     */
    private static final Class<?>[] classes =
            new Class[] {DbNote.class, DbEvent.class, DbRecoveryFile.class};

    /*Подпрограмма для генирации ormlite_config.txt*/
    public static void main(String[] args) throws IOException, SQLException {
        String currDirectory = "user.dir";
        String configPath = "/app/src/main/res/raw/ormlite_config.txt";
        String projectRoot = System.getProperty(currDirectory);
        String fullConfigPath = projectRoot + configPath;
        File configFile = new File(fullConfigPath);

        if(configFile.exists()) {
            configFile.delete();
            configFile = new File(fullConfigPath);
        }
        writeConfigFile(configFile, classes);
    }
}
