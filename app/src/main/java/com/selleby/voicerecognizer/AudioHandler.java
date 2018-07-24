package com.selleby.voicerecognizer;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;

public class AudioHandler implements Serializable {
    private static int SAMPLE_RATE = 8000;
    private boolean recordingAudio;
    private float[] bufferToSend;
    private int maximumAudioLengthInSeconds = 3;
    private ArrayBlockingQueue<int[]> blockingQueue;

    AudioHandler() {
        recordingAudio = false;
        blockingQueue = new ArrayBlockingQueue<>(1);
    }

    private native int[] bufferRelay(float[] buffer, int numberSamples, int samplingRate);

    public void setRunning(boolean bool) {
        recordingAudio = bool;
    }

    void recordAudio() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

                int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);

                if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                    bufferSize = SAMPLE_RATE * 2;
                }

                float[] tempAudioBuffer = new float[bufferSize / 2];
                float[] fullAudioBuffer = new float[maximumAudioLengthInSeconds * SAMPLE_RATE];

                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_FLOAT,
                        bufferSize);

                if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                    Log.d("AUDIO", "Audio Record could not initialize");
                    return;
                }
                record.startRecording();
                int numberSamples = 0;
                while(recordingAudio && numberSamples < (fullAudioBuffer.length - tempAudioBuffer.length)) {
                    numberSamples += record.read(tempAudioBuffer, 0, tempAudioBuffer.length, AudioRecord.READ_BLOCKING);
                    System.arraycopy(tempAudioBuffer, 0, fullAudioBuffer, numberSamples - tempAudioBuffer.length, tempAudioBuffer.length);
                }

                bufferToSend = new float[numberSamples];
                System.arraycopy(fullAudioBuffer, 0, bufferToSend, 0, numberSamples);
                int[] binResults = bufferRelay(bufferToSend, numberSamples, SAMPLE_RATE);

                try {
                    blockingQueue.put(binResults);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                record.stop();
                record.release();
            }
        }).start();
    }

    int[] getFFTResult() throws InterruptedException {
        return blockingQueue.take();
    }

}
