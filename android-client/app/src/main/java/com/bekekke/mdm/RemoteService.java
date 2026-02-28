package com.bekekke.mdm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.app.ActivityManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class RemoteService extends Service {
    private static final String TAG = "BEKEKKE_SERVICE";
    private static final String CHANNEL_ID = "MDM_Channel";
    private Socket mSocket;
    private Handler mHandler = new Handler();
    private String deviceId;

    @Override
    public void onCreate() {
        super.onCreate();
        deviceId = Build.ID + "_" + Build.SERIAL; // Simple Unique ID
        createNotificationChannel();
        startForeground(1, getNotification("Sistem Sedang Dimonitor..."));
        
        initSocket();
        startDataReporting();
    }

    private void initSocket() {
        try {
            // GANTI DENGAN IP LAPTOP ANDA JIKA RUN DI HP ASLI
            mSocket = IO.socket("http://192.168.1.100:4000"); 
            
            mSocket.on(Socket.EVENT_CONNECT, args -> {
                Log.d(TAG, "Connected to Server");
                JSONObject data = new JSONObject();
                try {
                    data.put("deviceId", deviceId);
                    data.put("deviceName", "Android_" + Build.MODEL);
                    data.put("type", "android_native");
                    mSocket.emit("register", data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

            mSocket.on("execute", args -> {
                JSONObject data = (JSONObject) args[0];
                Log.d(TAG, "Remote Command: " + data.toString());
            });

            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void startDataReporting() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendTelemetry();
                mHandler.postDelayed(this, 10000); // 10 seconds
            }
        }, 1000);
    }

    private void sendTelemetry() {
        if (mSocket != null && mSocket.connected()) {
            JSONObject data = new JSONObject();
            try {
                data.put("deviceId", deviceId);
                data.put("ram_usage", getRamUsage());
                data.put("battery", 88); // Mock for now
                mSocket.emit("data_update", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private int getRamUsage() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        double usedMem = mi.totalMem - mi.availMem;
        return (int) ((usedMem / mi.totalMem) * 100);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "BEKEKKE Remote Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private Notification getNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("BEKEKKE-MDM")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSocket != null) mSocket.disconnect();
    }
}
