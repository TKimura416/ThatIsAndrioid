package com.thatsit.android.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.thatsit.android.R;
import com.thatsit.android.Utility;
import com.thatsit.android.activities.ContactActivity;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.db.One2OneChatDb;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

public class UsersAdapter extends BaseAdapter {

	private String filter = "";
	private ArrayList<String> jids;
	private HashMap<Integer, View> viewContainer = new HashMap<>();
	private Activity activity;
	private XMPPConnection mConnection;
	private ArrayList<String> listcardname = new ArrayList<>();
	private ArrayList<String> listcardlastname = new ArrayList<>();
	private ArrayList<String> listcardprofilepic = new ArrayList<>();
	private LayoutInflater inflater;

	public UsersAdapter(ArrayList<String> jids,Activity activity,
						XMPPConnection mConnection,ArrayList<String> listcardname,
						ArrayList<String> listcardlastname,ArrayList<String> listcardprofilepic) {

		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		viewContainer = new HashMap<>();
		this.activity = activity;
		this.jids = jids;
		this.mConnection = mConnection;
		this.listcardname = listcardname;
		this.listcardlastname = listcardlastname;
		this.listcardprofilepic = listcardprofilepic;
	}

	public UsersAdapter(String filter,ArrayList<String> jids,Activity activity,
						XMPPConnection mConnection,ArrayList<String> listcardname,
						ArrayList<String> listcardlastname,ArrayList<String> listcardprofilepic) {

		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.filter = filter;
		viewContainer = new HashMap<>();
		this.activity = activity;
		this.jids = jids;
		this.mConnection = mConnection;
		this.listcardname = listcardname;
		this.listcardlastname = listcardlastname;
		this.listcardprofilepic = listcardprofilepic;
	}

	@Override
	public int getCount() {
		return jids.size();
	}

	@Override
	public String getItem(int arg0) {
		return jids.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}



