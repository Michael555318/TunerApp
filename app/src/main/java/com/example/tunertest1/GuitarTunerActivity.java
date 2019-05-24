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
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toolbar;

import com.github.anastr.speedviewlib.ImageLinearGauge;

import static com.example.tunertest1.Config.lightThemed;
import static com.example.tunertest1.Config.themesArray;
import static com.example.tunertest1.Config.tunersArray;

public class GuitarTunerActivity extends MainActivity {

    LinearLayout dynamicContent,bottonNavBar;
    android.support.v7.widget.Toolbar toolbar;

    private Spinner spinner;
    private ImageLinearGauge tuneProgressBar;

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

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(GuitarTunerActivity.this);
                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setTitle("Choose a tuner...")
                        .setItems(tunersArray, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    //tunerThread.interrupt();
                                    Intent startTuner = new Intent(getBaseContext(), TunerActivity.class);
                                    startActivity(startTuner);
                                }
                            }
                        });
                // 3. Get the <code><a href="/reference/android/app/AlertDialog.html">AlertDialog</a></code> from <code><a href="/reference/android/app/AlertDialog.Builder.html#create()">create()</a></code>
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
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
        tuneProgressBar = findViewById(R.id.tuneProgressBar);
        spinner = findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

}
