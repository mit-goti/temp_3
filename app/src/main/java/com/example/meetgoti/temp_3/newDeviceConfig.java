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
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meetgoti.temp_3.Bluetooth.Select;

import com.example.meetgoti.temp_3.Bluetooth_Class.Bluetooth;

import java.util.ArrayList;

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
    int seek_id;
    int number_id;
    int slider_id;
    ArrayList<TextView> al = new ArrayList();
    String a_string = "";
    int flag = 0;
    Thread th;
    ArrayList<String> i = new ArrayList();
    ArrayList<String> j = new ArrayList();

    public static boolean isStringInteger(String number ){
        try{
            Integer.parseInt(number);
        }catch(Exception e ){
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newdeviceconfig);
        th = Thread.currentThread();
        button_id = 0;
        seek_id = 0;
        number_id = 0;
        slider_id = 0;
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
        char a = 128;
        b.send_byte(a);
    }

    @Override
    public void onDisconnect(BluetoothDevice device, String message) {
        Display("Disconnected!");
        Display("Connecting again...");
        b.connectToDevice(device);
    }

    int title_flag = 0;
    @Override
    public void onMessage(final String message)  {
        Log.d("recieved" , message);
        if(isStringInteger(message)) {
            final int temp_int = Integer.parseInt(message);
            if(temp_int >> 7 == 1) {
                int cool = temp_int>>4;
                cool = cool%8;
                switch (cool) {
                    case 0 :
                        i.add("b");
                        break;
                    case 1:
                        i.add("s");
                        break;
                    case 2:
                        i.add("n");
                        break;
                    case 3:
                        i.add("sli");
                        break;

                }
            }
            else if (temp_int == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        construct();
                    }
                });
            }
        }
        else {
            if(title_flag == 0) {
                TextView t = findViewById(R.id.title);
                t.setText(message);
                title_flag = 1;
            }
            else {
                String[] splited = message.split("\\s+");
                if (splited.length == 2 && isStringInteger(splited[0]) && isStringInteger(splited[1]))
                    change_Number(message);
                else
                    j.add(message);
            }
        }
    }

    void construct() {
        for(int k = 0 ; k < i.size() ; k++) {
            if(i.get(k).equals("s")) {
                String[] splited = j.get(k).split("\\s+");
                add_seekBar(splited[0] , Float.parseFloat(splited[1])/16 , Integer.parseInt(splited[2]));
            }
            else if(i.get(k).equals("b"))
                add_button(j.get(k));
            else if(i.get(k).equals("n"))
                add_numberView(j.get(k) , 50);
            else if(i.get(k).equals("sli")) {
                add_slider(j.get(k));
            }
        }
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

    public void add_button(String mes) {
        View layout2 = LayoutInflater.from(this).inflate(R.layout.button_layout , my_linear_layout , false);
        final Button btn2 = layout2.findViewById(R.id.but);
        btn2.setText(mes);
        btn2.setBackground(this.getResources().getDrawable(R.drawable.roundedbutton));
        btn2.setId(button_id);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int a = view.getId();
                b.send_byte((char)a);
            }
        });
        button_id++;
        my_linear_layout.addView(layout2);
    }

    public void add_seekBar(String mes , final float multiplier , int number_ini) {
        View layout2 = LayoutInflater.from(this).inflate(R.layout.seek_bar , my_linear_layout , false);
        final SeekBar seek = layout2.findViewById(R.id.seekBar);
        seek.setId(100 + seek_id);
        seek.setMax(100);
        seek.setProgress(number_ini);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                al.get((int)seekBar.getId() - 100).setText(String.valueOf(i*multiplier));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("seekbar" , String.valueOf(seekBar.getProgress()));
                int a  = ((int)seekBar.getId() - 100);
                a = a | 0b00010000 ;
                b.send_byte((char) a);
                b.send_byte((char)seekBar.getProgress());
            }
        });
        TextView textBox = layout2.findViewById(R.id.SeekBar_Name);
        textBox.setText(mes);
        final TextView num = layout2.findViewById(R.id.number);
        num.setText(String.valueOf(number_ini));
        al.add(num);
        my_linear_layout.addView(layout2);

        seek_id++;
    }

    public void add_numberView( String mes , int initiate) {
        View layout2 = LayoutInflater.from(this).inflate(R.layout.number_view , my_linear_layout , false);
        final TextView t1 = layout2.findViewById(R.id.numberView);
        t1.setId(200 + number_id);
        final TextView t2 = layout2.findViewById(R.id.NumberView_Name);
        t2.setText(mes);
        t1.setText(String.valueOf(initiate));
        my_linear_layout.addView(layout2);
        number_id++;
    }

    public void add_slider(String name) {
        View layout2 = LayoutInflater.from(this).inflate(R.layout.slider , my_linear_layout , false);
        Switch sw = layout2.findViewById(R.id.switch_layout);
        sw.setId(300 + slider_id);
        if(name.charAt(name.length() - 1) == '0')
            sw.setChecked(false);
        if(name.charAt(name.length() - 1) == '1')
            sw.setChecked(true);
        sw.setText(name.substring(0 , name.length() - 1));
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean g) {
                Log.d("check" , String.valueOf(b));
                if(g) {
                    int a = ((int)compoundButton.getId()) - 300;
                    a = a | 0b00110000 ;
                    b.send_byte((char) a);
                    b.send_byte('1');
                }
                else {
                    int a = ((int)compoundButton.getId()) - 300;
                    a = a | 0b00110000 ;
                    b.send_byte((char) a);
                    b.send_byte('0');
                }
            }
        });
        my_linear_layout.addView(layout2);
        slider_id++;
    }

    public void change_Number( String message ) {
        String[] spllited = message.split("\\s+");
        TextView temp = findViewById(200 + 0);
        temp.setText(String.valueOf(5));
    }
}