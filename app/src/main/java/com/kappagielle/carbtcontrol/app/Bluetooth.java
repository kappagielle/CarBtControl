package com.kappagielle.carbtcontrol.app;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;


public class Bluetooth{

	public final static String TAG = "CAR CONTROL 1.0";
	private final static String BT_NAME = "HC-05";

	private static BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	protected ConnectedThread mConnectedThread;

	// SPP UUID service 
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	protected final Handler mHandler;
	public final static int BL_NOT_AVAILABLE = 1;        
	public final static int BL_INCORRECT_ADDRESS = 2;
	public final static int BL_REQUEST_ENABLE = 3;
	public final static int BL_SOCKET_FAILED = 4;
	public final static int RECIEVE_MESSAGE = 5;

	Bluetooth(Context context, Handler handler){
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		mHandler = handler;
		if (btAdapter == null) {
			mHandler.sendEmptyMessage(BL_NOT_AVAILABLE);
		}
	}
	
	public String getAddress() {
		
		if(btAdapter != null) {
			return btAdapter.getAddress();
		} else {
			return "";
		}
	}

	public void checkBTState() {
		if(btAdapter == null) { 
			mHandler.sendEmptyMessage(BL_NOT_AVAILABLE);
		} else {
			if (btAdapter.isEnabled()) {
				Log.d(TAG, "Bluetooth ON");
			} else {
				mHandler.sendEmptyMessage(BL_REQUEST_ENABLE);
			}
		}
	}

	public void btConnect() {   	
		Log.d(TAG, "...On Resume...");
			
		BluetoothDevice device = null;
		for(BluetoothDevice bd : btAdapter.getBondedDevices()) {
			if(bd.getName().equals(BT_NAME)) {
				device = bd;
				break;
			}
		}
		
		try {
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
			Log.d(TAG, "In onResume() and socket create failed: " + e.getMessage());
			mHandler.sendEmptyMessage(BL_SOCKET_FAILED);
			return;
		}

		btAdapter.cancelDiscovery();
		Log.d(TAG, "...Connecting...");
		try {
			btSocket.connect();
			Log.d(TAG, "...Connection ok...");
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				Log.d(TAG, "In onResume() and unable to close socket during connection failure" + e2.getMessage());
				mHandler.sendEmptyMessage(BL_SOCKET_FAILED);
				return;
			}
		}

		// Create a data stream so we can talk to server.
		Log.d(TAG, "...Create Socket...");

		try {
			outStream = btSocket.getOutputStream();
		} catch (IOException e) {
			Log.d(TAG, "In onResume() and output stream creation failed:" + e.getMessage());
			mHandler.sendEmptyMessage(BL_SOCKET_FAILED);
			return;
		}

		mConnectedThread = new ConnectedThread();
		mConnectedThread.start();

	}

	public void BT_onPause() {
		Log.d(TAG, "...On Pause...");
		if (outStream != null) {
			try {
				outStream.flush();
			} catch (IOException e) {
				Log.d(TAG, "In onPause() and failed to flush output stream: " + e.getMessage());
				mHandler.sendEmptyMessage(BL_SOCKET_FAILED);
				return;
			}
		}

		if (btSocket != null) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				Log.d(TAG, "In onPause() and failed to close socket." + e2.getMessage());
				mHandler.sendEmptyMessage(BL_SOCKET_FAILED);
				//return;
			}
		}
	}

	public void sendData(String message) {
		byte[] msgBuffer = message.getBytes();

		Log.i(TAG, "Send data: " + message);

		if (outStream != null) {
			try {
				outStream.write(msgBuffer);
			} catch (IOException e) {
				Log.d(TAG, "In onResume() and an exception occurred during write: " + e.getMessage());
				mHandler.sendEmptyMessage(BL_SOCKET_FAILED);
				//return;
			}
		} else Log.d(TAG, "Error Send data: outStream is Null");
	}

	private class ConnectedThread extends Thread {

		private final InputStream mmInStream;

		public ConnectedThread() {
			InputStream tmpIn = null;
			try {
				tmpIn = btSocket.getInputStream();
			} catch (IOException e) {
            //
            }

			mmInStream = tmpIn;
		}

		public void run() {
			byte[] buffer = new byte[256];
			int bytes;

			while (true) {
				try {
					bytes = mmInStream.read(buffer);
					mHandler.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();
				} catch (IOException e) {
					break;
				}
			}
		}
	}
}
