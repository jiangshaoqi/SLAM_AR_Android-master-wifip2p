package com.martin.ads.connection;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

// import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

// 3.3.2022 start
// server to receive data
public class WifiServerService extends IntentService {

    private static final String TAG = "WifiServerService";

    private ServerSocket serverSocket;

    private InputStream inputStream;

    // private ObjectInputStream objectInputStream;
    private DataInputStream dataInputStream;

    // private FileOutputStream fileOutputStream;

    private static final int PORT = 1995;
    private static final int ADD_AR_OBJ = 1;

    public WifiServerService() {
        super("WifiServerService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new WifiServerBinder();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        clean();
        int aim;
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(PORT));
            Socket client = serverSocket.accept();
            Log.e(TAG, "client address : " + client.getInetAddress().getHostAddress());
            inputStream = client.getInputStream();
            dataInputStream = new DataInputStream(inputStream);
            aim = dataInputStream.readInt();
            if(aim == ADD_AR_OBJ) {
                // this host notice that a peer is adding an AR object
                // 3.3.2022 only test, need more action
                Log.e(TAG, "AR object is added by peer");
            }
            serverSocket.close();
            inputStream.close();
            dataInputStream.close();
            serverSocket = null;
            inputStream = null;
            dataInputStream = null;

        } catch (Exception e) {
            Log.e(TAG, "receive file Exception: " + e.getMessage());
        } finally {
            clean();
            startService(new Intent(this, WifiServerService.class));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clean();
    }


    private void clean() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                serverSocket = null;
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
        if (dataInputStream != null) {
            try {
                dataInputStream.close();
                dataInputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
                fileOutputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        */
    }


    public class WifiServerBinder extends Binder {
        public WifiServerService getService() {
            return WifiServerService.this;
        }
    }
}
// 3.3.2022 end
