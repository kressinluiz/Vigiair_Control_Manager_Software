package org.mavlink.qgroundcontrol;

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
    private int localPort;
    private InetAddress serverAddress;
    private int serverPort;
    //private SocketConnection mSocketConnection;
    //private SocketSky mSocketSky;
    private DatagramSocket udpSocket;

    public static native void nativeFeedVideoBuffer(byte[] h264);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final byte[] H264Header = new byte[] { 0, 0, 0, 1 };

    public VideoClient(){
        try{
            //InetAddress serverAddr = InetAddress.getByName(null);
            //this.serverAddress = InetAddress.getByName("192.168.1.183");
            //this.localPort = 6000;
            //this.serverPort = 5600;
            //this.udpSocket = new DatagramSocket();
            //this.mSocketConnection = new SocketConnection(this.localPort, this.serverAddress, this.serverPort);
            //this.mSocketSky = new SocketSky(this.mSocketConnection);
        } catch(Exception e){

       }
    }

    //public CircularByteBuffer(int capacity) {
    //    this.capacity = capacity;
    //    this.buffer = new byte[this.capacity];
    //  }
    //array de bytes 524288 bytes = 0.524288 MB = 524,288 kilobits
    //array de bytes 2048 bytes
    //bitrate = 500000 kilobits/s =
    // 1000ms --- 500 kilobits
    //  32,768ms  ---- 16,384 kilobits = 2048 bytes
    //    8ms     ---- 4 kilobits      = 500 bytes
    //    40,32ms ----  20,16 kilobit      = 2520 bytes

    private final CircularByteBuffer circularByteBuffer = new CircularByteBuffer(524288);

    public void received(byte[] buffer, int off, int size) {
        //System.out.println("received!");
        // Size: 504 --- Off: 4 = Pacotes de 500 bytes
        //System.out.println("New Buffer!");
        //System.out.printf("Size: %d --- Off: %d", size, off);

        // 504 1008 1512 2016 2520
        this.circularByteBuffer.put(buffer, off, size);
        try {
          if (this.circularByteBuffer.available() > 2048)
            this.executor.execute(this.runnable);
        } catch (Exception exception) {}
      }

  private Runnable runnable = new Runnable() {
       private final byte[] buffer = new byte[2048];

       private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

       public void run() {
           android.os.Process.setThreadPriority(-18);
            //Log.d("MyApp", "RUN IN");
         while (VideoClient.this.circularByteBuffer.available() > this.buffer.length) {
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
                 //try{
                     //VideoClient.this.udpSocket = new DatagramSocket();
                     //VideoClient.this.serverAddress = InetAddress.getLocalHost();
                     //DatagramPacket packetSend = new DatagramPacket(h264s, h264s.length, InetAddress.getLocalHost(), VideoClient.this.serverPort);

                         //try{
                     //udpSocket.send(packetSend
                                nativeFeedVideoBuffer(h264s);
                         //} catch(IOException e){
                         //    System.out.println("UDPClient send: IOException " + e);
                         //}
//                 } catch (SocketException e){
//                     System.out.println("UDP Client: SocketException " + e);
//                 } catch (SecurityException e) {
//                     System.out.println("UDP Client: SecurityException " + e);
//                 } catch (UnknownHostException e) {
//                     System.out.println("UDP Client: UnknownHostException " + e);
//                 }
                 //FPVVideoClient.this.decodeH264(h264s, h264s.length);
               }
               start = i;
             }
             int end = BusinessUtils.FindR(buffer, bufferCount, VideoClient.this.H264Header);
             byte[] remainBuf = this.byteArrayOutputStream.toByteArray();
             this.byteArrayOutputStream.reset();
             this.byteArrayOutputStream.write(remainBuf, end, remainBuf.length - end);
           } catch (Exception exception) {Log.d("MyApp", "exception!!!", exception);}
           finally{
                //udpSocket.close();
           }
         }
            //Log.d("MyApp", "RUN OUT");
       }
     };
}
