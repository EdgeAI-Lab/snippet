package com.fhc.laser_monitor_sw_android_rk3399_wifi_app.action;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.fhc.laser_monitor_sw_android_rk3399_wifi_app.MainActivity;
import com.fhc.laser_monitor_sw_android_rk3399_wifi_app.SocketDataCallback;
import com.fhc.laser_monitor_sw_android_rk3399_wifi_app.SocketStateCallback;
import com.fhc.laser_monitor_sw_android_rk3399_wifi_app.client.ClientNoHeartbeat;
import com.fhc.laser_monitor_sw_android_rk3399_wifi_app.muxer.AVDataCallback;
import com.fhc.laser_monitor_sw_android_rk3399_wifi_app.muxer.BaseMuxer;
import com.fhc.laser_monitor_sw_android_rk3399_wifi_app.utils.CV;
import com.score.rahasak.utils.OpusDecoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;


public class AudioDecoder implements SocketDataCallback,SocketStateCallback{

    private static final String TAG = "Audio";
    private static final boolean DEBUG = true;
    private ClientNoHeartbeat mClient;
    private DecodeOpusEncodeAacThread mDecodeOpusEncodeAacThread;
    private WriteAacFileThread mWriteAacFileThread;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    //TODO: for mp4
    private volatile boolean RECORD_FLAG = false;

    private String filePath;
    private FileOutputStream fos;

    private LinkedBlockingQueue<byte[]> audioAacQueue = new LinkedBlockingQueue();
    private LinkedBlockingQueue<byte[]> audioOpusQueue = new LinkedBlockingQueue();

    public void setPause(byte pause) {
        isPause = pause;
    }


    private static byte isPause = 0x02;

    public void setAvDataCallback(AVDataCallback avDataCallback) {
        this.avDataCallback = avDataCallback;
    }

    private AVDataCallback avDataCallback;

    public AudioDecoder(){
        mClient = new ClientNoHeartbeat(CV.IP,6801,this,this);
    }

    public void start() {
        if (mDecodeOpusEncodeAacThread == null) {
            mDecodeOpusEncodeAacThread = new DecodeOpusEncodeAacThread();
            mDecodeOpusEncodeAacThread.setRunning(true);
            mDecodeOpusEncodeAacThread.start();
        }
    }


    public void stop() {
        if (mDecodeOpusEncodeAacThread != null) {
            mDecodeOpusEncodeAacThread.interrupt();
            mDecodeOpusEncodeAacThread.setRunning(false);
            mDecodeOpusEncodeAacThread = null;
        }
    }


    private static final SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
    //TODO: start record
    public void startRecord(String name) {
        if(DEBUG)Log.d(TAG, "start record ");
        RECORD_FLAG = true;

        filePath = Environment
                .getExternalStorageDirectory()
                + "/pcm"
                + "/"+mDateTimeFormat.format(new GregorianCalendar().getTime())
                + "-"
                + name
                +".pcm";

        File audioFile = new File(filePath);
        if(audioFile.exists()){
            audioFile.delete();
        }

        try {
            audioFile.createNewFile();
            fos = new FileOutputStream(audioFile);
        } catch (IOException e) {
            e.printStackTrace();
        }


        if(mWriteAacFileThread==null){
            mWriteAacFileThread = new WriteAacFileThread();
            mWriteAacFileThread.setRunning(true);
            mWriteAacFileThread.start();
        }


    }

