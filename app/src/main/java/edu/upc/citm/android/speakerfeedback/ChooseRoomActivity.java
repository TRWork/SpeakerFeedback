package edu.upc.citm.android.speakerfeedback;

import android.content.Context;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ChooseRoomActivity extends AppCompatActivity {

    private static final int MAIN_ACTIVITY = 0;

    private static final String SAVE_FILE_NAME = "recent_rooms.txt";

    EditText entered_room_id;
    Button continue_btn;
    private String password_input = "";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private boolean is_recent_room = false;

    // Model
    List<RecentRoomItem> recent_rooms;

    // Referències a elements de la pantalla
    private RecyclerView recent_rooms_recycle_view;
    private RecentRoomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        password_input = "";

        readItemList();

        entered_room_id = findViewById(R.id.edit_room_id);
        continue_btn = findViewById(R.id.enter_btn);

        recent_rooms_recycle_view = findViewById(R.id.recent_rooms_id);
        recent_rooms_recycle_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recent_rooms_recycle_view.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        );

        adapter = new RecentRoomAdapter(this, recent_rooms);

        recent_rooms_recycle_view.setAdapter(adapter);
        adapter.setOnClickListener(new RecentRoomAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                entered_room_id.setText(recent_rooms.get(position).getName().toString());
                is_recent_room = true;
                continue_btn.performClick();
            }
        });

    }

    private void saveItemList() {
        try {
            FileOutputStream outputStream = openFileOutput(SAVE_FILE_NAME, MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);
            for (int i = 0; i < recent_rooms.size(); i++) {
                RecentRoomItem item = recent_rooms.get(i);
                writer.write(String.format("%s;%s\n", item.getName(), item.getPassword()));
            }
            writer.close();
        }
        catch (FileNotFoundException e) {
            Log.e("SpeakerFeedback", "saveItemList: FileNotFoundException");
        }
        catch (IOException e) {
            Log.e("SpeakerFeedback", "saveItemList: IOException");
        }
    }

    private void readItemList() {
        recent_rooms = new ArrayList<>();
        try {
            FileInputStream inputStream = openFileInput(SAVE_FILE_NAME);
            InputStreamReader reader = new InputStreamReader(inputStream);
            Scanner scanner = new Scanner(reader);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(";");
                if(parts.length > 1)
                    recent_rooms.add(new RecentRoomItem(parts[0],parts[1]));
                else
                    recent_rooms.add(new RecentRoomItem(parts[0],""));
            }
        }
        catch (FileNotFoundException e) {
            Log.e("SpeakerFeedback", "readItemList: FileNotFoundException");
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
                        if (documentSnapshot.contains("password") && !documentSnapshot.getString("password").isEmpty() && !is_recent_room) { // Contains password
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

        if(recent_rooms.isEmpty()){
            recent_rooms.add(new RecentRoomItem(entered_room_id.getText().toString(),password_input));
            saveItemList();
        }else{
            for (RecentRoomItem recent_item : recent_rooms) {
                if(!recent_item.getName().toString().equals(entered_room_id.getText().toString())){
                    recent_rooms.add(new RecentRoomItem(entered_room_id.getText().toString(),password_input));
                    saveItemList();
                    break;
                }
            }
        }

        Intent data = new Intent();
        data.putExtra("room_id", entered_room_id.getText().toString());
        setResult(RESULT_OK, data);

        is_recent_room = false;
        finish();
    }
}
