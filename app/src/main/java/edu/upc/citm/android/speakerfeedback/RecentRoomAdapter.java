package edu.upc.citm.android.speakerfeedback;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class RecentRoomAdapter extends RecyclerView.Adapter<ItemRecentRoomHolder> {
    Context context;
    List<RecentRoomItem> recent_rooms;
    private RecentRoomAdapter.OnClickListener onClickListener;

    public RecentRoomAdapter(Context context, List<RecentRoomItem> items) {
        this.context = context;
        this.recent_rooms = items;
    }

    @NonNull
    @Override
    public ItemRecentRoomHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.recent_room_item, parent, false);
        return new ItemRecentRoomHolder(itemView, onClickListener);
    }

    @NonNull
    @Override
    public void onBindViewHolder(@NonNull ItemRecentRoomHolder holder, int position) {
        holder.bind(recent_rooms.get(position));

    }

    public void setOnClickListener(RecentRoomAdapter.OnClickListener listener) {
        this.onClickListener = listener;
    }


    public interface OnClickListener {
        void onClick(int position);
    }

    @Override
    public int getItemCount() {
        return recent_rooms.size();
    }
}
