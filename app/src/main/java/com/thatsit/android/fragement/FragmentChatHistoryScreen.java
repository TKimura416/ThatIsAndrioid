package com.thatsit.android.fragement;

import java.util.ArrayList;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.seasia.myquick.asyncTasks.Validate_ThatsItId_Async;
import com.seasia.myquick.model.ValidateThatsItID;
import com.thatsit.android.MainService;
import com.thatsit.android.R;
import com.thatsit.android.Utility;
import com.thatsit.android.activities.ContactActivity;
import com.thatsit.android.adapter.ImageAdapter;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.db.DbOpenHelper;
import com.thatsit.android.db.One2OneChatDb;
import com.thatsit.android.encryption.helper.EncryptionManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.seasia.myquick.model.AppSinglton;
import com.thatsit.android.interfaces.ValidateThatsItIdInterface;

@SuppressLint({ "ValidFragment", "NewApi" })
public class FragmentChatHistoryScreen extends SuperFragment{
    private final String TAG = FragmentChatHistoryScreen.class.getSimpleName();
    private ImageView mIMageVw_ProfilePic;
    private TextView mTxt_UserName,mpseudoDescription;
    private EditText mEdt_SearchChatHistory;
    private FragmentChatScreen mFragmentChatScreen;
    private FragmentChatHistoryScreen mChatHistoryScreen;
    private View mView;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private SharedPreferences mSharedPreferences,mSharedPreferences_reg;
    private ThatItApplication myApplication;
    private boolean Lastfragment = false;
    private ChatUsersAdapter adapter ;
    private ListView mlistChatHistory;
    private LayoutInflater inflater;
    private EncryptionManager encryptionManager ;
    private String psedoName, profiledescription,profileImageString,entryWithoutHost;
    private RosterEntry  rosterEntry;
    private String profilePicDrawable;
    private String personFirstName;
    private String personLastname;
    private Handler handler;
    private ArrayList<String> jids = new ArrayList<String>();
    private ArrayList<String> listcardname = new ArrayList<String>();
    private ArrayList<String> listcardlastname = new ArrayList<String>();
    private ArrayList<String> listcardprofilepic = new ArrayList<String>();
    private ArrayList<String> messageList = new ArrayList<String>();
    private ArrayList<String> timeList = new ArrayList<String>();
    private ArrayList<String> userTypeList = new ArrayList<String>();
    private ContactActivity hostActivity;
    private ArrayList<String> filteredArrayList = new ArrayList<String>();
    private ArrayList<String> filteredFirstName = new ArrayList<String>();
    private ArrayList<String> filteredLastName = new ArrayList<String>();
    private ArrayList<String> filteredProfilePic = new ArrayList<String>();
    private ArrayList<String> filteredMessage = new ArrayList<String>();
    private AlertDialog alertDialog;
    private ArrayList<String> getRosterHistoryList;
    private ArrayList<Integer> rosterHistoryToBeDeleted;

    public FragmentChatHistoryScreen(MainService mService) {
        myApplication = ThatItApplication.getApplication();
    }
    public FragmentChatHistoryScreen() {
    }

