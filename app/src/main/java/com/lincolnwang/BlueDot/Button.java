package com.lincolnwang.BlueDot;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import android.app.ProgressDialog;

import java.util.UUID;
import java.io.IOException;

public class Button extends AppCompatActivity  {


    private BluetoothService.BluetoothBinder mBinder;
    private double last_x = 0;
    private double last_y = 0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.button_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()){
            case R.id.menu_switch:
                intent = new Intent(this,RulePathActivity.class);
                startActivity(intent);
                this.finish();
                break;
            case R.id.menu_home:
                intent = new Intent(this,Devices.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button);

        Intent service = new Intent(Button.this,BluetoothService.class);
        Button.this.bindService(service,conn,Context.MODE_PRIVATE);
        TextView statusView = (TextView)findViewById(R.id.status);
        final View roundButton = (View)findViewById(R.id.roundButton);

        roundButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(mBinder == null || mBinder.getIsBtConnected() == false) return false;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    pressed(roundButton, event);

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    released(roundButton, event);

                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    moved(roundButton, event);
                }
                return true;
            }
        });

    }

    private void pressed(View roundButton, MotionEvent event) {
        double x = calcX(roundButton, event);
        double y = calcY(roundButton, event);
        mBinder.send(StringUtils.buildMessage("1", x, y));
        last_x = x;
        last_y = y;
    }

    private void released(View roundButton, MotionEvent event) {
        double x = calcX(roundButton, event);
        double y = calcY(roundButton, event);
        mBinder.send(StringUtils.buildMessage("0", x, y));
        last_x = x;
        last_y = y;
    }

    private void moved(View roundButton, MotionEvent event) {
        double x = calcX(roundButton, event);
        double y = calcY(roundButton, event);
        //has x or y changed?
        if ((x != last_x) || (y != last_y)) {
            mBinder.send(StringUtils.buildMessage("2", x, y));
            last_x = x;
            last_y = y;
        }
    }

    private double calcX(View roundButton, MotionEvent event) {
        double x = (event.getX() - (roundButton.getWidth() / 2)) / (roundButton.getWidth() / 2);
        x = (double)Math.round(x * 10000d) / 10000d;
        return x;
    }

    private double calcY(View roundButton, MotionEvent event) {
        double y = (event.getY() - (roundButton.getHeight() / 2)) / (roundButton.getHeight() /2) * -1;
        y = (double)Math.round(y * 10000d) / 10000d;
        return y;
    }

    private void msg(String message) {
        TextView statusView = (TextView)findViewById(R.id.status);
        statusView.setText(message);
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBinder = (BluetoothService.BluetoothBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
}
