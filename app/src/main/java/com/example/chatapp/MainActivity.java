package com.example.chatapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.github.library.bubbleview.BubbleLinearLayout;
import com.github.library.bubbleview.BubbleTextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import android.text.format.DateFormat;

public class MainActivity extends AppCompatActivity {

    private static int SIGN_IN_CODE = 1;
    private RelativeLayout activity_main;
    private FirebaseListAdapter<Message> adapter;
    private FloatingActionButton sendBtn;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_CODE) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(activity_main, "You are authorized!", Snackbar.LENGTH_SHORT).show();
                displayAllMessages();
            } else {
                Snackbar.make(activity_main, "You are not authorized!", Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity_main = findViewById(R.id.activity_main);
        sendBtn = findViewById(R.id.btnSend);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText textField = findViewById(R.id.message_field);
                if (textField.getText().toString() == "") return;
                FirebaseDatabase.getInstance().getReference().push().setValue(new Message(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), textField.getText().toString()));
                textField.setText("");
            }
        });

        // No auth user
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            this.startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_CODE);
        else {
            Snackbar.make(activity_main, "You are authorized!", Snackbar.LENGTH_SHORT).show();
            displayAllMessages();
        }
    }

    private void displayAllMessages () {
        ListView listOfMessages = findViewById(R.id.list_of_messages);
        adapter = new FirebaseListAdapter<Message>(this, Message.class, R.layout.list_item, FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View v, Message model, int position) {
                TextView mess_user, mess_time, mess_text;
                mess_user = v.findViewById(R.id.message_user);
                mess_time = v.findViewById(R.id.message_time);
                mess_text = v.findViewById(R.id.message_text);

                if (model.getUserName().equals(FirebaseAuth.getInstance().getCurrentUser().getDisplayName())) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mess_text.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_END);
                    mess_text.setBackgroundColor(Color.parseColor("#b4d5de"));
                    mess_text.setLayoutParams(params);
                }

                mess_user.setText(model.getUserName());
                mess_text.setText(model.getTextMessage());
                mess_time.setText(DateFormat.format("dd-mm-yyyy HH:mm", model.getMessageTime()));
            }
        };

        listOfMessages.setAdapter(adapter);
    }
}