package edu.upc.citm.android.speakerfeedback;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ChooseRoomActivity extends AppCompatActivity {

    private static final int MAIN_ACTIVITY = 0;

    EditText entered_room_id;
    private String password_input = "";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Model
    List<RecentRoomItem> recent_rooms;

    // Referències a elements de la pantalla
    private RecyclerView recent_rooms_recycle_view;
    private RecentRoomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        entered_room_id = findViewById(R.id.edit_room_id);

        recent_rooms_recycle_view = findViewById(R.id.recent_rooms_id);
        recent_rooms_recycle_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recent_rooms_recycle_view.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        );

        recent_rooms = new ArrayList<>();

        adapter = new RecentRoomAdapter(this, recent_rooms);
        recent_rooms_recycle_view.setAdapter(adapter);

        for (int i = 0; i < 30; ++i) {
            recent_rooms.add(new RecentRoomItem("Room: #" + i));
        }
    }

    public void OnContinue(View view) {
        if (entered_room_id.getText().toString().equals(""))
            Toast.makeText(this, "You need to enter a room ID!", Toast.LENGTH_SHORT).show();
        else
        {
            db.collection("rooms").document(entered_room_id.getText().toString()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Log.i("SpeakerFeedback", documentSnapshot.toString());
                    if (documentSnapshot.exists() && documentSnapshot.contains("open")) {
                        if (documentSnapshot.contains("password") && !documentSnapshot.getString("password").isEmpty()) { // Contains password
                            onPasswordPopup(documentSnapshot.get("password").toString());
                        }else {
                            sendDataAndFinish();
                        }
                    } else {

                        if (!documentSnapshot.exists()) {
                            Toast.makeText(ChooseRoomActivity.this,
                                    "Room with ID " + "'" + entered_room_id.getText().toString() + "'" + " does not exist. Try another one!", Toast.LENGTH_SHORT).show();
                        } else if (!documentSnapshot.contains("open")) {
                            Toast.makeText(ChooseRoomActivity.this,
                                    "Room with ID " + "'" + entered_room_id.getText().toString() + "'" + " is not open. Try another one!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChooseRoomActivity.this,
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("SpeakerFeedback", e.getMessage());
                }
            });
        }
    }

    protected  void onPasswordPopup(final String password)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter room password:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                password_input = input.getText().toString();
                if (password_input.equals(password))
                {
                    Toast.makeText(ChooseRoomActivity.this,
                            "Password correct", Toast.LENGTH_SHORT).show();

                    sendDataAndFinish();
                }
                else
                {
                    Toast.makeText(ChooseRoomActivity.this,
                            "Password incorrect! Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void sendDataAndFinish() {
        Intent data = new Intent();
        data.putExtra("room_id", entered_room_id.getText().toString());
        setResult(RESULT_OK, data);
        finish();
    }
}
