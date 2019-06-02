package com.example.tunertest1;

import java.util.ArrayList;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;

public class Config {

    public final static int RECORD_PERMISSION = 100;

    public final static double[] noteFrequencies = new double[]{
            // Total: 108   Index: 0 - 107    Rows(B): 0 - 0+12*8
            //  B       Bb        A        Gs       G       Fs       F       E        Eb        D        Cs       C
               7902,    7459,    7040,    6645,    6272,    5920, 5587.65, 5274.04, 4978.03, 4698.64, 4434.92, 4186.01,
            3951.07, 3729.31, 3520.00, 3322.44, 3135.96, 2959.96, 2793.83, 2637.02, 2489.02, 2349.32, 2217.46, 2093.00,
            1975.53, 1864.66, 1760.00, 1661.22, 1567.98, 1479.98, 1396.91, 1318.51, 1244.51, 1174.66, 1108.73, 1046.50,
            987.767, 932.328, 880.000, 830.609, 783.991, 739.989, 698.456, 659.255, 622.254, 587.330, 554.365, 523.251,
            493.883, 466.164, 440.000, 415.305, 391.995, 369.994, 349.228, 329.628, 311.127, 293.665, 277.183, 261.626,
            246.942, 233.082, 220.000, 207.652, 195.998, 184.997, 174.614, 164.814, 155.563, 146.832, 138.591, 130.813,
            123.471, 116.541, 110.000, 103.826, 97.9989, 92.4986, 87.3071, 82.4069, 77.7817, 73.4162, 69.2957, 65.4064,
            61.7354, 58.2705, 55.0000, 51.9131, 48.9994, 46.2493, 43.6535, 41.2034, 38.8909, 36.7081, 34.6478, 32.7032,
            30.8677, 29.1352, 27.5000, 25.9565, 24.4997, 23.1247, 21.8268, 20.6017, 19.4454, 18.3540, 17.3239, 16.3516};

    public static final double[] tuning0 = new double[] {82.4069, 110.000, 146.832, 195.998, 246.942, 329.628};
    public static final double[] tuning1 = new double[] {73.4162, 97.9989, 146.832, 195.998, 246.942, 293.665};
    public static final double[] tuning2 = new double[] {73.4162, 110.000, 146.832, 184.997, 220.000, 293.665};
    public static final double[] tuning3 = new double[] {73.4162, 110.000, 146.832, 195.998, 246.942, 329.628};
    public static final double[] tuning4 = new double[] {82.4069, 123.471, 164.814, 207.652, 246.942, 329.628};
    public static final double[] tuning5 = new double[] {77.7817, 103.826, 138.591, 184.997, 233.082, 311.127};

    public static final String[] tunersArray = new String[] {"Chromatic Tuner", "Guitar tuner"};

    public static final String[] themesArray = new String[] {"Light theme", "Dark Theme"};

    public static final String[] guitarTuningArray = new String[] {"Standard: E A D G B E", "Open G: D G D G B D", "Open D: D A D F♯ A D",
            "Drop D: D A D G B E", "Open E: E B E G♯ B E", "Half Step Down: D♯, G♯, C♯, F♯, A♯, D♯"};

    public static AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

    public static boolean lightThemed = true;

    //https://en.wikipedia.org/wiki/List_of_guitar_tunings

}
