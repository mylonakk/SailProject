package com.skm.sail;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MainActivity extends Activity implements View.OnClickListener {

	private static final String TAG = "MainActivity";
	EditText editText;
	Button updateButton;
	TextView textView;
	String message;
	DatagramSocket s;// socket for communication
	String packetInfo;// string to be sent
	byte[] txbuffer;// outgoing packet's information
	int serverPort = 48000;// the port used by the server
	byte[] hostIP = {(byte) 83, (byte) 212, (byte) 121, (byte) 161};// server's IP
	InetAddress hostAddress;// server's address
	DatagramPacket p;// packet used to send info
	byte[] rxbuffer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		editText = (EditText) findViewById(R.id.editText);
		textView = (TextView) findViewById(R.id.textView);
		updateButton = (Button) findViewById(R.id.Button);
		updateButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		String status = editText.getText().toString();
		try {
			new SendToServer().execute(status);    // status goes in doInBackground()
		} catch (Exception e) {
			Log.d(TAG, "Server sent status failed: " + e);
		}
	}

	private class SendToServer extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... strings) {
			try {
				s = new DatagramSocket();// open a socket at a random port
				rxbuffer = new byte[160];
				DatagramPacket q = new DatagramPacket(rxbuffer, rxbuffer.length);// packed used to get info
				hostAddress = InetAddress.getByAddress(hostIP);
				packetInfo = strings[0];
				int size;
				packetInfo = packetInfo.concat("PSTOP");
				txbuffer = packetInfo.getBytes();
				p = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, serverPort);
				// s.connect(hostAddress, serverPort);
				s.send(p);
				s.setSoTimeout(20000);
				for (; ; ) {
					s.receive(q);
					message = new String(rxbuffer, 0, q.getLength());
					if (message != null) {
						if (message.endsWith("PSTOP")) {
							return message.substring(0, message.length() - 5);
							//size = q.getLength();
							//return Integer.toString(size);
						}
					}
				}
				// Twitter.Status status = twitter.updateStatus(statuses[0]);
				// return status.text;
			} catch (Exception e) {
				Log.e(TAG, e.toString());
				e.printStackTrace();
				return "Failed to Post";
			}
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);
			textView.setText(s);
		}
	}
}
