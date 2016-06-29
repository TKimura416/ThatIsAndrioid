package com.thatsit.android.fragement;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.seasia.myquick.asyncTasks.GetDataAsyncDaysLeft;
import com.seasia.myquick.asyncTasks.GetDataAsyncSubscriptionHistory;
import com.seasia.myquick.asyncTasks.GetSubscriptionFeeAsync;
import com.seasia.myquick.asyncTasks.UpdateSubsciptionAsync;
import com.seasia.myquick.model.AppSinglton;
import com.seasia.myquick.model.CheckSubscriptionKeyValidity;
import com.seasia.myquick.model.GetSubscriptionHistoryTemplate;
import com.seasia.myquick.model.SubcriptionFeeTemplate;
import com.seasia.myquick.model.UpdateSubsciptionTemplate;
import com.thatsit.android.MainService;
import com.thatsit.android.R;
import com.thatsit.android.Utility;
import com.thatsit.android.activities.ContactActivity;
import com.thatsit.android.adapter.CustomAdapter_PaymentSetting;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.interfaces.DaysLeftInterface;
import com.thatsit.android.interfaces.SubscriptionFeeInterface;
import com.thatsit.android.interfaces.SubscriptionHistoryInterface;
import com.thatsit.android.interfaces.UpdateSubscriptionInterface;

@SuppressLint("ValidFragment")
public class FragmentPaymentSetting extends Fragment implements OnClickListener{
	private View mView;
	private Button mBtnBasic_Setting,mBtnChat_Setting,mBtnPayment_Setting,mBtn_BuyId_PayMent;
	private TextView mTxt_DaysLeft,mTxt_ExpiryDate,mTxt_CreatedOn;
	private FragmentManager mFragmentManager;
	private FragmentTransaction mFragmentTransaction;
	private FragmentChatSetting mFragmentChatSetting;
	private FragmentBasicSetting mFragmentBasicSetting;
	private CustomAdapter_PaymentSetting mCustomAdapter_PaymentSetting;
	private ListView mlist_View;
	private TextView tv_retry;
	private String RetriveVal_UpdateSubscription;
	private int DaysLeft;
	private String renewalFee;
	private MainService mService;
	private SharedPreferences mSharedPreferences;
	private int ACTION;
	private String ExpiryDate;
	String SubscribeDate;
	private ContactActivity hostActivity;
	private LinearLayout fragInvite_tabs_lnrlayout;

