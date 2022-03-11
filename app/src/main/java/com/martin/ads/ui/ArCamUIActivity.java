package com.martin.ads.ui;

/**
 * Created by Ads on 2017/3/9.
 */

import android.Manifest;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.hardware.Camera;

import android.net.wifi.WpsInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.martin.ads.constant.GlobalConstant;
import com.martin.ads.rendering.render.ArObjectWrapper;
import com.martin.ads.rendering.render.ObjRendererWrapper;
import com.martin.ads.slamar.NativeHelper;
import com.martin.ads.slamar.R;
import com.martin.ads.rendering.render.GLES10Demo;
import com.martin.ads.rendering.gles.GLRootView;
import com.martin.ads.utils.FpsMeter;
import com.martin.ads.utils.TouchHelper;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// 2.28.2022 start: add lib for wifi p2p connection
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;

import android.net.wifi.p2p.WifiP2pInfo;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.martin.ads.connection.DeviceAdapter;
import com.martin.ads.connection.DirectActionListener;
import com.martin.ads.connection.WifiClientTask;
import com.martin.ads.connection.WifiServerService;
import com.martin.ads.connection.WifiDirectBroadcastReceiver;
// 2.28.2022 end

public class ArCamUIActivity extends AppCompatActivity implements
        CameraGLViewBase.CvCameraViewListener2 {

    // 2.28.2022 wifi p2p connection start
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private WifiP2pInfo wifiP2pInfo;
    private boolean connectionInfoAvailable;

    private DeviceAdapter deviceAdapter;

    private List<WifiP2pDevice> wifiP2pDeviceList;


    private BroadcastReceiver broadcastReceiver;

    private WifiServerService wifiServerService;

    private WifiP2pDevice mWifiP2pDevice;

    // 3.3.2022 start for sender
    private boolean wifiP2pEnabled = false;
    // 3.3.3033 end for sender

    private static final int ADD_AR_OBJ = 1;

    private int device_idx;

    private final DirectActionListener directActionListener = new DirectActionListener() {

        @Override
        public void onChannelDisconnected() {

        }

        @Override
        public void wifiP2pEnabled(boolean enabled) {

        }

        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            if (wifiP2pInfo.groupFormed) {
                // for sender
                wifiP2pDeviceList.clear();
                ArCamUIActivity.this.wifiP2pInfo = wifiP2pInfo;

                // sender 3.8.2022
                deviceAdapter.notifyDataSetChanged();

                // for server: receiver
                /*
                connectionInfoAvailable = true;
                if (wifiServerService != null) {
                    new Intent(ArCamUIActivity.this, WifiServerService.class);
                }

                The receiver part will go to onCreate
                 */
            }
        }

        @Override
        public void onDisconnection() {
            // receiver:
            connectionInfoAvailable = false;

            // sender:
            wifiP2pDeviceList.clear();
            ArCamUIActivity.this.wifiP2pInfo = null;

            // 3.8.2022
            deviceAdapter.notifyDataSetChanged();
        }

        @Override
        public void onSelfDeviceAvailable(WifiP2pDevice wifiP2pDevice) {

        }

        @Override
        public void onPeersAvailable(Collection<WifiP2pDevice> wifiP2pDeviceList) {

            ArCamUIActivity.this.wifiP2pDeviceList.clear();
            ArCamUIActivity.this.wifiP2pDeviceList.addAll(wifiP2pDeviceList);

            // sender 3.8.2022
            deviceAdapter.notifyDataSetChanged();
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WifiServerService.WifiServerBinder binder = (WifiServerService.WifiServerBinder) service;
            wifiServerService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (wifiServerService != null) {
                wifiServerService = null;
            }
            bindService();
        }
    };
    // 2.28.2022 end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ar_ui_content);
        initToolbar();

        // 3.3.2022: for test, always set the connection with first device

        initView();

        // 2.28.2022 build wifi p2p connection start
        wifiP2pManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        if (wifiP2pManager == null) {
            finish();
            return;
        }
        channel = wifiP2pManager.initialize(this, getMainLooper(), directActionListener);
        broadcastReceiver = new WifiDirectBroadcastReceiver(wifiP2pManager, channel, directActionListener);
        registerReceiver(broadcastReceiver, WifiDirectBroadcastReceiver.getIntentFilter());
        bindService();

        // start up server service, it originate from onConnectionInfoAvailable()
        connectionInfoAvailable = true;
        if (wifiServerService != null) {
            new Intent(this, WifiServerService.class);
        }

        // 2.28.2022 end
    }

    // 2.28.2022 start
    private void bindService() {
        Intent intent = new Intent(ArCamUIActivity.this, WifiServerService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }
    // 2.28.2022 end

    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        mToolbar.setNavigationIcon(R.drawable.btn_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_menu:
                detectPlane = true;
                // 3.4.2022 start: notify peers that device adding AR obj
                if (wifiP2pInfo != null) {
                    new WifiClientTask(this, ADD_AR_OBJ).execute(wifiP2pInfo.groupOwnerAddress.getHostAddress());
                }
                // 3.4.2022 end
                break;


            case R.id.menuCreateGroup:
                Toast.makeText(this, "Create group...", Toast.LENGTH_SHORT).show();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
                wifiP2pManager.createGroup(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(ArCamUIActivity.this, "Create group success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(ArCamUIActivity.this, "Create group fail", Toast.LENGTH_SHORT).show();
                    }
                });
                break;

            case R.id.menuDirectDiscover:
                Toast.makeText(this, "Searching peers...", Toast.LENGTH_SHORT).show();
                wifiP2pDeviceList.clear();
                deviceAdapter.notifyDataSetChanged();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(ArCamUIActivity.this, "Need Permission on DirectDiscover", Toast.LENGTH_SHORT).show();
                    return true;
                }
                wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(ArCamUIActivity.this, "Searching success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(ArCamUIActivity.this, "Searching fail", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        System.exit(0);
    }

    private static final String    TAG = "SlamCamActivity";

    private Mat mRgba;
    private Mat                    mIntermediateMat;
    private Mat                    mGray;

    private CameraGLView mOpenCvCameraView;
    private boolean initFinished;

    private NativeHelper nativeHelper;
    TouchHelper touchHelper;

    private boolean detectPlane;

    private FpsMeter mFpsMeter = null;
    private TextView fpsText;

    private void initView(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        nativeHelper=new NativeHelper(this);
        mOpenCvCameraView = (CameraGLView) findViewById(R.id.my_fake_glsurface_view);
        mOpenCvCameraView.setVisibility(View.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        initFinished=false;

        touchHelper=new TouchHelper(this);
        initGLES10Demo();
        //initGLES20Demo();
        initGLES20Obj();

        View touchView=findViewById(R.id.touch_panel);
        touchView.setClickable(true);
        touchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return touchHelper.handleTouchEvent(event);
            }
        });
        touchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera camera=mOpenCvCameraView.getCamera();
                if (camera!=null) camera.autoFocus(null);
            }
        });
        //touchView.bringToFront();
        mOpenCvCameraView.init();

        fpsText = findViewById(R.id.text_fps);
        mFpsMeter = new FpsMeter();
        mFpsMeter.setResolution(GlobalConstant.RESOLUTION_WIDTH, GlobalConstant.RESOLUTION_HEIGHT);

        // 3.8.2022 set recycle view
        RecyclerView rv_deviceList = findViewById(R.id.rv_deviceList);
        wifiP2pDeviceList = new ArrayList<>();
        deviceAdapter = new DeviceAdapter(wifiP2pDeviceList);


        rv_deviceList.setAdapter(deviceAdapter);
        rv_deviceList.setLayoutManager(new LinearLayoutManager(this));

        // 3.10.2022 test if button click
        // test button show device list 1_st item
        Button test_button = (Button) findViewById(R.id.test_button);
        test_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wifiP2pDeviceList.size() > 0) {
                    mWifiP2pDevice = wifiP2pDeviceList.get(device_idx);
                    Toast.makeText(ArCamUIActivity.this, "device: "+mWifiP2pDevice.deviceName, Toast.LENGTH_SHORT).show();
                    device_idx = (device_idx + 1) % wifiP2pDeviceList.size();
                } else {
                    Toast.makeText(ArCamUIActivity.this, "no peer device", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button connect_button = (Button) findViewById(R.id.connect_button);
        connect_button.setOnClickListener(v -> connect());
    }

    private void initGLES10Demo() {
        final GLRootView glRootView=findViewById(R.id.ar_object_view_gles1);
        glRootView.setAspectRatio(GlobalConstant.RESOLUTION_WIDTH,GlobalConstant.RESOLUTION_HEIGHT);

        GLES10Demo gles10Demo=
                GLES10Demo.newInstance()
                        .setArObjectView(glRootView)
                        .setNativeHelper(nativeHelper)
                        .setContext(this)
                        .init(touchHelper);

        nativeHelper.addOnMVPUpdatedCallback(gles10Demo);
    }

//    private void initGLES20Demo() {
//        final GLRootView glRootView=findViewById(R.id.ar_object_view_gles2_sphere);
//        glRootView.setAspectRatio(GlobalConstant.RESOLUTION_WIDTH,GlobalConstant.RESOLUTION_HEIGHT);
//
//        ArObjectWrapper arObjectWrapper=
//                ArObjectWrapper.newInstance()
//                        .setArObjectView(glRootView)
//                        .setNativeHelper(nativeHelper)
//                        .setContext(this)
//                        .init(touchHelper);
//        nativeHelper.addOnMVPUpdatedCallback(arObjectWrapper);
//    }

    private void initGLES20Obj() {
        final GLRootView glRootView=findViewById(R.id.ar_object_view_gles2_obj);
        glRootView.setAspectRatio(GlobalConstant.RESOLUTION_WIDTH,GlobalConstant.RESOLUTION_HEIGHT);

        ObjRendererWrapper objRendererWrapper=
                ObjRendererWrapper.newInstance()
                        .setArObjectView(glRootView)
                        .setNativeHelper(nativeHelper)
                        .setContext(this)
                        /*
                        .setObjPath("patrick.obj")
                        .setTexturePath("Char_Patrick.png")
                        .setInitSize(0.20f)

                         */
                        .setObjPath("andy.obj")
                        .setTexturePath("andy.png")
                        .setInitSize(1.0f)
                        .init(touchHelper);
        nativeHelper.addOnMVPUpdatedCallback(objRendererWrapper);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(TAG, "OpenCV library found inside package. Using it!");
        mOpenCvCameraView.enableView();

        if (!initFinished) {
            initFinished=true;
            String resDir = this.getExternalFilesDir("SLAM").getAbsolutePath()+"/";
            Log.d(TAG, "onResume: "+resDir);
            nativeHelper.initSLAM(resDir);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

        // 3.2.2022 start, receiver
        if (wifiServerService != null) {
            unbindService(serviceConnection);
        }
        unregisterReceiver(broadcastReceiver);
        stopService(new Intent(this, WifiServerService.class));
        if (connectionInfoAvailable) {
            removeGroup();
        }
        // 3.2.2022 end
    }

    // 3.3.2022 start
    private void removeGroup() {
        wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(ArCamUIActivity.this, "remove Group success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(ArCamUIActivity.this, "remove Group fail", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // 3.3.2022 end

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
    }

    public Mat onCameraFrame(CameraGLViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if(initFinished){
            //Log.d("JNI_", "onCameraFrame: new image coming");
            int trackingResult=nativeHelper.processCameraFrame(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
            if(detectPlane){
                showHint("Request sent.");
                int detectResult=nativeHelper.detectPlane();
                detectPlane=false;
            }
            //Log.d("JNI_", "onCameraFrame: new image finished");
        }

        mFpsMeter.measure();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fpsText.setText(mFpsMeter.getText());
            }
        });
        return mRgba;
    }
    private void showHint(final String str){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ArCamUIActivity.this,str,Toast.LENGTH_LONG).show();
            }
        });
    }

    // 3.3.2022 start
    private void connect() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(ArCamUIActivity.this, "need permission in connect()", Toast.LENGTH_SHORT).show();
            return;
        }
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = mWifiP2pDevice.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        if (config.deviceAddress != null && mWifiP2pDevice != null) {
            wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                            Toast.makeText(ArCamUIActivity.this, "connect success", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int reason) {
                            Toast.makeText(ArCamUIActivity.this, "connect fail", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

    // 3.3.2022 end
}