    // TODO: stop record
    public void stopRecord() {
        if(DEBUG)Log.d(TAG, "stop record ");
        RECORD_FLAG = false;

        if(mWriteAacFileThread!=null){
            mWriteAacFileThread.interrupt();
            mWriteAacFileThread.setRunning(false);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mWriteAacFileThread.join();
                        fos.flush();
                        fos.close();
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    mWriteAacFileThread=null;
                    avDataCallback.onStopMux();
                }
            }).start();

        }
    }

    @Override
    public void onReceiveData(byte[] data) {
        try {
            if(isPause == 0x01){
                audioOpusQueue.put(data);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSocketState(byte state) {
        switch (state){
            case 0x01:
                start();
                if(DEBUG)Log.d(TAG,"socket connect success");
                break;
            case 0x02:
                stop();
                if(DEBUG)Log.e(TAG,"socket connect is broken!");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.mContext,"音频连接断开，请重启App!",Toast.LENGTH_LONG).show();
                    }
                });
                break;
        }
    }

    private class WriteAacFileThread extends Thread{
        private boolean isRunning = false;

        public void setRunning(boolean running){
            isRunning = running;
        }

        @Override
        public void run() {
            while (isRunning && !Thread.currentThread().isInterrupted()){
                //write AAC data with ADTS into file
                try {
                    byte[] aac_data = audioAacQueue.take();
                    //add ADTS header
                    avDataCallback.onFrame(aac_data);
                }  catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                writeAacFile();

            }
            if(DEBUG)Log.d(TAG,"write aac file thread quit!");
        }
    }

//    private void writeAacFile(){
//
//        //write AAC data with ADTS into file
//        try {
//            byte[] aac_data = audioAacQueue.take();
//            //add ADTS header
//            Log.d("ADTS","Add a ADTS header to audio");
//            fos.write(getADTSHeader(aac_data.length+7));
//
//            //write aac data into file
//            Log.d("Write","write into file");
//            fos.write(aac_data);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//    }


    private class DecodeOpusEncodeAacThread extends Thread {
        private boolean isRunning = false;
        // Sample rate must be one supported by Opus.
        static final int SAMPLE_RATE = 48000;

        int max_frame_size = SAMPLE_RATE;

        // 1 or 2
        static final int NUM_CHANNELS = 2;

        private MediaCodec.BufferInfo mBufferInfo;

        private AudioTrack track;
        byte[] audioBuf;

//        private OpusDecoder decoder;
        private MediaCodec audioEncoder;

        private byte[] frame;
        private int inIndex;

//        private short[] outBuf = new short[max_frame_size*NUM_CHANNELS];

//        private int decoded;

        boolean prepare() {

            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            mBufferInfo = new MediaCodec.BufferInfo();

            int minBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                    NUM_CHANNELS == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT);

            audioBuf = new byte[minBufSize];

            // init audio track for play
            track = new AudioTrack(AudioManager.STREAM_MUSIC,
                    48000,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufSize*4,
                    AudioTrack.MODE_STREAM);

            // Audio audioEncoder AAC
            MediaFormat format = MediaFormat.createAudioFormat(
                    MediaFormat.MIMETYPE_AUDIO_AAC, SAMPLE_RATE, NUM_CHANNELS);

            format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
            format.setInteger(MediaFormat.KEY_BIT_RATE, SAMPLE_RATE*16*2);
            format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, NUM_CHANNELS);
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,1024*1024);


            try {
                audioEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
                audioEncoder.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
                audioEncoder.start();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            // init opus decoder
//            decoder = new OpusDecoder();
//            decoder.init(SAMPLE_RATE, NUM_CHANNELS);

            // play audio
            track.play();

            return true;
        }

        void setRunning(boolean running) {
            isRunning = running;
        }

        @Override
        public void run() {
            if (!prepare()) {
                if(DEBUG)Log.w(TAG, "解码器初始化失败");
                isRunning = false;
            }
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                decodeOpusToPcm();
            }

            release();

        }

        private void decodeOpusToPcm() {

            boolean isEOS = false;
            while (!isEOS) {// 判断是否是流的结尾

//                if(isPause){
//                    return;
//                }

                try {
                    frame = audioOpusQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    isEOS=true;
                }

//                Log.d("PCM",Integer.toString(frame.length));

                /* des: decode opus data into PCM
                 * TODO: par: decoded 是解码后的大小（short）
                 */
//                decoded = decoder.decode(frame, outBuf, max_frame_size);

                track.write(frame, 0, frame.length);

                // start record, encode aac and write to file
                if(RECORD_FLAG){

                    try {
                        fos.write(frame);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // 停止录像
                        stopRecord();
                    }

                    // convert to little end
                    toLittleEnd(frame,frame.length);

                    inIndex = audioEncoder.dequeueInputBuffer(10000);
                    if (inIndex >= 0) {
                        ByteBuffer buffer = audioEncoder.getInputBuffer(inIndex);
                        if (buffer == null) {
                            if(DEBUG)Log.w(TAG, "buffer=null");
                            return;
                        }
                        buffer.clear();
                        if (frame == null) {
                            if(DEBUG)Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                            audioEncoder.queueInputBuffer(inIndex, 0, 0, 0,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            isEOS = true;
                            isRunning = false;

                        } else {
                            buffer.put(frame, 0, frame.length);
                            buffer.clear();
                            buffer.limit(frame.length);
                            audioEncoder.queueInputBuffer(inIndex, 0, frame.length, 0,
                                    MediaCodec.BUFFER_FLAG_KEY_FRAME);
                        }
                    } else {
                        isEOS = true;
                        if(DEBUG)Log.w(TAG,"get inputBuffer fail!");
                    }

                    int outIndex = audioEncoder.dequeueOutputBuffer(mBufferInfo, 10000);
                    if (outIndex >= 0 ) {

                        //get encode aac data
                        ByteBuffer buffer = audioEncoder.getOutputBuffer(outIndex);

                        //aac data fill into a byte[]
                        buffer.position(mBufferInfo.offset);
                        buffer.limit(mBufferInfo.offset + mBufferInfo.size);
                        byte[] aacData = new byte[mBufferInfo.size];
                        buffer.get(aacData);

                        // put the AAC data into a queue
                        audioAacQueue.add(aacData);
//                        Log.d("AAC",Integer.toString(aacData.length));

                        audioEncoder.releaseOutputBuffer(outIndex, false);

                    }else {
                        isEOS = true;
                        if(DEBUG)Log.w(TAG,"get OutputBuffer fail!    "+outIndex);
                    }
                }
            }
        }

        /**
         * 释放资源
         */
        private void release() {
//            if(decoder!=null){
//                decoder.close();
//                decoder = null;
//            }
            if(track!=null){
                track.stop();
                track.release();
                track = null;
            }

            if(audioEncoder!=null){
                audioEncoder.stop();
                audioEncoder.release();
                audioEncoder = null;
            }
            if(mClient!=null){
                mClient.stop();
            }

            if(DEBUG)Log.d(TAG,"audio render thread quit");
        }
    }


