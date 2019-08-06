package com.example.bluetuth;

import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import android.os.Handler;

public class ConnectedThread extends Thread {
    BluetoothSocket socket = null;
    private final InputStream inStream;
    private final OutputStream outStream;
    private final Handler handler;

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        this.socket = socket;

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }

        inStream = tmpIn;
        outStream = tmpOut;
        this.handler = handler;
    }

    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes;

        while(true) {
            try {
                bytes = inStream.read(buffer);
                byte[] content = new byte[bytes];
                for (int i = 0; i < bytes; ++i) {
                    content[i] = buffer[i];
                }
                String result = new String(content);
                Message message = handler.obtainMessage();
                message.what = 1;
                message.obj = result;
                handler.sendMessage(message);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void Write(byte[] stream) {
        try {
            outStream.write(stream);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Cancel() {
        try {
            this.socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}