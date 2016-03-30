package neos.planner.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import neos.planner.R;

/**
 * Created by IEvgen Boldyr on 28.03.16.
 * Project: Planner
 *
 * Приемщик для всех уведомлений созданных и отправленных AlarmManager'ом
 */

public class EventRemindReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();

        NotificationManager manager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(extras.getString("BODY"));
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE;
        manager.notify(1, notification); //Заменить единицу на ID события
    }
}
