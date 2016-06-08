package com.thatsit.android.parseutil;

import java.util.List;
import java.util.StringTokenizer;
import android.content.Context;
import android.util.Log;
import com.thatsit.android.R;
import com.thatsit.android.Utility;
import com.thatsit.android.application.ThatItApplication;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.seasia.myquick.model.AppSinglton;

public class ParseUtil {

	/**
	 * 
	 * <p>
	 * This method add senderId<i>(AppSinglton.thatsItPincode)</i> and receipient id ParseServer.
	 * This should be called whenever add request is sent to a user.  
	 * </p>
	 * @param context
	 * @param receipientId 
	 * @param saveCallback
	 */
	public  void addRequest(Context context , String receipientId ,ParseCallbackListener saveCallback ,final int requestId){
		ParseObject testObject = new ParseObject(context.getResources().getString(R.string.table_roster));
		testObject.put(context.getResources().getString(R.string.column_receipient), receipientId );
		testObject.put(context.getResources().getString(R.string.column_sender), AppSinglton.thatsItPincode );
		testObject.put(context.getResources().getString(R.string.column_operation_decider), 1/** 1 : - means 'A' sends request to 'B' **/);
		ParseCommonCallback saveCallback1 = new ParseCommonCallback(receipientId, context, saveCallback,requestId);
		testObject.saveInBackground(saveCallback1);
	}


	private String parseThatsitId(String id){
		id = id.toUpperCase();
		if(id.contains("@"))
			id = id.substring(0, id.indexOf("@")).toUpperCase();
		return id;
	}



