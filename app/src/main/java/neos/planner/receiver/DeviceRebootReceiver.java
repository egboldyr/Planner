package neos.planner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import neos.planner.annotation.About;
import neos.planner.service.UndoAlarmEventsService;

/**
 * Created by IEvgen Boldyr on 28.03.16.
 * Project: Planner
 *
 * Приемщик уведомления о перезагрузке устройства
 * Функция добавление всех действующих уведомлений по новой,
 * так как при перезагрузке устройства отчищается AlarmManager
 */

@About(author = "IEvgen_Boldyr", version = "0.1.0")
public class DeviceRebootReceiver extends BroadcastReceiver {

    /*Метод запускающий Service восстановления уведомлений в случае
    * если была выполнена перезагрузка устройства*/
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, UndoAlarmEventsService.class));
    }
}
