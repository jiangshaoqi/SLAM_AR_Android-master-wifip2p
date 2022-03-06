package com.martin.ads.connection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class WifiClientTask extends AsyncTask<Object, Integer, Boolean> {

    private static final String TAG = "client tag";
    private final Context context;

    private int aim;

    private static final int ADD_AR_OBJ = 1;

    private static final int RECV_PORT = 594;

    public WifiClientTask(Context context, int aim) {
        this.context = context.getApplicationContext();
        this.aim = aim;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        Socket socket = null;
        OutputStream outputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            String hostAddress = params[0].toString();

            // send notification of AR obj adding
            if(aim == ADD_AR_OBJ) {
                socket = new Socket();
                socket.bind(null);
                socket.connect((new InetSocketAddress(hostAddress, RECV_PORT)), 10000);
                outputStream = socket.getOutputStream();
                objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeInt(ADD_AR_OBJ);

                socket.close();
                outputStream.close();
                objectOutputStream.close();
                socket = null;
                outputStream = null;
                objectOutputStream = null;
                return true;
            }

        } catch (Exception e) {
            Log.e(TAG, "client send obj notification Exception: " + e.getMessage());
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        Log.e(TAG, "onPostExecute: " + aBoolean);
    }
}
