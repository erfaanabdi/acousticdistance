package com.example.erfan.acousticdistance;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by erfan on 5/25/16.
 */
public class GenerateTone {
    private double duration = 3; // seconds
    private final double sampleRate = 44100;
    private double numSamples;
    private final double sample[] = new double[150000*8];
    private int freqOfTone;// = 15000; // hz
    private final byte generatedSnd[] = new byte[300000*8];
    private int counter=0;

    Handler handler = new Handler();

    protected void createArraySound(final double d[],final double freq[]){
        if(counter==d.length) {
          //  SharedVar.inTransmission=0;
            counter=0;
            return;
        }
        createSound(d[counter],freq[counter]);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                counter++;
                createArraySound(d, freq);
            }
        }, (long) (d[counter] * 1000));

    }
    protected void createSound(double d,double freq) {
        numSamples=(int)(d*sampleRate);
        freqOfTone=(int)freq;
        // Use a new tread as this can take a while
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                genTone();
                handler.post(new Runnable() {

                    public void run() {
                        playSound();
                    }
                });
            }
        });

        thread.start();
    }

    void genTone(){
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {      // Fill the sample array
            sample[i] = Math.sin(freqOfTone * 2 * Math.PI * i / (sampleRate));
        }
        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.


        int idx = 0;
        int i;

        int ramp = (int)numSamples / 20;                                    // Amplitude ramp as a percent of sample count


        for (i = 0; i < ramp; ++i) {                                     // Ramp amplitude up (to avoid clicks)
            // Ramp up to maximum
            final short val = (short) ((sample[i] * 32767 * i / ramp));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }


        for (i = ramp; i < numSamples - ramp; ++i) {                        // Max amplitude for most of the samples
            // scale to maximum amplitude
            final short val = (short) ((sample[i] * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        for (i = ((int)numSamples - ramp); i < numSamples; ++i) {                               // Ramp amplitude down
            // Ramp down to zero
            final short val = (short) ((sample[i] * 32767 * (numSamples - i) / ramp));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

    }
    void playSound(){
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                (int)sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, 2*(int)numSamples,
                AudioTrack.MODE_STATIC);

        audioTrack.write(generatedSnd, 0, 2*(int) numSamples);
        audioTrack.play();

    }
}
