package neos.planner.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import neos.planner.R;
import neos.planner.annotation.About;
import neos.planner.entity.DbRecoveryFile;
import neos.planner.listeners.RecoveryDeleteClickListener;
import neos.planner.listeners.RecoveryUndoClickListener;
import neos.planner.sqlite.ORMLiteOpenHelper;

/**
 * Created by IEvgen Boldyr on 18.04.16.
 * Project: Planner
 *
 * Адаптер для отображения всех резервных копий созданных в приложении
 */

@About(author = "IEvgen_Boldyr", version = "0.1.0")
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
    public void onBindViewHolder(ViewHolder holder, final int position) {
        DbRecoveryFile file = files.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        holder.file.setText(file.getRecoveryInfo());
        holder.date.setText(sdf.format(file.getDate()));
        holder.btnUndo.setOnClickListener(new RecoveryUndoClickListener(files, position));
        //holder.btnDelete.setOnClickListener(new RecoveryDeleteClickListener(files, this, position));
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ORMLiteOpenHelper helper =
                            OpenHelperManager.getHelper(v.getContext(), ORMLiteOpenHelper.class);
                    Dao<DbRecoveryFile, Long> dao = helper.getRecoveryDao();

                    DbRecoveryFile file = files.get(position);
                    files.remove(position);
                    dao.deleteById(file.getId());

                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, files.size());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.mFileName) TextView file;
        @Bind(R.id.mCopyCreationDate) TextView date;
        @Bind(R.id.btnRecoveryUndo) ImageButton btnUndo;
        @Bind(R.id.btnRecoveryDelete) ImageButton btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
