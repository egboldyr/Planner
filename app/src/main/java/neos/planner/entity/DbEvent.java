package neos.planner.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by IEvgen Boldyr on 16.03.16.
 * Project: Planner
 *
 * Сущность описывающая отдельное запланированное событие
 *
 * ВНИМАНИЕ! После всех внесенных изменений в данный класс,
 * требуется повторная перегенирация файла ormlite_config
 * с помощью утилиты ORMLiteDataBaseConfigUtil
 */

@DatabaseTable(tableName = "PLANNER_EVENTS")
public class DbEvent {

    /*Автогенерируемый уникальный идентефикатор события*/
    @DatabaseField(generatedId = true)
    private Long id;

    /*Описание запланированного события*/
    @DatabaseField(columnName = "EVENTS_TITLE")
    private String event;

    /*Дата наступления запланнированого события*/
    @DatabaseField(columnName = "EVENTS_DATE")
    private String date;

    /*Время наступления запланированного события*/
    @DatabaseField(columnName = "EVENTS_TIME")
    private String time;

    /*Время напоминания до наступления события*/
    @DatabaseField(columnName = "EVENTS_REMIND")
    private String remind;

    /*Статус запланированого события */
    @DatabaseField(columnName = "EVENTS_STATUS")
    private String status;

    public DbEvent() {} /*Конструктор по умолчанию для ORMLite-Framework*/

    /*Конструктор для создания новой записи в тавлице БД
    * @param event  - Параметр передающий описание запланированного события
    * @param date   - Параметр передающий дату на которую запланировано событие
    * @param time   - Параметр передающий время на которое запланировано событие
    * @param status - Параметр передающий статус события
    * @param remind - Параметр передающий время до наступления события*/
    public DbEvent(String event, String date, String time, String status, String remind) {
        this.event = event;
        this.date = date;
        this.time = time;
        this.status = status;
        this.remind = remind;
    }

    /*Сеттеры для модификации и сохранения изменений в отдельных полях сущности*/
    public void setEvent(String event) {
        this.event = event;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setRemind(String remind) {
        this.remind = remind;
    }

    /*Геттеры для доступа к значениям отдельных полей сущности*/
    public Long getId() {
        return id;
    }
    public String getEvent() {
        return event;
    }
    public String getDate() {
        return date;
    }
    public String getTime() {
        return time;
    }
    public String getStatus() {
        return status;
    }
    public String getRemind() {
        return remind;
    }
}
