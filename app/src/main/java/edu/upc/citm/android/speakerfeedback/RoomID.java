package edu.upc.citm.android.speakerfeedback;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class RoomID extends AppCompatActivity {

    EditText entered_room_id;
    private String password_input = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        entered_room_id = findViewById(R.id.edit_room_id);
    }

    public void OnContinue(View view) {
        if (entered_room_id.getText().toString().equals(""))
            Toast.makeText(this, "You need to enter a room ID!", Toast.LENGTH_SHORT).show();
        else
        {
            // TODO: check if room ID exists. If it does: call the method below
            onPasswordPopup();
        }
    }

    protected  void onPasswordPopup()
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
                // TODO: check here if the password is correct
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
}
