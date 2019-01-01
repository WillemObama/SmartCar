package com.lincolnwang.BlueDot;

public class BluetoothConnection {

    private String _device_name;
    private String _device_address;
    private boolean _isBtConnected = false;

    public void set_device_name(String _device_name) {
        this._device_name = _device_name;
    }

    public void set_device_address(String _device_address) {
        this._device_address = _device_address;
    }

    public void set_isBtConnected(boolean _isBtConnected) {
        this._isBtConnected = _isBtConnected;
    }

    public String get_device_name() {
        return _device_name;
    }

    public String get_device_address() {
        return _device_address;
    }

    public boolean is_isBtConnected() {
        return _isBtConnected;
    }

    private static BluetoothConnection instance = new BluetoothConnection();

    private BluetoothConnection(){}

    public static BluetoothConnection newInstance(){
        return instance;
    }
}
