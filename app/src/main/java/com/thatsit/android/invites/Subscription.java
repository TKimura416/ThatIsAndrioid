
package com.thatsit.android.invites;


import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.thatsit.android.MainService;
import com.thatsit.android.R;
import com.thatsit.android.adapter.PresenceAdapter;
import com.thatsit.android.parcelable.Contact;
import com.thatsit.android.xmpputils.XmppManager;

/**
 * This activity is used to accept a subscription request.
 */

public class Subscription extends Activity {

	private static final Intent SERVICE_INTENT = new Intent();
	private static final String TAG = Subscription.class.getSimpleName();
	private String mContact;
	private MainService mService;
	private final ServiceConnection mServConn = new MainServiceConnection();
//	private final ConnectionBroadcastReceiver mReceiver = new ConnectionBroadcastReceiver();
	private final MyOnClickListener mClickListener = new MyOnClickListener();
	private final Handler handler;
	private final XmppManager xmppManager;
	private final XMPPConnection connection;

	static {
		SERVICE_INTENT.setComponent(new ComponentName("com.thatsit.android", "com.thatsit.android.MainService"));
	}

	/**
	 * Constructor.
	 */
	public Subscription() {
		handler = new Handler();
		xmppManager = XmppManager.getInstance();
		connection = xmppManager.getXMPPConnection();
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.subscription);

			findViewById(R.id.SubscriptionAccept).setOnClickListener(mClickListener);
			findViewById(R.id.SubscriptionRefuse).setOnClickListener(mClickListener);

			Contact c = new Contact(getIntent().getData());
			mContact = c.getJID();
			TextView tv = (TextView) findViewById(R.id.SubscriptionText);
			String str = String.format(getString(R.string.SubscriptText), mContact);
			tv.setText(str);
//			this.registerReceiver(mReceiver, new IntentFilter(MainService.CONNECTION_CLOSED));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		bindService(SERVICE_INTENT, mServConn, BIND_AUTO_CREATE);
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		unbindService(mServConn);
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
//		this.unregisterReceiver(mReceiver);
	}

	/**
	 * Send the presence stanza.
	 * 
	 * @param p presence stanza
	 */


	private void sendPresence(Presence p) {
		try {
			PresenceAdapter preAdapt = new PresenceAdapter(p); 
			Presence presence2 = new Presence(PresenceType.getPresenceTypeFrom(preAdapt.getType()));
			presence2.setTo(p.getTo());
			presence2.setFrom(connection.getUser());
			connection.sendPacket(presence2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Event simple click on buttons.
	 */
	private class MyOnClickListener implements OnClickListener {

		/**
		 * Constructor.
		 */
		public MyOnClickListener() {
		}

		@Override
		public void onClick(View v) {
			Presence presence = null;
			switch (v.getId()) {
			case R.id.SubscriptionAccept:
				try {
					presence = new Presence(Type.subscribed);
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}

			case R.id.SubscriptionRefuse:
				try {
					presence = new Presence(Type.unsubscribed);
					Toast.makeText(Subscription.this, getString(R.string.SubscriptRefused),
							Toast.LENGTH_SHORT).show();
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}

			default:
			}

			if (presence != null) {
				presence.setTo(mContact);
				sendPresence(presence);
			}
			finish();
		}
	}

	/**
	 * The ServiceConnection used to connect to the Beem service.
	 */
	private class MainServiceConnection implements ServiceConnection {

		/**
		 * Constructor.
		 */
		public MainServiceConnection() {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			mService = ((MainService.MyBinder) binder).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}
	}
}
