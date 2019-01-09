package edu.upc.citm.android.speakerfeedback;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;


import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersListActivity extends AppCompatActivity {

    // Model
    List<UserItem> items;

    // Referències a elements de la pantalla
    private RecyclerView items_view;
    private UserListAdapter adapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String room_id = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        items = new ArrayList<>();

        items_view = findViewById(R.id.users_list_view);

        adapter = new UserListAdapter(this, items);

        items_view.setLayoutManager(new LinearLayoutManager(this));
        items_view.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        );
        items_view.setAdapter(adapter);

        Intent intent = getIntent();
        room_id = intent.getStringExtra("room_id");
    }

    @Override
    protected void onStart() {
        super.onStart();

        db.collection("users").whereEqualTo("room", room_id).
                addSnapshotListener(this,usersListener);
    }

    private EventListener<QuerySnapshot> usersListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if (e != null) {
                Log.e("SpeakerFeedback", "Error on recieve users inside a room", e);
                return;
            }


            items.clear();
            for (DocumentSnapshot doc : documentSnapshots)
            {
                UserItem new_user = new UserItem(doc.getString("name"));
                items.add(new_user);
            }
            adapter.notifyDataSetChanged();
        }
    };
}
