package com.example.erfan.acousticdistance;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;

/**
 * The type Play tone thread.
 *
 * @author Nishant Srivastava
 * @project Zentone
 */
class PlayToneThread extends Thread {

    private boolean isPlaying = false;
    private final int freqOfTone;
    private final double duration;
    private AudioTrack audioTrack = null;
    private float volume = 0f;

    /**
     * Instantiates a new Play tone thread.
     *
     * @param freqOfTone          the freq of tone
     * @param duration            the duration
     * @param volume              the volume
     */
    public PlayToneThread(int freqOfTone, double duration, float volume ){
        this.freqOfTone = freqOfTone;
        this.duration = duration;
        this.volume = volume;
    }

    @Override
    public void run() {
        super.run();

        //Play tone
        playTone();
    }


    private void playTone() {
        if (!isPlaying) {
            isPlaying = true;

            int sampleRate = 44100;              // a number

            double dnumSamples = duration * sampleRate;
            dnumSamples = Math.ceil(dnumSamples);
            int numSamples = (int) dnumSamples;
            double sample[] = new double[numSamples];
            byte generatedSnd[] = new byte[2 * numSamples];


            for (int i = 0; i < numSamples; ++i) {      // Fill the sample array
                sample[i] = Math.sin(freqOfTone * 2 * Math.PI * i / (sampleRate));
            }

            // convert to 16 bit pcm sound array
            // assumes the sample buffer is normalized.
            // convert to 16 bit pcm sound array
            // assumes the sample buffer is normalised.
            int idx = 0;
            int i;

            int ramp = numSamples / 20;                                    // Amplitude ramp as a percent of sample count


        /*    for (i = 0; i < ramp; ++i) {                                     // Ramp amplitude up (to avoid clicks)
                // Ramp up to maximum
                final short val = (short) ((sample[i] * 32767 * i / ramp));
                // in 16 bit wav PCM, first byte is the low order byte
                generatedSnd[idx++] = (byte) (val & 0x00ff);
                generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            }
*/
            ramp=0;
            for (i = ramp; i < numSamples - ramp; ++i) {                        // Max amplitude for most of the samples
                // scale to maximum amplitude
                final short val = (short) ((sample[i] * 32767));
                // in 16 bit wav PCM, first byte is the low order byte
                generatedSnd[idx++] = (byte) (val & 0x00ff);
                generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            }
/*
            for (i = (numSamples - ramp); i < numSamples; ++i) {                               // Ramp amplitude down
                // Ramp down to zero
                final short val = (short) ((sample[i] * 32767 * (numSamples - i) / ramp));
                // in 16 bit wav PCM, first byte is the low order byte
                generatedSnd[idx++] = (byte) (val & 0x00ff);
                generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            }
*/
            try {
                int bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                        sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, bufferSize,
                        AudioTrack.MODE_STREAM);


                // Sanity Check for max volume, set after write method to handle issue in android
                // v 4.0.3
                float maxVolume = AudioTrack.getMaxVolume();
                System.out.println("Max volume :" + maxVolume);
                if (volume > maxVolume) {
                    volume = maxVolume;
                } else if (volume < 0) {
                    volume = 0;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    audioTrack.setVolume(volume);
                } else {
                    audioTrack.setStereoVolume(volume, volume);
                }

                System.out.println("volume :" + volume);


                audioTrack.play();                                          // Play the track
                audioTrack.write(generatedSnd, 0, 2*numSamples);    // Load the track

            } catch (Exception e) {
                e.printStackTrace();
            }
            stopTone();
        }
    }

    /**
     * Stop tone.
     */
    void stopTone() {
        if (audioTrack != null && audioTrack.getState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
            audioTrack.release();
            isPlaying = false;
        }
    }
}
