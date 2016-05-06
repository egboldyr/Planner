package neos.planner.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import neos.planner.R;
import neos.planner.entity.DbRecoveryFile;

/**
 * Created by IEvgen Boldyr on 18.04.16.
 * Project: Planner
 *
 * Адаптер для отображения всех резервных копий созданных в приложении
 */

public class DbRecoveryFileAdapter extends RecyclerView.Adapter<DbRecoveryFileAdapter.ViewHolder>{

    /*Список всех файлов*/
    private List<DbRecoveryFile> files;

    /*Конструктор для создания адаптера
    * @param List<DbRecoveryFile> list - параметр передающий список файлов для отображения*/
    public DbRecoveryFileAdapter(List<DbRecoveryFile> files) {
        this.files = files;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_recovery_file, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DbRecoveryFile file = files.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        holder.file.setText(file.getRecoveryInfo());
        holder.date.setText(sdf.format(file.getDate()));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.mFileName) TextView file;
        @Bind(R.id.mCopyCreationDate) TextView date;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