    @Override
    public void onAttach(Activity activity) {
        try {
            super.onAttach(activity);
            hostActivity = (ContactActivity) activity;
            mFragmentManager = getActivity().getSupportFragmentManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Utility.fragmentChatHistoryScreen = FragmentChatHistoryScreen.this;
            ((ContactActivity)getActivity()).iconStateChanger(false,true, false,false);
            Utility.fragPaymentSettingsOpen = false;
            Utility.fragChatIsOpen = false;
            checkServiceStart();
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            encryptionManager = new EncryptionManager();
            mChatHistoryScreen = new FragmentChatHistoryScreen(MainService.mService);
            Utility.fragChatHistoryOpened = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        mView= inflater.inflate(R.layout.fragment_chat_history_screen, container, false);
        this.inflater = inflater;

        initialise_Variables();
        initialiseSharedPreference();
        prepareChatRosterData();
        if(getResources().getBoolean(R.bool.isTablet)){
            ContactActivity.textView_toolbar_title.setText("Chats");
        }
        getUserDataFromSharedPrefernce();
        initialise_Listeners();
        return mView;
    }

    /**
     * Initialse class variables.
     */
    private void initialise_Variables() {
        mlistChatHistory=(ListView)mView.findViewById(R.id.lst_chatHistory);
        mIMageVw_ProfilePic=(ImageView) mView.findViewById(R.id.ChatHistory_profile_pic);
        mTxt_UserName=(TextView) mView.findViewById(R.id.txt_UserName);
        mpseudoDescription=(TextView) mView.findViewById(R.id.txt_UserName2);
        mEdt_SearchChatHistory=(EditText) mView.findViewById(R.id.edt_SearchChatHistory);
    }

    /**
     * Get user details from shared preference.
     */
    private void getUserDataFromSharedPrefernce() {

        profileImageString = mSharedPreferences_reg.getString("ProfileImageString", "");
        psedoName = mSharedPreferences_reg.getString("PseudoName", "");
        profiledescription = mSharedPreferences_reg.getString("Pseudodescription", "");

        setDataToRespectiveFields();
    }

    private void initialiseSharedPreference() {
        mSharedPreferences_reg = getActivity().getSharedPreferences("register_data", getActivity().MODE_WORLD_READABLE);
        mSharedPreferences = getActivity().getSharedPreferences("USERID", getActivity().MODE_WORLD_READABLE);
        AppSinglton.userId = mSharedPreferences.getString("USERID", "");
    }

    @Override
    public void onStop() {
        super.onStop();
        Utility.fragChatHistoryOpened = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utility.fragmentChatHistoryScreen = FragmentChatHistoryScreen.this;
        handler = new Handler();
        Utility.UserPauseStatus(getActivity());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void prepareChatRosterData() {
        try {
            jids.clear();
            listcardname.clear();
            listcardprofilepic.clear();
            messageList.clear();
            userTypeList.clear();
            listcardlastname.clear();
            timeList.clear();

            ThatItApplication.getApplication().openDatabase();
            Cursor cursor = One2OneChatDb.getAllChatRoster();
            cursor.moveToFirst();

            if (cursor.moveToFirst()) {
                do {
                    String jid = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_JID));
                    String messages = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_MESSAGE));
                    String message = messages.toString();
                    String userType = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_OWNER_OR_PARTICIPANT));
                    String time = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_TIMESTAMP));
                    jids.add(jid);
                    messageList.add(message);
                    timeList.add(time);
                    userTypeList.add(userType);
                } while (cursor.moveToNext());
            }
            cursor.close();

            for (int i = 0; i < jids.size(); i++) {
                Cursor cursor_entry = One2OneChatDb.getRosterEntry(jids.get(i));
                cursor_entry.moveToFirst();

                if (cursor_entry.getCount() > 0) {
                    String firstname = cursor_entry.getString(cursor_entry.getColumnIndex(DbOpenHelper.FIRSTNAME));
                    String lastname = cursor_entry.getString(cursor_entry.getColumnIndex(DbOpenHelper.LASTNAME));
                    String profilePic = cursor_entry.getString(cursor_entry.getColumnIndex(DbOpenHelper.PROFILE_PIC_URL));

                    if (TextUtils.isEmpty(firstname)) {
                        firstname = "My Name";
                    }
                    if (TextUtils.isEmpty(lastname)) {
                        lastname = "";
                    }
                    if (TextUtils.isEmpty(profilePic)) {
                        profilePic = "";
                    }
                    listcardname.add(firstname);
                    listcardprofilepic.add(profilePic);
                    listcardlastname.add(lastname);
                }
            }
            cursor.close();
            setUserAdapter();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialise click listeners.
     */
    private void initialise_Listeners() {

        mlistChatHistory.setTextFilterEnabled(true);
        mEdt_SearchChatHistory.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

                try {
                    adapter.getFilter().filter(arg0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }

        });

        mlistChatHistory.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, final View arg1, final int position, long arg3) {
                try {
                    Lastfragment = true;
                    if(!mEdt_SearchChatHistory.getText().toString().trim().equalsIgnoreCase("")){
                        rosterEntry = MainService.mService.getRosterEntryFromJID(filteredArrayList.get(position));
                        entryWithoutHost = filteredArrayList.get(position);
                        personFirstName = filteredFirstName.get(position);
                        entryWithoutHost = entryWithoutHost.replace("@190.97.163.145", "");
                        personLastname = filteredLastName.get(position);
                        try {
                            profilePicDrawable = filteredProfilePic.get(position);
                            // profilePicDrawable= (Drawable) arg1.getTag(R.string.roster_profile_pic_tag);
                        } catch (Exception e) {
                            profilePicDrawable = null;
                        }
                    }
                    else {
                        // mEdt_SearchChatHistory.setText("");
                        rosterEntry = MainService.mService.getRosterEntryFromJID(jids.get(position));
                        entryWithoutHost = jids.get(position);
                        personFirstName = listcardname.get(position);
                        entryWithoutHost = entryWithoutHost.replace("@190.97.163.145", "");
                        personLastname = listcardlastname.get(position);
                        try {
                            profilePicDrawable = listcardprofilepic.get(position);
                            // profilePicDrawable= (Drawable) arg1.getTag(R.string.roster_profile_pic_tag);
                        } catch (Exception e) {
                            profilePicDrawable = null;
                        }
                    }
                    if (Utility.googleServicesUnavailable == true) {
                        new Validate_ThatsItId_Async(getActivity(), entryWithoutHost, mValidateThatsItIdInterface).execute();
                    } else {
                        removeIncomingPings();
                        openFragmentChatScreen();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Remove star from corresponding jID when message is viewed.
     */
    private void removeIncomingPings() {

        ThatItApplication.getApplication().getIncomingPings().remove(rosterEntry);
        ThatItApplication.getApplication().getIncomingFilePings().remove(rosterEntry);
    }

    /**
     * Go to chat screen to view entire chat.
     */
    private void openFragmentChatScreen() {

        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentChatScreen  = new FragmentChatScreen(MainService.mService, rosterEntry,profilePicDrawable,Lastfragment,personFirstName,personLastname);
        mFragmentTransaction.replace(R.id.fragmentContainer, mFragmentChatScreen);
        mFragmentTransaction.commit();
    }

    /**
     * The Adapter class to provide access to the data items.
     */
    class ChatUsersAdapter extends BaseAdapter implements Filterable{

        private LayoutInflater inflater;
        private ArrayList<String> jids = new ArrayList<String>();
        private ArrayList<String> listcardname = new ArrayList<String>();
        private ArrayList<String> listcardprofilepic = new ArrayList<String>();
        private ArrayList<String> messageList = new ArrayList<String>();
        private ArrayList<String> timeList = new ArrayList<String>();
        private ArrayList<String> userTypeList = new ArrayList<String>();


        public ChatUsersAdapter(ArrayList<String> jids,	ArrayList<String> listcardname,
                                ArrayList<String> listcardprofilepic,ArrayList<String> messageList,
                                ArrayList<String> timeList,ArrayList<String> userTypeList){

            inflater = (LayoutInflater) hostActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.jids = jids;
            this.listcardname = listcardname;
            this.listcardprofilepic = listcardprofilepic;
            this.messageList = messageList;
            this.timeList = timeList;
            this.userTypeList = userTypeList;
        }

        @Override
        public int getCount() {
            return jids.size();
        }

        @Override
        public String getItem(int position) {
            return jids.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {

                ViewHolder holder;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.adapter_fragment_chat_history, parent, false);
                    holder = new ViewHolder();
                    holder.img = (ImageView) convertView.findViewById(R.id.profile_picture);
                    holder.name = (TextView) convertView.findViewById(R.id.txt_UserName);
                    holder.lastMsg = (TextView) convertView.findViewById(R.id.txt_UserDescrption);
                    holder.myQuickID = (TextView) convertView.findViewById(R.id.txt_UserID);
                    holder.img_star = (ImageView) convertView.findViewById(R.id.img_star);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                holder.name.setText(listcardname.get(position));
                holder.myQuickID.setText(jids.get(position).split("@")[0]);

                String revisedMessage = processSmileyCodes(messageList.get(position));
                holder.lastMsg.setText(encryptionManager.decryptPayload(revisedMessage));

                if (listcardprofilepic.get(position) != null && listcardprofilepic.size() != 0) {

                    ContactActivity.options = new DisplayImageOptions.Builder()
                            .displayer(new RoundedBitmapDisplayer(200))
                            .showImageOnFail(R.drawable.no_img)
                            .showImageForEmptyUri(R.drawable.no_img)
                            .cacheOnDisk(true)
                            .cacheInMemory(true)
                            .build();
                    ContactActivity.loader.displayImage(listcardprofilepic.get(position), holder.img,ContactActivity.options);

                    if (ThatItApplication.getApplication().getIncomingPings().contains(jids.get(position))) {
                        holder.img_star.setVisibility(View.VISIBLE);
                    } else if (!ThatItApplication.getApplication().getIncomingPings().contains(jids.get(position))) {
                        holder.img_star.setVisibility(View.GONE);
                    }

                    //### setting red star for incoming file messages
                    else if (ThatItApplication.getApplication().getIncomingFilePings().contains(jids.get(position))) {
                        holder.img_star.setVisibility(View.VISIBLE);
                    } else if (!ThatItApplication.getApplication().getIncomingFilePings().contains(jids.get(position))) {
                        holder.img_star.setVisibility(View.GONE);
                    }
                    Object viewDrawable = holder.img.getDrawable();
                    convertView.setTag(R.string.roster_profile_pic_tag, viewDrawable);
                }
                return convertView;
            } catch (Exception e) {
                e.printStackTrace();
                return new View(hostActivity);
            }
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();

                    filteredArrayList =  new ArrayList<String>();
                    filteredFirstName =  new ArrayList<String>();
                    filteredLastName = new ArrayList<String>();
                    filteredProfilePic =  new ArrayList<String>();
                    filteredMessage =  new ArrayList<String>();

                    if(constraint != null && jids!=null  && constraint.length()>0) {
                        for(int i =0 ; i <jids.size();i++){
                            String searchText = jids.get(i);
                            if(searchText.startsWith(constraint.toString())){
                                filteredArrayList.add(jids.get(i));
                                filteredFirstName.add(listcardname.get(i));
                                filteredLastName.add(listcardlastname.get(i));
                                filteredProfilePic.add(listcardprofilepic.get(i));
                                filteredMessage.add(messageList.get(i));
                            }
                        }
                    }else if( constraint.length()==0){
                      /*  filteredArrayList = jids;
                        filteredFirstName = listcardname;
                        filteredProfilePic = listcardprofilepic;
                        filteredMessage = messageList;*/
                        filteredArrayList.clear();
                        filteredFirstName.clear();
                        filteredLastName.clear();
                        filteredProfilePic.clear();
                        filteredMessage.clear();
                    }
                    filterResults.values = filteredArrayList;
                    filterResults.count = filteredArrayList.size();
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence contraint, FilterResults results) {
                    filteredArrayList = (ArrayList<String>) results.values;
                    if (results.count > 0) {

                        if(filteredArrayList.size()>0){
                            adapter = new ChatUsersAdapter(filteredArrayList,filteredFirstName,filteredProfilePic,filteredMessage,
                                    userTypeList,timeList);
                            mlistChatHistory.setAdapter(adapter);
                        }
                    } else {
                        prepareChatRosterData();
                    }
                }
            };
        }
    }

    private static class ViewHolder{
        ImageView img;
        TextView name;
        TextView lastMsg;
        TextView myQuickID;
        ImageView img_star;
    }


    /**
     *
     * @param message Text message with emoticons
     * @return
     */
    private String processSmileyCodes(String message){
        try{
            for(int index=0;index<ImageAdapter.mThumbIdsForDispatch.length;index++){
                message = message.replaceAll(ImageAdapter.mThumbIdsForDispatch[index], ImageAdapter.mThumbIds[index]);
            }
        }catch(Exception e){
            return message;
        }
        return message;
    }


    private void setDataToRespectiveFields() {

        // Set Profile Pic
        ContactActivity.options = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer(200))
                .showImageOnFail(R.drawable.no_img)
                .showImageForEmptyUri(R.drawable.no_img)
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .build();
        ContactActivity.loader.displayImage(profileImageString.replaceAll(" ", "%20"), mIMageVw_ProfilePic, ContactActivity.options);

        // Set Pseudo Name
        if (psedoName.equals("")) {
            mTxt_UserName.setText("My Name");
        } else {
            mTxt_UserName.setText(psedoName);
        }

        // Set Pseudo Description
        if (profiledescription.equals("")) {
            mpseudoDescription.setText("Hi I Am Using That's It");
        } else {
            mpseudoDescription.setText(profiledescription);
        }
    }

    private void setUserAdapter() {

        adapter = new ChatUsersAdapter(jids,listcardname,listcardprofilepic,messageList,
                userTypeList,timeList);
        mlistChatHistory.setAdapter(adapter);
    }

    /**
     *  Check if roster entry exists on Admin
     */

    ValidateThatsItIdInterface mValidateThatsItIdInterface = new ValidateThatsItIdInterface() {
        @Override
        public void validateThatsItId(ValidateThatsItID mValidateThatsItID) {

            if(mValidateThatsItID != null){

                String status = mValidateThatsItID.getValidateUserPincodeResult().getStatus().getStatus();
                if(status.equalsIgnoreCase("1")){

                    // Pincode is existing on Admin
                    removeIncomingPings();
                    openFragmentChatScreen();
                }else{
                    // open remove roster alert
                    openAlert("This contact has been disabled by Admin and will be removed from your contact list");
                }
            }
        }
    };


    private void openAlert(final String Message) {

        handler.post(new Runnable() {

            @Override
            public void run() {

                try {
                    if (alertDialog == null) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertDialogBuilder.setTitle("That's It");
                        alertDialogBuilder.setMessage(Message);

                        alertDialogBuilder.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {

                                        alertDialog.dismiss();
                                        alertDialog = null;

                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    Utility.removeFriendIfExists(rosterEntry.getUser());
                                                    removeContactFromRoster(rosterEntry);
                                                    ThatItApplication.getApplication().getSentInvites().remove(rosterEntry.getUser().toUpperCase());
                                                    ThatItApplication.getApplication().getSentInvites().remove(rosterEntry.getUser().toLowerCase());
                                                    try {
                                                        Utility mUtility = new Utility();
                                                        mUtility.deleteUnsubscribedUserChatHistory(
                                                                getRosterHistoryList,rosterHistoryToBeDeleted,
                                                                rosterEntry.getUser(),MainService.mService);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    prepareChatRosterData();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }).start();
                                    }
                                });

                        alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                        alertDialog.setCancelable(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     *  Delete Roster from Openfire
     */
    private void removeContactFromRoster(RosterEntry entryToBeRemoved) {
        try {
            MainService.mService.connection.getRoster().removeEntry(entryToBeRemoved);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }
}
