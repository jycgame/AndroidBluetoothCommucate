package com.example.bluetuth;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.util.concurrent.Semaphore;

public class ServerActivity extends AppCompatActivity {

    TableLayout tableLayout;
    int currentRowIndex = -1;

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            if (message.what == 0) {
                currentRowIndex++;
                TableRow row = (TableRow) tableLayout.getChildAt(currentRowIndex);
                EditText et = (EditText) row.getChildAt(0);
                et.setText((String) message.obj);
            }

            if (message.what == 1) {
                Toast.makeText(getApplicationContext(), (String) message.obj, Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        EditText et = findViewById(R.id.editText);
        et.setFocusable(false);
        et.setText(Const.SERVICEUUID.toString());

        tableLayout = findViewById(R.id.clientTable);

        AcceptThread acceptThread = new AcceptThread();
        acceptThread.start();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private final String NAME = "ArtExplorerBlueServer";
        private final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = adapter.listenUsingRfcommWithServiceRecord(NAME, Const.serviceUuid);
            }catch (Exception e) {
                e.printStackTrace();
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket connectedSocket = null;

            while(true) {
                try {
                    connectedSocket = mmServerSocket.accept();

                    BluetoothDevice device = connectedSocket.getRemoteDevice();
                    String name = device.getName();
                    String address = device.getAddress();
                    //update ui
                    Message message = handler.obtainMessage();
                    message.what = 0;
                    message.obj = name == null ? address : name;
                    handler.sendMessage(message);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    break;
                }

                //We can close this server if we only accept one client.
                //But we don't since we need to support multiple clients.
                //We need a separate thread to handle one client
                Thread clientThread = new ConnectedThread(connectedSocket, handler);
                clientThread.start();
            }
        }
    }
}
