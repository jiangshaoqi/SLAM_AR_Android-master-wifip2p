package com.martin.ads.connection;

import static com.martin.ads.connection.MatUtil.matFromJson;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

// import java.io.FileOutputStream;
import org.opencv.core.Mat;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// 3.3.2022 start
// server to receive data
public class WifiServerService extends IntentService {

    private static final String TAG = "WifiServerService";

    private ServerSocket serverSocket;

    private InputStream inputStream;

    private ObjectInputStream objectInputStream;
    // private DataInputStream dataInputStream;

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onHandleIntent(Intent intent) {
        ResultReceiver rec = intent.getParcelableExtra("receiverTag");

        clean();
        // int aim;
        Mat Rgba;
        Mat gray;
        String matRgbaString;
        String matGrayString;
        String[] hostView = new String[16];
        String[] hostModel = new String[16];
        String clientAddress;
        ArrayList<String> addressList;

        OutputStream outputStream;
        ObjectOutputStream objectOutputStream;

        try {
            // 5.16.2022
            int numBytes = 0;

            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(PORT));
            Socket client = serverSocket.accept();
            clientAddress = client.getInetAddress().getHostAddress();
            Log.e(TAG, "client address : " + clientAddress);
            inputStream = client.getInputStream();
            // dataInputStream = new DataInputStream(inputStream);
            objectInputStream = new ObjectInputStream(inputStream);

            long startTime_2b = System.nanoTime();
            //aim = dataInputStream.readInt();
            matRgbaString = (String) objectInputStream.readObject();
            matGrayString = (String) objectInputStream.readObject();
            numBytes = numBytes + matRgbaString.getBytes().length;
            numBytes = numBytes + matGrayString.getBytes().length;

            // 4.1.2022 start
            for(int i = 0; i < 16; i ++) {
                hostView[i] = (String) objectInputStream.readObject();
                numBytes = numBytes + hostView[i].getBytes().length;
            }
            for(int i = 0; i < 16; i ++) {
                hostModel[i] = (String) objectInputStream.readObject();
                numBytes = numBytes + hostModel[i].getBytes().length;
            }
            // end
            addressList = (ArrayList<String>) objectInputStream.readObject();

            if(matRgbaString != null && matGrayString != null) {
                Rgba = matFromJson(matRgbaString);
                gray = matFromJson(matRgbaString);
                Log.e(TAG, "Receive the Processed frame");
            }
            Log.e(TAG, "2B phase Time: " + ((System.nanoTime()-startTime_2b)/1000000)+ "mS\n");

            // 6.7.2022 start
            outputStream = client.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(new String("msg from server"));
            outputStream.close();
            objectOutputStream.close();

            Log.e(TAG, "Total bytes been received: " + numBytes + "\n");
            serverSocket.close();
            inputStream.close();
            // dataInputStream.close();
            objectInputStream.close();

            serverSocket = null;
            inputStream = null;
            // dataInputStream = null;
            objectInputStream = null;

            // send back to activity
            Bundle bundle = new Bundle();
            bundle.putString("Mat rgba String", matRgbaString);
            bundle.putString("Mat gray String", matGrayString);
            bundle.putStringArray("hostView String arr", hostView);
            bundle.putStringArray("hostModel String arr", hostModel);

            // 5.19.2022
            bundle.putString("client address", clientAddress);
            bundle.putStringArrayList("client address list", addressList);
            if(addressList != null && addressList.size() > 0) {
                Log.e(TAG, "server receive addr: " + addressList.get(0));
            }
            rec.send(0, bundle);

        } catch (Exception e) {
            Log.e(TAG, "receive file Exception: " + e.getMessage());
        } finally {
            clean();
            Intent temp_intent = new Intent(this, WifiServerService.class);
            temp_intent.putExtra("receiverTag", rec);
            startService(temp_intent);
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
        if (objectInputStream != null) {
            try {
                objectInputStream.close();
                objectInputStream = null;
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
