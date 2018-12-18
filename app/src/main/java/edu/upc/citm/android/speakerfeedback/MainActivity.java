package edu.upc.citm.android.speakerfeedback;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REGISTER_USER = 0;
    private static final int ENTER_ROOM_ID = 1;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private TextView textView;
    private String userId;
    private List<Poll> polls = new ArrayList<>();

    private RecyclerView polls_view;
    private Adapter adapter;

    private boolean connected;
    private boolean logged;
    private String room_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        polls_view = findViewById(R.id.polls_view);
        adapter = new Adapter();

        polls_view.setLayoutManager(new LinearLayoutManager(this));
        polls_view.setAdapter(adapter);

        textView = findViewById(R.id.user_counter_textview);

        logged = false;
        connected = false;

        getOrRegisterUser();

    }

    private void startFirestoreListenerService(String room_id) {
        Intent intent = new Intent(this, FirestoreListenerService.class);
        intent.putExtra("room", room_id);
        startService(intent);
        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        prefs.edit()
                .putString("roomId", room_id)
                .commit();
        connected = true;
    }

    private void stopFirestoreListenerService() {
        Intent intent = new Intent(this, FirestoreListenerService.class);
        stopService(intent);
    }

    @Override
    protected void onDestroy() {

        db.collection("users").document(userId)
                .update(
                        "room", FieldValue.delete()
                );
        Log.i("SpeakerFeedback", "onDestroy");

        super.onDestroy();

    }

    private EventListener<DocumentSnapshot> roomListener = new EventListener<DocumentSnapshot>() {
        @Override
       public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
            if (e != null) {
                Log.e("SpeakerFeedback", "Error on recieve rooms/testroom", e);
                return;
            }

            if (!documentSnapshot.contains("open") || !documentSnapshot.getBoolean("open"))
            {
                stopFirestoreListenerService();
                finish();
            }
            else
            {
                String name = documentSnapshot.getString("name");
                setTitle(name);
            }
        }
    };

    private EventListener<QuerySnapshot> usersListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if (e != null) {
                Log.e("SpeakerFeedback", "Error on recieve users inside a room", e);
                return;
            }
            textView.setText(String.format("Users connected: %d", documentSnapshots.size()));

            // How to get data list
            //String usersNames = "";
            /*for (DocumentSnapshot doc : documentSnapshots) {
                usersNames += doc.getString("name") + "\n";
            }*/
           // textView.setText(usersNames);
            //
        }
    };

    private EventListener<QuerySnapshot> pollsListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if(e != null){
                Log.e("SpekerFeedback","Error al rebre la llista de 'polls'");
                return;
            }
            polls = new ArrayList<>();
            for (DocumentSnapshot doc:documentSnapshots){
                Poll poll = doc.toObject(Poll.class);
                poll.setPoll_id(doc.getId());
                polls.add(poll);
            }
            Log.i("SpeakerFeedBack",String.format("He carregat %d polls.", polls.size()));

            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        // TODO: check if you're already in a room. If not: call the method below
        if(!connected && logged)
            chooseRoom();

        if(connected){
            db.collection("rooms").document(room_id)
                    .addSnapshotListener(this,roomListener);

            db.collection("users").whereEqualTo("room", room_id).
                    addSnapshotListener(this,usersListener);

            db.collection("rooms").document(room_id).collection("polls")
                    .orderBy("start", Query.Direction.DESCENDING).addSnapshotListener(this, pollsListener);
        }
    }

    private void getOrRegisterUser() {
        // Busquem a les preferències de l'app l'ID de l'usuari per saber si ja s'havia registrat
        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        userId = prefs.getString("userId", null);
        if (userId == null) {
            // Hem de registrar l'usuari, demanem el nom
            Intent intent = new Intent(this, RegisterUserActivity.class);
            startActivityForResult(intent, REGISTER_USER);
            Toast.makeText(this, "Encara t'has de registrar", Toast.LENGTH_SHORT).show();

        } else {
            // Ja està registrat, mostrem el id al Log
            Log.i("SpeakerFeedback", "userId = " + userId);
            logged = true;
        }
    }

    public void enterRoom(String room_id) {
        db.collection("users").document(userId)
                .update(
                        "room", room_id, "last_active", new Date()
                );
    }

    private void chooseRoom() {
        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        room_id = prefs.getString("roomId", null);

        if(room_id == null){
            Intent intent = new Intent(this, RoomID.class);
            startActivityForResult(intent,ENTER_ROOM_ID);
        }else{
            startFirestoreListenerService(room_id);
            enterRoom(room_id);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REGISTER_USER:
                if (resultCode == RESULT_OK) {
                    String name = data.getStringExtra("name");
                    registerUser(name);
                    logged = true;
                } else {
                    Toast.makeText(this, "Has de registrar un nom", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case ENTER_ROOM_ID:
                    room_id = data.getStringExtra("room_id");
                    startFirestoreListenerService(room_id);
                    enterRoom(room_id);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void registerUser(String name) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("name", name);
        db.collection("users").add(fields).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                // Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                // textview.setText(documentReference.getId());
                userId = documentReference.getId();
                SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
                prefs.edit()
                        .putString("userId", userId)
                        .commit();
                Log.i("SpeakerFeedback", "New user: userId = " + userId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("SpeakerFeedback", "Error creant objecte", e);
                Toast.makeText(MainActivity.this,
                        "No s'ha pogut registrar l'usuari, intenta-ho més tard", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public void onClickUsersBar(View view) {

        // Call the usr list activity here
        Intent intent = new Intent(this, UsersListActivity.class);
        startActivity(intent);
    }

    public void OnCardClicked(int pos) {
        Poll poll = polls.get(pos);

        if(!poll.isOpen())
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(poll.getQuestion());
        String[] poll_options = new String[poll.getOptions().size()];

        for (int i = 0; i < poll.getOptions().size(); i++){
            poll_options[i] = poll.getOptions().get(i);
        }

        builder.setItems(poll_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Map<String,Object> map = new HashMap<String,Object>();

                        map.put("pollid", polls.get(0).getPoll_id());
                        map.put("option",which);

                        db.collection("rooms").document("testroom").
                                collection("votes").document(userId).set(map);
                    }
                });

        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.speaker_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.close_session_btn:
                stopFirestoreListenerService();
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private CardView card_view;
        private TextView label_view;
        private TextView question_view;
        private TextView options_view;

        public ViewHolder(View itemView) {
            super(itemView);
            card_view = itemView.findViewById(R.id.card_view);
            label_view = itemView.findViewById(R.id.label_view);
            question_view = itemView.findViewById(R.id.question_view);
            options_view = itemView.findViewById(R.id.options_view);
            card_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    OnCardClicked(pos);
                }
            });
        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder>{

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.poll_view,parent,false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Poll poll = polls.get(position);
            if(position == 0){
                holder.label_view.setVisibility(View.VISIBLE);
                if(poll.isOpen()){
                    holder.label_view.setText("Active");
                   // poll.setPoll_id();
                }else{
                    holder.label_view.setText("Previous");
                }
            }else{
                if(!poll.isOpen() && polls.get(position-1).isOpen()){
                    holder.label_view.setVisibility(View.VISIBLE);
                    holder.label_view.setText("Previous");
                }else{
                    holder.label_view.setVisibility(View.GONE);
                }
            }

            holder.card_view.setCardElevation(poll.isOpen() ? 10.0f: 0.0f);
            if(!poll.isOpen())
                holder.card_view.setCardBackgroundColor(0xFFE0E0E0);
            holder.question_view.setText(poll.getQuestion());
            holder.options_view.setText(poll.getOptionsAsString());
        }

        @Override
        public int getItemCount() {
            return polls.size();
        }
    }
}
