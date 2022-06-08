package com.martin.ads.connection;

import static com.martin.ads.connection.MatUtil.matToJson;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.opencv.core.Mat;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class WifiClientTask extends AsyncTask<Object, Integer, Boolean> {

    private static final String TAG = "client tag";
    private final Context context;

    private int aim;

    private static final int ADD_AR_OBJ = 1;

    private static final int RECV_PORT = 1995;

    public WifiClientTask(Context context, int aim) {
        this.context = context.getApplicationContext();
        this.aim = aim;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected Boolean doInBackground(Object... params) {
        Socket socket = null;
        OutputStream outputStream = null;
        ObjectOutputStream objectOutputStream = null;

        InputStream inputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            // 5.16.2022
            // total data transfer in bytes
            Log.e(TAG, "begin to send");
            int numBytes = 0;

            String hostAddress = params[0].toString();
            Mat Rgba = (Mat) params[1];
            Mat gray = (Mat) params[2];
            float[] hostView = (float[]) params[3];
            float[] hostModel = (float[]) params[4];
            List<String> addressList = (List<String>) params[5];
            // Log.e(TAG, "mat type "+Integer.toString(Rgba.type()));

            String matRgbaString = matToJson(Rgba);
            String matGrayString = matToJson(gray);
            numBytes = numBytes + matRgbaString.getBytes().length;
            numBytes = numBytes + matGrayString.getBytes().length;
            // send notification of AR obj adding
            if(aim == ADD_AR_OBJ) {
                socket = new Socket();
                socket.bind(null);
                socket.connect((new InetSocketAddress(hostAddress, RECV_PORT)), 10000);
                long startTime_1b = System.nanoTime();
                outputStream = socket.getOutputStream();
                objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(matRgbaString);
                objectOutputStream.writeObject(matGrayString);
                // 4.1.2022 start
                for(float f : hostView) {
                    objectOutputStream.writeObject(String.valueOf(f));
                    numBytes = numBytes + String.valueOf(f).getBytes().length;
                }
                for(float f : hostModel) {
                    objectOutputStream.writeObject(String.valueOf(f));
                    numBytes = numBytes + String.valueOf(f).getBytes().length;
                }
                Log.e(TAG, "good now");
                if(addressList == null)
                    addressList = new ArrayList<>();
                objectOutputStream.writeObject(addressList);
                // end

                // 6.7.2022 start, client receive after send
                inputStream = socket.getInputStream();
                objectInputStream = new ObjectInputStream(inputStream);
                String reply = (String)objectInputStream.readObject();
                Log.e(TAG, "1B phase Time: " + hostAddress + " : " + ((System.nanoTime()-startTime_1b)/1000000)+ "mS\n");
                Log.e(TAG, "Total bytes transferred: " + numBytes + "\n");
                Log.e(TAG, reply);
                inputStream.close();
                objectInputStream.close();
                // end

                socket.close();
                outputStream.close();
                // dataOutputStream.close();
                objectOutputStream.close();

                socket = null;
                outputStream = null;
                // dataOutputStream = null;
                objectOutputStream = null;

                Log.e(TAG, "client send obj notification");

                return true;
            }

        } catch (Exception e) {
            Log.e(TAG, "client send obj notification Exception: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        Log.e(TAG, "onPostExecute: " + aBoolean);
    }
}
