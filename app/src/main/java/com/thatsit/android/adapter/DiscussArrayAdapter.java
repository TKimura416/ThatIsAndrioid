package com.thatsit.android.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.jivesoftware.smack.XMPPConnection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.thatsit.android.R;
import com.thatsit.android.activities.ContactActivity;
import com.thatsit.android.beans.OneBubble;
import com.thatsit.android.db.One2OneChatDb;
import com.thatsit.android.encryption.helper.EncryptionManager;
import com.thatsit.android.xmpputils.XmppManager;

public class DiscussArrayAdapter extends BaseAdapter {
    private OneBubble bubble;
    private final Activity activity;
    private XMPPConnection connection;
    private XmppManager xmppManager;
    private final HashMap<Integer, View> viewContainer = new HashMap<>();
    private String getMessageStatus;
    private final List<OneBubble> messages = new ArrayList<>();
    final int EMPTY = 0;
    private final EncryptionManager encryptionManager;
    String revisedMessage;
    private String recipientPhoto = null;
    private final One2OneChatDb dbClientInstance;
    private final ListView listView;
    private final LayoutInflater inflater;

    public void add(OneBubble newBubble) {
        if (messages.size() == EMPTY) {
            messages.add(newBubble);
        } else {
            OneBubble oldBubble = messages.get(messages.size() - 1);
            if (oldBubble.isOwner && newBubble.isOwner) {
                messages.add(newBubble);
            }
            if (oldBubble.isOwner && !newBubble.isOwner) {
                messages.add(newBubble);
            }
            if (!oldBubble.isOwner && newBubble.isOwner) {
                messages.add(newBubble);
            }
            if (!oldBubble.isOwner && !newBubble.isOwner) {
                messages.add(newBubble);
            }
        }
        notifiAdapter();
    }

    public void remove(int position) {
        messages.remove(position);
        notifyDataSetChanged();
    }

    public DiscussArrayAdapter(Activity activity, String chatRecipientPhoto, ListView listView) {

        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listView = listView;
        this.activity = activity;
        this.recipientPhoto = chatRecipientPhoto;
        dbClientInstance = new One2OneChatDb(activity);
        encryptionManager = new EncryptionManager();
    }

    @Override
    public int getCount() {
        return this.messages.size();
        /*if(this.messages.size() <= FragmentChatScreen.itemSize){
            return this.messages.size();
        }
           else{
            return FragmentChatScreen.itemSize;
        }*/

    }

