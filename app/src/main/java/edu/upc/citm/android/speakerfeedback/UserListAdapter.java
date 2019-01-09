package edu.upc.citm.android.speakerfeedback;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<ItemUserHolder> {
    Context context;
    List<UserItem> items;

    public UserListAdapter(Context context, List<UserItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ItemUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.user_item_view, parent, false);
        return new ItemUserHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemUserHolder holder, int pos) {
        holder.bind(items.get(pos));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

