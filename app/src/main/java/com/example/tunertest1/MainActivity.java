package com.example.tunertest1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.anastr.speedviewlib.ImageLinearGauge;
import com.github.anastr.speedviewlib.SpeedView;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.exception.util.ExceptionContextProvider;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.io.IOException;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    public static RadioGroup radioGroup;
    public static RadioButton metronome;
    public static RadioButton tuner;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        radioGroup = findViewById(R.id.radioGroup1);
        metronome = findViewById(R.id.metronome);
        tuner = findViewById(R.id.tuner);

        Intent in=new Intent(getBaseContext(),TunerActivity.class);
        startActivity(in);
        overridePendingTransition(0, 0);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                Intent in;
                switch (checkedId)
                {
                    case R.id.metronome:
                        in=new Intent(getBaseContext(),MetronomeActivity.class);
                        finish();
                        startActivity(in);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.tuner:
//                        in = new Intent(getBaseContext(), TunerActivity.class);
//                        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        finishAffinity();
//                        finish();
//                        startActivity(in);

                        in = new Intent(getBaseContext(), TunerActivity.class);
                        int mPendingIntentId = 123456;
                        PendingIntent mPendingIntent = PendingIntent.getActivity(getBaseContext(), mPendingIntentId, in,
                                PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        System.exit(0);

                        break;
                    default:
                        break;
                }
            }
        });
    }

}

//  ♯   ♭