package com.example.erfan.acousticdistance;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Decoder {

    int audioSource = MediaRecorder.AudioSource.MIC;    // Audio source is the device MIC
    int channelConfig = AudioFormat.CHANNEL_IN_MONO;    // Recording in mono
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT; // Records in 16bit
    public MainActivity activity;
    int blockSize = 512;                               // deal with this many samples at a time
    private FFT fft=new FFT(blockSize);                           // The fft double array
    int sampleRate = 44100;                             // Sample rate in Hz
    public double frequency = 0.0;                      // the frequency given
    private boolean started=false;

    private int alen=25;
    private int c1[]=new int[alen];
    private int c2[]=new int[alen];
    private long t;
    int bufferSize;// = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioEncoding);                // Gets the minimum buffer needed
    AudioRecord audioRecord;// = new AudioRecord(audioSource, sampleRate, channelConfig, audioEncoding, bufferSize);   // The RAW PCM sample recording



    short[] buffer = new short[2*blockSize];          // Save the raw PCM samples as short bytes

    //  double[] audioDataDoubles = new double[(blockSize*2)]; // Same values as above, as doubles
    //   -----------------------------------------------
    double[] re = new double[blockSize];
    double[] im = new double[blockSize];
    int[] magnitude = new int[blockSize];
    private Thread recordingThread = null;



    private void startRecording(){

        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioEncoding);                // Gets the minimum buffer needed
        audioRecord = new AudioRecord(audioSource, sampleRate, channelConfig, audioEncoding, bufferSize);   // The RAW PCM sample recording

        audioRecord.startRecording();
        started = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                analyzeIt();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }


    private void stopRecording() {
        // stops the recording activity
        if (null != audioRecord) {
            started = false;
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            recordingThread = null;
        }
    }
    int rr=0,count71=0,count66=0,count7166=0,count62=0,count57=0,count6257=0;
    double avg1=0;
    double  tmp;
    double last10[]=new double[10];
    int cntl=0;
    private void analyzeIt() {
        while(started){

            int bufferReadResult = audioRecord.read(buffer, 0, 2*blockSize);

            for(int i = 0; i < blockSize && i < bufferReadResult; i++) {
                re[i]=(double) buffer[i*2] / 32768.0;
                im[i]=(double) buffer[i*2+1] / 32768.0;
            }
           fft.fft(re,im);
            for(int i = 0; i < blockSize; i++)
                magnitude[i] = (int)Math.sqrt((re[i] * re[i]) + (im[i]*im[i]));

        /*    String sss="";
            int xxx=0;
            for(int i=40;i<72;i++){
                sss+=Integer.toString(i) + "=" + Integer.toString(magnitude[i])+ " ";
                if(magnitude[i]<10)
                    sss+=" ";
                xxx+=magnitude[i];
            }
            if(xxx>5)
                Log.i("ssss",sss);
*//*
            if(2==2)
            continue;
*/
            if(c1[rr]!=0) {
                c1[rr]=0;
                avg1-=c1[rr];
                count71--;
            }
            if(c2[rr]!=0) {
                c2[rr]=0;
                count57--;
            }



            if(SharedVar.mode==0) {
                avg1+=magnitude[71];
                if (magnitude[71] > 1) {
                    c1[rr] = magnitude[71];
                    count71++;
                }
                if (count71 >= 20) {
                    for (int i = 0; i < alen; i++)
                        c1[i] = 0;
                    count71 = 0;
                    tmp=avg1/20;
                    for(int i=0;i<4;i++)
                        last10[i]=last10[i+1];
                    last10[4]=tmp;
                    double avg=0;
                    for(int i=0;i<5;i++)
                        avg+=last10[i];
                    Log.i("sss","xxxx    "+  Double.toString(tmp) + " " + Double.toString(avg/5));

                    if(tmp>2)
                    activity.findViewById(R.id.tv).post(new Runnable() {
                        public void run() {
                            activity.detected(2,0);
                          //  ((TextView)activity.findViewById(R.id.tv)).setText(Double.toString(tmp));
                        }
                    });
                    avg1=0;
                }
            }
            if(false && magnitude[71]>1) {

                String sss = "";
                int xxx=1;
                for (int i = rr;xxx>0 && i>=0;i--) {
                    xxx=c1[i];
                    sss += Integer.toString(c1[i]) + ",";
                }
                for (int i = alen-1;xxx>0 && i>rr;i--) {
                    xxx = c1[i];
                    sss += Integer.toString(c1[i]) + ",";
                }
                //Log.i("sss",sss);

            }
            if(SharedVar.mode==1) {
                if (magnitude[57] > 15) {
                    c2[rr]=1;
                    count57++;
                    if(count57==1)
                        t = System.currentTimeMillis();;

                }
                if (count57 >= 10) {
                    for (int i = 0; i < alen; i++)
                        c2[i] = 0;
                  count57= 0;


                    activity.findViewById(R.id.tv).post(new Runnable() {
                        public void run() {
                            activity.detected(57,t);

                        }
                    });
                }
            }
           //if(magnitude[71] + magnitude[57]>1)
             //   Log.i("sss",Integer.toString(magnitude[57]) + " " + Integer.toString(magnitude[71]));
          //  if(magnitude[57] + magnitude[62]+magnitude[66]+magnitude[71]>1){
           //     Log.i("ssss",Integer.toString(magnitude[71]) + " " +Integer.toString(magnitude[66]) + " " +Integer.toString(magnitude[62]) + " " +Integer.toString(magnitude[57]) + " " );
            //    Log.i("ssss",Integer.toString(count71) + " " +Integer.toString(count66) +  " " + Integer.toString(count7166) + " "  +Integer.toString(count62) + " " +Integer.toString(count57) + " " + Integer.toString(count6257));
           // }
            rr++;
            if(rr==alen)
                rr=0;


           // if(magnitude[71]+magnitude[66]>2)
            // Log.i("sssss",Integer.toString((int)magnitude[71]) + " "+Integer.toString((int)magnitude[66]));
        }
    }



    public void trigger(){
        if(started){
         stopRecording();
        } else {
        startRecording();
        }
    }


}
