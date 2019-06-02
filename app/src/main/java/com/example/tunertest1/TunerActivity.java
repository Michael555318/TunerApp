package com.example.tunertest1;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.solver.widgets.ConstraintWidgetContainer;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aigestudio.wheelpicker.WheelPicker;
import com.github.anastr.speedviewlib.ImageLinearGauge;
import com.github.anastr.speedviewlib.SpeedView;

import org.apache.commons.math3.linear.ConjugateGradient;

import java.util.ArrayList;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

import static com.example.tunertest1.Config.RECORD_PERMISSION;
import static com.example.tunertest1.Config.dispatcher;
import static com.example.tunertest1.Config.lightThemed;
import static com.example.tunertest1.Config.noteFrequencies;
import static com.example.tunertest1.Config.themesArray;
import static com.example.tunertest1.Config.tunersArray;
import static com.example.tunertest1.Config.tuning0;
import static com.example.tunertest1.Config.tuning1;
import static com.example.tunertest1.Config.tuning2;
import static com.example.tunertest1.Config.tuning3;
import static com.example.tunertest1.Config.tuning4;
import static com.example.tunertest1.Config.tuning5;

public class TunerActivity extends MainActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener{

    // Widgets
    private SpeedView differenceDisplay;
    private ImageLinearGauge tuneProgressBar;
    private ImageView cr_scale;
    private TextView octive;
    private ImageView pointer;
    private Toolbar toolbar;
    private BottomNavigationView navigation;
    private Thread tunerThread;
    LinearLayout dynamicContent,bottonNavBar;
    private WheelPicker notePicker;
    private Spinner spinner;
    private TextView display;
    private LinearLayout notesDisplay;
    private Button button1, button2, button3, button4, button5, button6;

