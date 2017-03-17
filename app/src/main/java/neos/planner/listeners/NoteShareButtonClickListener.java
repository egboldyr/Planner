package neos.planner.listeners;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Telephony;
import android.view.View;

import neos.planner.R;
import neos.planner.annotation.About;
import neos.planner.entity.DbNote;

/**
 * Created by IEvgen Boldyr on 28.03.16.
 * Project: Planner
 *
 * Слушатель события Click для NoteShareButton*/

@About(author = "IEvgen_Boldyr", version = "0.1.0")
public class NoteShareButtonClickListener implements View.OnClickListener {

    private Context context;
    private DbNote note;

    /*Конструктор для создания NoteShareButtonClickListener
    * @param note - Параметр передающий в слушатель данные о содержимом заметки*/
    public NoteShareButtonClickListener(DbNote note) {
        this.note = note;
    }

    /*Метод обрабатывающий событие OnClickListener*/
    @Override
    public void onClick(View v) {
        context = v.getContext();
        callShareDialog();
    }

    /*Диалоговое окошко для функционала ShareNoteButton*/
    private void callShareDialog() {
        CharSequence[] actions = {
                context.getString(R.string.note_share_button_send_sms),
                context.getString(R.string.note_share_button_send_email)
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0 : {
                        callSendBySMS();

                        break;
                    }
                    case 1 : {
                        callSendByEmail();
                        break;
                    }
                }
            }
        });
        builder.show();
    }

    /*Метод отправляющий заметку по СМС*/
    private void callSendBySMS() {
        String smsPackageName = Telephony.Sms.getDefaultSmsPackage(context);
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + ""));
        intent.putExtra("sms_body", note.getTitle() + "\n" + note.getNoteText());
        if (smsPackageName != null) {
            intent.setPackage(smsPackageName);
        }
        context.startActivity(intent);
    }

    /*Метод отправляющий заметку по эл. почте*/
    private void callSendByEmail() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, "");
        intent.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
        intent.putExtra(Intent.EXTRA_TEXT, note.getNoteText());
        context.startActivity(intent);
    }
}
