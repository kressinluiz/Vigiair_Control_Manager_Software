package com.vigiair.vigiapp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Arrays;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.util.Log;

import com.shenyaocn.android.OpenH264.ByteArrayOutputStream;
import com.skydroid.fpvlibrary.utils.BusinessUtils;
import com.skydroid.fpvlibrary.socket.SocketConnection;

public class VideoClient {

    public static native void nativeFeedVideoBuffer(byte[] h264);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final byte[] H264Header = new byte[] { 0, 0, 0, 1 };
    private boolean isAppReadyForVideo;

    public VideoClient(){
        isAppReadyForVideo = false;
    }

    public void setAppReadyForVideo(){
        isAppReadyForVideo = true;
    }

    private final CircularByteBuffer circularByteBuffer = new CircularByteBuffer(524288);

    public void received(byte[] buffer, int off, int size) {
        //System.out.println("received!");
        //mSocketSky.sendMessage(buffer);
        //Log.d("MyApp", "h264 rcv and sent");
        if(isAppReadyForVideo == true){
            this.circularByteBuffer.put(buffer, off, size);
            try {
              if (this.circularByteBuffer.available() > 2048)
                this.executor.execute(this.runnable);
            } catch (Exception exception) {}
        }
      }

  private Runnable runnable = new Runnable() {
       private final byte[] buffer = new byte[2048];

       private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

       public void run() {
           android.os.Process.setThreadPriority(-18);
         while (VideoClient.this.circularByteBuffer.available() > this.buffer.length) {
             //System.out.println("inside inside HERE!");
           int count = VideoClient.this.circularByteBuffer.get(this.buffer);
           if (count <= 4)
             return;
           try {
             this.byteArrayOutputStream.write(this.buffer, 0, count);
             byte[] buffer = this.byteArrayOutputStream.getBuf();
             int bufferCount = this.byteArrayOutputStream.getCount();
             int start = BusinessUtils.Find(buffer, bufferCount, 0, VideoClient.this.H264Header);
             while (start != -1) {
               int i = BusinessUtils.Find(buffer, bufferCount, start + VideoClient.this.H264Header.length, VideoClient.this.H264Header);
               if (i != -1) {
                 byte[] h264s = Arrays.copyOfRange(buffer, start, i);
                                nativeFeedVideoBuffer(h264s);
               }
               start = i;
             }
             int end = BusinessUtils.FindR(buffer, bufferCount, VideoClient.this.H264Header);
             byte[] remainBuf = this.byteArrayOutputStream.toByteArray();
             this.byteArrayOutputStream.reset();
             this.byteArrayOutputStream.write(remainBuf, end, remainBuf.length - end);
           } catch (Exception exception) {Log.d("MyApp", "exception!!!", exception);}
           finally{

           }
         }
       }
     };
}
