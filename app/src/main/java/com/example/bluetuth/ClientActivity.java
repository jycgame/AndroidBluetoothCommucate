package com.example.bluetuth;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class ClientActivity extends AppCompatActivity {

    BluetoothAdapter adapter;
    Set<BluetoothDevice> bondedDevices;
    TableLayout tableLayout;
    HashMap<Button, BluetoothDevice> buttonDeviceMap;
    BluetoothSocket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        tableLayout = findViewById(R.id.clientTable);

        final Button sendMessageButton = findViewById(R.id.button6);
        sendMessageButton.setEnabled(false);
        final EditText messageEdit = (EditText) findViewById(R.id.editText4);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = messageEdit.getText().toString();

                try {
                    OutputStream outStream = socket.getOutputStream();
                    outStream.write(text.getBytes());
                    outStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        adapter = BluetoothAdapter.getDefaultAdapter();
        bondedDevices = adapter.getBondedDevices();

        buttonDeviceMap = new HashMap<>();

        int maxTableRow = tableLayout.getChildCount();
        int currentRowIndexInTable = 0;

        for (int i = 0; i < maxTableRow; ++i) {
            TableRow row = (TableRow) tableLayout.getChildAt(i);
            EditText et = (EditText) row.getChildAt(0);
            et.setText("");
            et.setFocusable(false);
            et.setClickable(false);
            Button button = (Button) row.getChildAt(1);
            button.setEnabled(false);
            buttonDeviceMap.put(button, null);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BluetoothDevice device = buttonDeviceMap.get((Button)view);
                    if (device != null) {
                        try {
                            socket = device.createRfcommSocketToServiceRecord(Const.serviceUuid);
                            socket.connect();
                            sendMessageButton.setEnabled(true);
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else {
                        Toast.makeText(getApplicationContext(), "device is null", Toast.LENGTH_LONG);
                    }
                }
            });
        }

        for (Iterator iterator = bondedDevices.iterator(); iterator.hasNext(); ) {
            BluetoothDevice device = (BluetoothDevice) iterator.next();
            String name = device.getName();
            String address = device.getAddress();

            if (currentRowIndexInTable < maxTableRow) {
                TableRow row = (TableRow) tableLayout.getChildAt(currentRowIndexInTable++);
                EditText et = (EditText) row.getChildAt(0);
                et.setText(name == null ? address : name);
                Button button = (Button) row.getChildAt(1);
                button.setEnabled(true);
                button.setText("连接");
                buttonDeviceMap.put(button, device);
            }
        }
    }
}
