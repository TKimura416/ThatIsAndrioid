package com.thatsit.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//import org.jivesoftware.smack.RosterEntry;
//import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.thatsit.android.activities.GroupInfoActivity;
import com.thatsit.android.activities.InviteContactsToRoster;
import com.thatsit.android.activities.InviteContactsToRoster.chat_option;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.db.One2OneChatDb;
import com.thatsit.android.fragement.FragmentContact;
import com.thatsit.android.parseutil.ParseCallbackListener;
import com.thatsit.android.parseutil.ParseUtil;
import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.seasia.myquick.model.AppSinglton;

public class MyGroupsHelper {

	private static Activity mContext;
	private static String  groupName="";
	private static final ParseUtil parseUtil = new ParseUtil();
	private static Handler handler = new Handler();
	private static Dialog createGroupDialog;
	private static ArrayList<RosterGroup> list = null;


	public static void createNewGroupDialog(final Activity context, final XMPPTCPConnection xmppConnectionInstance) {

		if (createGroupDialog == null) {
			createGroupDialog = new Dialog(context);
			createGroupDialog.setContentView(R.layout.create_group_dialog);
			createGroupDialog.setTitle("That's It");
			createGroupDialog.setCancelable(false);

			try {
				Button btnDone = (Button) createGroupDialog.findViewById(R.id.btn_done);
				Button btnCancel = (Button) createGroupDialog.findViewById(R.id.btn_cancel);
				final EditText edtGroupName = (EditText) createGroupDialog.findViewById(R.id.edt_groupName);

				btnDone.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						try {

							if(NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())){

								if(MainService.mService.connection.isConnected()
										&& MainService.mService.connection.isAuthenticated()){

									//startCreateGroupDialog();
									FragmentContact.imgVwAddNewGroup.setEnabled(true);
									InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(edtGroupName.getWindowToken(), 0);
									groupName = edtGroupName.getText().toString().trim().toLowerCase();

									if (groupName.trim().toCharArray().length == 0) {
										Toast.makeText(context,"Enter group name ", Toast.LENGTH_SHORT).show();
									} else {

										Collection<RosterGroup> rGroups = Roster.getInstanceFor(MainService.mService.connection).getGroups();
										list = new ArrayList<>(rGroups);

										ArrayList<String> groupList = new ArrayList<>();
										for (int i = 0; i < list.size(); i++) {
											groupList.add(list.get(i).getName().split("__")[1].replaceAll("%2b", " "));
										}

										if (groupList.contains(groupName)) {
											Utility.showMessage("Group with same name already exists");
										} else {

											groupName = groupName.replaceAll(" ", "%2b");
											createGroupDialog.dismiss();
											createGroupDialog = null;
											Utility.startDialog(context);

											new Thread(new Runnable() {
												@Override
												public void run() {
													final String groupName_complete = AppSinglton.thatsItPincode.toLowerCase() + "__" + groupName;
													if (!xmppConnectionInstance.isConnected()) {
														try {
															xmppConnectionInstance.connect();
															joinParse(groupName_complete, xmppConnectionInstance);

														} catch (XMPPException e1) {
															e1.printStackTrace();
															Utility.showMessage("Error while creating group.");
														} catch (InterruptedException e) {
															e.printStackTrace();
														} catch (IOException e) {
															e.printStackTrace();
														} catch (SmackException e) {
															e.printStackTrace();
														}
													} else {
														joinParse(groupName_complete, xmppConnectionInstance);
													}
												}
											}).start();
										}
									}
								}else{
									Utility.showMessage("Please wait while connection gets restored");
								}

							}else{
								Utility.showMessage("No Network Available");
							}


						} catch (Exception e) {
							e.printStackTrace();
							//	dismissCreateGroupDialog();
							Utility.stopDialog();
							createGroupDialog = null;
							Toast.makeText(context,"Error while creating group.",Toast.LENGTH_SHORT).show();
						}
					}
				});

				btnCancel.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						try {
							FragmentContact.imgVwAddNewGroup.setEnabled(true);
							InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(edtGroupName.getWindowToken(), 0);
							createGroupDialog.dismiss();
							createGroupDialog = null;
						} catch (Exception e) {
							createGroupDialog = null;
							e.printStackTrace();
						}
					}
				});

			} catch (Exception e) {
				createGroupDialog = null;
				e.printStackTrace();
			}
		}
		createGroupDialog.show();
	}

	public static void selectChatGroupOption(
			final MultiUserChat chatGrpInstance,
			final XMPPConnection mConnection, final Activity parentReference)throws Exception {

		mContext = parentReference;

		final CharSequence[] optionsBasicUser = { "Leave Group","Invite People","Group Members", "Cancel" };


		AlertDialog.Builder builder = new AlertDialog.Builder(parentReference);
		builder.setTitle("Make your selection");
		builder.setCancelable(false);
		builder.setItems(optionsBasicUser, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int item) {
				if(!NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())){
					Utility.showMessage("No Network Available");
					return ;
				}
				if (optionsBasicUser[item].equals("Leave Group")) {
					try {
						Utility.startDialog(mContext);
						if(handler == null){
							handler = new Handler();
						}
						new Thread(new Runnable() {
							@Override
							public void run() {

								RosterGroup rGroup = Roster.getInstanceFor(mConnection).getGroup(
										chatGrpInstance.getRoom().toString().split("@")[0]);
								String groupName;
								try {
									groupName = rGroup.getName();
									InviteContactsToRoster.addRemove(chat_option.LEAVE, AppSinglton.thatsItPincode,groupName);
									if (rGroup != null && rGroup.getEntries().size()!=0) {
										for (RosterEntry entry : rGroup.getEntries())
											rGroup.removeEntry(entry);

										leaveGroupFromParse(groupName,chatGrpInstance);
										leaveGroupFromDatabase(groupName);

										MainService.mService.onLeaveGroup(groupName);

									}else{
										handler.post(new Runnable() {

											@Override
											public void run() {
												showEmptyGroupPrompt(parentReference);
											}
										});
									}
								} catch (Exception e) {
									e.printStackTrace();

								}
								//	dismissCreateGroupDialog();
								Utility.stopDialog();
								triggerFragmentRefresh("Refresh_Group_Adapter");
							}
						}).start(); 

						//	chatGrpInstance.destroy("deletinggroup","random@conference." + Constants.HOST);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (optionsBasicUser[item].equals("Invite People")) {
					if(handler == null){
						handler = new Handler();
					}
					new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								ThatItApplication.getApplication().setCurrentMUCRefernece(chatGrpInstance);
								ThatItApplication.getApplication().setCurrentRosterGroupReference(
										Roster.getInstanceFor(mConnection).getGroup(chatGrpInstance.getRoom().toString().split("@")[0]));

								handler.post(new Runnable() {

									@Override
									public void run() {
										String groupNameWithMessage = chatGrpInstance.getRoom().toString().split("@")[0].split("__")[1].replaceAll("%2b", " ");
										Intent intent = new Intent(parentReference, InviteContactsToRoster.class);
										intent.putExtra("GROUP NAME",groupNameWithMessage);
										parentReference.startActivity(intent);
									}
								});

							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).start();

				}
				if (optionsBasicUser[item].equals("Group Members")) {
					try {
						Intent it = new Intent(ThatItApplication.getApplication(),GroupInfoActivity.class);
						it.putExtra("group_name", chatGrpInstance.getRoom().toString());
						it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						ThatItApplication.getApplication().startActivity(it);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (optionsBasicUser[item].equals("Cancel")) {
					try {
						dialog.dismiss();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			/**
			 * Leave from corresponding room
			 * @param groupNamePrefix - room name
			 */
			private void leaveGroupFromDatabase(String groupNamePrefix) {
				One2OneChatDb chatDb = new One2OneChatDb(ThatItApplication.getApplication());
				chatDb.leaveGroup( groupNamePrefix);
			}
		});
		builder.show();
	}

	private static void leaveGroupFromParse(final String mGroupName, final MultiUserChat chatGrpInstance){

		parseUtil.leaveGroup( mGroupName , AppSinglton.thatsItPincode, new ParseCallbackListener() {

			@Override
			public void done(List<ParseObject> receipients, ParseException e,
					int requestId) {
			}

			@Override
			public void done(ParseException parseException, int requestId) {
				if(parseException==null)
					try {
						chatGrpInstance.leave();
					} catch (SmackException.NotConnectedException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				else{
					parseException.printStackTrace();
				}
			}
		}, ParseCallbackListener.OPERATION_LEAVE_GROUP);

	}

	/**
	 * Show prompt if room has no members.
	 * @param contextReference roomname
	 */
	private static void  showEmptyGroupPrompt( Activity contextReference){
		mContext = contextReference;
		AlertDialog.Builder builder = new AlertDialog.Builder(contextReference);
		builder.setMessage("This user has been scheduled to be left from this group!")
		.setCancelable(false)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private static void joinParse(final String groupName_complete, final XMPPConnection xmppConnectionInstance){
		parseUtil.joinGroup(groupName_complete, AppSinglton.thatsItPincode, new ParseCallbackListener() {
			@Override
			public void done(List<ParseObject> receipients, ParseException e,
					int requestId) {
			}
			@Override
			public void done(ParseException parseException, int requestId) {
				if(parseException==null){
					boolean isGroupCreated = false;

					try {
						MainService.mService.createAndJoinGroup(groupName,new GroupCreatingJoinListener() {

							@Override
							public void onGroupCreateJoin() {
								handler.post(new Runnable() {
									@Override
									public void run() {
										Intent intent = new Intent(ThatItApplication.getApplication(),InviteContactsToRoster.class);
										intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										intent.putExtra("GROUP NAME",groupName);
										ThatItApplication.getApplication().startActivity(intent);
									}
								});
							}
						});
						isGroupCreated = true;
					} catch (Exception e) {
						Log.e(getClass().getCanonicalName(), "Error in Join Parse : "+e.getMessage());
					}	

					if(!isGroupCreated)
						parseUtil.leaveGroup(groupName_complete, AppSinglton.thatsItPincode, null, -1);

				}else{
					Utility.showMessage("Unable to create Group");
				}
			}
		}, ParseCallbackListener.OPERATION_JOIN_GROUP);
	} 

	private static void triggerFragmentRefresh(String statusCode){

		Intent intent = new Intent();
		intent.setAction(statusCode);
		ThatItApplication.getApplication().sendBroadcast(intent);
	}

}
