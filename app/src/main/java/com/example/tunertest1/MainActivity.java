package com.example.tunertest1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.math3.complex.Complex;

import java.io.IOException;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    // Instance Variables
    Button recordButton;
    TextView display;
    MediaRecorder recorder;
    private double t = 0;
    private int counter = 0;
    boolean mStartRecording = true;
    Complex[] amplitudeFunction = new Complex[SAMPLE_AMOUNT];

    private final static double SAMPLE_TIME = 1;
    private final static int SAMPLE_AMOUNT = 50;
    private final static double INTERVAL = SAMPLE_TIME / SAMPLE_AMOUNT;

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


    // Constants
    final static int RECORD_PERMISSION = 100;
    final static String LOG_TAG = "Audio Test Prepare";
    private static String fileName = null;
    private static int AMP_REF = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.3gp";
        ActivityCompat.requestPermissions(this, permissions,
                RECORD_PERMISSION);

        // WireWidgets - record button and display textview
        recordButton = findViewById(R.id.button_main_record);
        display = findViewById(R.id.textView_main_display);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    recordButton.setText("Stop recording");
                    final Handler handler=new Handler();
                    handler.post(new Runnable(){
                        @Override
                        public void run() {
                            // upadte textView here
                            if (recorder != null) {
                                if (t <= SAMPLE_TIME) {
                                    int amplitude = recorder.getMaxAmplitude();
                                    amplitudeFunction[counter] = new Complex(t*INTERVAL, amplitude);
                                    t += INTERVAL;
                                    counter++;
                                } else {
                                    display.setText("" + determineFrequency(fft(amplitudeFunction)));
                                    reset();
                                }
                                //Log.i("AMPLITUDE", new Integer(amplitude).toString());
                            }
                            handler.postDelayed(this,(long)INTERVAL*1000); // set time here to refresh textView
                        }
                    });
                } else {
                    recordButton.setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        });

    }

    // Recording Methods
    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();;
        } else {
            stopRecording();
        }
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    private void displayAmplitude(final TextView displayer) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i == 0) {

                    try {
                        sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (recorder != null) {
                        int amplitude = recorder.getMaxAmplitude();

                        //Here you can put condition (low/high)
                        Log.i("AMPLITUDE", new Integer(amplitude).toString());
                        displayer.setText(new Integer(amplitude).toString());
                    }
                }
            }
        });
    }

    public static Complex[] fft(Complex[] x) {
        int N = x.length;

        // fft of even terms
        Complex[] even = new Complex[N / 2];
        for (int k = 0; k < N / 2; k++) {
            even[k] = x[2 * k];
        }
        Complex[] q = fft(even);

        // fft of odd terms
        Complex[] odd = even; // reuse the array
        for (int k = 0; k < N / 2; k++) {
            odd[k] = x[2 * k + 1];
        }
        Complex[] r = fft(odd);

        // combine
        Complex[] y = new Complex[N];
        for (int k = 0; k < N / 2; k++) {
            double kth = -2 * k * Math.PI / N;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k] = q[k].add(wk.multiply(r[k]));
            y[k + N / 2] = q[k].subtract(wk.multiply(r[k]));
        }
        return y;
    }

    private double determineFrequency(Complex[] a) {
        double maxF = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i].getReal() > maxF) {
                maxF = a[i].getReal();
            }
        }
        return maxF;
    }

    private void reset() {
        t = 0;
        counter = 0;
    }

}

