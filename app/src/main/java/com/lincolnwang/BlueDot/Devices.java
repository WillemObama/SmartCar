package com.lincolnwang.BlueDot;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.telecom.Connection;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import android.net.Uri;

import java.util.Set;
import java.util.ArrayList;

public class Devices extends AppCompatActivity implements BluetoothListener {

    ListView devicelist;
    ImageButton infoButton;
    ProgressDialog progress;

    static BluetoothService.BluetoothBinder mBinder;

    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";
    public static String EXTRA_NAME = "device_name";
    private Intent bluetoothService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        initPermission();

        devicelist = (ListView)findViewById(R.id.listView);
        infoButton = (ImageButton) findViewById(R.id.infoButton);

        //if the device has bluetooth
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if(myBluetooth == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

            //finish apk
            this.finish();
            System.exit(0);

        } else if(!myBluetooth.isEnabled()) {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon,1);
        }

        infoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Uri uri = Uri.parse("https://bluedot.readthedocs.io");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        pairedDevicesList();

    }

    private void pairedDevicesList() {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0) {
            // create a list of paired bluetooth devices
            for(BluetoothDevice bt : pairedDevices)
            {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String deviceName = info.split("\n")[0];
            String address = info.split("\n")[1];
            if(mBinder != null &&  mBinder.getDeviceAddress().equals(address)){
                Toast.makeText(Devices.this,"设备已连接",Toast.LENGTH_LONG).show();
            }
            else {
                progress = ProgressDialog.show(Devices.this, "Connecting", "Please wait...");  //show a progress dialog
                Intent service = new Intent(Devices.this,BluetoothService.class);
                bluetoothService = service;
                service.putExtra(EXTRA_NAME,deviceName);
                service.putExtra(EXTRA_ADDRESS,address);
                startService(service);

                Intent sensorService = new Intent(Devices.this,SensorService.class);
                startService(sensorService);

                Devices.this.bindService(service,conn,Context.MODE_PRIVATE);
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_graphics:
                Intent rulePathActivity = new Intent(this,RulePathActivity.class);
                startActivity(rulePathActivity);
                break;
            case R.id.menu_motor:
                Intent motorActivity = new Intent(this,MotorActivity.class);
                startActivity(motorActivity);
                break;
            case R.id.menu_joystick:
                Intent buttonActivity = new Intent(this,Button.class);
                startActivity(buttonActivity);
                break;
            case R.id.menu_sensor:
                Intent sensorActivity = new Intent(this,SensorActivity.class);
                startActivity(sensorActivity);
                break;
            default:    break;
        }
        return true;
    }

    @Override
    public void onBluetoothConnectStart() {

    }

    @Override
    public void onBluetoothConnectSuccess() {
        progress.dismiss();
        Toast.makeText(Devices.this, "连接成功", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBluetoothConnectFailed() {
        progress.dismiss();
        stopService(bluetoothService);
        Devices.this.unbindService(conn);
        mBinder = null;
    }

    @Override
    public void onBluetoothDisconnect() {
        stopService(bluetoothService);
        Devices.this.unbindService(conn);
        mBinder = null;
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBinder = (BluetoothService.BluetoothBinder) iBinder;
            mBinder.setBluetoothListener(Devices.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private void initPermission() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.RECORD_AUDIO
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }
}

