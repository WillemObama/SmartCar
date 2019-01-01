package com.lincolnwang.BlueDot;

import java.net.PortUnreachableException;

public interface BluetoothListener {

    public void onBluetoothConnectStart();

    public void onBluetoothConnectSuccess();

    public void onBluetoothConnectFailed();

    public void onBluetoothDisconnect();

}