	public FragmentPaymentSetting(MainService service){
		mService = service;
	}
	public FragmentPaymentSetting( ){

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((ContactActivity)getActivity()).iconStateChanger(false, false, false, true);
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		hostActivity = (ContactActivity) activity;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		mView= inflater.inflate(R.layout.fragment_account_payment_settings, container, false);
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN|WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		registerReceiver();
		initialise_Variables();
		if(getResources().getBoolean(R.bool.isTablet)){
			ContactActivity.textView_toolbar_title.setText("Payment Settings");
			fragInvite_tabs_lnrlayout.setVisibility(View.GONE);
		}
		initialise_Listener();
		
		Utility.fragPaymentSettingsOpen = true;
		mSharedPreferences = getActivity().getSharedPreferences("USERID", getActivity().MODE_WORLD_READABLE);
		AppSinglton.userId = mSharedPreferences.getString("USERID", "");
		try {

			ACTION = 1;
			new GetDataAsyncSubscriptionHistory(getActivity(),
					AppSinglton.userId,mSubscriptionHistoryInterface).execute();
			new GetDataAsyncDaysLeft(getActivity(),AppSinglton.userId,
					mDaysLeftInterface).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mView;
	}

	@Override
	public void onResume() {
		super.onResume();

		if(getResources().getBoolean(R.bool.isTablet)) {
			if (hostActivity.mDrawerLayout != null && hostActivity.navDrawerView != null) {
				hostActivity.mDrawerLayout.closeDrawer(hostActivity.navDrawerView);
			}
		}
		Utility.UserPauseStatus(getActivity());
	}

	private void registerReceiver() {
		getActivity().registerReceiver(new IncomingReceiver(),new IntentFilter("Renewal Successful"));
	}
	private void initialise_Listener() {
		mBtnBasic_Setting.setOnClickListener(this);
		mBtnChat_Setting.setOnClickListener(this);
		mBtnPayment_Setting.setOnClickListener(this);
		mBtn_BuyId_PayMent.setOnClickListener(this);
		tv_retry.setOnClickListener(this);
	}

	private void initialise_Variables() {

		mFragmentChatSetting =new FragmentChatSetting(mService);
		mFragmentBasicSetting=new FragmentBasicSetting(mService);

		fragInvite_tabs_lnrlayout = (LinearLayout)mView.findViewById(R.id.fragInvite_tabs_lnrlayout);
		mBtnBasic_Setting =(Button)mView.findViewById(R.id.basic_Setting);
		mBtnChat_Setting=(Button)mView.findViewById(R.id.chat_Setting);
		mBtnPayment_Setting=(Button)mView.findViewById(R.id.payment_Setting);
		mBtn_BuyId_PayMent = (Button) mView.findViewById(R.id.Btn_BuyNew_Id_Payset);
		mTxt_DaysLeft = (TextView) mView.findViewById(R.id.DayLeft_Val);
		mTxt_ExpiryDate  = (TextView) mView.findViewById(R.id.ExpireDate_Val);
		mTxt_CreatedOn  = (TextView) mView.findViewById(R.id.CreatedOn_Val);
		mlist_View =(ListView) mView.findViewById(R.id.list_UserId);
		tv_retry=(TextView)mView.findViewById(R.id.tv_retry);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.basic_Setting:
			try {
				mFragmentManager = getActivity().getSupportFragmentManager();
				mFragmentTransaction = mFragmentManager.beginTransaction();
				mFragmentTransaction.replace(R.id.fragmentContainer, mFragmentBasicSetting);	
				mFragmentTransaction.commit();
				//	ContactActivity.mStackChatAccount.pop();
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;

		case R.id.chat_Setting:
			try {
				mFragmentManager =getActivity().getSupportFragmentManager();
				mFragmentTransaction = mFragmentManager.beginTransaction();
				mFragmentTransaction.replace(R.id.fragmentContainer, mFragmentChatSetting);	
				mFragmentTransaction.commit();
				//ContactActivity.mStackChatAccount.pop();
				//ContactActivity.mStackChatAccount.add(mFragmentChatSetting);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;

		case R.id.tv_retry:
			try {
				if (NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {

					ACTION = 1;
					new GetDataAsyncSubscriptionHistory(getActivity(),
							AppSinglton.userId,mSubscriptionHistoryInterface).execute();
					new GetDataAsyncDaysLeft(getActivity(),AppSinglton.userId,
							mDaysLeftInterface).execute();
					mlist_View.setVisibility(View.VISIBLE);
					tv_retry.setVisibility(View.INVISIBLE);

				} else {
					Toast.makeText(getActivity(),getResources().getString(R.string.Network_Availability), Toast.LENGTH_SHORT).show();
					tv_retry.setVisibility(View.VISIBLE);
					mlist_View.setVisibility(View.INVISIBLE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}			
			break;

		default:
			break;
		}
	}

	/**
	 *  UPDATE SUBSCRIPTION INTERFACE
	 */
	UpdateSubscriptionInterface mUpdateSubscriptionInterface = new UpdateSubscriptionInterface() {

		@Override
		public void updateSubscription(UpdateSubsciptionTemplate updateSubsciption) {
			try {

				RetriveVal_UpdateSubscription=updateSubsciption.getmUpdateSubscriptionResult().getmUpdateSubscriptionParams()[0].getRetVal();
				if(RetriveVal_UpdateSubscription.equals("2")){

					ACTION = 1;

					new GetDataAsyncDaysLeft(getActivity(),AppSinglton.userId,
							mDaysLeftInterface).execute();

					new GetDataAsyncSubscriptionHistory(getActivity(),
							AppSinglton.userId,mSubscriptionHistoryInterface).execute();

				}
				else{
					Toast.makeText(getActivity(), "Renewal Error, Please Try Later..", Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 *  GET NO OF DAYS LEFT TO EXPIRE
	 */
	DaysLeftInterface mDaysLeftInterface = new DaysLeftInterface() {

		@Override
		public void daysLeft(CheckSubscriptionKeyValidity daysLeft) {
			try {
				if(daysLeft!=null){
					DaysLeft = daysLeft.getmCheckSubscriptionResult().getCheckSubscriptionParams()[0].getDaysLeft();

					if(DaysLeft<0){
						mTxt_DaysLeft.setText("0");
					}else{
						mTxt_DaysLeft.setText("" + DaysLeft);
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};


	/**
	 *  GET SUBSCRIPTION HISTORY (EXPIRY DATE)
	 */

	SubscriptionHistoryInterface mSubscriptionHistoryInterface = new SubscriptionHistoryInterface() {

		@Override
		public void subscriptionHistory(
				GetSubscriptionHistoryTemplate mGetSubscriptionHistoryTemplate) {

			try {
				if(mGetSubscriptionHistoryTemplate!=null){

					if(ACTION == 1){

						mTxt_ExpiryDate.setText(mGetSubscriptionHistoryTemplate.getGetSubscriptionHistoryResult().getGetSubscriptionHistoryParams()[0].getSubscriptionExpiryDate());
						mTxt_CreatedOn.setText(mGetSubscriptionHistoryTemplate.getGetSubscriptionHistoryResult().getGetSubscriptionHistoryParams()[0].getSubscriptionDate());
						mCustomAdapter_PaymentSetting = new CustomAdapter_PaymentSetting(getActivity(),mGetSubscriptionHistoryTemplate.getGetSubscriptionHistoryResult().getGetSubscriptionHistoryParams());
						mlist_View.setAdapter(mCustomAdapter_PaymentSetting);
						mCustomAdapter_PaymentSetting.notifyDataSetChanged();
					}
					else if(ACTION == 2){
						ExpiryDate = mGetSubscriptionHistoryTemplate.getGetSubscriptionHistoryResult().getGetSubscriptionHistoryParams()[0].getSubscriptionExpiryDate();
						ExpiryDate =ExpiryDate.replace(" ", "-");
						ExpiryDate = ExpiryDate.replace(",", "");
						checkTimePassed();
					}

				}else{
					tv_retry.setVisibility(View.VISIBLE);
					mlist_View.setVisibility(View.INVISIBLE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};


	/**
	 *  GET SUBCRIPTION FEE
	 */
	SubscriptionFeeInterface mSubscriptionFeeInterface = new SubscriptionFeeInterface() {

		@Override
		public void subscriptionFee(
				SubcriptionFeeTemplate mSubcriptionFeeTemplate) {

			if(mSubcriptionFeeTemplate != null){

				int status = Integer.parseInt(mSubcriptionFeeTemplate.getGetSubscriptionFeeResult().getStatus().getStatus());
				if(status == 1){
					renewalFee = mSubcriptionFeeTemplate.getGetSubscriptionFeeResult().getGetSubscriptionFeeParams()[0].getRenewalfee();
					String subcriptionFee = mSubcriptionFeeTemplate.getGetSubscriptionFeeResult().getGetSubscriptionFeeParams()[0].getSubscriptionfee();
				}
				else{
					Utility.showMessage("Renewal Error");
				}
			}
			else{
				Utility.showMessage("Renewal Error");
			}
		}
	};

	public void checkTimePassed(){

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("MMM-dd-yyyy");
		String currentDate = df.format(cal.getTime());
		Date formattedcurrentDate = null;
		try {
			formattedcurrentDate = df.parse(currentDate);
		}
		catch (ParseException e) {
			e.printStackTrace();
		}


		Date expiryDateFormatted = null;
		try {
			expiryDateFormatted = df.parse(ExpiryDate);
		}
		catch (ParseException e) {
			e.printStackTrace();
		}

		if(formattedcurrentDate.compareTo(expiryDateFormatted)>0){ // shld be >
			// Current Date exceeded Expiry Date
			new GetSubscriptionFeeAsync(getActivity(),
					mSubscriptionFeeInterface).execute();
		}else{
			openPrompt();
		}
	}
	private void openPrompt() {
		try {
			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
			// set title
			alertDialogBuilder.setTitle("That's It");		
			alertDialogBuilder.setMessage("Your That's It ID is still active.You can renew once it gets expired.");		
			alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			});

			final AlertDialog dialog = alertDialogBuilder.create();
			dialog.show();
			dialog.setCancelable(false);
			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Broadcast receiver to display alert for connectiom error incase no network or to open fragment payment setting.
	 */
	public class IncomingReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (intent.getAction().equals("Renewal Successful")) {
					renewalFee = "1.99";

					new UpdateSubsciptionAsync(getActivity(),AppSinglton.userId,
							renewalFee,SubscribeDate,mUpdateSubscriptionInterface).execute();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
