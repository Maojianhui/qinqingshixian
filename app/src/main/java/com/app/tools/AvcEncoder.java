package com.app.tools;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import tech.shutu.jni.YuvUtils;

/**
 * Created by asus on 2017/6/15.
 */

public class AvcEncoder {
    private static final String TAG = "AvcEncoder";
    private MediaCodec mediaCodec;
    private BufferedOutputStream outputStream;
    private byte[] yuv420 = null;
    public byte[] outPut=null;
    private final int previewWidth = 352;     //水平像素
    private final int previewHeight = 288;     //垂直像素
    YuvUtils yuvPic=new YuvUtils();
    public AvcEncoder() {
        //输出到本地
        File f = new File(Environment.getExternalStorageDirectory(), "DCIM/video_encoded.264");
        yuv420 = new byte[previewWidth*previewHeight*3/2];
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(f));
            Log.i("AvcEncoder", "outputStream initialized");
        } catch (Exception e){
            e.printStackTrace();
        }
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }
        YuvUtils.allocateMemo(previewWidth*previewHeight*3/2,0,previewWidth*previewHeight*3/2);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc",previewWidth, previewHeight);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, previewWidth*previewHeight*2);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 10);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
        //COLOR_FormatYUV420SemiPlanar  s9
        //COLOR_FormatYUV420Planar      s6
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();
    }

    public void close() {
        try {
            mediaCodec.stop();
            mediaCodec.release();
            yuvPic.releaseMemo();
//            outputStream.flush();
//            outputStream.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    int mCount=0;

    // called from Camera.setPreviewCallbackWithBuffer(...) in other class
    public byte[] offerEncoder(byte[] input) {
        Log.i("AvcEncoder", "offerEncoder: ");
        byte[] dstYuv = new byte[previewWidth*previewHeight* 3 / 2];
        byte[] dstYuv1 = new byte[previewWidth*previewHeight* 3 / 2];
//        //s9设备
//
//        NV21ToNV12(input, yuv420, 1280, 720);
//        input=yuv420;
//        s6设备
        swapYV12toI420(input,previewWidth,previewHeight);
        yuvPic.scaleAndRotateYV12ToI420(i420bytes,dstYuv,previewWidth,previewHeight,90,previewWidth,previewHeight);

        input=dstYuv;

        try {
            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(input);
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length,  mCount * 1000000 / 15, 0);
                mCount++;
            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,0);
            System.out.println("outputBufferIndex = " + outputBufferIndex);
            Log.i(TAG, "outputFirst");

            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
            outPut= new byte[bufferInfo.size];
            System.out.println("outData = " + outPut.length);

            outputBuffer.get(outPut);
            //输出到文件
            outputStream.write(outPut, 0, outPut.length);
            Log.i("AvcEncoder", outPut.length + " bytes written");
            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            System.out.println("outputEnd");
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return outPut;
    }

    private void NV21ToNV12(byte[] nv21,byte[] nv12,int width,int height){
        if(nv21 == null || nv12 == null)return;
        int framesize = width*height;
        int i = 0,j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for(i = 0; i < framesize; i++){
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize/2; j+=2)
        {
            nv12[framesize + j-1] = nv21[j+framesize];
        }
        for (j = 0; j < framesize/2; j+=2)
        {
            nv12[framesize + j] = nv21[j+framesize-1];
        }
    }

    byte[] i420bytes = null;
    private byte[] swapYV12toI420(byte[] yv12bytes, int width, int height) {
        if (i420bytes == null)
            i420bytes = new byte[yv12bytes.length];
        for (int i = 0; i < width*height; i++)
            i420bytes[i] = yv12bytes[i];
        for (int i = width*height; i < width*height + (width/2*height/2); i++)
            i420bytes[i] = yv12bytes[i + (width/2*height/2)];
        for (int i = width*height + (width/2*height/2); i < width*height + 2*(width/2*height/2); i++)
            i420bytes[i] = yv12bytes[i - (width/2*height/2)];
        return i420bytes;
    }

}
