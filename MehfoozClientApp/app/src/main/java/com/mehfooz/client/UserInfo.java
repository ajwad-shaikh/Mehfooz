package com.mehfooz.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.EditText;

public class UserInfo extends AppCompatActivity {

    private String tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final EditText name = findViewById(R.id.name);
        final EditText number = findViewById(R.id.number);

        Intent intent = getIntent();
        name.setText(intent.getStringExtra("name"));
        number.setText(intent.getStringExtra("number"));
        tag = intent.getStringExtra("tag");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("name",name.getText().toString());
                returnIntent.putExtra("number",number.getText().toString());
                returnIntent.putExtra("tag",tag);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
