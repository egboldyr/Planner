package neos.planner.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by IEvgen Boldyr on 10.04.16.
 * Project: Planner
 *
 * Сущность описывающая путь к файлу резервной копии
 *
 * ВНИМАНИЕ! После всех внесенных изменений в данный класс,
 * требуется повторная перегенирация файла ormlite_config
 * с помощью утилиты ORMLiteDataBaseConfigUtil
 */

@DatabaseTable(tableName = "PLANNER_BACK_UP")
public class DbRecoveryFile {

    /*Автогенерируемый уникальный идентефикатор файла*/
    @DatabaseField(generatedId = true)
    private Long id;

    /*Информация о резервной копии*/
    @DatabaseField(columnName = "RECOVERY_INFO")
    private String recoveryInfo;

    /*Имя файла с резервной копией заметок*/
    @DatabaseField(columnName = "NOTES_FILE_NAME")
    private String notesFile;

    /*Имя файла с резервной копией событий*/
    @DatabaseField(columnName = "EVENTS_FILE_NAME")
    private String eventsFile;

    /*Дата создания резервной копии*/
    @DatabaseField(columnName = "CREATION_DATE")
    private Date date;

    public DbRecoveryFile() {} /*Конструктор по умолчанию для ORMLite-Framework*/

    /*Конструктор для создания новой записи в тавлице БД
    * @param recoveryInfo   - Параметр передающий описание резервной копии
    * @param notesFile      - Параметр передающий имя файла с копией Заметок
    * @param eventsFile     - Параметр передающий имя файла с копией Событий
    * @param date       - Параметр передающий дату создания резервной копии*/
    public DbRecoveryFile(String recoveryInfo, String notesFile, String eventsFile, Date date) {
        this.recoveryInfo = recoveryInfo;
        this.notesFile = notesFile;
        this.eventsFile = eventsFile;
        this.date = date;
    }

    /*Геттеры для доступа к значениям отдельных полей сущности*/
    public Long getId() {
        return id;
    }
    public String getRecoveryInfo() {
        return recoveryInfo;
    }
    public String getNotesFile() {
        return notesFile;
    }
    public String getEventsFile() {
        return eventsFile;
    }
    public Date getDate() {
        return date;
    }
}
