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
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

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
        // DataOutputStream dataOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            String hostAddress = params[0].toString();
            Mat Rgba = (Mat) params[1];
            Mat gray = (Mat) params[2];
            Log.e(TAG, "mat type "+Integer.toString(Rgba.type()));

            String matRgbaString = matToJson(Rgba);
            String matGrayString = matToJson(gray);

            // send notification of AR obj adding
            if(aim == ADD_AR_OBJ) {
                socket = new Socket();
                socket.bind(null);
                socket.connect((new InetSocketAddress(hostAddress, RECV_PORT)), 10000);
                outputStream = socket.getOutputStream();
                // dataOutputStream = new DataOutputStream(outputStream);
                // dataOutputStream.writeInt(ADD_AR_OBJ);
                objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(matRgbaString);
                objectOutputStream.writeObject(matGrayString);

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
