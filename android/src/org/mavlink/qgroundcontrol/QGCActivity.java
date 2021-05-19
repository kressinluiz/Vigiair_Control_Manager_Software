package com.vigiair.vigiapp;

/* Copyright 2013 Google Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * Project home page: http://code.google.com/p/usb-serial-for-android/
 */
///////////////////////////////////////////////////////////////////////////////////////////
//  Written by: Mike Goza April 2014
//
//  These routines interface with the Android USB Host devices for serial port communication.
//  The code uses the usb-serial-for-android software library.  The QGCActivity class is the
//  interface to the C++ routines through jni calls.  Do not change the functions without also
//  changing the corresponding calls in the C++ routines or you will break the interface.
//
////////////////////////////////////////////////////////////////////////////////////////////
import android.content.Context;
import android.Manifest;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;

//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;

import java.io.File;


import java.util.List;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Locale;

import android.text.TextUtils;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.File;
import android.os.Environment;

import java.io.IOException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.widget.Toast;
import android.util.Log;
import android.os.PowerManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.PendingIntent;
import android.view.WindowManager;
import android.os.Bundle;
import android.bluetooth.BluetoothDevice;

import com.hoho.android.usbserial.driver.*;
import org.qtproject.qt5.android.bindings.QtActivity;
import org.qtproject.qt5.android.bindings.QtApplication;

//------------------------------------------------------------
//Skydroid SDK
import com.skydroid.android.usbserial.DeviceFilter;
import com.skydroid.android.usbserial.USBMonitor;
import com.skydroid.fpvlibrary.enums.PTZAction;
import com.skydroid.fpvlibrary.usbserial.UsbSerialConnection;
import com.skydroid.fpvlibrary.usbserial.UsbSerialControl;
import com.skydroid.fpvlibrary.utils.BusinessUtils;
import com.skydroid.fpvlibrary.video.FPVVideoClient;
import com.skydroid.fpvlibrary.widget.GLHttpVideoSurface;
import com.skydroid.fpvlibrary.enums.Sizes;
import android.os.Handler;
import android.os.Looper;
import java.io.File;
import android.media.MediaScannerConnection;
import android.net.Uri;

//-------------------------------------------------------------

public class QGCActivity extends QtActivity
{
    //------------------------------------------------------------
    //Skydroid SDK
        private Sizes size_lq = Sizes.Size_320x240;
        private Sizes size_mq = Sizes.Size_640x480;
        private Sizes size_hq = Sizes.Size_640x480_900k;
        private Context mContext = null;
        private USBMonitor mUSBMonitor = null;
        private UsbDevice mUsbDevice = null;
        private GLHttpVideoSurface mPreviewDualVideoView = null;
        private FPVVideoClient mFPVVideoClient = null;
        private VideoClient mVideoClient = null;
        private UsbSerialConnection mUsbSerialConnection = null;
        private UsbSerialControl mUsbSerialControl = null;
        private Handler mainHanlder = new Handler(Looper.getMainLooper());
    //-------------------------------------------------------------

    public  static int                                  BAD_DEVICE_ID = 0;
    private static QGCActivity                          _instance = null;
    private static UsbManager                           _usbManager = null;
    private static List<UsbSerialDriver>                _drivers;
    private static HashMap<Integer, UsbIoManager>       m_ioManager;
    private static HashMap<Integer, Long>               _userDataHashByDeviceId;
    private static final String                         TAG = "QGC_QGCActivity";
    private static PowerManager.WakeLock                _wakeLock;
    private static final String                         ACTION_USB_PERMISSION = "com.vigiair.vigiapp.action.USB_PERMISSION";
    private static PendingIntent                        _usbPermissionIntent = null;
    private TaiSync                                     taiSync = null;
    private Timer                                       probeAccessoriesTimer = null;
    private static WifiManager.MulticastLock            _wifiMulticastLock;
    
    public static Context m_context;

    private final static ExecutorService m_Executor = Executors.newSingleThreadExecutor();

    private final static UsbIoManager.Listener m_Listener =
            new UsbIoManager.Listener()
            {
                @Override
                public void onRunError(Exception eA, long userData)
                {
                    Log.e(TAG, "onRunError Exception");
                    nativeDeviceException(userData, eA.getMessage());
                }

                @Override
                public void onNewData(final byte[] dataA, long userData)
                {
                    nativeDeviceNewData(userData, dataA);
                }
            };

