package neos.planner.listeners;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import neos.planner.annotation.About;

/**
 * Created by IEvgen Boldyr on 17.03.16.
 * Project: Planner
 *
 * Cлушатель события нажатие на один из элементов списка заметок
 */

@About(author = "IEvgen_Boldyr", version = "0.1.0")
public class DbNoteItemClickListener implements RecyclerView.OnItemTouchListener {
    private OnItemClickListener listener;
    private GestureDetector detector;
    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public DbNoteItemClickListener(Context context, OnItemClickListener listener) {
        this.listener = listener;
        detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        if (detector.onTouchEvent(e)) {
            View view = rv.findChildViewUnder(e.getX(), e.getY());
            if (view != null && !view.dispatchTouchEvent(e)) {
                if (rv.getChildPosition(view) == RecyclerView.NO_POSITION) {
                    //Возвращаем true так как нажата дочерняя клавиша ShareButton
                    //Описание OnClickShareButton нахоится непосредственно в адаптере DbNoteAdapter
                    return true;
                }
                listener.onItemClick(view, rv.getChildPosition(view));
                return true;
            }
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {}

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
}