    @Override
    public OneBubble getItem(int index) {
        return this.messages.get(index);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View convertviews;
        RosterListViewHolder viewHolder;
        bubble = getItem(position);
        if (viewContainer.isEmpty() || !viewContainer.containsKey(position)) {

            viewHolder = new RosterListViewHolder();

            convertviews = inflater.inflate(R.layout.adapter_fragment_chat_discuss, parent, false);

            viewHolder.chat_layout = (LinearLayout) convertviews.findViewById(R.id.adapterChatSender_lnr);
            viewHolder.message = (TextView) convertviews.findViewById(R.id.adapterChatSender_txt_chatDetails);
            viewHolder.firstname = (TextView) convertviews.findViewById(R.id.adapterChatSender_text_userName);
            viewHolder.time = (TextView) convertviews.findViewById(R.id.adapterChatSender_text_time);
            viewHolder.online_img = (ImageView) convertviews.findViewById(R.id.adapterChatSender_img_online);
            viewHolder.tick_img_deliver = (ImageView) convertviews.findViewById(R.id.adapterChatUser_img_tick_deliver);
            viewHolder.tick_img_read = (ImageView) convertviews.findViewById(R.id.adapterChatUser_img_tick_read);
            viewHolder.friend_img = (ImageView) convertviews.findViewById(R.id.adapterChatSender_img_senderPic);
            viewHolder.img_relLayout = (RelativeLayout) convertviews.findViewById(R.id.rltv_circularImage);
            viewHolder.rel_left_arrow = (RelativeLayout) convertviews.findViewById(R.id.rel_left_arrow);
            viewHolder.rel_right_arrow = (RelativeLayout) convertviews.findViewById(R.id.rel_right_arrow);
            convertviews.setTag(viewHolder);
            viewContainer.put(position, convertviews);

        } else {
            convertviews = viewContainer.get(position);
            viewHolder = (RosterListViewHolder) convertviews.getTag();
        }
            revisedMessage = processSmileyCodes(bubble.message);
            viewHolder.message.setText(encryptionManager.decryptPayload(revisedMessage));
            viewHolder.firstname.setText(bubble.name);

            if (recipientPhoto != null) {
                //viewHolder.friend_img.setImageDrawable(recipientPhoto);
                    setUniversalLoader();
                ContactActivity.loader.displayImage(recipientPhoto, viewHolder.friend_img, ContactActivity.options);
            } else {
                //viewHolder.friend_img.setImageResource(R.drawable.no_img);
                setUniversalLoader();
                ContactActivity.loader.displayImage("", viewHolder.friend_img, ContactActivity.options);
            }

            if (bubble.isVisibleTime) {
                viewHolder.message.setBackgroundColor(Color.TRANSPARENT);
                viewHolder.chat_layout.setGravity(Gravity.CENTER_HORIZONTAL);
                viewHolder.message.setTextColor(Color.BLACK);
                viewHolder.firstname.setVisibility(View.GONE);
            } else {
                if (bubble.isOwner) {
                    try {
                        viewHolder.rel_left_arrow.setVisibility(View.INVISIBLE);
                        viewHolder.rel_right_arrow.setVisibility(View.VISIBLE);
                        viewHolder.chat_layout.setGravity(Gravity.RIGHT);
                        viewHolder.firstname.setText("You");
					int lastpos = messages.size()-1;
					OneBubble bubbles = getItem(lastpos);
					if(bubbles.messageStatus.equals("R")){
						viewHolder.tick_img_deliver.setVisibility(View.VISIBLE);
						viewHolder.tick_img_read.setVisibility(View.VISIBLE);
					}
					else{
                        getMessageStatus = bubble.messageStatus;
                        if (getMessageStatus.equals("R")) {
                            viewHolder.tick_img_deliver.setVisibility(View.VISIBLE);
                            viewHolder.tick_img_read.setVisibility(View.VISIBLE);
                        } else if (getMessageStatus.equals("D")) {

                            viewHolder.tick_img_deliver.setVisibility(View.VISIBLE);
                            viewHolder.tick_img_read.setVisibility(View.GONE);
                        }
                        }
                        viewHolder.img_relLayout.setVisibility(View.GONE);
                        viewHolder.time.setText(getDate(bubble.timestamp, "EEE MMM dd HH:mm:ss"));
                    } catch (Exception e) {
                        viewHolder.img_relLayout.setVisibility(View.GONE);
                        viewHolder.time.setText(getDate(bubble.timestamp, "EEE MMM dd HH:mm:ss"));
                        e.printStackTrace();
                    }
                } else {
                    viewHolder.rel_left_arrow.setVisibility(View.VISIBLE);
                    viewHolder.rel_right_arrow.setVisibility(View.INVISIBLE);
                    viewHolder.chat_layout.setGravity(Gravity.LEFT);
                    viewHolder.message.setTextColor(Color.BLACK);
                    viewHolder.firstname.setVisibility(View.VISIBLE);
                    viewHolder.online_img.setVisibility(View.VISIBLE);
                    viewHolder.img_relLayout.setVisibility(View.VISIBLE);
                    viewHolder.tick_img_deliver.setVisibility(View.GONE);
                    viewHolder.tick_img_read.setVisibility(View.GONE);
                    viewHolder.time.setText(getDate(bubble.timestamp, "EEE MMM dd HH:mm:ss"));
                }
            }
            String msgSubject = "";
            if (bubble.messageSubject != null) {
                msgSubject = bubble.messageSubject;
                if (!bubble.messageSubject.equalsIgnoreCase("")) {
                    if (dbClientInstance.isFileDownloaded(msgSubject)) {
                        viewHolder.message.setTextColor(Color.BLUE);
                    }
                }
            }
            convertviews.setTag(R.string.msg_subject_key, msgSubject);

            listView.setSelection(getCount() - 1);

        return convertviews;
    }

    private void setUniversalLoader() {
        ContactActivity.options = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer(200))
                .showImageOnFail(R.drawable.no_img)
                .showImageForEmptyUri(R.drawable.no_img)
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .build();
    }


    @SuppressLint("SimpleDateFormat")
    public static String getDate(long milliSeconds, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public void notifiAdapter() {
        this.notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private String processSmileyCodes(String message) {

        try {
            for (int index = 0; index < ImageAdapter.mThumbIdsForDispatch.length; index++) {
                message = message.replaceAll(ImageAdapter.mThumbIdsForDispatch[index], ImageAdapter.mThumbIds[index]);
            }
        } catch (Exception e) {
            return message;
        }
        return message;
    }

    /**
     * Enables you to access each list item view without the need for the look up, saving valuable processor cycles.
     */
    private class RosterListViewHolder {
        TextView time;
        ImageView tick_img_deliver, tick_img_read;
        TextView message;
        TextView firstname;
        ImageView online_img, friend_img;
        LinearLayout chat_layout;
        RelativeLayout img_relLayout;
        RelativeLayout rel_left_arrow, rel_right_arrow;
    }
}