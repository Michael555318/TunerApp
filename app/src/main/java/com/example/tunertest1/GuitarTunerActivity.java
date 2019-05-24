package com.example.tunertest1;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toolbar;

import static com.example.tunertest1.Config.lightThemed;
import static com.example.tunertest1.Config.themesArray;

public class GuitarTunerActivity extends AppCompatActivity {

    LinearLayout dynamicContent,bottonNavBar;
    android.support.v7.widget.Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_guitar_tuner);

        dynamicContent = (LinearLayout)  findViewById(R.id.dynamicContent);
        bottonNavBar= (LinearLayout) findViewById(R.id.bottonNavBar);
        View wizard = getLayoutInflater().inflate(R.layout.activity_guitar_tuner, null);
        dynamicContent.addView(wizard);

        RadioGroup rg=(RadioGroup)findViewById(R.id.radioGroup1);
        RadioButton rb=(RadioButton)findViewById(R.id.tuner);

        wireWidgets();

        setSupportActionBar(toolbar);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tuner_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (item.getTitle().equals("Theme")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(GuitarTunerActivity.this);
            if (lightThemed) {
                builder.setTitle("Choose a theme")
                        .setSingleChoiceItems(themesArray, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 1) {
                                    lightThemed = false;
                                } else {
                                    lightThemed = true;
                                }
                                Intent restart = new Intent(GuitarTunerActivity.this, GuitarTunerActivity.class);
                                startActivity(restart);
                            }
                        });
            } else {
                builder.setTitle("Choose a theme")
                        .setSingleChoiceItems(themesArray, 1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 1) {
                                    lightThemed = false;
                                } else {
                                    lightThemed = true;
                                }
                                Intent restart = new Intent(GuitarTunerActivity.this, GuitarTunerActivity.class);
                                startActivity(restart);
                            }
                        });
            }
            AlertDialog dialog = builder.create();
            dialog.show();

        }
        return super.onOptionsItemSelected(item);
    }

    private void wireWidgets() {
        toolbar = findViewById(R.id.toolbar);
    }

}
