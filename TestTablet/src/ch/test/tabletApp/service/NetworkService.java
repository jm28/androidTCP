package ch.test.tabletApp.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NetworkService extends Service {

	//Debugging
	private static final String TAG = "NetworkService";

	//Constants
	public static final int SERVERPORT = 6000;
	
	//Member fields
	private static boolean running = false;
	private static ServerSocket mServerSocket = null;
	
	private Thread receiveThread = null;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		start();
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}	
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy called");
		stop();
		stopSelf();
	}
	
	private void stop() {
		try {
			running = false;
			
			if (mServerSocket!=null) {
				mServerSocket.close();
				mServerSocket = null;
			}
			
			receiveThread = null;
		} catch(IOException e) {
			Log.e(TAG, "Error on stop", e);
		}
	}
	
	private void start() {
		try {
			if (mServerSocket==null) {
				mServerSocket = new ServerSocket(SERVERPORT);

				if (receiveThread==null) {
					receiveThread = new Thread(new ReceiveThread());
					receiveThread.start();
				}
			} 
		} catch (IOException e) {
			Log.e(TAG, "Error on start", e);
		}
	}
	
	class ReceiveThread implements Runnable {
		
		@Override
		public void run() {
			String tmp = null;
			String result = "";
			Socket socket = null;
			
			while(running) {
				try {
					if (socket==null) {
						socket = mServerSocket.accept();
						
						BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						
						do {
							tmp = input.readLine();
							if (tmp!=null) result += tmp;
						} while (tmp==null);
						
						//handle message
						Log.d(TAG, "Received " + result);
						
						input.close();
						socket.close();
						socket = null;
						tmp = null;
						result = "";
					}
				} catch (Exception e) {
					Log.e(TAG, "ReceiveThread", e);
				}
			}
		}
	}
}
