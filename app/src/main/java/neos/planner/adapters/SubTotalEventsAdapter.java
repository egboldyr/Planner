package neos.planner.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import neos.planner.R;

/**
 * Created by IEvgen Boldyr on 10.04.16.
 * Project: Planner
 */
public class SubTotalEventsAdapter extends RecyclerView.Adapter<SubTotalEventsAdapter.ViewHolder> {

    private Context context;
    private Integer allEvents;
    private Integer onlyActiveEvents;
    private Integer onlyFinishEvents;

    public SubTotalEventsAdapter(Context context,
                   Integer allEvents, Integer onlyActiveEvents, Integer onlyFinishEvents) {
        this.context = context;
        this.allEvents = allEvents;
        this.onlyActiveEvents = onlyActiveEvents;
        this.onlyFinishEvents = onlyFinishEvents;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_subtotal_events, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.allEvents.setText(
                context.getResources().getString(R.string.subtotal_all_events) + allEvents);
        holder.activeEvents.setText(
                context.getResources().getString(R.string.subtotal_only_active_events) + onlyActiveEvents);
        holder.finishEvents.setText(
                context.getResources().getString(R.string.subtotal_finish_events) + onlyFinishEvents);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.mAllEvents) TextView allEvents;
        @Bind(R.id.mActiveEvents) TextView activeEvents;
        @Bind(R.id.mFinishEvents) TextView finishEvents;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
