package com.example.tunertest1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import static com.example.tunertest1.Config.noteFrequencies;

public class TunerActivity extends AppCompatActivity {
    //todo: finish menu and stuff: https://developer.android.com/guide/topics/ui/menus.html

    // Widgets
    private SpeedView differenceDisplay;
    private TextView displayNote;
    private ImageLinearGauge tuneProgressBar;
    private ImageView cr_scale;
    private TextView octive;
    private ImageView pointer;
    private Toolbar toolbar;

    private WheelPicker notePicker;

    // Tools
    private int progressBarTimer = 1;
    private float lastPitch;
    private int pitchTimer = 3;
    AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);
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
                    startTuner1(dispatcher);
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
        setContentView(R.layout.activity_tuner);

        ActivityCompat.requestPermissions(this, permissions,
                RECORD_PERMISSION);

        wireWidgets();
        setSupportActionBar(toolbar);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        startTuner1(dispatcher);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tuner_menu, menu);
        return true;
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

    private void startTuner1(AudioDispatcher dispatcher) {
        cr_scale.setVisibility(View.VISIBLE);
        pointer.setVisibility(View.VISIBLE);
        octive.setVisibility(View.VISIBLE);

        notePicker.setVisibility(View.INVISIBLE);

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
                        double diff = findScaledDiff(roundPitch(lastPitch, pitchInHz));
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
    }

    private void setDisplay(double diff) {
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

}
