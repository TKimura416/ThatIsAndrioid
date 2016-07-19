package com.thatsit.android.adapter;

import java.util.ArrayList;
import java.util.TreeMap;

import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.thatsit.android.MainService;
import com.thatsit.android.MyGroupsHelper;
import com.thatsit.android.R;
import com.thatsit.android.Utility;
import com.thatsit.android.activities.MUCActivity;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.xmpputils.Constants;
import com.thatsit.android.xmpputils.XmppManager;
import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.seasia.myquick.model.AppSinglton;

public class ChatGroupsAdapter extends BaseAdapter{

	private final Activity parentReference;
	private ArrayList<RosterGroup> rosterGroups;
	MultiUserChat muChat;

	public ChatGroupsAdapter(Activity context ,ArrayList<RosterGroup> chatGroups){
		parentReference = context;
		rosterGroups =chatGroups;
		parseGroups();
	}

	/**
	 * Display groups in alphabetic order.
	 */
	private void parseGroups(){
		TreeMap<String, RosterGroup> tMap = new TreeMap<>();
		
		for(int i =0;i<rosterGroups.size();i++){
			tMap.put(rosterGroups.get(i).getName().split("__")[1].replaceAll("%2b", " "), rosterGroups.get(i));
		}
	
		rosterGroups = new ArrayList<>();
		
		for (TreeMap.Entry<String,RosterGroup> entry: tMap.entrySet()) {
			RosterGroup holder = entry.getValue();
			rosterGroups.add(holder);
		}
	}

	@Override
	public int getCount() {
		return rosterGroups.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@SuppressLint("NewApi")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		final RosterGroup  currentRosterGroup = rosterGroups.get(position);
		
		
		
		if(convertView==null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(parentReference).inflate(R.layout.adapter_fragements_chat_groups, null);
			holder.txtvwChatGroupOptions = (TextView) convertView.findViewById(R.id.txt_groupOptions);
			holder.txtvwChatGroupname = (TextView) convertView.findViewById(R.id.txt_UserName);
			holder.img_star = (ImageView) convertView.findViewById(R.id.img_star);
			convertView.setTag(holder);

		}else{
			holder =(ViewHolder) convertView.getTag();
		}
		holder.txtvwChatGroupname.setText(currentRosterGroup.getName().split("__")[1].replaceAll("%2b", " "));
		if (ThatItApplication.getApplication().getIncomingGroupPings().contains(currentRosterGroup.getName())) {
			holder.img_star.setVisibility(View.VISIBLE);
		}else{
			holder.img_star.setVisibility(View.GONE);
		}
		
		holder.txtvwChatGroupOptions.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try{
//					muChat =new MultiUserChat(XmppManager.getInstance().getXMPPConnection(),currentRosterGroup.getName()+"@conference."+Constants.HOST, MultiUserChatManager.getInstanceFor(XmppManager.getInstance().getXMPPConnection()));
					MyGroupsHelper.selectChatGroupOption(muChat, XmppManager.getInstance().getXMPPConnection(), parentReference);
				}catch(Exception e){
					Toast.makeText(parentReference, "Could not retreive room options", Toast.LENGTH_LONG).show();
				}
			}
		});
		if(convertView!=null){
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					
					if(NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())){
					if (MainService.mService.connection != null) {
						AppSinglton.currentGroupName = rosterGroups.get(position).getName() + "@conference." + Constants.HOST;
						ThatItApplication.getApplication().getIncomingGroupPings().remove(rosterGroups.get(position).getName());
						new Thread() {
							public void run() {
								try {
									
//									MultiUserChat muChat = new MultiUserChat(MainService.mService.connection,AppSinglton.currentGroupName);
									ThatItApplication.getApplication().setCurrentGroupChatCoversation(muChat);
									
									Intent it = new Intent(ThatItApplication.getApplication(),MUCActivity.class);
									it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									ThatItApplication.getApplication().startActivity(it);
									parentReference.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											notifyDataSetChanged();
										}
									});									
								} catch (Exception ignored) {
								}

							}
						}.start();
					}
					}else{
						Toast.makeText(ThatItApplication.getApplication(), "No Network Available", Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		}

		if(position==0
				&& 
			!Utility.fragmentContact.areGroupsReady){
			//			Utility.stopDialog();
			Utility.fragmentContact.areGroupsReady= true;
			Utility.fragmentContact.progressBar_groups.setVisibility(View.GONE);
		}
		
		return convertView;
	}

	/**
	 * ViewHolder - enables you to access each list item view without the need for the look up, saving valuable processor cycles.
	 */
	public class ViewHolder{
		public TextView txtvwChatGroupname;
		public TextView txtvwChatGroupOptions;
		public ImageView img_star;
	}
}
