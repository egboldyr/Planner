package neos.planner.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by IEvgen Boldyr on 30.03.16.
 * Project: Planner
 *
 * Сервис для востановления событий в памяти устройства,
 * в случае если устройство было перезагружено
 */

public class UndoAlarmEventsService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        //Реализоать логику востановления после перезагрузки
        return null;
    }
}
