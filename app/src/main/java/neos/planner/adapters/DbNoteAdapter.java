package neos.planner.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import neos.planner.R;
import neos.planner.annotation.About;
import neos.planner.entity.DbNote;
import neos.planner.listeners.NoteShareButtonClickListener;

/**
 * Created by IEvgen Boldyr on 16.03.16.
 * Project: Planner
 *
 * Адаптер для отображения пользовательских заметок выбранных из БД
 */

@About(author = "IEvgen_Boldyr", version = "0.1.0")
public class DbNoteAdapter extends RecyclerView.Adapter<DbNoteAdapter.ViewHolder> {

    /*Список всех заметок*/
    private List<DbNote> notes;

    /*Конструктор для создания адаптера
    * @param notes - Параметр передающий список заметок для отображения*/
    public DbNoteAdapter(List<DbNote> notes) {
        this.notes = notes;
    }

    /*Метод выполняющий создание отдельной записи в списке*/
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_note, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    /*Метод который заполняет список необходимыми данными из коллекции*/
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        DbNote note = notes.get(position);

        holder.title.setText(note.getTitle());
        holder.text.setText(note.getNoteText());
        holder.group.setText(note.getGroup());
        holder.date.setText(note.getUpdateDate());
        holder.share.setOnClickListener(new NoteShareButtonClickListener(note));
    }

    /*Метод возвращающий количество записей переданных для вывода*/
    @Override
    public int getItemCount() {
        return notes.size();
    }

    /*Внутренний класс связывающий отдельную запись из списка
      с компонентами виджетов для отображения данных */
    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.mNoteTitle) TextView title;
        @Bind(R.id.mNoteText) TextView text;
        @Bind(R.id.mNoteGroup) TextView group;
        @Bind(R.id.mUpdateDate) TextView date;
        @Bind(R.id.btnShareNote) ImageButton share;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
