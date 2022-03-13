package com.martin.ads.connection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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

    @Override
    protected Boolean doInBackground(Object... params) {
        Socket socket = null;
        OutputStream outputStream = null;
        DataOutputStream dataOutputStream = null;
        // ObjectOutputStream objectOutputStream = null;
        try {
            String hostAddress = params[0].toString();

            // send notification of AR obj adding
            if(aim == ADD_AR_OBJ) {
                socket = new Socket();
                socket.bind(null);
                socket.connect((new InetSocketAddress(hostAddress, RECV_PORT)), 10000);
                outputStream = socket.getOutputStream();
                dataOutputStream = new DataOutputStream(outputStream);
                dataOutputStream.writeInt(ADD_AR_OBJ);

                socket.close();
                outputStream.close();
                dataOutputStream.close();
                socket = null;
                outputStream = null;
                dataOutputStream = null;

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
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
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