    // Tools
    private int progressBarTimer = 1;
    private float lastPitch;
    private int pitchTimer = 3;
    public final static ArrayList<String> notes = new ArrayList<>();
    private int tuner1 = 1; //tuner1 = 1 -> Chromatic Tuner 1
                            //       = 2 -> Chromatic Tuner 2
                            //       = 3 -> Guitar Tuner
                            private int tuning = 0;
    double tuningF = 0;
    int tuningIndex = 0;
    boolean[] tuned = new boolean[] {false, false, false, false, false, false};

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RECORD_PERMISSION:
                permissionToRecordAccepted = grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();
    }

    // Navigation Bar
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    tuner1 = 1;
                    tunersSetUp(tuner1);
                    return true;
                case R.id.navigation_dashboard:
                    tuner1 = 2;
                    tunersSetUp(tuner1);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_tuner);
        if (lightThemed) {
            dynamicContent = (LinearLayout)  findViewById(R.id.dynamicContent);
            bottonNavBar= (LinearLayout) findViewById(R.id.bottonNavBar);
            RadioGroup radiogroup = (RadioGroup) bottonNavBar.getChildAt(0);
            radiogroup.getChildAt(1).setBackgroundColor(getResources().getColor(R.color.colorLightPrimaryClicked));
            radiogroup.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.colorLightPrimary));
            View wizard = getLayoutInflater().inflate(R.layout.activity_tuner, null);
            dynamicContent.addView(wizard);

            RadioGroup rg=(RadioGroup)findViewById(R.id.radioGroup1);
            RadioButton rb=(RadioButton)findViewById(R.id.tuner);
        } else {
            dynamicContent = (LinearLayout)  findViewById(R.id.dynamicContent);
            bottonNavBar= (LinearLayout) findViewById(R.id.bottonNavBar);
            RadioGroup radiogroup = (RadioGroup) bottonNavBar.getChildAt(0);
            radiogroup.getChildAt(1).setBackgroundColor(getResources().getColor(R.color.colorLightPrimaryClicked));
            radiogroup.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.colorLightPrimary));
            View wizard = getLayoutInflater().inflate(R.layout.activity_tuner_dark, null);
            dynamicContent.addView(wizard);

            RadioGroup rg=(RadioGroup)findViewById(R.id.radioGroup1);
            RadioButton rb=(RadioButton)findViewById(R.id.tuner);
        }

        ActivityCompat.requestPermissions(this, permissions,
                RECORD_PERMISSION);

        wireWidgets();
        setSupportActionBar(toolbar);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        startTuner1();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(TunerActivity.this);
                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setTitle("Choose a tuner...")
                        .setItems(tunersArray, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 1) {
                                    tuner1 = 3;
                                    tunersSetUp(tuner1);
                                } else if (which == 0) {
                                    tuner1 = 1;
                                    tunersSetUp(tuner1);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(TunerActivity.this);
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
                                tunerThread.interrupt();
                                Intent restart = new Intent(getBaseContext(), TunerActivity.class);
                                finish();
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
                                tunerThread.interrupt();
                                Intent restart = new Intent(getBaseContext(), TunerActivity.class);
                                finish();
                                startActivity(restart);
                            }
                        });
            }
            AlertDialog dialog = builder.create();
            dialog.show();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
        tuning = position;
        setUpDisplay();

        // Showing selected spinner item
        if (tuner1 == 3) {
            Toast.makeText(TunerActivity.this, "" + item, Toast.LENGTH_LONG).show();
        }
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onClick(View v) {
        // Perform action on click
        switch(v.getId()) {
            case R.id.button1:
                display.setText(button1.getText());
                button1.setBackgroundColor(Color.BLUE);
                reset(1);
                tuningF = getNoteFrequency(1, tuning);
                tuningIndex = 1;
                break;
            case R.id.button2:
                display.setText(button2.getText());
                button2.setBackgroundColor(Color.BLUE);
                reset(2);
                tuningF = getNoteFrequency(2, tuning);
                tuningIndex = 2;
                break;
            case R.id.button3:
                display.setText(button3.getText());
                button3.setBackgroundColor(Color.BLUE);
                reset(3);
                tuningF = getNoteFrequency(3, tuning);
                tuningIndex = 3;
                break;
            case R.id.button4:
                display.setText(button4.getText());
                button4.setBackgroundColor(Color.BLUE);
                reset(4);
                tuningF = getNoteFrequency(4, tuning);
                tuningIndex = 4;
                break;
            case R.id.button5:
                display.setText(button5.getText());
                button5.setBackgroundColor(Color.BLUE);
                reset(5);
                tuningF = getNoteFrequency(5, tuning);
                tuningIndex = 5;
                break;
            case R.id.button6:
                display.setText(button6.getText());
                button6.setBackgroundColor(Color.BLUE);
                reset(6);
                tuningF = getNoteFrequency(6, tuning);
                tuningIndex = 6;
                break;
        }
    }

    private void wireWidgets() {
        differenceDisplay = findViewById(R.id.speedView);
        tuneProgressBar = findViewById(R.id.tuneProgressBar);
        cr_scale = findViewById(R.id.cr_image);
        octive = findViewById(R.id.octiveDisplay);
        pointer = findViewById(R.id.imageView_arrow);
        notePicker = findViewById(R.id.notePicker);
        setUpNotePicker();
        toolbar = findViewById(R.id.toolbar);
        navigation = findViewById(R.id.navigation);
        spinner = findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        display = findViewById(R.id.display);
        notesDisplay = findViewById(R.id.noteDisplay);
        button1 = findViewById(R.id.button1);
        button1.setOnClickListener(this);
        button2 = findViewById(R.id.button2);
        button2.setOnClickListener(this);
        button3 = findViewById(R.id.button3);
        button3.setOnClickListener(this);
        button4 = findViewById(R.id.button4);
        button4.setOnClickListener(this);
        button5 = findViewById(R.id.button5);
        button5.setOnClickListener(this);
        button6 = findViewById(R.id.button6);
        button6.setOnClickListener(this);
    }

    private void setUpNotePicker() {
        notes.add("C");
        notes.add("C♯");
        notes.add("D");
        notes.add("E♭");
        notes.add("E");
        notes.add("F");
        notes.add("F♯");
        notes.add("G");
        notes.add("G♯");
        notes.add("A");
        notes.add("B♭");
        notes.add("B");
        notePicker.setData(notes);
        notePicker.setCyclic(true);
        notePicker.setCurved(true);
        notePicker.setIndicator(true);
    }

    private void startTuner1() {
        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                final float pitchInHz = result.getPitch();
                final int selectedNoteIndex = notePicker.getCurrentItemPosition();
                final String selectedNoteName = notes.get(selectedNoteIndex);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //display.setText("" + findNote(pitchInHz));
                            if (pitchTimer == 3) {
                                lastPitch = pitchInHz;
                                pitchTimer = 0;
                            }
                            if (pitchInHz == -1 || (pitchInHz-lastPitch)>= pitchInHz/2) {
                                pitchTimer++;
                            }

                            if (tuner1 == 1) {
                                int diff = findScaledDiff(roundPitch(lastPitch, pitchInHz));
                                setDisplay(diff);
                                //displayNote.setText(findNote(roundPitch(lastPitch, pitchInHz)));
                                octive.setText("" + getOctive(pitchInHz));
                                cr_scale.setRotation(getRotationAngle(findNote(roundPitch(lastPitch, pitchInHz))));
                                if (Math.abs(findScaledDiff(roundPitch(lastPitch, pitchInHz))) == 0 && findScaledDiff(roundPitch(lastPitch, pitchInHz))!=-10
                                        && progressBarTimer < 40) {
                                    tuneProgressBar.speedPercentTo(progressBarTimer*10);
                                    progressBarTimer++;
                                    if (tuneProgressBar.getCurrentSpeed() >= 90) {
                                        Toast.makeText(TunerActivity.this, "In tune! "+ findNote(roundPitch(lastPitch, pitchInHz)), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    tuneProgressBar.speedPercentTo(0, 1000);
                                    progressBarTimer = 1;
                                }
                                //Log.d("tag", "" + findScaledDiff(roundPitch(lastPitch, pitchInHz)));

                            } else if (tuner1 == 2) {
                                //display.setText("" + findNote(pitchInHz));
                                int diff = findScaledDiff2(roundPitch(lastPitch, pitchInHz), selectedNoteName);
                                setDisplay2(diff);
                                if (Math.abs(findScaledDiff2(roundPitch(lastPitch, pitchInHz), selectedNoteName)) <= 1
                                        && progressBarTimer < 50) {
                                    tuneProgressBar.speedPercentTo(progressBarTimer*6);
                                    progressBarTimer++;
                                    if (tuneProgressBar.getCurrentSpeed() >= 90) {
                                        Toast.makeText(TunerActivity.this, "In tune! "+ findNote(roundPitch(lastPitch, pitchInHz)), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    tuneProgressBar.speedPercentTo(0, 1000);
                                    progressBarTimer = 1;
                                }
                                //Log.d("tag", "" + findScaledDiff2(roundPitch(lastPitch, pitchInHz)));
                            } else {
                                int diff = findScaledDiff3(roundPitch(lastPitch, pitchInHz));
                                setDisplay2(diff);
                                //displayNote.setText(findNote(roundPitch(lastPitch, pitchInHz)));
                                if (Math.abs(findScaledDiff3(roundPitch(lastPitch, pitchInHz))) <= 1
                                        && progressBarTimer < 50) {
                                    tuneProgressBar.speedPercentTo(progressBarTimer*6);
                                    progressBarTimer++;
                                    if (tuneProgressBar.getCurrentSpeed() >= 90) {
                                        Toast.makeText(TunerActivity.this, "In tune! "+ findNote(roundPitch(lastPitch, pitchInHz)), Toast.LENGTH_SHORT).show();
                                        tuned[tuningIndex-1] = true;
                                    }
                                } else {
                                    tuneProgressBar.speedPercentTo(0, 1000);
                                    progressBarTimer = 1;
                                }
                            }
                        }
                    });
            }
        };
        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(p);
        tunerThread = new Thread(dispatcher,"Audio Dispatcher");
        tunerThread.start();
    }

    private void setDisplay(int diff) {
        if (diff == -10) {
            differenceDisplay.speedPercentTo(50);
        } else if (Math.abs(diff) == 0) {
            differenceDisplay.speedPercentTo(50, 300);
        } else if (Math.abs(diff) <= 1) {
            if (diff > 0) {
                differenceDisplay.speedPercentTo(60, 300);
            } else {
                differenceDisplay.speedPercentTo(40, 300);
            }
        } else if (Math.abs(diff) <= 2) {
            if (diff > 0) {
                differenceDisplay.speedPercentTo(70, 300);
            } else {
                differenceDisplay.speedPercentTo(30, 300);
            }
        } else if (Math.abs(diff) <= 3) {
            if (diff > 0) {
                differenceDisplay.speedPercentTo(80, 300);
            } else {
                differenceDisplay.speedPercentTo(20, 300);
            }
        } else {
            if (diff > 0) {
                differenceDisplay.speedPercentTo(90, 300);
            } else {
                differenceDisplay.speedPercentTo(10, 300);
            }
        }
    }

    private void setDisplay2(int diff) {
        if (Math.abs(diff) <= 1) {
            differenceDisplay.speedPercentTo(50, 300);
        } else if (Math.abs(diff) <= 5) {
            if (diff > 0) {
                differenceDisplay.speedPercentTo(60, 300);
            } else {
                differenceDisplay.speedPercentTo(40, 300);
            }
        } else if (Math.abs(diff) <= 10) {
            if (diff > 0) {
                differenceDisplay.speedPercentTo(70, 300);
            } else {
                differenceDisplay.speedPercentTo(30, 300);
            }
        } else if (Math.abs(diff) <= 30) {
            if (diff > 0) {
                differenceDisplay.speedPercentTo(80, 300);
            } else {
                differenceDisplay.speedPercentTo(20, 300);
            }
        } else {
            if (diff > 0) {
                differenceDisplay.speedPercentTo(90, 300);
            } else {
                differenceDisplay.speedPercentTo(10, 300);
            }
        }
    }

    private String getOctive(double f) {
        double minDifference = 10;
        int index = 0;
        if (f!= -1) {
            for (int i = 0; i < noteFrequencies.length; i++) {
                if (Math.abs(f - noteFrequencies[i]) <= minDifference) {
                    minDifference = Math.abs(f - noteFrequencies[i]);
                    index = i;
                }
            }
            if (index <= 11) {
                return "8";
            } else if (index <= 11+12) {
                return "7";
            } else if (index <= 11+12*2) {
                return "6";
            } else if (index <= 11+12*3) {
                return "5";
            } else if (index <= 11+12*4) {
                return "4";
            } else if (index <= 11+12*5) {
                return "3";
            } else if (index <= 11+12*6) {
                return "2";
            } else if (index <= 11+12*7) {
                return "1";
            }
        }
        return " ";
    }

    private float getRotationAngle(String note) {
        if (note.equals("C")) {
            return 0;
        } else if (note.equals("C♯")) {
            return -30;
        } else if (note.equals("D")) {
            return -60;
        } else if (note.equals("E♭")) {
            return -90;
        } else if (note.equals("E")) {
            return -120;
        } else if (note.equals("F")) {
            return -150;
        } else if (note.equals("F♯")) {
            return 180;
        } else if (note.equals("G")) {
            return 150;
        } else if (note.equals("G♯")) {
            return 120;
        } else if (note.equals("A")) {
            return 90;
        } else if (note.equals("B♭")) {
            return 60;
        } else {
            return 30;
        }
    }

    private double roundPitch(double lastf, double thisf) {
        if (thisf == -1 && lastPitch != -1) {
            return lastf;
        }
        return thisf;
    }

    private String findNote(double frequency) {
        double minDifference = 10;
        int index = 0;
        if (frequency != -1) {
            for (int i = 0; i < noteFrequencies.length; i++) {
                if (Math.abs(frequency - noteFrequencies[i]) <= minDifference) {
                    minDifference = Math.abs(frequency - noteFrequencies[i]);
                    index = i;
                }
            }
            return getNoteName(index);
        } else {
            return " ";
        }
    }

    private int findScaledDiff(double frequency) {  // percent off from a scale of 1 to 10
        double minDifference = 100;
        int index = 0;

        if (frequency != -1) {
            for (int i = 0; i < noteFrequencies.length; i++) {
                if (Math.abs(frequency - noteFrequencies[i]) < minDifference) {
                    minDifference = Math.abs(frequency - noteFrequencies[i]);
                    index = i;
                }
            }
            if (index > 0) {
                double nextFrequency = noteFrequencies[index-1];
                double thisFrequency = noteFrequencies[index];
                double lastFrequency = noteFrequencies[index+1];
                if (frequency >= lastFrequency && frequency <= thisFrequency) {
                    double diff = thisFrequency - lastFrequency;
                    double scaleSection = diff / 5;
                    int n = 0;
                    while (Math.abs(thisFrequency - n*scaleSection - frequency) > scaleSection) {
                        thisFrequency -= n*scaleSection;
                        n++;
                    }
                    return n;
                } else if (frequency >= thisFrequency && frequency <= nextFrequency) {
                    double diff = nextFrequency - thisFrequency;
                    double scaleSection = diff / 5;
                    int n = 0;
                    while (Math.abs(thisFrequency + n*scaleSection - frequency) > scaleSection) {
                        thisFrequency += n*scaleSection;
                        n++;
                    }
                    return -n;
                }
            }
        }
        return -10;

    }

    private int findScaledDiff2(double frequency, String note) {
        double[] notes = getNoteFrequency(note);
        double minDiff = 10000;
        int index = 0;
        for (int i = 0; i < notes.length; i++) {
            if (i == 0) {
                minDiff = Math.abs(frequency-notes[0]);
            } else {
                if (Math.abs(frequency-notes[i]) < minDiff) {
                    minDiff = Math.abs(frequency-notes[i]);
                    index = i;
                }
            }
        }
        double targetF = notes[index];
        return (int)(frequency - targetF);
    }

    private String getNoteName(int index) {
        if (index%12 == 11) {
            return "C";
        } else if (index%12 == 10) {
            return "C♯";
        } else if (index%12 == 9) {
            return "D";
        } else if (index%12 == 8) {
            return "E♭";
        } else if (index%12 == 7) {
            return "E";
        } else if (index%12 == 6) {
            return "F";
        } else if (index%12 == 5) {
            return "F♯";
        } else if (index%12 == 4) {
            return "G";
        } else if (index%12 == 3) {
            return "G♯";
        } else if (index%12 == 2) {
            return "A";
        } else if (index%12 == 1) {
            return "B♭";
        } else {
            return "B";
        }
    }

    private double[] getNoteFrequency(String note) {
        if (note.equals("B")) {
            return createNoteFArray(0);
        } else if (note.equals("B♭")) {
            return createNoteFArray(1);
        } else if (note.equals("A")) {
            return createNoteFArray(2);
        } else if (note.equals("G♯")) {
            return createNoteFArray(3);
        } else if (note.equals("G")) {
            return createNoteFArray(4);
        } else if (note.equals("F♯")) {
            return createNoteFArray(5);
        } else if (note.equals("F")) {
            return createNoteFArray(6);
        } else if (note.equals("E")) {
            return createNoteFArray(7);
        } else if (note.equals("E♭")) {
            return createNoteFArray(8);
        } else if (note.equals("D")) {
            return createNoteFArray(9);
        } else if (note.equals("C♯")) {
            return createNoteFArray(10);
        } else {
            return createNoteFArray(11);
        }
    }

    private double[] createNoteFArray(int a) {
        return new double[] {noteFrequencies[a], noteFrequencies[a+12], noteFrequencies[a+12*2], noteFrequencies[a+12*3],
                noteFrequencies[a+12*4], noteFrequencies[a+12*5], noteFrequencies[a+12*6],
                noteFrequencies[a+12*7], noteFrequencies[a+12*8]};
    }

    private void setUpDisplay() {
        if (tuning == 0) {
            button1.setText("E");
            button2.setText("A");
            button3.setText("D");
            button4.setText("G");
            button5.setText("B");
            button6.setText("E");
        } else if (tuning == 1) {
            button1.setText("D");
            button2.setText("G");
            button3.setText("D");
            button4.setText("G");
            button5.setText("B");
            button6.setText("D");
        } else if (tuning == 2) {
            button1.setText("D");
            button2.setText("A");
            button3.setText("D");
            button4.setText("F♯");
            button5.setText("A");
            button6.setText("D");
        } else if (tuning == 3) {
            button1.setText("D");
            button2.setText("A");
            button3.setText("D");
            button4.setText("G");
            button5.setText("B");
            button6.setText("E");
        } else if (tuning == 4) {
            button1.setText("E");
            button2.setText("B");
            button3.setText("E");
            button4.setText("G♯");
            button5.setText("B");
            button6.setText("E");
        } else if (tuning == 5) {
            button1.setText("D♯");
            button2.setText("G♯");
            button3.setText("C♯");
            button4.setText("F♯");
            button5.setText("A♯");
            button6.setText("D♯");
        } else if (tuning == 6) {
            button1.setText("D");
            button2.setText("A");
            button3.setText("D");
            button4.setText("G");
            button5.setText("A");
            button6.setText("D");
        }
        display.setText(" ");
        button1.setBackgroundColor(Color.LTGRAY);
        button2.setBackgroundColor(Color.LTGRAY);
        button3.setBackgroundColor(Color.LTGRAY);
        button4.setBackgroundColor(Color.LTGRAY);
        button5.setBackgroundColor(Color.LTGRAY);
        button6.setBackgroundColor(Color.LTGRAY);
        tuned = new boolean[] {false, false, false, false, false, false};
    }

    public void reset(int index) {
        for (int i = 1; i <= 6; i++) {
            Button a = (Button)(notesDisplay.getChildAt(i));
            if (i != index) {
                if (!tuned[i-1]) {
                    a.setBackgroundColor(Color.LTGRAY);
                } else {
                    a.setBackgroundColor(Color.GREEN);
                }
            } else {
                if (tuned[i-1]) {
                    a.setBackgroundColor(Color.GREEN);
                }
            }
        }
    }

    private int findScaledDiff3(double frequency) {
        if (frequency != -1) {
            return (int)(frequency - tuningF);
        } else {
            return -10;
        }
    }

    private double getNoteFrequency(int index, int tuning) {
        if (tuning == 0) {
            if (index == 1) {return tuning0[0];}
            if (index == 2) {return tuning0[1];}
            if (index == 3) {return tuning0[2];}
            if (index == 4) {return tuning0[3];}
            if (index == 5) {return tuning0[4];}
            if (index == 6) {return tuning0[5];}
        } else if (tuning == 1) {
            if (index == 1) {return tuning1[0];}
            if (index == 2) {return tuning1[1];}
            if (index == 3) {return tuning1[2];}
            if (index == 4) {return tuning1[3];}
            if (index == 5) {return tuning1[4];}
            if (index == 6) {return tuning1[5];}
        } else if (tuning == 2) {
            if (index == 1) {return tuning2[0];}
            if (index == 2) {return tuning2[1];}
            if (index == 3) {return tuning2[2];}
            if (index == 4) {return tuning2[3];}
            if (index == 5) {return tuning2[4];}
            if (index == 6) {return tuning2[5];}
        } else if (tuning == 3) {
            if (index == 1) {return tuning3[0];}
            if (index == 2) {return tuning3[1];}
            if (index == 3) {return tuning3[2];}
            if (index == 4) {return tuning3[3];}
            if (index == 5) {return tuning3[4];}
            if (index == 6) {return tuning3[5];}
        } else if (tuning == 4) {
            if (index == 1) {return tuning4[0];}
            if (index == 2) {return tuning4[1];}
            if (index == 3) {return tuning4[2];}
            if (index == 4) {return tuning4[3];}
            if (index == 5) {return tuning4[4];}
            if (index == 6) {return tuning4[5];}
        } else {
            if (index == 1) {return tuning5[0];}
            if (index == 2) {return tuning5[1];}
            if (index == 3) {return tuning5[2];}
            if (index == 4) {return tuning5[3];}
            if (index == 5) {return tuning5[4];}
            if (index == 6) {return tuning5[5];}
        }
        return 0;
    }

    private void tunersSetUp(int tuner1) {
        if (tuner1 == 1) {
            navigation.setVisibility(View.VISIBLE);
            navigation.setEnabled(true);

            cr_scale.setVisibility(View.VISIBLE);
            pointer.setVisibility(View.VISIBLE);
            octive.setVisibility(View.VISIBLE);

            notePicker.setVisibility(View.INVISIBLE);

            spinner.setVisibility(View.INVISIBLE);
            spinner.setEnabled(false);
            display.setVisibility(View.INVISIBLE);
            notesDisplay.setVisibility(View.INVISIBLE);
        } else if (tuner1 == 2) {
            navigation.setVisibility(View.VISIBLE);
            navigation.setEnabled(true);

            cr_scale.setVisibility(View.INVISIBLE);
            pointer.setVisibility(View.INVISIBLE);
            octive.setVisibility(View.INVISIBLE);

            notePicker.setVisibility(View.VISIBLE);

            spinner.setVisibility(View.INVISIBLE);
            spinner.setEnabled(false);
            display.setVisibility(View.INVISIBLE);
            notesDisplay.setVisibility(View.INVISIBLE);
        } else {
            navigation.setVisibility(View.INVISIBLE);
            navigation.setEnabled(false);

            cr_scale.setVisibility(View.INVISIBLE);
            pointer.setVisibility(View.INVISIBLE);
            octive.setVisibility(View.INVISIBLE);

            notePicker.setVisibility(View.INVISIBLE);

            spinner.setVisibility(View.VISIBLE);
            spinner.setEnabled(true);
            display.setVisibility(View.VISIBLE);
            notesDisplay.setVisibility(View.VISIBLE);
        }
    }

}
