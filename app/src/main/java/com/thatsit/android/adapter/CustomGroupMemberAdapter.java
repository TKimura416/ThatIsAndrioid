package com.thatsit.android.adapter;

import java.util.ArrayList;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.VCard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.thatsit.android.R;
import com.thatsit.android.activities.ContactActivity;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.view.CircularImageView;
import com.thatsit.android.xmpputils.XmppManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class CustomGroupMemberAdapter  extends ArrayAdapter<String>{

	private Context context;
	private ArrayList<String> memberJids= new ArrayList<>();
	XMPPConnection connection;
	Handler vCardHandler;
	LayoutInflater inflater;
	private Bitmap bitmapImage ;

	public CustomGroupMemberAdapter(ArrayList<String>  ids) {

		super(ThatItApplication.getApplication(), R.layout.groupinfo_item );
		this.context= ThatItApplication.getApplication();
		connection = XmppManager.getInstance().getXMPPConnection();
		vCardHandler = new Handler();
		this.memberJids = ids;
		inflater = (LayoutInflater) ThatItApplication.getApplication().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public String getItem(int position) {
		return memberJids.get(position);
	}

	@Override
	public int getCount() {
		return memberJids.size();
	}


	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {


		//		INITIALISE VIEWS
		convertView =inflater.inflate(R.layout.groupinfo_item, null);
		final ImageView mMemberImage = (ImageView)convertView.findViewById(R.id.friend_images);
		final TextView mPseudoName= (TextView)convertView.findViewById(R.id.txt_UserDescrption);
		final TextView mMemberName  = (TextView)convertView.findViewById(R.id.txt_UserName);
		final TextView mJabberId  = (TextView)convertView.findViewById(R.id.txt_profileDescrption4);

		//		SET VALUES
		final VCard vcard = new VCard();
		new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					String complete_jid = memberJids.get(position);
					if(!complete_jid.contains("@")){
						complete_jid = complete_jid+"@"+connection.getHost();
					}
					vcard.load(connection,complete_jid);
				} catch (Exception e) {
					e.printStackTrace();
				}

				vCardHandler.post(new Runnable() {
					@Override
					public void run() {

						if(vcard.getField("profile_picture_url")!=null){

							setUniversalLoader();
							ContactActivity.loader.displayImage(vcard.getField("profile_picture_url").replaceAll(" ", "%20"), mMemberImage, ContactActivity.options);
						}
						else{
							//bitmapImage = BitmapFactory.decodeResource(context.getResources(),R.drawable.no_img);
							//mMemberImage.setImageBitmap(bitmapImage);
							setUniversalLoader();
							ContactActivity.loader.displayImage("", mMemberImage, ContactActivity.options);
						}


						if(TextUtils.isEmpty(vcard.getFirstName())){
							mMemberName.setText("My Name");
						}else{
							mMemberName.setText(vcard.getFirstName()+"");
						}

						if(TextUtils.isEmpty(vcard.getLastName())){
							mPseudoName.setText("Hi I Am Using That's It");
						}else{
							mPseudoName.setText(vcard.getLastName()+"");
						}
						mJabberId.setText(memberJids.get(position).toLowerCase()+"");
					}
				});
			}
		}).start();

		return convertView;
	}

	private void setUniversalLoader() {
		ContactActivity.options = new DisplayImageOptions.Builder()
				.displayer(new RoundedBitmapDisplayer(200))
				.showImageOnFail(R.drawable.no_img)
				.showImageForEmptyUri(R.drawable.no_img)
				.showImageOnLoading(R.drawable.no_img)
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.build();
	}
}
