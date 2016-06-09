package com.thatsit.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.thatsit.android.R;
import com.thatsit.android.xmpputils.XmppManager;
import com.seasia.myquick.model.GetSubscriptionHistoryParams;


public class CustomAdapter_PaymentSetting extends BaseAdapter {
	final GetSubscriptionHistoryParams[] mHistory;
	private String MY_QUICK_ID ="";


	private final LayoutInflater layoutinflater;
	public CustomAdapter_PaymentSetting(Context context,GetSubscriptionHistoryParams[] subscriptionHistory) {

		layoutinflater = LayoutInflater.from(context);
		this.mHistory=subscriptionHistory;
		if(XmppManager.getInstance().getXMPPConnection()!=null && XmppManager.getInstance().getXMPPConnection().isConnected()){
			MY_QUICK_ID= XmppManager.getInstance().getXMPPConnection().getUser().split("@")[0];
		}
	}

	@Override
	public int getCount() {
		return mHistory.length;
	}

	@Override
	public Object getItem(int arg0) {
		return arg0;
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(final int position, View convertview, ViewGroup arg2) {

		ViewHolder holder = null;

		try {
			if (convertview == null) {
				holder = new ViewHolder();
				convertview = layoutinflater.inflate(R.layout.adapter_fragment_payment_setting, null);
				holder.Txt_CreatedOn = (TextView) convertview.findViewById(R.id.txt_CreatedOn);
				holder.Txt_Action = (TextView) convertview.findViewById(R.id.txt_Action);
				holder.Txt_Id = (TextView) convertview.findViewById(R.id.txt_Id);
				holder.Txt_Amount = (TextView) convertview.findViewById(R.id.txt_Amount);
				convertview.setTag(holder);

			} else {
				holder = (ViewHolder) convertview.getTag();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		holder.Txt_CreatedOn.setText((mHistory[position].getSubscriptionDate()));
		holder.Txt_Action.setText(((mHistory[position].getSubscriptionAction())));
		holder.Txt_Id.setText((MY_QUICK_ID));
		holder.Txt_Amount.setText((mHistory[position].getSubscriptionFee())+"$");
		//holder.Txt_Amount.setText("1.99"+"$");
		return convertview;
	}

	/**
	 * ViewHolder - enables you to access each list item view without the need for the look up, saving valuable processor cycles.
	 */
	class ViewHolder {
		TextView Txt_Action,Txt_CreatedOn,Txt_Id,Txt_Amount;
	}
}
