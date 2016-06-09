package com.thatsit.android.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.thatsit.android.R;
import com.thatsit.android.RefreshApplicationListener;
import com.thatsit.android.Utility;
import com.thatsit.android.adapter.CustomGroupMemberAdapter;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.parseutil.ParseCallbackListener;
import com.thatsit.android.parseutil.ParseUtil;
import com.myquick.filebrowser.AndroidExplorer;
import com.parse.ParseException;
import com.parse.ParseObject;

public class GroupInfoActivity extends Activity {

	public static boolean isPromtAllowed=true;
	private ListView lvMembers;
	private TextView mtvGroupName;
	private CustomGroupMemberAdapter groupMemberAdapter;
	private ParseUtil parseUtil = new ParseUtil();

	@Override
	protected void onDestroy() {
		super.onDestroy();     
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Utility.allowAuthenticationDialog=false;
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_group_info);
		
		Utility.setDeviceTypeAndSecureFlag(GroupInfoActivity.this);
		initialiseViews();
		StringTokenizer stringTokenizer = new StringTokenizer(getIntent()
				.getStringExtra("group_name"), "@");
		String groupName1 = stringTokenizer.nextToken();
		groupName1 = groupName1.substring(groupName1.indexOf("__") + 2,
				groupName1.length());
		if(groupName1.contains("%2b")){
			groupName1 = groupName1.replace("%2b", " ");
		}
		mtvGroupName.setText(groupName1);

		getGroupMembers();
	}


	/**
	 * Get all room member list.
	 */
	private void getGroupMembers() {

		Utility.startDialog(GroupInfoActivity.this);
		parseUtil.getGroupMembers(getIntent().getStringExtra("group_name"),
				new ParseCallbackListener() {
			@Override
			public void done(List<ParseObject> receipients,
					ParseException e, int requestId) {
				if (e != null) {
					groupMemberAdapter = new CustomGroupMemberAdapter(new ArrayList<String>());
				} else {
					StringTokenizer stringTokenizer = new StringTokenizer(
							receipients.get(0).getString(ThatItApplication.getApplication()
									.getString(	R.string.column_group_members))," ");

					ArrayList<String> members = new ArrayList<>();

					int size = stringTokenizer.countTokens();

					for (int i = 0; i < size; i++) {
						members.add(stringTokenizer.nextToken());
					}
					groupMemberAdapter = new CustomGroupMemberAdapter(members);
				}
				lvMembers.setAdapter(groupMemberAdapter);
				Utility.stopDialog();
			}

			@Override
			public void done(ParseException parseException,
					int requestId) {
			}
		}, -1);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(Utility.allowAuthenticationDialog==true){
			Utility.showLock(GroupInfoActivity.this);
		}
		Utility.UserPauseStatus(GroupInfoActivity.this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Utility.taskPromtOnStop(isPromtAllowed , GroupInfoActivity.this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Utility.allowAuthenticationDialog=false;
		finish();      
	}

	/**
	 * Initialse class variables.
	 */
	private void initialiseViews() {
		lvMembers = (ListView) findViewById(R.id.lvMembers);
		mtvGroupName = (TextView) findViewById(R.id.txt_PersnName);
	}
}
