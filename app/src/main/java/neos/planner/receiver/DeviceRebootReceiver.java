package neos.planner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by IEvgen Boldyr on 28.03.16.
 * Project: Planner
 *
 * Приемщик уведомления о перезагрузке устройства
 * Функция добавление всех действующих уведомлений по новой,
 * так как при перезагрузке устройства отчищается AlarmManager
 */

public class DeviceRebootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
