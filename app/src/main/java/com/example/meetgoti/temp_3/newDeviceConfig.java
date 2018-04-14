package com.example.meetgoti.temp_3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.meetgoti.temp_3.Bluetooth.Select;

import com.example.meetgoti.temp_3.Bluetooth_Class.Bluetooth;

/**
 * Created by meetgoti on 05/04/18.
 */

public class newDeviceConfig extends AppCompatActivity implements Bluetooth.CommunicationCallback {

    private TextView connection_state;
    private String name;
    private Bluetooth b;
    private boolean registered;
    private ViewGroup my_linear_layout;
    int button_id;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newdeviceconfig);

        button_id = 0;
        connection_state = findViewById(R.id.Connection_State);
        registered = false;
        b = new Bluetooth(this);
        b.enableBluetooth();
        b.setCommunicationCallback(this);
        int pos = getIntent().getExtras().getInt("pos");
        my_linear_layout = (ViewGroup) findViewById(R.id.linear);

        Log.d("Debug" , "Here");
        name = b.getPairedDevices().get(pos).getName().toString();

        Log.d("Debug" , "Here");
        setTitle(name);
        Display("Connecting....");
        Log.d("Debug" , "Here");
        b.connectToDevice(b.getPairedDevices().get(pos));

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReciever , filter);
        registered = true;
        for(int i = 0 ; i < 17 ; i++)
            add_button();
        add_button();
    }

    private void Display(String s) {
        connection_state.setText(s);
        Log.d("Tag" ,"Connection : " + s);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(registered) {
            unregisterReceiver(mReciever);
            registered = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.close:
                b.removeCommunicationCallback();
                b.disconnect();
                Intent intent = new Intent(this, Select.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private BroadcastReceiver mReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                Intent intent1 = new Intent(newDeviceConfig.this, Select.class);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        if(registered) {
                            unregisterReceiver(mReciever);
                            registered=false;
                        }
                        startActivity(intent1);
                        finish();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        if(registered) {
                            unregisterReceiver(mReciever);
                            registered=false;
                        }
                        startActivity(intent1);
                        finish();
                        break;
                }
            }
        }
    };

    @Override
    public void onConnect(BluetoothDevice device) {
        Display("Connected to " + device.getName()  + device.getAddress());
        byte a = 5;
        b.send_byte(a);

    }

    @Override
    public void onDisconnect(BluetoothDevice device, String message) {
        Display("Disconnected!");
        Display("Connecting again...");
        b.connectToDevice(device);
    }

    @Override
    public void onMessage(String message) {
        Display("Received");
        Log.d("tag_from_device" , message);
    }

    @Override
    public void onError(String message) {
        Display("Error : " + message );
    }

    @Override
    public void onConnectError(final BluetoothDevice device, String message) {
        Display("Error" + message);
        Display("Trying Again in 3s");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        b.connectToDevice(device);
                        Display("Connecting......");
                    }
                } , 2000);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(newDeviceConfig.this , Select.class);
        i.putExtra("Check","Check");
        startActivity(i);
    }

    public void add_button() {
        View layout2 = LayoutInflater.from(this).inflate(R.layout.button_layout , my_linear_layout , false);
        final Button btn2 = layout2.findViewById(R.id.but);
        btn2.setText("WORLD");
        btn2.setBackground(this.getResources().getDrawable(R.drawable.roundedbutton));
        btn2.setId(button_id);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int a = view.getId();
                btn2.setText("OK Pressed " + a);
            }
        });
        button_id++;
        my_linear_layout.addView(layout2);
    }
}