	/**
	 * Create entry of group on parse
	 * 
	 * @param groupName
	 */
	public void joinGroup(final String groupName,final String creator,final ParseCallbackListener callback,final int requestId){

		StringTokenizer stringTokenizer = new StringTokenizer(groupName,"@");
		String groupName1= stringTokenizer.nextToken();

		final String memberName1 = parseThatsitId(creator);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(ThatItApplication.getApplication().getResources().getString(R.string.class_groups));
		//	    query.whereEqualTo(ThatItApplication.getApplication().getResources().getString(R.string.column_group_members),memberName1);
		query.whereEqualTo(ThatItApplication.getApplication().getResources().getString(R.string.column_group_name),groupName1 );
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> invites, ParseException e) {
				try {
					//progressDialog.dismiss();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				if (e == null) {
					//	        		SPLIT JID till @
					StringTokenizer stringTokenizer = new StringTokenizer(groupName,"@");
					String groupName= stringTokenizer.nextToken();
					String creator1 = parseThatsitId(creator);
					String members= "";



					if(invites.size()>0){
						members= invites.get(0)
								.getString(ThatItApplication
										.getApplication()
										.getString(R.string.column_group_members));


						members = members.replace(creator1, "");

						members = members.replace("  ", " ").trim();

						if(members.length()>=0 ){
							creator1 = members+" "+creator1;
						}else creator1 = members;

					}

					ParseObject parseObject =new ParseObject(ThatItApplication.getApplication().getResources().getString(R.string.class_groups));
					if(invites.size()>0){
						parseObject = invites.get(0);
					}

					parseObject.put(ThatItApplication.getApplication().getResources().getString(R.string.column_group_members), creator1);
					parseObject.put(ThatItApplication.getApplication().getResources().getString(R.string.column_group_name), groupName);
					ParseCommonCallback saveCallback1 = new ParseCommonCallback(creator1, ThatItApplication.getApplication(), callback,requestId);
					parseObject.saveInBackground(saveCallback1);
				} else{
					callback.done(e, requestId);
				}
			}
		});
	}



	/**
	 * 
	 * <p>
	 * This method remove senderId<i>(AppSinglton.thatsItPincode)</i> and receipient id from ParseServer.
	 * </p>
	 * @param context
	 * @param receipientId 
	 * @param saveCallback
	 */
	public  void leaveGroup( String groupName,String memberName ,final ParseCallbackListener saveCallback ,final int requestId){

		//		ParseObject parseObject = new ParseObject(ThatItApplication.getApplication().getResources().getString(R.string.column_group_name));
		final String	memberName1 = parseThatsitId(memberName);

		final ParseCommonCallback deleteCallback = new ParseCommonCallback(memberName1, ThatItApplication.getApplication(), saveCallback,requestId);

		ParseQuery<ParseObject> query = ParseQuery.getQuery(ThatItApplication.getApplication().getResources().getString(R.string.class_groups));

		query.whereContains(ThatItApplication.getApplication().getResources().getString(R.string.column_group_members),memberName1);
		query.whereEqualTo(ThatItApplication.getApplication().getResources().getString(R.string.column_group_name),groupName );
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> invites, ParseException e) {
				try {
					//progressDialog.dismiss();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				if (e == null) {
					// iterate over all messages and delete them
					for(int i =0;i<invites.size()-1;i++){
						leaveGroup(invites.get(i), memberName1,null);
					}

					if(invites.size()>0){
						leaveGroup(invites.get(invites.size()-1)  , memberName1,deleteCallback);
					}else{
						try {
							saveCallback.done(null, requestId);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}

				}else{
					saveCallback.done(e, requestId);
				} 
			}
		});
	}

	private void leaveGroup(ParseObject parseObject,String memberName ,ParseCommonCallback  parseCommonCallback){
		String members= parseObject.getString(ThatItApplication.getApplication().getString(R.string.column_group_members));
		if(members.replace(" ", "").trim().length()==0){
			//    		parse group delete operation
			try {
				if(parseCommonCallback!=null){
					parseObject.deleteInBackground(parseCommonCallback);
				}else{
					parseObject.deleteInBackground();	
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}else{
			members = members.replace(memberName , "");
			members = members.replace("  ", " ").trim();

			if(members.replace(" ", "").trim().length()==0){
				parseObject.deleteInBackground();
			}else{
				parseObject.put(ThatItApplication.getApplication().getString(R.string.column_group_members), members);;
			}

			parseObject.saveInBackground();
		}


	}

	/**
	 * 
	 * <p>
	 * This method remove senderId<i>(AppSinglton.thatsItPincode)</i> and receipient id from ParseServer.
	 * </p>
	 * @param context
	 * @param receipientId 
	 * @param saveCallback
	 */
	public void removeRequest(Context context ,String senderId, String receipientId ,final ParseCallbackListener saveCallback ,final int requestId){

		context = ThatItApplication.getApplication();

		receipientId = parseThatsitId(receipientId);
		senderId = parseThatsitId(senderId);

		final ParseCommonCallback deleteCallback = new ParseCommonCallback(receipientId, context, saveCallback,requestId);

		ParseQuery<ParseObject> query = ParseQuery.getQuery(context.getResources().getString(R.string.table_roster));

		query.whereEqualTo(context.getResources().getString(R.string.column_sender), senderId );
		query.whereEqualTo(context.getResources().getString(R.string.column_receipient), receipientId );

		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> invites, ParseException e) {
				try {
					//progressDialog.dismiss();
				} catch (Exception e1) {
					//						e1.printStackTrace();
				}
				if (e == null) {
					// iterate over all messages and delete them


					for(int i =0;i<invites.size()-1;i++){
						invites.get(i).deleteInBackground();
					}
					if(invites.size()>0){
						invites.get(invites.size()-1).deleteInBackground(deleteCallback);
					}else{
						saveCallback.done(null, requestId);
					}

				} 
			}
		});

	}

	/**
	 * 
	 * <p>
	 * This method remove senderId<i>(AppSinglton.thatsItPincode)</i> and receipient id from ParseServer.
	 * </p>
	 * @param context
	 * @param receipientId 
	 * @param saveCallback
	 */
	public  void areExists(Context context , final ParseCallbackListener saveCallback ,final int requestId){
		ParseQuery<ParseObject> query = ParseQuery.getQuery(context.getResources().getString(R.string.table_roster));
		query.whereEqualTo(context.getResources().getString(R.string.column_sender) , Utility.getUserName());
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> phoneList, ParseException e) {
				saveCallback.done(phoneList, e,requestId);
			}
		});
	}

	/**
	 * 
	 * <p>
	 * This method remove senderId<i>(AppSinglton.thatsItPincode)</i> and receipient id from ParseServer.
	 * </p>
	 * @param context
	 * @param receipientId 
	 * @param saveCallback
	 */
	public  void isExists(Context context , String senderId ,String receipientId,final ParseCallbackListener saveCallback ,final int requestId){

		senderId = parseThatsitId(senderId);
		receipientId = parseThatsitId(receipientId);

		ParseQuery<ParseObject> query = ParseQuery.getQuery(context.getResources().getString(R.string.table_roster));
		query.whereEqualTo(context.getResources().getString(R.string.column_sender) , senderId);
		query.whereEqualTo(context.getResources().getString(R.string.column_receipient) , receipientId);

		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> phoneList, ParseException e) {
				saveCallback.done(phoneList, e,requestId);
			}
		});
	}

	public void updateOperation(final Context context , String senderId ,String receipientId ,final ParseCallbackListener callback ,final int requestId , final ParseOperationDecider decider){
		receipientId = parseThatsitId(receipientId);
		senderId = parseThatsitId(senderId);

		final ParseCommonCallback operation_callback = new ParseCommonCallback(receipientId, context, callback,requestId);

		ParseQuery<ParseObject> query = ParseQuery.getQuery(context.getResources().getString(R.string.table_roster));

		query.whereEqualTo(context.getResources().getString(R.string.column_sender), senderId );
		query.whereEqualTo(context.getResources().getString(R.string.column_receipient), receipientId );

		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> invites, ParseException e) {
				try {
					//	progressDialog.dismiss();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				if (e == null) {
					// iterate over all messages and delete them
					int operation=-1;

					switch (decider) {

					case FRIEND_REQUEST_SENT:
						operation=1;
						break;

					case FRIEND_REQUEST_RECEIVED:
						operation=2;
						break;


					case FRIEND_REQUEST_ACCEPTED:
						operation=3;
						break;

					case FRIEND_REQUEST_DECLINED:
						operation=4;
						break;

					case FRIEND_REQUEST_ACCEPTED_RESPONSE:
						//		    				operation=5;
						break;

					case FRIEND_REQUEST_DECLINED_RESPPONSE:
						//		    				operation=6;
						break;
					}

					for(int i =0;i<invites.size()-1;i++){
						ParseObject object = invites.get(i);
						object.put(context.getResources().getString(R.string.column_operation_decider),operation);
						object.saveInBackground();
					}

					if(invites.size()>0){
						ParseObject object = invites.get(invites.size()-1);
						object.put(context.getResources().getString(R.string.column_operation_decider),operation);
						object.saveInBackground(operation_callback);
					}else{
						callback.done(null, requestId);
					}

				} 
			}
		});
	}

	class ParseCommonCallback implements SaveCallback,DeleteCallback{

		String jid ="";
		Context context;
		ParseCallbackListener parseCallbackListener;
		int requestId=-1;

		/**
		 * Save jid temporarily and starts dialog
		 * 
		 * @param jid
		 */
		public ParseCommonCallback(String jid,Context context,ParseCallbackListener callbackListener,int requestId) {
			this.context=context;
			this.parseCallbackListener = callbackListener;
			this.requestId = requestId;
			setReceipientId(jid);
		}

		/**
		 * Save jid temporarily and starts dialog
		 * 
		 * @param jid
		 */
		public void setReceipientId(String jid){
			this.jid=jid;
			onPreExecute();
		}

		/**
		 * It loads dialog
		 */
		public void onPreExecute(){
			try {
				//progressDialog = new ProgressDialog(context);
				//				progressDialog.setMessage(context.getResources().getString(R.string.sendingRequest));
				//progressDialog.setCancelable(true);
				//progressDialog.show();
			} catch (Exception e) {
				Log.e("Parse Util- Error to be ignored", e.getMessage()+"");
			}
		}

		/**
		 * It dismisses dialog
		 */
		public void onPostExecute(){
			try{
				//progressDialog.dismiss();
			}catch(Exception e){
				Log.e("Parse Util- Error to be ignored", e.getMessage()+"");
			}
		}




		/**
		 * Used to dismiss dialog and if senderId and receipientIds 
		 * </br>
		 * are saved to parse then sends friend request.
		 * 
		 * @param e
		 * 
		 */
		@Override
		public void done(ParseException e) {
			onPostExecute();
			parseCallbackListener.done(e,requestId);
		};
	}

	//	private ProgressDialog progressDialog=null;

	/**
	 * Create entry of group on parse
	 * 
	 * @param groupName
	 */
	public void getGroupMembers(final String groupName,final ParseCallbackListener callback,final int requestId){

		StringTokenizer stringTokenizer = new StringTokenizer(groupName,"@");
		String groupName1= stringTokenizer.nextToken();

		ParseQuery<ParseObject> query = ParseQuery.getQuery(ThatItApplication.getApplication().getResources().getString(R.string.class_groups));
		//	    query.whereEqualTo(ThatItApplication.getApplication().getResources().getString(R.string.column_group_members),memberName1);
		query.whereEqualTo(ThatItApplication.getApplication().getResources().getString(R.string.column_group_name),groupName1 );
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> invites, ParseException e) {

				try {
					//	progressDialog.dismiss();
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				callback.done(invites,e,requestId);
			}
		});
	}


}
