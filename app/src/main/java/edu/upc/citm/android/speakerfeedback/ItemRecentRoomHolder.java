package edu.upc.citm.android.speakerfeedback;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

class ItemRecentRoomHolder extends RecyclerView.ViewHolder {

    private TextView name_view;

    public ItemRecentRoomHolder(@NonNull View itemView, final RecentRoomAdapter.OnClickListener onClickListener,final RecentRoomAdapter.OnLongClickListener onLongClickListener) {
        super(itemView);
        name_view = itemView.findViewById(R.id.recent_room_view);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    int pos = getAdapterPosition();
                    onClickListener.onClick(pos);
                }
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                boolean ret = false;
                if (onLongClickListener != null) {
                    int pos = getAdapterPosition();
                    onLongClickListener.onLongClick(pos);
                    ret = true;
                }
                return  ret;
            }
        });
    }

    public void bind(RecentRoomItem item) {
        name_view.setText(item.getName());
    }

}
