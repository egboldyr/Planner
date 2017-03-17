package neos.planner.listeners;

import android.content.Context;
import android.view.View;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import neos.planner.adapters.DbRecoveryFileAdapter;
import neos.planner.annotation.About;
import neos.planner.entity.DbRecoveryFile;
import neos.planner.sqlite.ORMLiteOpenHelper;

/**
 * Created by IEvgen Boldyr on 15.10.16.
 *
 * Слушатель события для RecoveryDeleteButton
 */

@About(author = "IEvgen_Boldyr", version = "0.1.0")
public class RecoveryDeleteClickListener implements View.OnClickListener {

    /*Переменные необходимые для работы слушателя*/
    private List<DbRecoveryFile> files;
    private DbRecoveryFileAdapter adapter;
    private Integer position;

    /*Конструктор который создает объект RecoveryDeleteClickListener
    * @params files    - Список всех существующих в приложении копий
    *         adapter  - Адаптер отображения данных
    *         position - Позиция элемента в списке*/
    public RecoveryDeleteClickListener(List<DbRecoveryFile> files,
                                       DbRecoveryFileAdapter adapter, Integer position) {
        this.files = files;
        this.adapter = adapter;
        this.position = position;
    }

    @Override
    public void onClick(View v) {
        try {
            ORMLiteOpenHelper helper =
                    OpenHelperManager.getHelper(v.getContext(), ORMLiteOpenHelper.class);
            Dao<DbRecoveryFile, Long> dao = helper.getRecoveryDao();

            DbRecoveryFile file = files.get(position);
            files.remove(position);
            dao.deleteById(file.getId());

            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, files.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
