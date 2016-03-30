package neos.planner.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by IEvgen Boldyr on 16.03.16.
 * Project: Planner
 *
 * Сущность описывающая пользовательскую заметку, а также
 * сопутсвующую ей вспомогательную информацию
 *
 * ВНИМАНИЕ! После всех внесенных изменений в данный класс,
 * требуется повторная перегенирация файла ormlite_config
 * с помощью утилиты ORMLiteDataBaseConfigUtil
 */

@DatabaseTable(tableName = "PLANNER_NOTES")
public class DbNote {

    /*Автогенерируемый уникальный идентефикатор заметки*/
    @DatabaseField(generatedId = true)
    private Long id;

    /*Название (заголовок) пользовательской заметки*/
    @DatabaseField(columnName = "NOTE_TITLE")
    private String title;

    /*Содержимое пользовательской заметки*/
    @DatabaseField(columnName = "NOTE_TEXT")
    private String noteText;

    /*Группа к которой пренадлежит заметка*/
    @DatabaseField(columnName = "NOTE_GROUP")
    private String group;

    /*Дата и время созания заметки*/
    @DatabaseField(columnName = "CREATION_DATE")
    private String creationDate;

    /*Дата и время последнего изменения заметки*/
    @DatabaseField(columnName = "UPDATE_DATE")
    private String updateDate;

    public DbNote() {} /*Конструктор по умолчанию для ORMLite-Framework*/

    /*Конструктор для создания новой записи в таблице БД
    * Параметры:
    * @param title        - Параметр передающий название заметки
    * @param noteText     - Параметр передающий основной текст заметки
    * @param group        - Параметр перадающий группу к которой относится заметка
    * @param creationDate - Параметр передающий дату создания заметки
    * @param updateDate   - Параметр передающий дату послднего изменения*/
    public DbNote(String title, String noteText,
                  String group, String creationDate, String updateDate) {
        this.title = title;
        this.noteText = noteText;
        this.group = group;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
    }

    /*Сеттеры для модификации и сохранения изменений в отдельных полях сущности*/
    public void setTitle(String title) {
        this.title = title;
    }
    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }
    public void setGroup(String group) {
        this.group = group;
    }
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    /*Геттеры для доступа к значениям отдельных полей сущности*/
    public Long getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getNoteText() {
        return noteText;
    }
    public String getGroup() {
        return group;
    }
    public String getCreationDate() {
        return creationDate;
    }
    public String getUpdateDate() {
        return updateDate;
    }
}
