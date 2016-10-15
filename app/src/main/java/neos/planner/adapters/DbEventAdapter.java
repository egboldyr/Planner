package neos.planner.adapters;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import neos.planner.R;
import neos.planner.entity.DbEvent;
import neos.planner.listeners.EventShareButtonClickListener;
import neos.planner.receiver.EventRemindReceiver;
import neos.planner.sqlite.ORMLiteOpenHelper;

/**
 * Created by IEvgen Boldyr on 20.03.16.
 * Project: Planner
 *
 * Адаптер для отображения запланированных событий пользователя выбраных из БД
 */

public class DbEventAdapter extends RecyclerView.Adapter<DbEventAdapter.ViewHolder> {

    /*Список всех событий*/
    private List<DbEvent> events;

    /*Конструктор для создания адаптера
    * @param events - Параметр передающий список событий для отображния*/
    public DbEventAdapter(List<DbEvent> events) {
        this.events = events;
    }

    /*Метод выполняющий создание отдельной записи в списке*/
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_event, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    /*Метод который заполняет список необходимыми данными из коллекции*/
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        DbEvent event = events.get(position);

        holder.dateAndTime.setText(event.getDate() + " " + event.getTime());
        holder.status.setText(event.getStatus());
        holder.body.setText(event.getEvent());
        holder.share.setOnClickListener(new EventShareButtonClickListener(event));
        /*В будущем разобраться почему тот же самый код не работает через отдельный Listener
        * Класс описывающий слушатель для данного элемента EventDeleteButtonClickListener*/
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ORMLiteOpenHelper helper =
                    OpenHelperManager.getHelper(v.getContext(), ORMLiteOpenHelper.class);
                    Dao<DbEvent, Long> dao = helper.getEventsDao();

                    DbEvent event = events.get(position);
                    events.remove(position);
                    dao.deleteById(event.getId());

                    Intent intent = new Intent(v.getContext(), EventRemindReceiver.class);
                    intent.putExtra("BODY", event.getEvent());
                    String id = Long.toString(event.getId());

                    PendingIntent pendingIntent =
                            PendingIntent.getBroadcast(v.getContext(), Integer.parseInt(id),
                                    intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager manager =
                            (AlarmManager) v.getContext().getSystemService(Context.ALARM_SERVICE);
                    manager.cancel(pendingIntent);

                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, events.size());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*Метод возвращающий количесво записей переданных для вывода*/
    @Override
    public int getItemCount() {
        return events.size();
    }

    /*Внутренний класс связывающий отдельную запись из списка
      с компонентами виджетов для отображения данных */
    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.mEventDateAndTime) TextView dateAndTime;
        @Bind(R.id.mEventStatus) TextView status;
        @Bind(R.id.mEventText) TextView body;
        @Bind(R.id.btnShareEvent) ImageButton share;
        @Bind(R.id.btnRemoveEvent) ImageButton remove;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