	@SuppressLint("InflateParams")
	@Override
	public View getView(final int position, View convertView,ViewGroup parent) {

	//	try {
			if (!jids.get(position).startsWith(filter)) {
				return new View(activity);
			}

			final RosterEntry entry = getEntryUsingJid(jids.get(position));
			View convertviews;
			RosterListViewHolder viewHolder;
			if (viewContainer.isEmpty()	|| !viewContainer.containsKey(position)) {
				viewHolder = new RosterListViewHolder();
				convertviews = inflater.inflate(R.layout.adapter_fragements_contacts, parent, false);
				viewHolder.tvName = (TextView) convertviews.findViewById(R.id.txt_UserName);
				viewHolder.tvStatus = (TextView) convertviews.findViewById(R.id.txt_UserDescrption);
				viewHolder.tvMyQuickID = (TextView) convertviews.findViewById(R.id.txt_profileDescrption4);
				viewHolder.tvPersence = (ImageView) convertviews.findViewById(R.id.profile_picture);
				viewHolder.friend_images = (ImageView) convertviews.findViewById(R.id.friend_images);
				viewHolder.img_star = (ImageView) convertviews.findViewById(R.id.img_star);
				convertviews.setTag(viewHolder);
				viewContainer.put(position, convertviews);
			}
			else {
				convertviews = viewContainer.get(position);
				viewHolder = (RosterListViewHolder) convertviews.getTag();
			}
			String s = "no_value";
			try {
				if(!TextUtils.isEmpty(entry.getUser())) {
					s = entry.getUser().split("@")[0];
				}else{
					s = jids.get(position);
				}
			} catch (NullPointerException e) {
			}
			viewHolder.tvMyQuickID.setText(s);

			try {
				if (listcardprofilepic.get(position) != null && listcardprofilepic.size() != 0) {

					ContactActivity.options = new DisplayImageOptions.Builder()
							.displayer(new RoundedBitmapDisplayer(200))
							.showImageOnFail(R.drawable.no_img)
							.showImageForEmptyUri(R.drawable.no_img)
							.cacheOnDisk(true)
							.cacheInMemory(true)
							.build();
					ContactActivity.loader.displayImage(listcardprofilepic.get(position), viewHolder.friend_images,ContactActivity.options);
				}

				if (listcardname.get(position) != null && listcardname.size() != 0) {
					if (!listcardname.get(position).equalsIgnoreCase("")) {
						viewHolder.tvName.setText(listcardname.get(position));
					} else {
						viewHolder.tvName.setText("My Name");
					}
				}
				if (listcardlastname.get(position) != null	&& listcardlastname.size() != 0) {

					if (!listcardlastname.get(position).equalsIgnoreCase("")) {
						viewHolder.tvStatus.setText(listcardlastname.get(position));
					} else {
						viewHolder.tvStatus.setText("Hi I Am Using That's It");
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			try {
				if(entry!=null) {
					if (ThatItApplication.getApplication().getIncomingPings()
							.contains(entry.getUser())) {
						viewHolder.img_star.setVisibility(View.VISIBLE);
					} else if (!ThatItApplication.getApplication()
							.getIncomingPings().contains(entry.getUser())) {
						viewHolder.img_star.setVisibility(View.GONE);
					}
					// ### setting red star for incoming file messages
					else if (ThatItApplication.getApplication()
							.getIncomingFilePings().contains(entry.getUser())) {
						viewHolder.img_star.setVisibility(View.VISIBLE);
					} else if (!ThatItApplication.getApplication()
							.getIncomingFilePings().contains(entry.getUser())) {
						viewHolder.img_star.setVisibility(View.GONE);
					}
				}else{
					viewHolder.img_star.setVisibility(View.GONE);
				}
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
			if(entry!=null) {
				Presence presence = mConnection.getRoster().getPresence(
						entry.getUser());
				Presence.Mode userMode = presence.getMode();
				// // remove from roster List
				if (presence.getType().equals(Presence.Type.unsubscribed)) {
					try {
						mConnection.getRoster().removeEntry(entry);
					} catch (XMPPException e) {
						e.printStackTrace();
					}
				}
				if (presence.getType() == Presence.Type.available) {
					One2OneChatDb one2OneChatDb = new One2OneChatDb(activity);
					one2OneChatDb.updateMessagestatus(entry.getUser(), "R");
					if (userMode == Mode.dnd) {
						viewHolder.tvPersence.setBackgroundColor(Color
								.parseColor("#FF0000"));
					} else if (userMode == Mode.chat) {
						viewHolder.tvPersence.setBackgroundColor(Color
								.parseColor("#FF8C00"));
					} else {
						viewHolder.tvPersence.setBackgroundColor(Color
								.parseColor("#00FF00"));
					}
				} else if (presence.getType().equals(Presence.Type.subscribe)) {
					viewHolder.tvPersence.setBackgroundColor(Color
							.parseColor("#AA0000"));

				} else {
					viewHolder.tvPersence.setBackgroundColor(Color
							.parseColor("#454545"));
				}

				//Object viewDrawable = viewHolder.friend_images.getDrawable();

			}else{
				viewHolder.tvPersence.setBackgroundColor(Color
						.parseColor("#454545"));
			}
			convertviews.setTag(viewHolder);
			if (position == 0) {
				//	isAllowed = true;
				Utility.VcardLoadedOnce = true;
				Utility.stopDialog();
			}

			return convertviews;
		/*} catch (Exception e) {
			e.printStackTrace();

			if (position == 0) {
				//isAllowed = true;
				Utility.VcardLoadedOnce = true;
				Utility.stopDialog();
			}
			return new View(activity);
		}*/
	}

	private class RosterListViewHolder {
		TextView tvName;
		TextView tvStatus;
		TextView tvMyQuickID;
		ImageView tvPersence;
		ImageView friend_images;
		ImageView img_star;
	}

	private RosterEntry getEntryUsingJid(String jid) {
		jid = jid.toLowerCase();

		if (!jid.contains("@")) {
			jid = jid + "@" + mConnection.getHost();
		}
		return mConnection.getRoster().getEntry(jid);
	}
}