//    private byte[] getADTSHeader(int packetLen) {
//        byte[] packet = new byte[7];
//        int profile = 2;  //AAC LC
//        int freqIdx = 3;  //48KHz
//        int chanCfg = 2;  //2ch
//        packet[0] = (byte) 0xFF;
//        packet[1] = (byte) 0xF1;
//        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
//        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
//        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
//        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
//        packet[6] = (byte) 0xFC;
//        return packet;
//    }


//    private byte[] addBytes(byte[] data1, byte[] data2) {
//        byte[] data3 = new byte[data1.length + data2.length];
//        System.arraycopy(data1, 0, data3, 0, data1.length);
//        System.arraycopy(data2, 0, data3, data1.length, data2.length);
//        return data3;
//
//    }

//    private byte[] toByteArray(short[] src, int len) {
//
//        //TODO: 注意字节序
//        byte[] dest = new byte[len << 1];
//        for (int i = 0; i < len; i++) {
////            dest[i * 2] = (byte) (src[i] >> 8);
////            dest[i * 2 + 1] = (byte) (src[i] >> 0);
//            dest[i * 2] = (byte) src[i];
//            dest[i * 2 + 1] = (byte) (src[i] >> 8);
//        }
//
//        return dest;
//    }


    private void toLittleEnd(byte[] src, int len) {

        //TODO: 注意字节序
        byte temp;
        for (int i = 0; i < len; i+=2) {

            temp = src[i];
            src[i]=src[i+1];
            src[i+1]=temp;

        }
    }

}
