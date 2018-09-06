
// get the min buffer of audio buffer
int minBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO ,
                    AudioFormat.ENCODING_PCM_16BIT);

// init audio track for play audio
track = new AudioTrack(AudioManager.STREAM_SYSTEM,
                  SAMPLE_RATE,
                  AudioFormat.CHANNEL_OUT_MONO ,
                  AudioFormat.ENCODING_PCM_16BIT,
                  minBufSize,
                  AudioTrack.MODE_STREAM);
 
// get PCM data from mic
int size = recorder.read(audioBuf,0,audioBuf.length);
if (size > 0) {

    // fead audio buffer for playing
    track.write(audioBuf,0,audioBuf.length);

    if(RECORD_FLAG){
        if (mCallback != null) {
            mCallback.audioData(audioBuf);
            Log.d(TAG, "录音字节数:" + size);
        }
    }
}
