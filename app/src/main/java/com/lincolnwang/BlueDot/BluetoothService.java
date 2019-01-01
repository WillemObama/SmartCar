package com.lincolnwang.BlueDot;

import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class BluetoothService extends Service {

    public static final String Tag = "BluetoothService";

    public String device_name;
    public String device_address;

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    public boolean isBtConnected = false;
    public boolean connectionLost = false;

    private BluetoothListener bluetoothListener;
    public void setBluetoothListener(BluetoothListener listener){
        this.bluetoothListener = listener;

    }

    BluetoothBinder mBinder = new BluetoothBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent newIntent = intent;
        device_name = newIntent.getStringExtra(Devices.EXTRA_NAME);
        device_address = newIntent.getStringExtra(Devices.EXTRA_ADDRESS);
        new ConnectBT().execute();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... devices) { //while the progress dialog is shown, the connection is done in background
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(device_address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            } catch (IOException e) {
                ConnectSuccess = false;//if the try failed, you can check the exception here
                if(bluetoothListener != null)
                    bluetoothListener.onBluetoothConnectFailed();

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) { //after the doInBackground, it checks if everything went fine
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                Toast.makeText(getApplicationContext(), "Failed to connect", Toast.LENGTH_LONG).show();

            } else {
                isBtConnected = true;
                // start the connection monitor
                new MonitorConnection().execute();
                if(bluetoothListener != null)
                    bluetoothListener.onBluetoothConnectSuccess();
            }
        }
    }

    private class MonitorConnection extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... devices) {
            while (!connectionLost) {
                try {
                    byte[] buffer = new byte[20];
                    int count = btSocket.getInputStream().read(buffer);
                    if(count > 0)
                    {
                        String s = new String(buffer);
                        float distance = Float.valueOf(s);
                        Sensor.DISTANCE.setCurrentValue(distance);
                    }
                } catch (IOException e) {
                    connectionLost = true;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // if the bt is still connected, the connection must have been lost
            if (isBtConnected) {
                try {
                    isBtConnected = false;
                    btSocket.close();
                } catch (IOException e) {
                    // nothing doing, we are ending anyway!
                }
                if(bluetoothListener != null)
                    bluetoothListener.onBluetoothDisconnect();
                Toast.makeText(getApplicationContext(), "Connection lost", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void send(String message) {
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write(message.getBytes());
            } catch (IOException e) {
                //msg("Error : " + e.getMessage());
                if(e.getMessage().contains("Broken pipe")) disconnect();
            }
        } else {

        }
    }

    private void disconnect() {
        if (btSocket!=null) {
            try {
                isBtConnected = false;
                btSocket.close();
                bluetoothListener.onBluetoothDisconnect();
            } catch (IOException e) {

            }
        }
        Toast.makeText(getApplicationContext(),"Disconnected",Toast.LENGTH_LONG).show();
    }

    public class BluetoothBinder extends Binder{

        public BluetoothService getService(){
            return BluetoothService.this;
        };

        public void send(String Msg){
            BluetoothService.this.send(Msg);
        }

        public void setBluetoothListener(BluetoothListener listener){BluetoothService.this.setBluetoothListener(listener);}

        public boolean getIsBtConnected(){return BluetoothService.this.isBtConnected;}

        public String getDeviceAddress(){return BluetoothService.this.device_address;}

        public String getDeviceName(){return BluetoothService.this.device_name;}
    }

}
