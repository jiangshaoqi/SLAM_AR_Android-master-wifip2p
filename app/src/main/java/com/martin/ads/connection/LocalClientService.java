package com.martin.ads.connection;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class LocalClientService extends Service {

    String TAG = "Local Client tag";

    ResultReceiver rec;

    private final IBinder mBinder = new LocalBinder();

    Socket socket;
    int SERVER_PORT = 1995;
    String GOAddress;
    OutputStream outputStream;
    InputStream inputStream;
    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;

    String matRgbaString;
    String matGrayString;
    String[] hostView = new String[16];
    String[] hostModel = new String[16];


    public class LocalBinder extends Binder {
        public LocalClientService getService() {
            return LocalClientService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(rec == null)
            rec = intent.getParcelableExtra("receiverTag");
        if(socket == null) {
            GOAddress = intent.getParcelableExtra("GOaddress");
            try {
                socket = new Socket();
                socket.setTcpNoDelay(true);
                socket.bind(null);
                socket.connect((new InetSocketAddress(GOAddress, SERVER_PORT)), 10000);
                outputStream = socket.getOutputStream();
                objectOutputStream = new ObjectOutputStream(outputStream);
                inputStream = socket.getInputStream();
                objectInputStream = new ObjectInputStream(inputStream);

                new Thread(new LocalClientService.ThreadReceive()).start();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        matRgbaString = intent.getParcelableExtra("matRgba");
        matGrayString = intent.getParcelableExtra("matGray");
        hostView = intent.getStringArrayExtra("hostView");
        hostModel = intent.getStringArrayExtra("hostModel");
        new Thread(new LocalClientService.ThreadSend()).start();
        return START_NOT_STICKY;
    }

    public class ThreadReceive implements Runnable {
        @Override
        public void run() {
            while(true) {
                try {
                    matRgbaString = (String) objectInputStream.readObject();
                    matGrayString = (String) objectInputStream.readObject();
                    for(int i = 0; i < 16; i ++)
                        hostView[i] = (String) objectInputStream.readObject();
                    for(int i = 0; i < 16; i ++)
                        hostModel[i] = (String) objectInputStream.readObject();

                    Bundle bundle = new Bundle();
                    bundle.putString("Mat rgba String", matRgbaString);
                    bundle.putString("Mat gray String", matGrayString);
                    bundle.putStringArray("hostView String arr", hostView);
                    bundle.putStringArray("hostModel String arr", hostModel);
                    rec.send(0, bundle);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }

    class ThreadSend implements Runnable {

        @Override
        public void run() {
            try {
                objectOutputStream.writeObject(matRgbaString);
                objectOutputStream.writeObject(matGrayString);
                for(String s : hostView) {
                    objectOutputStream.writeObject(s);
                }
                for(String s : hostModel) {
                    objectOutputStream.writeObject(s);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clean();
    }

    private void clean() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
                inputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (objectInputStream != null) {
            try {
                objectInputStream.close();
                objectInputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (outputStream != null) {
            try {
                outputStream.close();
                outputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (objectOutputStream != null) {
            try {
                objectOutputStream.close();
                objectOutputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
