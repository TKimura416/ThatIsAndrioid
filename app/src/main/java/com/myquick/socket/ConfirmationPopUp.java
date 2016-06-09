package com.myquick.socket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.thatsit.android.R;

public class ConfirmationPopUp extends Activity{
	
	private TextView txtMessage;
	private TextView txtOk;
	private TextView txtCancel;
	private Intent intent;
	
	private String ip;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notification_manager);
		
		intent = getIntent();
		
		ip = intent.getExtras().getString("ip");
		
		
		txtMessage = (TextView)findViewById(R.id.txtMessage);
		txtMessage.setText(ip+" has shares file with you do you want to recieve?");
		txtOk = (TextView)findViewById(R.id.accept);
		txtOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				
				ClientChatThread send_message =  new ClientChatThread(ip,ConfirmationPopUp.this,Constants.ACCEPT);
				new Thread(send_message).start();	
				finish();
					
			}
		});
		
		txtCancel = (TextView)findViewById(R.id.rjecte);
		txtCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				ClientChatThread send_message =  new ClientChatThread(ip,ConfirmationPopUp.this,Constants.REJECT);
				new Thread(send_message).start();	
				finish();
			}
		});
		
		

			}

			

		
	
		
	

}

