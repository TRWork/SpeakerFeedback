package edu.upc.citm.android.speakerfeedback;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class ItemUserHolder extends RecyclerView.ViewHolder {
    private TextView name_view;

    public ItemUserHolder(@NonNull View itemView) {
        super(itemView);
        name_view = itemView.findViewById(R.id.user_name_view);
    }

    public void bind(UserItem item) {
        name_view.setText(item.getName());
    }
}
