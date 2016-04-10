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

    /*Имя файла с резервной копией*/
    @DatabaseField(columnName = "FILE_NAME")
    private String fileName;

    /*Путь к файлу с резервной копией*/
    @DatabaseField(columnName = "FILE_PATH")
    private String filePath;

    /*Дата создания резервной копии*/
    @DatabaseField(columnName = "CREATION_DATE")
    private Date date;

    public DbRecoveryFile() {} /*Конструктор по умолчанию для ORMLite-Framework*/

    /*Конструктор для создания новой записи в тавлице БД
    * @param fileName   - Параметр передающий имя файла с созданой копией
    * @param filePath   - Параметр передающий путь к файлу
    * @param date       - Параметр передающий дату создания резервной копии*/
    public DbRecoveryFile(String fileName, String filePath, Date date) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.date = date;
    }

    /*Геттеры для доступа к значениям отдельных полей сущности*/
    public Long getId() {
        return id;
    }
    public String getFileName() {
        return fileName;
    }
    public String getFilePath() {
        return filePath;
    }
    public Date getDate() {
        return date;
    }
}
