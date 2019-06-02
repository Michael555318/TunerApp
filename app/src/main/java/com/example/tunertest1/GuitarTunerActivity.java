package com.example.tunertest1;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.github.anastr.speedviewlib.ImageLinearGauge;
import com.github.anastr.speedviewlib.SpeedView;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

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

public class GuitarTunerActivity extends MainActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    LinearLayout dynamicContent,bottonNavBar;
    android.support.v7.widget.Toolbar toolbar;

    private Spinner spinner;
    private ImageLinearGauge tuneProgressBar;
    private TextView display;
    private LinearLayout notesDisplay;
    private SpeedView differenceDisplay;

    private Button button1, button2, button3, button4, button5, button6;
    private int tuning = 0;
    Thread tunerThread;
    double tuningF = 0;
    int tuningIndex = 0;
    boolean[] tuned = new boolean[] {false, false, false, false, false, false};
    boolean stop = false;
    PitchDetectionHandler pdh;

    private int progressBarTimer = 1;
    private float lastPitch;
    private int pitchTimer = 3;

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

        setUpDisplay();

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
                                    tunerThread.interrupt();
                                    stop = true;
                                    pdh = null;
                                    Intent startTuner = new Intent(getBaseContext(), TunerActivity.class);
                                    startActivity(startTuner);
                                    finish();
                                }
                            }
                        });
                // 3. Get the <code><a href="/reference/android/app/AlertDialog.html">AlertDialog</a></code> from <code><a href="/reference/android/app/AlertDialog.Builder.html#create()">create()</a></code>
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                final float pitchInHz = result.getPitch();
                if (!stop) {
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
                            if (Math.abs(findScaledDiff(roundPitch(lastPitch, pitchInHz))) <= 1
                                    && progressBarTimer < 50) {
                                tuneProgressBar.speedPercentTo(progressBarTimer*6);
                                progressBarTimer++;
                                if (tuneProgressBar.getCurrentSpeed() >= 90) {
                                    Toast.makeText(GuitarTunerActivity.this, "In tune! "+ findNote(roundPitch(lastPitch, pitchInHz)), Toast.LENGTH_SHORT).show();
                                    tuned[tuningIndex-1] = true;
                                }
                            } else {
                                tuneProgressBar.speedPercentTo(0, 1000);
                                progressBarTimer = 1;
                            }
                            Log.d("guitar", "" + pitchInHz);
                        }
                    });
                }
            }
        };
        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(p);
        tunerThread = new Thread(dispatcher,"Audio Dispatcher");
        tunerThread.start();
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
        spinner.setOnItemSelectedListener(this);
        display = findViewById(R.id.display);
        notesDisplay = findViewById(R.id.noteDisplay);
        differenceDisplay = findViewById(R.id.speedView);
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
        tuning = position;
        setUpDisplay();

        // Showing selected spinner item
        Toast.makeText(GuitarTunerActivity.this, "" + item, Toast.LENGTH_LONG).show();
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

    private void setDisplay(int diff) {
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

    private double roundPitch(double lastf, double thisf) {
        if (thisf == -1 && lastPitch != -1) {
            return lastf;
        }
        return thisf;
    }

    private int findScaledDiff(double frequency) {
        if (frequency != -1) {
            return (int)(frequency - tuningF);
        } else {
            return -10;
        }
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

}
