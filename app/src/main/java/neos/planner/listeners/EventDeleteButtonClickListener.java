package neos.planner.listeners;

import android.view.View;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import neos.planner.adapters.DbEventAdapter;
import neos.planner.entity.DbEvent;
import neos.planner.sqlite.ORMLiteOpenHelper;

/**
 * Created by IEvgen Boldyr on 29.03.16.
 * Project: Planner
 *
 * Слушатель событий для EventRemoveButton
 */

public class EventDeleteButtonClickListener implements View.OnClickListener {

    private List<DbEvent> events;
    private DbEventAdapter adapter;
    private Integer position;

    /*Конструктор для создания экземпляра класса Listener'а
    * @param DbEvent event          - параметр передает данные о событии которое мы удаляем
    * @param DbEventAdapter adapter - параметр передает ссылку на вызвавший адаптер
    * @param Integer position       - параметр передающий позицию удаляемого элемента в списке*/
    public EventDeleteButtonClickListener(
            List<DbEvent> events, DbEventAdapter adapter, Integer position) {
        this.events = events;
        this.adapter = adapter;
        this.position = position;
    }

    /*Метод обрабатывающий нажатие на элементе EventDeleteButton*/
    @Override
    public void onClick(View v) {
        try {
            ORMLiteOpenHelper helper =
                    OpenHelperManager.getHelper(v.getContext(), ORMLiteOpenHelper.class);
            Dao<DbEvent, Long> dao = helper.getEventsDao();

            DbEvent event = events.get(position);
            events.remove(position);
            dao.deleteById(event.getId());

            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, events.size());
            //Дополнить логикой удаления оповещения из AlarmManager
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