    private final BroadcastReceiver mOpenAccessoryReceiver =
        new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {
                    UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (accessory != null && intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        openAccessory(accessory);
                    }
                } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                    UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (accessory != null) {
                        closeAccessory(accessory);
                    }
                }
            }
        };

    private static UsbSerialDriver _findDriverByDeviceId(int deviceId) {
        for (UsbSerialDriver driver: _drivers) {
            if (driver.getDevice().getDeviceId() == deviceId) {
                return driver;
            }
        }
        return null;
    }

    private static UsbSerialDriver _findDriverByDeviceName(String deviceName) {
        for (UsbSerialDriver driver: _drivers) {
            if (driver.getDevice().getDeviceName().equals(deviceName)) {
                return driver;
            }
        }
        return null;
    }

    private final static BroadcastReceiver _usbReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.i(TAG, "BroadcastReceiver USB action " + action);

                if (ACTION_USB_PERMISSION.equals(action)) {
                    synchronized (_instance) {
                        UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (device != null) {
                            UsbSerialDriver driver = _findDriverByDeviceId(device.getDeviceId());

                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                qgcLogDebug("Permission granted to " + device.getDeviceName());
                                driver.setPermissionStatus(UsbSerialDriver.permissionStatusSuccess);
                            } else {
                                qgcLogDebug("Permission denied for " + device.getDeviceName());
                                driver.setPermissionStatus(UsbSerialDriver.permissionStatusDenied);
                            }
                        }
                    }
                } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {
                        if (_userDataHashByDeviceId.containsKey(device.getDeviceId())) {
                            nativeDeviceHasDisconnected(_userDataHashByDeviceId.get(device.getDeviceId()));
                        }
                    }
                }

                try {
                    nativeUpdateAvailableJoysticks();
                } catch(Exception e) {
                    Log.e(TAG, "Exception nativeUpdateAvailableJoysticks()");
                }
            }
        };

    // Native C++ functions which connect back to QSerialPort code
    private static native void nativeDeviceHasDisconnected(long userData);
    private static native void nativeDeviceException(long userData, String messageA);
    private static native void nativeDeviceNewData(long userData, byte[] dataA);
    private static native void nativeUpdateAvailableJoysticks();

    // Native C++ functions called to log output
    public static native void qgcLogDebug(String message);
    public static native void qgcLogWarning(String message);

    public native void nativeInit();

    // QGCActivity singleton
    public QGCActivity()
    {
        _instance =                 this;
        _drivers =                  new ArrayList<UsbSerialDriver>();
        _userDataHashByDeviceId =   new HashMap<Integer, Long>();
        m_ioManager =               new HashMap<Integer, UsbIoManager>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        nativeInit();
        PowerManager pm = (PowerManager)_instance.getSystemService(Context.POWER_SERVICE);
        _wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "QGroundControl");
        if(_wakeLock != null) {
            _wakeLock.acquire();
        } else {
            Log.i(TAG, "SCREEN_BRIGHT_WAKE_LOCK not acquired!!!");
        }
        _instance.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        _usbManager = (UsbManager)_instance.getSystemService(Context.USB_SERVICE);

        // Register for USB Detach and USB Permission intent
        IntentFilter filter = new IntentFilter();
        //filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        //filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        //filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        _instance.registerReceiver(_instance._usbReceiver, filter);

        // Create intent for usb permission request
        _usbPermissionIntent = PendingIntent.getBroadcast(_instance, 0, new Intent(ACTION_USB_PERMISSION), 0);

	// Workaround for QTBUG-73138
	if (_wifiMulticastLock == null)
            {
                WifiManager wifi = (WifiManager) _instance.getSystemService(Context.WIFI_SERVICE);
                _wifiMulticastLock = wifi.createMulticastLock("QGroundControl");
                _wifiMulticastLock.setReferenceCounted(true);
            }

	_wifiMulticastLock.acquire();
	Log.d(TAG, "Multicast lock: " + _wifiMulticastLock.toString());


        try {
            taiSync = new TaiSync();

            IntentFilter accessoryFilter = new IntentFilter(ACTION_USB_PERMISSION);
            filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
            registerReceiver(mOpenAccessoryReceiver, accessoryFilter);

            probeAccessoriesTimer = new Timer();
            probeAccessoriesTimer.schedule(new TimerTask() {
                @Override
                public void run()
                {
                    probeAccessories();
                }
            }, 0, 3000);
        } catch(Exception e) {
           Log.e(TAG, "Exception: " + e);
        }

    //setContentView(R.layout.activity_usbserial);
    requestPermissions();
    //initView();

    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 0
        );
    }

    @Override
    public void onResume() {
        super.onResume();

        //---------------------------------------------------------------------------------
        //Skydroid SDK
        this.mContext = this;
        init();


        if(mUsbDevice != null){
            try {
                mUsbSerialConnection.openConnection(mUsbDevice);
                mFPVVideoClient.startPlayback();
                mUsbSerialControl.setResolution(size_hq);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        //---------------------------------------------------------------------------------

        // Plug in of USB ACCESSORY triggers only onResume event.
        // Then we scan if there is actually anything new
        probeAccessories();
    }

    @Override
    protected void onDestroy()
    {
        //-----------------------------------------------
        //Skydroid SDK
        disconnected();
        desconfiguraUsbMonitor();
        //-----------------------------------------------

        if (probeAccessoriesTimer != null) {
            probeAccessoriesTimer.cancel();
        }
        unregisterReceiver(mOpenAccessoryReceiver);
        try {
            if (_wifiMulticastLock != null) {
                _wifiMulticastLock.release();
                Log.d(TAG, "Multicast lock released.");
            }
            if(_wakeLock != null) {
                _wakeLock.release();
            }
        } catch(Exception e) {
           Log.e(TAG, "Exception onDestroy()");
        }
        super.onDestroy();
    }

//---------------------------------------------------------------------------------
//Skydroid SDK
    private void init(){

        if(mUsbSerialConnection == null){
            criaProdutorVideo();
        }

        if(mFPVVideoClient == null){
            criaFpvVideoClient();
        }

        if(mVideoClient == null){
            criaConsumidorVideo();
        }

        if(mUsbSerialControl == null){
            configuraUsb();
        }
    }

    private void criaProdutorVideo(){
        mUsbSerialConnection = new UsbSerialConnection(mContext);
        mUsbSerialConnection.setDelegate(new UsbSerialConnection.Delegate() {
            @Override
            public void onH264Received(final byte[] bytes, int paySize) {
                if(mFPVVideoClient != null){
                    mVideoClient.received(bytes,4,paySize);
                //    mFPVVideoClient.received(bytes,4,paySize);
                    System.out.println("getting packets!");
                }
            }

            @Override
            public void onGPSReceived(byte[] bytes) {
                //System.out.println("onGPSReceived!");
            }

            @Override
            public void onDataReceived(byte[] bytes) {
                System.out.println("onDataReceived!");
            }

            @Override
            public void onDebugReceived(byte[] bytes) {
                //System.out.println("onDebugReceived!");
            }
        });
    }

    private void destroiProdutorVideo(){
        if(mUsbSerialConnection != null){
            try {
                mUsbSerialConnection.closeConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mUsbSerialConnection = null;
        }
    }

    private void criaFpvVideoClient(){

        mFPVVideoClient = new FPVVideoClient();
        mFPVVideoClient.setDelegate(new FPVVideoClient.Delegate() {
            @Override
            public void onStopRecordListener(final String fileName) {
                System.out.println("onStopRecordListener!");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sendBroadcast(
                                new Intent(
                                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                        Uri.fromFile(new File(fileName))
                                )
                        );
                        MediaScannerConnection.scanFile(
                                mContext,
                                fileName.split(" "),
                                null,
                                null
                        );
                        Toast.makeText(
                                mContext,
                                fileName,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }

            @Override
            public void onSnapshotListener(final String fileName) {

                System.out.println("onSnapshotListener!");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sendBroadcast(
                                new Intent(
                                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                        Uri.fromFile(new File(fileName))
                                )
                        );
                        MediaScannerConnection.scanFile(
                                mContext,
                                fileName.split(" "),
                                null,
                                null
                        );
                        Toast.makeText(
                                mContext,
                                fileName,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }

            @Override
            public void renderI420(byte[] frame, int width, int height) {
                //mPreviewDualVideoView.renderI420(frame,width,height);
            }

            @Override
            public void setVideoSize(int picWidth, int picHeight) {
                //mPreviewDualVideoView.setVideoSize(picWidth,picHeight,mainHanlder);
            }

            @Override
            public void resetView() {
                //mPreviewDualVideoView.resetView(mainHanlder);
            }
        });

    }

    private void destroiFpvVideoClient(){
        if(mFPVVideoClient != null){
            mFPVVideoClient.stopPlayback();
            //mFPVVideoClient = null;
        }
    }

    private void criaConsumidorVideo(){
        mVideoClient = new VideoClient();
    }

    private void configuraUsb(){

        mUsbSerialControl = new UsbSerialControl(mUsbSerialConnection);
        configuraUsbMonitor();

    }

    private void desconfiguraUsb(){
        if(mUsbSerialControl != null){
            mUsbSerialControl = null;
        }
    }

    private void configuraUsbMonitor(){
        if(mUSBMonitor == null){
            mUSBMonitor = new USBMonitor(mContext,mOnDeviceConnectListener);
            List<DeviceFilter> deviceFilters = DeviceFilter.getDeviceFilters(mContext, R.xml.device_filter);
            mUSBMonitor.setDeviceFilter(deviceFilters);
            mUSBMonitor.register();
        }
    }

    private void desconfiguraUsbMonitor(){
        if(mUSBMonitor != null){
            mUSBMonitor.unregister();
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
    }

    private USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        // USB device attach
        @Override
        public void onAttach(final UsbDevice device) {
            if(deviceHasConnected(device) || mUsbDevice != null){
                return;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(device == null){
                            System.out.println("onAttach device==null");
                            List<UsbDevice> devices = mUSBMonitor.getDeviceList();
                            if(devices.size() == 1){
                                mUSBMonitor.requestPermission(devices.get(0));
                            }
                        }else {
                            System.out.println("onAttach device!=null");
                            mUSBMonitor.requestPermission(device);
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }

        // USB device detach
        @Override
        public void onDettach(UsbDevice device) {
            System.out.println("onDettach");
            if (!BusinessUtils.deviceIsUartVideoDevice(device)) {
                return;
            }
            if (!deviceHasConnected(device)) {
                return;
            }
            disconnected();
        }

        // USB device has obtained permission
        @Override
        public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock var2, boolean var3) {

            if (!BusinessUtils.deviceIsUartVideoDevice(device)) {
                return;
            }
            if (deviceHasConnected(device)) {
                return;
            }

            synchronized (this){
                if (BusinessUtils.deviceIsUartVideoDevice(device)) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(mUsbSerialControl == null){
                                configuraUsb();
                            }
                        }
                    });

//                    try {
//                        mUsbSerialConnection.openConnection(device);
                          mUsbDevice = device;
//                        mFPVVideoClient.startPlayback();
//                        mUsbSerialControl.setResolution(size_hq);
                        System.out.println("onConnect!");
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
                }
            }
        }

        // USB device disconnected
        @Override
        public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock var2) {
            System.out.println("onDisconnect");
            if (!BusinessUtils.deviceIsUartVideoDevice(device)) {
                return;
            }
            if (!deviceHasConnected(device)) {
                return;
            }
            disconnected();
        }

        // USB device obtained permission failed
        @Override
        public void onCancel() {

        }
    };

    private void disconnected(){
        System.out.println("CALL to disconnected()");

        destroiFpvVideoClient();

        destroiProdutorVideo();

        desconfiguraUsbMonitor();
        configuraUsbMonitor();

        mUsbDevice = null;

    }

    private boolean deviceHasConnected(UsbDevice usbDevice){
        return usbDevice != null && usbDevice == mUsbDevice;
    }

//---------------------------------------------------------------------------------

    public void onInit(int status) {
    }

    /// Incrementally updates the list of drivers connected to the device
    private static void updateCurrentDrivers()
    {
//        List<UsbSerialDriver> currentDrivers = UsbSerialProber.findAllDevices(_usbManager);

//        // Remove stale drivers
//        for (int i=_drivers.size()-1; i>=0; i--) {
//            boolean found = false;
//            for (UsbSerialDriver currentDriver: currentDrivers) {
//                if (_drivers.get(i).getDevice().getDeviceId() == currentDriver.getDevice().getDeviceId()) {
//                    found = true;
//                    break;
//                }
//            }

//            if (!found) {
//                qgcLogDebug("Remove stale driver " + _drivers.get(i).getDevice().getDeviceName());
//                _drivers.remove(i);
//            }
//        }

//        // Add new drivers
//        for (int i=0; i<currentDrivers.size(); i++) {
//            boolean found = false;
//            for (int j=0; j<_drivers.size(); j++) {
//                if (currentDrivers.get(i).getDevice().getDeviceId() == _drivers.get(j).getDevice().getDeviceId()) {
//                    found = true;
//                    break;
//                }
//            }

//            if (!found) {
//                UsbSerialDriver newDriver =     currentDrivers.get(i);
//                UsbDevice       device =        newDriver.getDevice();
//                String          deviceName =    device.getDeviceName();

//                _drivers.add(newDriver);
//                qgcLogDebug("Adding new driver " + deviceName);

//                // Request permission if needed
//                if (_usbManager.hasPermission(device)) {
//                    qgcLogDebug("Already have permission to use device " + deviceName);
//                    newDriver.setPermissionStatus(UsbSerialDriver.permissionStatusSuccess);
//                } else {
//                    qgcLogDebug("Requesting permission to use device " + deviceName);
//                    newDriver.setPermissionStatus(UsbSerialDriver.permissionStatusRequested);
//                    _usbManager.requestPermission(device, _usbPermissionIntent);
//                }
//            }
//        }
    }

    /// Returns array of device info for each unopened device.
    /// @return Device info format DeviceName:Company:ProductId:VendorId
    public static String[] availableDevicesInfo()
    {
        updateCurrentDrivers();

        if (_drivers.size() <= 0) {
            return null;
        }

        List<String> deviceInfoList = new ArrayList<String>();

        for (int i=0; i<_drivers.size(); i++) {
            String          deviceInfo;
            UsbSerialDriver driver = _drivers.get(i);

            if (driver.permissionStatus() != UsbSerialDriver.permissionStatusSuccess) {
                continue;
            }

            UsbDevice device = driver.getDevice();

            deviceInfo = device.getDeviceName() + ":";

            if (driver instanceof FtdiSerialDriver) {
                deviceInfo = deviceInfo + "FTDI:";
            } else if (driver instanceof CdcAcmSerialDriver) {
                deviceInfo = deviceInfo + "Cdc Acm:";
            } else if (driver instanceof Cp2102SerialDriver) {
                deviceInfo = deviceInfo + "Cp2102:";
            } else if (driver instanceof ProlificSerialDriver) {
                deviceInfo = deviceInfo + "Prolific:";
            } else {
                deviceInfo = deviceInfo + "Unknown:";
            }

            deviceInfo = deviceInfo + Integer.toString(device.getProductId()) + ":";
            deviceInfo = deviceInfo + Integer.toString(device.getVendorId()) + ":";

            deviceInfoList.add(deviceInfo);
        }

        String[] rgDeviceInfo = new String[deviceInfoList.size()];
        for (int i=0; i<deviceInfoList.size(); i++) {
            rgDeviceInfo[i] = deviceInfoList.get(i);
        }

        return rgDeviceInfo;
    }

    /// Open the specified device
    ///     @param userData Data to associate with device and pass back through to native calls.
    /// @return Device id
    public static int open(Context parentContext, String deviceName, long userData)
    {
        int deviceId = BAD_DEVICE_ID;

        m_context = parentContext;

        UsbSerialDriver driver = _findDriverByDeviceName(deviceName);
        if (driver == null) {
            qgcLogWarning("Attempt to open unknown device " + deviceName);
            return BAD_DEVICE_ID;
        }

        if (driver.permissionStatus() != UsbSerialDriver.permissionStatusSuccess) {
            qgcLogWarning("Attempt to open device with incorrect permission status " + deviceName + " " + driver.permissionStatus());
            return BAD_DEVICE_ID;
        }

        UsbDevice device = driver.getDevice();
        deviceId = device.getDeviceId();

        try {
            driver.setConnection(_usbManager.openDevice(device));
            driver.open();
            driver.setPermissionStatus(UsbSerialDriver.permissionStatusOpen);

            _userDataHashByDeviceId.put(deviceId, userData);

            UsbIoManager ioManager = new UsbIoManager(driver, m_Listener, userData);
            m_ioManager.put(deviceId, ioManager);
            m_Executor.submit(ioManager);

            qgcLogDebug("Port open successful");
        } catch(IOException exA) {
            driver.setPermissionStatus(UsbSerialDriver.permissionStatusRequestRequired);
            _userDataHashByDeviceId.remove(deviceId);

            if(m_ioManager.get(deviceId) != null) {
                m_ioManager.get(deviceId).stop();
                m_ioManager.remove(deviceId);
            }
            qgcLogWarning("Port open exception: " + exA.getMessage());
            return BAD_DEVICE_ID;
        }

        return deviceId;
    }

    public static void startIoManager(int idA)
    {
        if (m_ioManager.get(idA) != null)
            return;

        UsbSerialDriver driverL = _findDriverByDeviceId(idA);

        if (driverL == null)
            return;

        UsbIoManager managerL = new UsbIoManager(driverL, m_Listener, _userDataHashByDeviceId.get(idA));
        m_ioManager.put(idA, managerL);
        m_Executor.submit(managerL);
    }

    public static void stopIoManager(int idA)
    {
        if(m_ioManager.get(idA) == null)
            return;

        m_ioManager.get(idA).stop();
        m_ioManager.remove(idA);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Sets the parameters on an open port.
    //
    //  Args:   idA - ID number from the open command
    //          baudRateA - Decimal value of the baud rate.  I.E. 9600, 57600, 115200, etc.
    //          dataBitsA - number of data bits.  Valid numbers are 5, 6, 7, 8
    //          stopBitsA - number of stop bits.  Valid numbers are 1, 2
    //          parityA - No Parity=0, Odd Parity=1, Even Parity=2
    //
    //  Returns:  T/F Success/Failure
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static boolean setParameters(int idA, int baudRateA, int dataBitsA, int stopBitsA, int parityA)
    {
        UsbSerialDriver driverL = _findDriverByDeviceId(idA);

        if (driverL == null)
            return false;

        try
        {
            driverL.setParameters(baudRateA, dataBitsA, stopBitsA, parityA);
            return true;
        }
        catch(IOException eA)
        {
            return false;
        }
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Close the device.
    //
    //  Args:  idA - ID number from the open command
    //
    //  Returns:  T/F Success/Failure
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static boolean close(int idA)
    {
        UsbSerialDriver driverL = _findDriverByDeviceId(idA);

        if (driverL == null)
            return false;

        try
        {
            stopIoManager(idA);
            _userDataHashByDeviceId.remove(idA);
            driverL.setPermissionStatus(UsbSerialDriver.permissionStatusRequestRequired);
            driverL.close();

            return true;
        }
        catch(IOException eA)
        {
            return false;
        }
    }



    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Write data to the device.
    //
    //  Args:   idA - ID number from the open command
    //          sourceA - byte array of data to write
    //          timeoutMsecA - amount of time in milliseconds to wait for the write to occur
    //
    //  Returns:  number of bytes written
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    public static int write(int idA, byte[] sourceA, int timeoutMSecA)
    {
        UsbSerialDriver driverL = _findDriverByDeviceId(idA);

        if (driverL == null)
            return 0;

        try
        {
            return driverL.write(sourceA, timeoutMSecA);
        }
        catch(IOException eA)
        {
            return 0;
        }
        /*
        UsbIoManager managerL = m_ioManager.get(idA);

        if(managerL != null)
        {
            managerL.writeAsync(sourceA);
            return sourceA.length;
        }
        else
            return 0;
        */
    }

    public static boolean isDeviceNameValid(String nameA)
    {
        for (UsbSerialDriver driver: _drivers) {
            if (driver.getDevice().getDeviceName() == nameA)
                return true;
        }

        return false;
    }

    public static boolean isDeviceNameOpen(String nameA)
    {
        for (UsbSerialDriver driverL: _drivers) {
            if (nameA.equals(driverL.getDevice().getDeviceName()) && driverL.permissionStatus() == UsbSerialDriver.permissionStatusOpen) {
                return true;
            }
        }

        return false;
    }



    /////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Set the Data Terminal Ready flag on the device
    //
    //  Args:   idA - ID number from the open command
    //          onA - on=T, off=F
    //
    //  Returns:  T/F Success/Failure
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public static boolean setDataTerminalReady(int idA, boolean onA)
    {
        try
        {
            UsbSerialDriver driverL = _findDriverByDeviceId(idA);

            if (driverL == null)
                return false;

            driverL.setDTR(onA);
            return true;
        }
        catch(IOException eA)
        {
            return false;
        }
    }



    ////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Set the Request to Send flag
    //
    //  Args:   idA - ID number from the open command
    //          onA - on=T, off=F
    //
    //  Returns:  T/F Success/Failure
    //
    ////////////////////////////////////////////////////////////////////////////////////////////
    public static boolean setRequestToSend(int idA, boolean onA)
    {
        try
        {
            UsbSerialDriver driverL = _findDriverByDeviceId(idA);

            if (driverL == null)
                return false;

            driverL.setRTS(onA);
            return true;
        }
        catch(IOException eA)
        {
            return false;
        }
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Purge the hardware buffers based on the input and output flags
    //
    //  Args:   idA - ID number from the open command
    //          inputA - input buffer purge.  purge=T
    //          outputA - output buffer purge.  purge=T
    //
    //  Returns:  T/F Success/Failure
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public static boolean purgeBuffers(int idA, boolean inputA, boolean outputA)
    {
        try
        {
            UsbSerialDriver driverL = _findDriverByDeviceId(idA);

            if (driverL == null)
                return false;

            return driverL.purgeHwBuffers(inputA, outputA);
        }
        catch(IOException eA)
        {
            return false;
        }
    }



    //////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Get the native device handle (file descriptor)
    //
    //  Args:   idA - ID number from the open command
    //
    //  Returns:  device handle
    //
    ///////////////////////////////////////////////////////////////////////////////////////////
    public static int getDeviceHandle(int idA)
    {
        UsbSerialDriver driverL = _findDriverByDeviceId(idA);

        if (driverL == null)
            return -1;

        UsbDeviceConnection connectL = driverL.getDeviceConnection();
        if (connectL == null)
            return -1;
        else
            return connectL.getFileDescriptor();
    }

    UsbAccessory openUsbAccessory = null;
    Object openAccessoryLock = new Object();

    private void openAccessory(UsbAccessory usbAccessory)
    {
        Log.i(TAG, "openAccessory: " + usbAccessory.getSerial());
        try {
            synchronized(openAccessoryLock) {
                if ((openUsbAccessory != null && !taiSync.isRunning()) || openUsbAccessory == null) {
                    openUsbAccessory = usbAccessory;
                    taiSync.open(_usbManager.openAccessory(usbAccessory));
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "openAccessory exception: " + e);
            taiSync.close();
            closeAccessory(openUsbAccessory);
        }
    }

    private void closeAccessory(UsbAccessory usbAccessory)
    {
        Log.i(TAG, "closeAccessory");

        synchronized(openAccessoryLock) {
            if (openUsbAccessory != null && usbAccessory == openUsbAccessory && taiSync.isRunning()) {
                taiSync.close();
                openUsbAccessory = null;
            }
        }
    }

    Object probeAccessoriesLock = new Object();

    private void probeAccessories()
    {
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        new Thread(new Runnable() {
            public void run() {
                synchronized(openAccessoryLock) {
//                    Log.i(TAG, "probeAccessories");
                    UsbAccessory[] accessories = _usbManager.getAccessoryList();
                    if (accessories != null) {
                       for (UsbAccessory usbAccessory : accessories) {
                           if (usbAccessory == null) {
                               continue;
                           }
                           if (_usbManager.hasPermission(usbAccessory)) {
                               openAccessory(usbAccessory);
                           }
                       }
                    }
                }
            }
        }).start();
    }
}

