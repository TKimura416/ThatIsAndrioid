package com.thatsit.android.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.thatsit.android.R;
import com.thatsit.android.Utility;
import com.thatsit.android.activities.ContactActivity;
import com.thatsit.android.beans.TemplateGroupMessageHolder;
import com.thatsit.android.encryption.helper.EncryptionManager;
import com.thatsit.android.xmpputils.Constants;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.seasia.myquick.model.AppSinglton;


public class MyGroupAdapter extends BaseAdapter{

	private final ArrayList<TemplateGroupMessageHolder> messagesMUC;
	private final HashMap<Integer,View> viewContainer = new HashMap<>();
	private final Context context;
	private final XMPPConnection connection;
	private final Handler vCardHandler = new Handler();
	private final EncryptionManager encryptionManager=new EncryptionManager();
	private final ListView listView;

	public MyGroupAdapter(Context context,final XMPPConnection connection,ArrayList<TemplateGroupMessageHolder> messagesMUC,ListView listView) {
		this.context=context;
		this.messagesMUC = messagesMUC;
		this.connection = connection;
		this.listView = listView;
	}

	@Override
	public int getCount() {
		return messagesMUC.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		View convertviews;

		if (viewContainer.isEmpty() || !viewContainer.containsKey(position)) {
			holder = new ViewHolder();	
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertviews = inflater.inflate(R.layout.adapter_group_chat_modified,parent, false);

			holder.txt_message = (TextView) convertviews.findViewById(R.id.adapterChatSender_text_userName);
			holder.rel_left_arrow = (RelativeLayout) convertviews.findViewById(R.id.rel_left_arrow);
			holder.rel_right_arrow = (RelativeLayout) convertviews.findViewById(R.id.rel_right_arrow);
			holder.profilePic = (ImageView) convertviews.findViewById(R.id.profilePic);
			holder.adapterChatUser_img_tick= (ImageView) convertviews.findViewById(R.id.adapterChatUser_img_tick);
			holder.lvUpper= (LinearLayout) convertviews.findViewById(R.id.lvUpper);

			holder.txtAddedRemoved=(TextView)convertviews.findViewById(R.id.tvAddedRemoved);
			holder.txtAddedRemoved.setVisibility(View.GONE);

			convertviews.setTag(holder);
			viewContainer.put(position, convertviews);
		}
		else
		{
			convertviews=viewContainer.get(position);
			holder = (ViewHolder) convertviews.getTag();
		}

		final VCard vcard = new VCard();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String complete_jid = messagesMUC.get(position).getJid();





					if(!complete_jid.contains("@")){
						complete_jid = complete_jid+"@"+connection.getHost();
					}
					VCardManager.getInstanceFor(connection).loadVCard(complete_jid);
//					vcard.load(connection,complete_jid);
				} catch (Exception e) {
					e.printStackTrace();
				}
				vCardHandler.post(new Runnable() {
					@Override
					public void run() {

						try {
							ContactActivity.options = new DisplayImageOptions.Builder()
									.displayer(new RoundedBitmapDisplayer(200))
									.showImageOnFail(R.drawable.no_img)
									.showImageForEmptyUri(R.drawable.no_img)
									.cacheInMemory(true)
									.cacheOnDisk(true)
									.build();
							ContactActivity.loader.displayImage(vcard.getField("profile_picture_url"), holder.profilePic, ContactActivity.options);
						} catch (Exception e) {
							e.printStackTrace();
						}			
					}
				});
			}
		}).start();

		try {
			String message = encryptionManager.decryptPayload(messagesMUC.get(position).getMessage().toString());
			message = Utility.processSmileyCodes(message);

			holder.txt_message.setTextColor(Color.BLACK);

			if(message.startsWith(Constants.ADD_PERSON) || message.startsWith(Constants.LEFT_PERSON)){

				StringTokenizer stringTokenizer = new StringTokenizer(message,"(");
				String message_2 = stringTokenizer.nextToken();
				String message_original = stringTokenizer.nextToken();

				holder.lvUpper.setVisibility(View.GONE);
				holder.txtAddedRemoved.setVisibility(View.VISIBLE);
				try {
					holder.txtAddedRemoved.setText("("+message_original);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{			
				holder.txt_message.setText(message);
			}

			if(messagesMUC.get(position).getJid().equalsIgnoreCase(AppSinglton.thatsItPincode)){
				holder.rel_left_arrow.setVisibility(View.INVISIBLE);
				holder.rel_right_arrow.setVisibility(View.VISIBLE);
				holder.adapterChatUser_img_tick.setVisibility(View.VISIBLE);

			}else {
				holder.rel_right_arrow.setVisibility(View.INVISIBLE);
				holder.rel_left_arrow.setVisibility(View.VISIBLE);
				holder.adapterChatUser_img_tick.setVisibility(View.INVISIBLE);
			}
			listView.setSelection(getCount() - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return convertviews;
	}

	private class ViewHolder{
		TextView txt_message,txtAddedRemoved;
		RelativeLayout rel_left_arrow;
		RelativeLayout rel_right_arrow;
		LinearLayout lvUpper;
		ImageView profilePic,adapterChatUser_img_tick;
	}
}

