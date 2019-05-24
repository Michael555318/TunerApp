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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import static com.example.tunertest1.Config.lightThemed;
import static com.example.tunertest1.Config.noteFrequencies;
import static com.example.tunertest1.Config.themesArray;
import static com.example.tunertest1.Config.tunersArray;

public class TunerActivity extends MainActivity {
    //todo: finish menu and stuff: https://developer.android.com/guide/topics/ui/menus.html
    //todo: test tuners and work on guitar tuner (new activities)

    // Widgets
    private SpeedView differenceDisplay;
    private TextView displayNote;
    private ImageLinearGauge tuneProgressBar;
    private ImageView cr_scale;
    private TextView octive;
    private ImageView pointer;
    private Toolbar toolbar;
    private BottomNavigationView navigation;
    private View container;

    LinearLayout dynamicContent,bottonNavBar;

    private WheelPicker notePicker;

    // Tools
    private int progressBarTimer = 1;
    private float lastPitch;
    private int pitchTimer = 3;
    public final static ArrayList<String> notes = new ArrayList<>();

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
                    startTuner1();
                    return true;
                case R.id.navigation_dashboard:
                    endTuner1();
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
                                // The 'which' argument contains the index position
                                // of the selected item
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
                                Intent restart = new Intent(TunerActivity.this, TunerActivity.class);
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
                                Intent restart = new Intent(TunerActivity.this, TunerActivity.class);
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
        differenceDisplay = findViewById(R.id.speedView);
        displayNote = findViewById(R.id.textiew_displayNote);
        tuneProgressBar = findViewById(R.id.tuneProgressBar);
        cr_scale = findViewById(R.id.cr_image);
        octive = findViewById(R.id.octiveDisplay);
        pointer = findViewById(R.id.imageView_arrow);
        notePicker = findViewById(R.id.notePicker);
        setUpNotePicker();
        toolbar = findViewById(R.id.toolbar);
        navigation = findViewById(R.id.navigation);
        container = findViewById(R.id.container);
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
        cr_scale.setVisibility(View.VISIBLE);
        pointer.setVisibility(View.VISIBLE);
        octive.setVisibility(View.VISIBLE);

        notePicker.setVisibility(View.INVISIBLE);

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);
        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                final float pitchInHz = result.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //display.setText("" + findNote(pitchInHz));
                        if (pitchTimer == 3) {
                            lastPitch = pitchInHz;
                            pitchTimer = 0;
                        }
                        if (pitchInHz == -1) {
                            pitchTimer++;
                        }
                        int diff = findScaledDiff(roundPitch(lastPitch, pitchInHz));
                        setDisplay(diff);
                        //displayNote.setText(findNote(roundPitch(lastPitch, pitchInHz)));
                        octive.setText("" + getOctive(pitchInHz));
                        cr_scale.setRotation(getRotationAngle(findNote(roundPitch(lastPitch, pitchInHz))));
                        if (Math.abs(findScaledDiff(roundPitch(lastPitch, pitchInHz))) <= 1
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
                        //Log.d("tag", "" + findScaledDiff(roundPitch(lastPitch, pitchInHz)));
                    }
                });
            }
        };
        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(p);
        new Thread(dispatcher,"Audio Dispatcher").start();
    }

    private void endTuner1() {
        cr_scale.setVisibility(View.INVISIBLE);
        pointer.setVisibility(View.INVISIBLE);
        octive.setVisibility(View.INVISIBLE);

        notePicker.setVisibility(View.VISIBLE);

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);
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
                        if (pitchInHz == -1) {
                            pitchTimer++;
                        }
                        int diff = findScaledDiff2(roundPitch(lastPitch, pitchInHz), selectedNoteName);
                        setDisplay2(diff);
                        if (Math.abs(findScaledDiff2(roundPitch(lastPitch, pitchInHz), selectedNoteName)) <= 3
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
                        Log.d("tag", "" + selectedNoteName);
                    }
                });
            }
        };
        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(p);
        new Thread(dispatcher,"Audio Dispatcher").start();
    }

    private void setDisplay(int diff) {
        if (Math.abs(diff) <= 1) {
            differenceDisplay.speedPercentTo(50, 300);
        } else if (Math.abs(diff) <= 2) {
            if (diff > 0) {
                differenceDisplay.speedPercentTo(60, 300);
            } else {
                differenceDisplay.speedPercentTo(40, 300);
            }
        } else if (Math.abs(diff) <= 3) {
            if (diff > 0) {
                differenceDisplay.speedPercentTo(70, 300);
            } else {
                differenceDisplay.speedPercentTo(30, 300);
            }
        } else if (Math.abs(diff) <= 4) {
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
        if (Math.abs(diff) <= 3) {
            differenceDisplay.speedPercentTo(50, 300);
        } else if (Math.abs(diff) <= 10) {
            if (diff > 0) {
                differenceDisplay.speedPercentTo(60, 300);
            } else {
                differenceDisplay.speedPercentTo(40, 300);
            }
        } else if (Math.abs(diff) <= 50) {
            if (diff > 0) {
                differenceDisplay.speedPercentTo(70, 300);
            } else {
                differenceDisplay.speedPercentTo(30, 300);
            }
        } else if (Math.abs(diff) <= 100) {
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
        double minDifference = 10;
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
                    int n = 1;
                    while (thisFrequency - n*scaleSection > frequency) {
                        thisFrequency -= n*scaleSection;
                        n++;
                    }
                    return +n;
                } else if (frequency >= thisFrequency && frequency <= nextFrequency) {
                    double diff = nextFrequency - thisFrequency;
                    double scaleSection = diff / 5;
                    int n = 1;
                    while (thisFrequency + n*scaleSection < frequency) {
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

}
