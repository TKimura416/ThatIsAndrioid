package com.thatsit.android;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.system.ErrnoException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.myquickapp.receivers.NetworkChangeReceiver;
import com.thatsit.android.activities.ContactActivity;
import com.thatsit.android.activities.InviteContactsToRoster.MyRosterListnerInvite;
import com.thatsit.android.activities.SplashActivity;
import com.thatsit.android.activities.SuggestContactActivity.MyRosterListnerSuggest;
import com.thatsit.android.activities.TermsAndConditionsActivity;
import com.thatsit.android.activities.WelcomeActivity;
import com.thatsit.android.adapter.PresenceAdapter;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.beans.PushNotificationService;
import com.thatsit.android.db.DbOpenHelper;
import com.thatsit.android.db.One2OneChatDb;
import com.thatsit.android.fragement.FileDownloadStatusCallback;
import com.thatsit.android.fragement.FragmentChatScreen;
import com.thatsit.android.fragement.FragmentContact.MyRosterListner;
import com.thatsit.android.invites.PresenceType;
import com.thatsit.android.parcelable.Contact;
import com.thatsit.android.parseutil.ParseCallbackListener;
import com.thatsit.android.parseutil.ParseOperationDecider;
import com.thatsit.android.parseutil.ParseUtil;
import com.thatsit.android.ping.PingExtension;
import com.thatsit.android.xmpputils.Constants;
import com.thatsit.android.xmpputils.Status;
import com.thatsit.android.xmpputils.XmppManager;
import com.myquickapp.receivers.ConnectionBroadcastReceiver;
import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.seasia.myquick.model.AppSinglton;

/**
 * XMMP main service, always connected to XMPP server
 * It Contains the following info :
 * Login to openfire
 * Message Listener: To receive incoming message
 * Group Listener: Called on incoming group message or when new Group is created
 * Packet listener: To receive presence packets
 * Friend request receive or denied listeners
 */
@SuppressLint("NewApi")
public class MainService extends Service {

    final String TAG = getClass().getSimpleName();
    static Context context;
    private String mPreviousStatus;
    private static String FilePath;
    private int mPreviousMode;
    private Handler mHandler;
    public Timer mTimer;
    private static Vibrator vibrator;
    private Handler handler = new Handler();
    private static Notification mNotification;
    private String name;
    private ArrayList<String> getRosterHistoryList;
    public static ArrayList<String> roomList = new ArrayList<>();
    private ArrayList<Integer> rosterHistoryToBeDeleted;
    private String unsubsrcibedUser;
    public ArrayList<String> group_name = new ArrayList<String>();
    public Hashtable<String, MultiUserChat> group_mucs = new Hashtable<String, MultiUserChat>();
    public Hashtable<String, MUCPacketListener> group_packet_listeners = new Hashtable<String, MUCPacketListener>();
    private ChatManager chatmanager = null;
    private PacketListener mMessagePacketListener = null;
    PacketCollector mpPacketCollector;

    // Preferences
    private SharedPreferences mSettings;
    private static SharedPreferences AudioPreference;
    private static SharedPreferences mSharedPreferences;

    // Application
    private static ThatItApplication myApplication;

    // Notification
    private static NotificationManager mNotificationManager;
    public static int NOTIFICATION_FRIEND_REQUEST = 1;
    public static int NOTIFICATION_MESSAGE_RECEIVED = 2;
    private int FOREGROUND_NOTIFICATION_ID = 0;

    // XMPP
    public XMPPConnection connection;
    private XmppManager xmppConnectionManager;
    public static MainService mService;

    private final IBinder mBinder = new MyBinder();
    private String senderID;
    private String msg;
    private String from;

    // Broadcast Action
    public static final String CONNECTION_CLOSED = "com.thatsit.app.action.XMPP.ConnectionClosed";
    public static final String SIGNIN = "com.thatsit.app.action.XMPP.SIGNIN";
    public static final String SIGNOUT = "com.thatsit.app.action.XMPP.SIGNOUT";
    public static final String CHAT = "com.thatsit.app.action.XMPP.one2oneChat";

    // Broadcast receiver
    private static ConnectionBroadcastReceiver connectionBroadcastReceiver;
    private NetworkChangeReceiver networkChangeReceiver;

    // Listeners
    public IncomingChatManagerListener mIncomingChatManagerListener = new IncomingChatManagerListener();
    public MyMessageListner mMessageListner = new MyMessageListner();
    public final SubscribePacketListener mSubscribePacketListener = new SubscribePacketListener();
    public final UnSubscribePacketListener mUnSubscribePacketListener = new UnSubscribePacketListener();
    public final PingListener mPingListener = new PingListener();
    private FileTransferManager fileTransferManager;
    ParseUtil parseUtil = new ParseUtil();
    MyParseListener myParseListener = new MyParseListener();

    // Booleans
    private static boolean boolean_serviceCreatedOnce = false;
    public static boolean boolean_groupCreated = false;
    static boolean boolean_istTimeLoad = true;
    private static String RadioVibrate;

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public void onCreate() {

        if (!boolean_serviceCreatedOnce) {
            mService = this;
            if (!Utility.isAppStarted) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        parseUtil.areExists(ThatItApplication.getApplication(), myParseListener, ParseCallbackListener.OPERATION_ON_START);
                    }
                }).start();
            } else {
                taskOnCreate();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            setForegroundServiceNotification();
            ThatItApplication.getApplication().openDatabase();
            createConnectAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean_serviceCreatedOnce = true;
        return START_STICKY;
    }


    /**
     * Set foreground service with a notification ID
     */
    private void setForegroundServiceNotification() {

        Notification notification = new Notification(0, null, System.currentTimeMillis());
        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
    }

    private void taskOnCreate() {
        super.onCreate();
        try {
            Utility.isAppStarted = true;
            myApplication = ThatItApplication.getApplication();
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            initialiseHandler();
            initialiseSharedPreferences();
            setNotificationManager();
            registerReceivers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Utility.getUserName().length() > 0)
            Utils.loginUser();
    }

    /**
     * Initialise Notification Manager
     */
    private void setNotificationManager() {
        mNotificationManager = (NotificationManager) myApplication.getSystemService(NOTIFICATION_SERVICE);
    }

    /**
     * Setup shared preference
     */
    private void initialiseSharedPreferences() {
        AudioPreference = getSharedPreferences("AudioPreference", 0);
        FilePath = AudioPreference.getString("AudioPreference", "");
        mSettings = PreferenceManager.getDefaultSharedPreferences(ThatItApplication.getApplication());
    }

    /**
     * Setup handler
     */
    private void initialiseHandler() {
        handler = new Handler();
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Register connection broadcast receiver
     */
    private void registerReceivers() {
        connectionBroadcastReceiver = new ConnectionBroadcastReceiver(MainService.this);
        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainService.CONNECTION_CLOSED);
        filter.addAction(MainService.SIGNIN);
        registerReceiver(connectionBroadcastReceiver, filter);
    }

    /**
     * Do not receive message of group which has been left
     * Receive if you are still part of that group
     */
    public void onLeaveGroup(String groupName) {
        if (groupName.contains("@conference")) {
            groupName = groupName.substring(0, groupName.lastIndexOf("@conference"));
        }
        group_name.remove(groupName);
        if (groupName.contains("%2b")) {
            groupName = groupName.replace("%2b", " ");
        }
        group_mucs.get(groupName).removeMessageListener(group_packet_listeners.get(groupName));

        group_mucs.remove(groupName);
        group_packet_listeners.remove(groupName);
    }

    /**
     * Join all groups after successful login
     */
    public void joinToGroups() {

        if (connection.isConnected() && connection.isAuthenticated()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean_istTimeLoad = false;

                    if (xmppConnectionManager == null || connection == null) {
                        xmppConnectionManager = XmppManager.getInstance();
                        connection = xmppConnectionManager.getXMPPConnection();
                    }
                    DiscussionHistory history = new DiscussionHistory();
                    history.setMaxStanzas(0);
                    mSharedPreferences = ThatItApplication.getApplication().getSharedPreferences("UpdatePseudoName", 0);
                    try {
                        List<RosterGroup> rGroups = new ArrayList<RosterGroup>();
                        Collection<RosterGroup> rGroups_Collection = connection.getRoster().getGroups();
                        rGroups = new ArrayList<RosterGroup>(rGroups_Collection);
                        for (int i = 0; i < rGroups.size(); i++) {
                            String r_name = rGroups.get(i).getName();
                            r_name = r_name.replace("%2b", " ");

                            MultiUserChat muChat = new MultiUserChat(connection, rGroups.get(i).getName() + "@conference." + Constants.HOST);
                            String nicknameToJoin = null;
                            if (TextUtils.isEmpty(mSharedPreferences.getString("pseudoName", "anonymous"))) {
                                nicknameToJoin = "My Name" + " (" + connection.getUser().split("@")[0] + ")";
                            } else {
                                nicknameToJoin = mSharedPreferences.getString("pseudoName", "anonymous") + " (" + connection.getUser().split("@")[0] + ")";
                            }

                            String room_name = muChat.getRoom().split("@")[0].replace("%2b", " ");

                            if (!muChat.isJoined()) {
                                try {
                                    muChat.join(nicknameToJoin, "", history, SmackConfiguration.getPacketReplyTimeout());

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            if (!group_name.contains(room_name)) {
                                group_name.add(room_name);
                                MUCPacketListener listener = new MUCPacketListener(group_name);
                                muChat.addMessageListener(listener);
                                onGroupAddedJoined(room_name, muChat, listener);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }).start();
        }
    }


    public void joinToGroups(final OnGroupJoined groupJoined) {

        if (connection.isConnected() && connection.isAuthenticated()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean_istTimeLoad = false;

                    if (xmppConnectionManager == null || connection == null) {
                        xmppConnectionManager = XmppManager.getInstance();
                        connection = xmppConnectionManager.getXMPPConnection();
                    }
                    DiscussionHistory history = new DiscussionHistory();
                    history.setMaxStanzas(0);
                    mSharedPreferences = ThatItApplication.getApplication().getSharedPreferences("UpdatePseudoName", 0);
                    try {
                        List<RosterGroup> rGroups = new ArrayList<RosterGroup>();
                        Collection<RosterGroup> rGroups_Collection = connection.getRoster().getGroups();
                        rGroups = new ArrayList<RosterGroup>(rGroups_Collection);
                        for (int i = 0; i < rGroups.size(); i++) {
                            String r_name = rGroups.get(i).getName();
                            r_name = r_name.replace("%2b", " ");

                            MultiUserChat muChat = new MultiUserChat(connection, rGroups.get(i).getName() + "@conference." + Constants.HOST);
                            String nicknameToJoin = null;
                            if (TextUtils.isEmpty(mSharedPreferences.getString("pseudoName", "anonymous"))) {
                                nicknameToJoin = "My Name" + " (" + connection.getUser().split("@")[0] + ")";
                            } else {
                                nicknameToJoin = mSharedPreferences.getString("pseudoName", "anonymous") + " (" + connection.getUser().split("@")[0] + ")";
                            }

                            String room_name = muChat.getRoom().split("@")[0].replace("%2b", " ");

                            if (!muChat.isJoined()) {
                                try {
                                    muChat.join(nicknameToJoin, "", history, SmackConfiguration.getPacketReplyTimeout());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (!group_name.contains(room_name)) {
                                group_name.add(room_name);
                                MUCPacketListener listener = new MUCPacketListener(group_name);
                                muChat.addMessageListener(listener);
                                onGroupAddedJoined(room_name, muChat, listener);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    groupJoined.OnGroupJoined();
                }
            }).start();
        } else {
            groupJoined.OnGroupJoined();
        }
    }


    /**
     * Listener when new group is joined
     *
     * @param room_name
     * @param multiUserChat
     * @param listener
     */
    private void onGroupAddedJoined(String room_name, MultiUserChat multiUserChat, MUCPacketListener listener) {
        group_mucs.put(room_name, multiUserChat);
        group_packet_listeners.put(room_name, listener);
    }

    /**
     * Create group -> Join group -> Send invitaion
     */
    public void createAndJoinGroup(final String groupName, final GroupCreatingJoinListener listener) {

        if (connection.isConnected() && connection.isAuthenticated()) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    System.out.println("" + MainService.this.getClass().getCanonicalName() + "   :" + " joinand create");
                    xmppConnectionManager = XmppManager.getInstance();

                    MultiUserChat muc = new MultiUserChat(connection, connection.getUser().split("@")[0] + "__" + groupName + "@conference." + Constants.HOST);
                    try {
                        connection.getRoster().createGroup(connection.getUser().split("@")[0] + "__" + groupName);

                    } catch (Exception e) {
                        Utility.stopDialog();
                        Utility.showMessage("Creation Error");
                        e.printStackTrace();
                    }

                    DiscussionHistory history = new DiscussionHistory();
                    history.setMaxStanzas(0);

                    String room_name = null;

                    try {
                        muc.join(connection.getUser().split("@")[0], "", history, SmackConfiguration.getPacketReplyTimeout());
                        room_name = muc.getRoom().split("@conference")[0].replace("%2b", " ");

                    } catch (XMPPException e) {
                        Utility.stopDialog();
                        e.printStackTrace();
                    }

                    if (!group_name.contains(room_name) && room_name != null) {
                        group_name.add(room_name);
                        MUCPacketListener mucListener = new MUCPacketListener(connection.getUser().split("@")[0] + "__" + groupName);
                        muc.addMessageListener(mucListener);
                        if (room_name != null) {
                            onGroupAddedJoined(room_name, muc, mucListener);
                        }
                        ThatItApplication.getApplication().setCurrentMUCRefernece(muc);
                        ThatItApplication.getApplication().setCurrentRosterGroupReference(connection.getRoster().getGroup(connection.getUser().split("@")[0] + "__" + groupName));
                        setConfig(muc, connection);
                        listener.onGroupCreateJoin();
                        triggerFragmentRefresh("Refresh_Group_Adapter");
                    }
                }
            }).start();
        }
    }

    /**
     * Setup XMPP Config
     *
     * @param multiUserChat
     * @param connectionInstance
     */
    private static void setConfig(MultiUserChat multiUserChat, XMPPConnection connectionInstance) {

        try {
            Form form = multiUserChat.getConfigurationForm();
            Form submitForm = form.createAnswerForm();
            for (Iterator<FormField> fields = submitForm.getFields();
                 fields.hasNext(); ) {
                FormField field = (FormField) fields.next();
                if (!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null) {
                    submitForm.setDefaultAnswer(field.getVariable());
                }
            }
            submitForm.setAnswer("muc#roomconfig_publicroom", true);
            submitForm.setAnswer("muc#roomconfig_persistentroom", true);
            List<String> owners = new ArrayList<String>();
            owners.add(connectionInstance.getUser());
            submitForm.setAnswer("muc#roomconfig_roomowners", owners);

            multiUserChat.sendConfigurationForm(submitForm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Common muc packet listener for group chat
     */
    public static class MUCPacketListener implements PacketListener {

        private String messageFromId;
        private String jabberID;
        private String message;
        private ArrayList<String> groupName = new ArrayList<String>();

        public MUCPacketListener(ArrayList<String> group_name) {
            this.groupName = group_name;
        }

        public MUCPacketListener(String groupName) {
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void processPacket(Packet packet) {

            if (packet != null) {

                Message messageObject = (Message) packet;
                String from = messageObject.getFrom();
                String room = from.substring(0, from.lastIndexOf("@"));
                String senderID = "";

                try {
                    int start = from.indexOf("(");
                    int end = from.indexOf(")");
                    messageFromId = from.substring(start, end);
                    messageFromId = messageFromId.replace("(", "");
                    senderID = messageFromId;

                } catch (Exception e) {
                    StringTokenizer stringTokenizer = new StringTokenizer(from, "/");
                    String room1 = stringTokenizer.nextToken();
                    room = room1.substring(0, room1.lastIndexOf("@"));
                    try {
                        messageFromId = stringTokenizer.nextToken();
                    } catch (Exception e1) {
                        //Log.e("MainService", e1.getMessage()+"");
                    }
                    if (!TextUtils.isEmpty(senderID)) {
                        senderID = messageFromId.substring(0, messageFromId.indexOf("@"));
                    }
                }

                if (!senderID.equalsIgnoreCase(AppSinglton.thatsItPincode.toLowerCase())) {

                    if (!messageObject.getBody().equals("This room is not anonymous.")) {
                        AppSinglton.MessageFromId.add(messageFromId);

                        if (!messageFromId.contains("@"))
                            messageFromId = messageFromId + "@" + com.thatsit.android.xmpputils.Constants.HOST;

                        jabberID = messageFromId;

                        String group_room = room.split("@")[0].split("__")[1].replaceAll("%2b", " ");

                        setIncomingGroupChatNotification("Message Group " + group_room);

                        ThatItApplication.getApplication().getIncomingGroupPings().add(room);
                        message = Utility.processSmileyCodes(messageObject.getBody().replaceAll("____", " : "));
                        One2OneChatDb.addGroupMessage(room, senderID, messageObject.getBody());

                        sendBroadcastToRefreshGroupAdapter();
                        sendBroadcastToUpdateMucChatUI(room);
                    }
                }
            }
        }

        /**
         * SEND BROADCAST TO MUC ACTIVITY TO ADD BUBBLE IN THE LIST.
         */
        private void sendBroadcastToUpdateMucChatUI(String room) {
            Intent intent = new Intent();
            intent.setAction("GROUP MESSAGE");
            intent.putExtra("group_name", room);
            intent.putExtra("message", message);
            intent.putExtra("jid", jabberID);
            ThatItApplication.getApplication().sendBroadcast(intent);
        }
    }

    /**
     * Broadcast to refresh group adapter to display ping on group
     */
    private static void sendBroadcastToRefreshGroupAdapter() {
        Intent intent = new Intent();
        intent.setAction("Refresh_Group_Adapter");
        ThatItApplication.getApplication().sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            boolean_serviceCreatedOnce = false;
            boolean_istTimeLoad = true;
            try {
                if (Utility.contactActivity != null) {
                    Utility.contactActivity.finish();
                    Utility.contactActivity = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update notification sound.
     */
    public void updateNotificationSound() {
        try {
            AudioPreference = getSharedPreferences("AudioPreference", 0);
            FilePath = AudioPreference.getString("AudioPreference", "");
            mNotificationManager = (NotificationManager) myApplication.getSystemService(NOTIFICATION_SERVICE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Incoming chat notification
     */
    public void setIncomingChatNotification() {

        AudioPreference = getSharedPreferences("AudioPreference", 0);
        FilePath = AudioPreference.getString("AudioPreference", "");

        mNotificationManager = (NotificationManager) myApplication.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(myApplication)
                .setSmallIcon(R.drawable.notification_star);

        mBuilder.setContentTitle("New Message");

        Intent intent = new Intent(myApplication, ContactActivity.class);
        Utility.groupNotificationClicked = false;
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP /* Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT*/);
        intent.setAction(Intent.ACTION_MAIN);
        PendingIntent notifIntent = PendingIntent.getActivity(myApplication, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(notifIntent);
        mBuilder.setAutoCancel(true);
        SharedPreferences mSharedPreferencesVibRead = getSharedPreferences("mSharedPreferencesVibrateValue", 1);
        RadioVibrate = mSharedPreferencesVibRead.getString("Vibrate_Value", "");
        if (!TextUtils.isEmpty(RadioVibrate) && RadioVibrate.equalsIgnoreCase("On")
                ) {
            if (FilePath.equals("")) {
                mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                vibrator.vibrate(3000);
            } else {
                File source = new File(FilePath);
                if (source.exists()) {
                    setCustomNotificationTone();
                    vibrator.vibrate(3000);
                } else {
                    mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                }
            }
        } else {
            if (FilePath.equals("")) {
                mBuilder.setDefaults(Notification.DEFAULT_SOUND);
            } else {
                File source = new File(FilePath);
                if (source.exists()) {
                    setCustomNotificationTone();
                } else {
                    mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                }
            }
        }
        try {
            // refresh fragment contacts
            Utility.fragmentContact.usersAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
        mNotificationManager.notify(NOTIFICATION_MESSAGE_RECEIVED, mBuilder.build());
    }


    /**
     * Incoming group message notification
     */
    public static void setIncomingGroupChatNotification(String msg) {

        AudioPreference = ThatItApplication.getApplication().getSharedPreferences("AudioPreference", MODE_WORLD_READABLE);
        FilePath = AudioPreference.getString("AudioPreference", "");

        mNotificationManager = (NotificationManager) myApplication.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(myApplication)
                .setSmallIcon(R.drawable.notification_star);

        mBuilder.setContentTitle(msg);
        Intent intent = new Intent(myApplication, ContactActivity.class);
        intent.putExtra("groupNotification", "groupNotification");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Intent.ACTION_MAIN);

        PendingIntent notifIntent = PendingIntent.getActivity(myApplication, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(notifIntent);
        mBuilder.setAutoCancel(true);
        SharedPreferences mSharedPreferencesVibRead = ThatItApplication.getApplication().getSharedPreferences("mSharedPreferencesVibrateValue", 1);
        RadioVibrate = mSharedPreferencesVibRead.getString("Vibrate_Value", "");
        if (!TextUtils.isEmpty(RadioVibrate) && RadioVibrate.equalsIgnoreCase("On")
                ) {
            if (FilePath.equals("")) {
                mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                vibrator.vibrate(3000);
            } else {
                File source = new File(FilePath);
                if (source.exists()) {
                    setCustomNotificationTone();
                    vibrator.vibrate(3000);
                } else {
                    mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                }
            }

        } else {
            if (FilePath.equals("")) {
                mBuilder.setDefaults(Notification.DEFAULT_SOUND);
            } else {
                File source = new File(FilePath);
                if (source.exists()) {
                    setCustomNotificationTone();
                } else {
                    mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                }
            }
        }
        mNotificationManager.notify(NOTIFICATION_MESSAGE_RECEIVED, mBuilder.build());
    }

    /**
     * Utility method to create and make connection asynchronously
     */
    private synchronized void createConnectAsync() {
        try {
            if (connection == null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        createConnection();
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Setup XMPP connection
     */
    public void createConnection() {
        try {
            xmppConnectionManager = XmppManager.getInstance();
            connection = xmppConnectionManager.getXMPPConnection();
        } catch (Exception e) {
            e.printStackTrace();
            Utility.stopDialog();
        }
    }

    public synchronized void connectAsync() {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Utility.reloginCalled == true) {
                        Thread.sleep(4000);
                    }
                    connect();
                } catch (Exception e) {
                    Log.e(TAG, "Error while connecting asynchronously", e);
                }
            }
        });
        t.start();

    }

    /**
     * Connect to Xmpp server (openfir)
     */
    public synchronized void connect() throws Exception {
        if (connection == null) {
            createConnection();
        }
        if (connection.isConnected()) {
            login();
        } else {
            try {
                connection.connect();
                //SASLAuthentication.supportSASLMechanism("PLAIN", 0);
                connection.addConnectionListener(new ConnecionListenerAdapter());
                try {
                    if (connection.isConnected()) {
                        login();
                    } else {
                        sendConnectionErrorWhileSignInBroadcast();
                        sendConnectionErrorBroadcast();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Utility.stopDialog();
                sendConnectionErrorWhileSignInBroadcast();
                sendConnectionErrorBroadcast();
                Utils.isLoginTaskRunning = false;
            }
        }
    }

    /**
     * Login to xmpp server
     * Username - jID
     * Password  - get from edittext
     */
    public void login() throws Exception {

        try {
            mSettings = PreferenceManager.getDefaultSharedPreferences(ThatItApplication.getApplication());
            String login = mSettings.getString(ThatItApplication.ACCOUNT_USERNAME_KEY, "");
            String password = mSettings.getString(ThatItApplication.ACCOUNT_PASSWORD_KEY, "");
            //String identifier = getDeviceID();
            if (!TextUtils.isEmpty(login) && !TextUtils.isEmpty((password))) {
                try {
                    if (!connection.isAuthenticated()) {
                        connection.login(login, password);
                    }
                } catch (IllegalStateException e) {
                    Utility.loginCalledOnce = false;
                } catch (Exception e) {
                    if (e.getMessage().equalsIgnoreCase("not-authorized(401)")) {
                        Utility.stopDialog();
                        Utility.showMessage("That's It ID does not exists");
                        clearCredential();
                        if (Utility.mTimer != null) {
                            Utility.mTimer.cancel();
                            Utility.mTimer = null;
                        }
                        return;
                    } else if (e.getMessage().toString().contains("No response")
                            && NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                    sendConnectionErrorWhileSignInBroadcast();
                    sendConnectionErrorBroadcast();
                    Utility.stopDialog();
                    Utility.loginCalledOnce = false;
                    Utils.isLoginTaskRunning = false;
                    boolean_serviceCreatedOnce = false;
                    e.printStackTrace();
                    return;
                }
            } else if (!TextUtils.isEmpty(Utility.getUserName()) && !TextUtils.isEmpty((Utility.getPassword()))) {
                connection.login(login, password);
            } else {
                Utility.stopDialog();
            }

            if (connection.isConnected() && connection.isAuthenticated()) {

                ThatItApplication.getApplication().setConnected(true);
                setPersence(Type.available);
                sendSignInBroadCast();
                changeStatusAndPriority(Status.CONTACT_STATUS_AVAILABLE, "");

                // Send Presence Packets
                PacketFilter filter = new PacketFilter() {
                    @Override
                    public boolean accept(Packet packet) {
                        if (packet instanceof Presence) {
                            Presence pres = (Presence) packet;
                            if (pres.getType() == Presence.Type.subscribe)
                                return true;
                        }
                        return false;
                    }
                };
                PacketFilter filter_unsubscribed = new PacketFilter() {
                    @Override
                    public boolean accept(Packet packet) {
                        if (packet instanceof Presence) {
                            Presence pres = (Presence) packet;
                            if (pres.getType() == Presence.Type.unsubscribed)
                                return true;
                        }
                        return false;
                    }
                };

                connection.addPacketListener(mSubscribePacketListener, filter);
                connection.addPacketListener(mUnSubscribePacketListener, filter_unsubscribed);

                //Add Incoming Chat Listener on Connection
                setIncomingChatListner();

                //Add Incoming File Listener on Connection
                setFileTransferListener();

                filter = new PacketTypeFilter(PingExtension.class);
                connection.addPacketListener(mPingListener, filter);


                Utility.connectionClosedCalled = false;

                connection.addPacketListener(new PacketListener() {

                    public void processPacket(Packet packet) {

                        if (Utility.reloginCalled == false) {
                            Message message = (Message) packet;
                            if (message.getBody() != null) {
                                String fromName = StringUtils.parseBareAddress(message.getFrom());
                                mMessagePacketListener = this;
                            }
                        }
                    }
                }, filter);

                joinToGroups();
                networkChangeReceiver = new NetworkChangeReceiver();
                IntentFilter networkChangeReceiverFilter = new IntentFilter();
                networkChangeReceiverFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                registerReceiver(networkChangeReceiver, networkChangeReceiverFilter);
                //setTimer();
            } else {
                Utility.showMessage("No Response from Server");
            }

        } catch (Exception e) {
            if (e instanceof ErrnoException) {
                Utility.stopDialog();
            }
            if (connection != null && connection.isConnected()) {
                connection.disconnect();
                try {
                    Utility.stopDialog();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * Timer to update user presence
     */
    public void setTimer() {

        try {
            if (mTimer != null) {
                mTimer.cancel();
            }
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {
                            if (!connection.isConnected() || !connection.isAuthenticated()) {
                                try {
                                    createConnection();
                                    connectAsync();
                                    performBackgroundTimerTask();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                performBackgroundTimerTask();
                            }
                            if (Utility.googleServicesUnavailable == true) {
                                checkExpiryStatus("checkExpiryStatus");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 15000, 15000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send presence packets in the background
     */
    private void performBackgroundTimerTask() {
        if (connection.isConnected() && connection.isAuthenticated()) {
            if (!isRunningInForeground()) {
                Presence p = new Presence(Type.available, "I am away", 42, Mode.chat);
                MainService.mService.connection.sendPacket(p);
            } else {
                SharedPreferences mSharedPreferencesAvlRead = getSharedPreferences("mSharedPreferencesAvlValue", 0);
                String RadioStatus = mSharedPreferencesAvlRead.getString("Avl_Value", "");

                if (!TextUtils.isEmpty(RadioStatus) && RadioStatus.equals("Busy")) {
                    Presence p = new Presence(Type.available, "I am busy", 42, Mode.dnd);
                    MainService.mService.connection.sendPacket(p);
                } else {
                    Presence p = new Presence(Type.available, "I am online", 42, Mode.available);
                    MainService.mService.connection.sendPacket(p);
                }
            }
            try {
                joinToGroups();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Clear connection listeners before relogin
     */
    private void clearConnectionCallbacks() {

        if (mSubscribePacketListener != null)
            connection.removePacketListener(mSubscribePacketListener);

        if (mUnSubscribePacketListener != null)
            connection.removePacketListener(mUnSubscribePacketListener);

        if (mPingListener != null)
            connection.removePacketListener(mPingListener);

        if (mMessagePacketListener != null)
            connection.removePacketListener(mMessagePacketListener);

        chatmanager.removeChatListener(mIncomingChatManagerListener);
    }

    /**
     * Send signin broadcast
     */
    private void sendSignInBroadCast() {

        boolean i = true;

        try {
            if (WelcomeActivity.userVisited == true) {
                i = true;
            } else {
                i = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Utility.contactActivity == null) {

            if (SplashActivity.userVisited == true && i == true) {

                Utility.stopDialog();
                Intent intent = new Intent();
                intent.setAction(SIGNIN);
                ThatItApplication.getApplication().sendBroadcast(intent);
            } else {
                if (SplashActivity.userVisited == true || WelcomeActivity.userVisited == true) {
                    Intent intent = new Intent(ThatItApplication.getApplication(), ContactActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    Utility.allowAuthenticationDialog = true;
                }
            }
        }
    }

    /**
     * Send presence available after login
     */
    public void changeStatusAndPriority(int status, String msg) {
        try {
            Presence pres = new Presence(Presence.Type.available);
            String m;
            if (msg != null)
                m = msg;
            else
                m = mPreviousStatus;
            pres.setStatus(m);
            mPreviousStatus = m;
            Presence.Mode mode = Status.getPresenceModeFromStatus(status);
            if (mode != null) {
                pres.setMode(mode);
                mPreviousMode = status;
            } else {
                pres.setMode(Status.getPresenceModeFromStatus(mPreviousMode));
            }
            connection.sendPacket(pres);

            updateNotification(Status.getStatusFromPresence(pres), m, SIGNIN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send Presence packet to connection
     */

    public void setPersence(Type type) {
        try {
            Presence presence = new Presence(type);
            if (type != Presence.Type.available) {
                presence.setStatus("Bye:)");
            } else {
                presence.setStatus("Hi...");
            }
            connection.sendPacket(presence);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the notification for the status.
     */

    private void updateNotification(int status, String text, String action) {
        try {
            Notification mStatusNotification = null;
            mStatusNotification.defaults = Notification.DEFAULT_LIGHTS;
            mStatusNotification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public NotificationManager getNotificationManager() {
        return mNotificationManager;
    }

    /**
     * Disconnect xmpp connection from openfie
     */
    public boolean disconnect() {
        mNotificationManager.cancelAll();
        if (connection != null && connection.isConnected()) {
            connection.disconnect();
            myApplication.setConnected(false);
        }
        return true;
    }

    /**
     * Bind service
     */
    public class MyBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }

    /**
     * XMPP connection callback listener
     */
    public class ConnecionListenerAdapter implements ConnectionListener {

        public ConnecionListenerAdapter() {
        }

        @Override
        public void connectionClosed() {

            performTaskOnConnectionClosed();
        }

        @Override
        public void connectionClosedOnError(Exception exception) {
            try {
                performTaskOnConnectionClosedOnError();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void connectionFailed(String errorMsg) {
            myApplication.setConnected(false);
            Utility.stopDialog();
        }

        @Override
        public void reconnectingIn(final int arg0) {
        }

        @Override
        public void reconnectionFailed(Exception arg0) {
            myApplication.setConnected(false);
        }

        @Override
        public void reconnectionSuccessful() {
            myApplication.setConnected(true);
            if (mService == null) {
                startService(new Intent(getApplicationContext(), MainService.class));
            }
        }
    }

    /**
     * Perform operation on connection closed
     */
    private void performTaskOnConnectionClosed() {

        if (Utility.connectionClosedCalled == false) {
            try {
                Utility.loginCalledOnce = false;
                Utility.connectionClosedCalled = true;
                boolean_serviceCreatedOnce = false;
                Utils.isLoginTaskRunning = false;

                try {
                    if (WelcomeActivity.dismissProgressBar == true) {
                        Utility.stopDialog();
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                sendConnectionErrorWhileSignInBroadcast();
                sendConnectionErrorBroadcast();

                if (Utility.disconnected == false) {
                    reLogin();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Called when connection gets disconnected on internet state changed
     */
    private void performTaskOnConnectionClosedOnError() {

        boolean_serviceCreatedOnce = false;
        Utils.isLoginTaskRunning = false;
        Utility.loginCalledOnce = false;
        if (WelcomeActivity.dismissProgressBar == true) {
            try {
                Utility.stopDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Reconnect to openfire on connection break
     */
    public void reLogin() {

        if (NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {
            Utility.reloginCalled = true;
            try {
                MainService.mService.disconnect();
                clearConnectionCallbacks();
            } catch (Exception e) {
                e.printStackTrace();
            }
            connectAsync();
        }
    }

    /**
     * Called when Roster Entry is added
     */
    private class SubscribePacketListener implements PacketListener {

        public SubscribePacketListener() {
        }

        @Override
        public void processPacket(Packet packet) {
            from = packet.getFrom();

            NotificationCompat.Builder notif = new NotificationCompat.Builder(myApplication);
            String title = myApplication.getString(R.string.AcceptContactRequest, from.split("@")[0]);
            String text = myApplication.getString(R.string.AcceptContactRequestFrom, from.split("@")[0]);
            notif.setTicker(title).setContentTitle(title);

            if (android.os.Build.VERSION.SDK_INT < 16) {
                notif.setContentText(text);
            } else {
                notif.setSubText(text);
            }
            if (!checkForExistingEntry(from)) {

                Roster roster = MainService.mService.connection.getRoster();
                Collection<RosterEntry> entries = roster.getEntries();
                List<RosterEntry> userList = new ArrayList<RosterEntry>(entries);

                ArrayList<String> existIds = new ArrayList<String>();
                for (int i = 0; i < userList.size(); i++) {
                    String userId = userList.get(i).getUser();
                    existIds.add(userId);
                }
                if (!existIds.contains(from)) {
                    notif.setSmallIcon(R.drawable.notification_star);
                    notif.setAutoCancel(true).setWhen(System.currentTimeMillis());

                    SharedPreferences mSharedPreferencesVibRead = getSharedPreferences("mSharedPreferencesVibrateValue", 1);
                    RadioVibrate = mSharedPreferencesVibRead.getString("Vibrate_Value", "");
                    if (!TextUtils.isEmpty(RadioVibrate) && RadioVibrate.equalsIgnoreCase("On")
                            ) {
                        if (FilePath.equals("")) {
                            notif.setDefaults(Notification.DEFAULT_SOUND);
                            vibrator.vibrate(3000);
                        } else {
                            setCustomNotificationTone();
                            vibrator.vibrate(3000);
                        }
                    } else {
                        if (FilePath.equals("")) {
                            notif.setDefaults(Notification.DEFAULT_SOUND);
                        } else {
                            setCustomNotificationTone();
                        }
                    }
                    Intent intent = new Intent(myApplication, ContactActivity.class);
                    intent.setData(Contact.makeXmppUri(from));
                    ThatItApplication.getApplication().getIncomingRequestHash().put(from, packet);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setAction(Intent.ACTION_MAIN);

                    PendingIntent notifIntent = PendingIntent.getActivity(myApplication, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    notif.setContentIntent(notifIntent);
                    notif.setAutoCancel(true);
                    MainService.this.sendNotification(NOTIFICATION_FRIEND_REQUEST, notif.getNotification());

                    parseUtil.updateOperation(mService, from, AppSinglton.thatsItPincode, myParseListener, ParseCallbackListener.OPERATION_FRIEND_REQUEST_RECEIVED
                            , ParseOperationDecider.FRIEND_REQUEST_RECEIVED);
                }
            } else {
                ThatItApplication.getApplication().getSentInvites().remove(from.toUpperCase());
                ThatItApplication.getApplication().getSentInvites().remove(from.toLowerCase());
                showNotification("Subscription Message", " accepted your request on That's It.");

                Utility.onFriendAdded(from);
                triggerFragmentRefresh("Refresh_User_Adapter");
                parseUtil.removeRequest(mService, AppSinglton.thatsItPincode, from, myParseListener, ParseCallbackListener.OPERATION_FRIEND_REQUEST_DELETED);
            }
            Utility.fragmentInvitationsent.populateSentInvitations();
            Utility.fragmentInvitationReceive.populateIncomingInvitation();
        }
    }

    /**
     * Parse listener to filter when entry gets subscribed or unsubscribed
     */
    class MyParseListener implements ParseCallbackListener {

        @Override
        public void done(ParseException parseException, int requestId) {
            switch (requestId) {

                case OPERATION_FRIEND_REQUEST_RECEIVED:
                    Log.e("Operation", "operation_received");
                    break;

                case OPERATION_FRIEND_REQUEST_DELETED:
                    Log.e("Operation", "operation_deleted");
                    break;
            }
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void done(List<ParseObject> receipients, ParseException e,
                         int requestId) {

            switch (requestId) {
                case OPERATION_ON_START:
                    try {
                        Hashtable<String, Boolean> hashMap = new Hashtable<String, Boolean>();
                        for (int i = 0; i < receipients.size(); i++) {
                            String id = receipients.get(i).getString(getResources().getString(R.string.column_receipient));
                            id = (id + "@" + connection.getServiceName()).toUpperCase();
                            hashMap.put(id, true);
                        }
                        ThatItApplication.getApplication().setSentInvites(hashMap);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    taskOnCreate();
                    break;
            }
        }
    }

    /**
     * Show notificaton when user is added to group
     */
    private void showNotification(String title, String textMessage) {

        String text = from.split("@")[0] + textMessage;

        AudioPreference = getSharedPreferences("AudioPreference", 0);
        FilePath = AudioPreference.getString("AudioPreference", "");

        mNotificationManager = (NotificationManager) myApplication.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(myApplication)
                .setSmallIcon(R.drawable.notification_star);

        mBuilder.setContentTitle(text);

        Intent intent = new Intent(myApplication, ContactActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP /* Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT*/);
        intent.setAction(Intent.ACTION_MAIN);
        PendingIntent notifIntent = PendingIntent.getActivity(myApplication, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(notifIntent);
        mBuilder.setAutoCancel(true);
        SharedPreferences mSharedPreferencesVibRead = getSharedPreferences("mSharedPreferencesVibrateValue", 1);
        RadioVibrate = mSharedPreferencesVibRead.getString("Vibrate_Value", "");
        if (!TextUtils.isEmpty(RadioVibrate) && RadioVibrate.equalsIgnoreCase("On")
                ) {
            if (FilePath.equals("")) {
                mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                vibrator.vibrate(3000);
            } else {
                File source = new File(FilePath);
                if (source.exists()) {
                    setCustomNotificationTone();
                    vibrator.vibrate(3000);
                } else {
                    mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                }
            }
        } else {
            if (FilePath.equals("")) {
                mBuilder.setDefaults(Notification.DEFAULT_SOUND);
            } else {
                File source = new File(FilePath);
                if (source.exists()) {
                    setCustomNotificationTone();
                } else {
                    mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                }
            }
        }
        mNotificationManager.notify(4, mBuilder.build());

    }

    /**
     * Called when Roster Entry is removed
     */

    private class UnSubscribePacketListener implements PacketListener {

        public UnSubscribePacketListener() {
        }

        @SuppressLint("DefaultLocale")
        @SuppressWarnings("deprecation")
        @Override
        public void processPacket(Packet packet) {
            String from = packet.getFrom();

            Log.i(TAG, "Notify from   " + Contact.makeXmppUri(from));
            if (connection != null && connection.isAuthenticated()) {
                if (connection.getRoster().contains(from)) {
                    Collection<RosterEntry> rasterEntried = connection.getRoster().getEntries();

                    for (RosterEntry currentRosterEntry : rasterEntried) {
                        if (currentRosterEntry.getUser().equalsIgnoreCase(from)) {

                            try {
                                connection.getRoster().removeEntry(currentRosterEntry);
                                unsubsrcibedUser = currentRosterEntry.getUser();
                                ThatItApplication.getApplication().getIncomingPings().remove(currentRosterEntry.getUser());
                                ThatItApplication.getApplication().getIncomingFilePings().remove(currentRosterEntry.getUser());
                                ThatItApplication.getApplication().getSentInvites().remove(currentRosterEntry.getUser().toUpperCase());
                                ThatItApplication.getApplication().getSentInvites().remove(currentRosterEntry.getUser().toLowerCase());
                                Utility mUtility = new Utility();
                                mUtility.deleteUnsubscribedUserChatHistory(getRosterHistoryList,
                                        rosterHistoryToBeDeleted, unsubsrcibedUser, mService);

                                triggerFragmentRefresh("Refresh_User_Adapter");
                                Utility.removeFriendIfExists(from);


                                NotificationCompat.Builder notif = new NotificationCompat.Builder(myApplication);
                                String title = "Unsubscription Message";

                                String text = from.split("@")[0] + " unsubscribed you from That's It.";
                                notif.setTicker(title).setContentTitle(text);

                                Intent intent = new Intent(myApplication, ContactActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP /* Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT*/);
                                intent.setAction(Intent.ACTION_MAIN);
                                PendingIntent notifIntent = PendingIntent.getActivity(myApplication, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                notif.setContentIntent(notifIntent);
                                notif.setAutoCancel(true);

                                if (android.os.Build.VERSION.SDK_INT < 16) {
                                    notif.setContentText(text);
                                } else {
                                    notif.setSubText(text);
                                }

                                notif.setSmallIcon(R.drawable.notification_star);
                                notif.setAutoCancel(true).setWhen(System.currentTimeMillis());
                                SharedPreferences mSharedPreferencesVibRead = getSharedPreferences("mSharedPreferencesVibrateValue", 1);
                                RadioVibrate = mSharedPreferencesVibRead.getString("Vibrate_Value", "");
                                if (!TextUtils.isEmpty(RadioVibrate) && RadioVibrate.equalsIgnoreCase("On")
                                        ) {
                                    if (FilePath.equals("")) {
                                        notif.setDefaults(Notification.DEFAULT_SOUND);
                                        vibrator.vibrate(3000);
                                    } else {
                                        setCustomNotificationTone();
                                        vibrator.vibrate(3000);
                                    }
                                } else {
                                    if (FilePath.equals("")) {
                                        notif.setDefaults(Notification.DEFAULT_SOUND);
                                    } else {
                                        setCustomNotificationTone();
                                    }
                                }

                                try {
                                    Utility.fragmentInvitationsent.populateSentInvitations();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                MainService.this.sendNotification(5, notif.getNotification());

                            } catch (XMPPException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else if (ThatItApplication.getApplication().getIncomingRequestHash().containsKey(from)) {

                    ThatItApplication.getApplication().getIncomingRequestHash().remove(from);

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(mService.NOTIFICATION_FRIEND_REQUEST);
                }
            }
            parseUtil.removeRequest(mService, AppSinglton.thatsItPincode, from, myParseListener, ParseCallbackListener.OPERATION_FRIEND_REQUEST_DELETED);
            Utility.fragmentInvitationReceive.populateIncomingInvitation();
        }
    }

    private void triggerFragmentRefresh(String statusCode) {

        Intent intent = new Intent();
        intent.setAction(statusCode);
        sendBroadcast(intent);
    }

    private void checkExpiryStatus(String statusCode) {

        Intent intent = new Intent();
        intent.setAction(statusCode);
        sendBroadcast(intent);
    }

    /**
     * Show a notification using the preference of the user.
     *
     * @param id    the id of the notification.
     * @param notif the notification to show
     */
    public void sendNotification(int id, Notification notif) {
        NotificationManager mNotificationManager = (NotificationManager) myApplication
                .getSystemService(NOTIFICATION_SERVICE);
        notif.defaults |= Notification.DEFAULT_VIBRATE;
        notif.ledARGB = 0xff0000ff; // Blue color
        notif.ledOnMS = 1000;
        notif.ledOffMS = 1000;
        notif.flags |= Notification.FLAG_SHOW_LIGHTS;

        mNotificationManager.notify(id, notif);

    }

    /**
     * Listens Ping Request and responds with a pong
     */
    private class PingListener implements PacketListener {

        public PingListener() {
        }

        @Override
        public void processPacket(Packet packet) {
            if (!(packet instanceof PingExtension))
                return;
            PingExtension p = (PingExtension) packet;
            if (p.getType() == IQ.Type.GET) {
                PingExtension pong = new PingExtension();
                pong.setType(IQ.Type.RESULT);
                pong.setTo(p.getFrom());
                pong.setPacketID(p.getPacketID());
                connection.sendPacket(pong);
            }
        }
    }

    /**
     * Get roster list from openfire
     *
     * @param myRosterListner
     * @return list of roster objects
     */
    public ArrayList<RosterEntry> getRosters(MyRosterListner myRosterListner) {
        ArrayList<RosterEntry> rosterEntries = new ArrayList<RosterEntry>();
        try {
            Roster roster = connection.getRoster();
            roster.addRosterListener(myRosterListner);
            Collection<RosterEntry> entries = roster.getEntries();

            for (RosterEntry entry : entries) {
                rosterEntries.add(entry);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rosterEntries;
    }

    public ArrayList<RosterEntry> getRostersSuggest(
            MyRosterListnerSuggest myRosterListner) {
        ArrayList<RosterEntry> rosterEntries = new ArrayList<RosterEntry>();
        try {
            Roster roster = connection.getRoster();
            roster.addRosterListener(myRosterListner);
            Collection<RosterEntry> entries = roster.getEntries();

            for (RosterEntry entry : entries) {
                rosterEntries.add(entry);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rosterEntries;
    }

    public ArrayList<RosterEntry> getRostersInvite(
            MyRosterListnerInvite myRosterListner) {
        ArrayList<RosterEntry> rosterEntries = new ArrayList<RosterEntry>();
        try {
            Roster roster = connection.getRoster();
            roster.addRosterListener(myRosterListner);
            Collection<RosterEntry> entries = roster.getEntries();

            for (RosterEntry entry : entries) {
                rosterEntries.add(entry);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rosterEntries;
    }


    public class IncomingChatManagerListener implements ChatManagerListener {
        @Override
        public void chatCreated(Chat chat, boolean createdLocally) {

            if (!createdLocally) {
                chat.addMessageListener(mMessageListner);
            }
        }
    }

    /**
     * Incoming Message listener.
     */
    public class MyMessageListner implements MessageListener {
        @Override
        public void processMessage(final Chat chat, final Message message) {
            //message_read
            if (message.getSubject("isFile") != null) {
                message.getSubject("isFile");
            }

            String messageFrom1 = message.getFrom();

            if (messageFrom1.endsWith("/Smack")) {
                messageFrom1 = messageFrom1.substring(0, messageFrom1.lastIndexOf("/Smack"));
            }

            final String messageFrom = messageFrom1;

            Collection<PacketExtension> extensions = message.getExtensions();
            for (PacketExtension iterable_element : extensions) {
                if (iterable_element instanceof GroupChatInvitation) {

                    boolean_groupCreated = true;
                    GroupChatInvitation invitation = (GroupChatInvitation) iterable_element;
                    String roomName = invitation.getRoomAddress();
                    subscribeToGroupInvitation(roomName, message.getFrom());

                    if (boolean_istTimeLoad) {
                        ThatItApplication.getApplication().getIncomingGroupPings().add(roomName.substring(0, roomName.lastIndexOf("@")));
                    }
                }
            }
            try {
                Utility.fragmentContact.setGroupAdapter();
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            if (message.getBody() != null) {

                // IP required for socket file transfer
                if (message.getBody().equalsIgnoreCase("What is your ip")) {
                    if (!connection.isConnected()) return;
                    senderID = message.getFrom();
                    msg = "What is your ip";
                    sendReverseIP();
                } else if (message.getBody().equalsIgnoreCase("Send IP")) {
                    if (!connection.isConnected()) return;
                    try {
                        from = message.getFrom();
                        AppSinglton.IpAddress = message.getSubject();
                    } catch (Exception e) {
                        Log.d(TAG, "Error Delivering block");
                    }
                } else if (message.getBody().equalsIgnoreCase("Group Invitation")) {
                    if (!connection.isConnected()) return;
                    try {
                        if (boolean_groupCreated == true) {
                            from = message.getFrom();
                            showNotification("Group Added", " added you in a group.");
                            triggerFragmentRefresh("Refresh_Group_Adapter");
                            boolean_groupCreated = false;
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "Error Delivering block");
                    }
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            saveParticipantChat(chat, message);
                        }
                    }).start();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (Utility.fragChatHistoryOpened == true) {
                                    Utility.fragmentChatHistoryScreen.prepareChatRosterData();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            sendChatBroadast(chat, message);
                            checkForAppOpened(messageFrom);
                        }
                    });
                }
            }
        }

    }

    /**
     * App is in foreground or background
     */

    private void checkForAppOpened(String messageFrom) {
        setIncomingChatNotification();
        ThatItApplication.getApplication().getIncomingPings().add(messageFrom);
    }

    /**
     * Send IPV4 address to sender for file transfer
     */
    private void sendMessage(String senderId2, String msg2, MyMessageListner mMessageListner2) {

        if (!connection.isConnected()) return;
        ChatManager chatmanager = connection.getChatManager();
        Chat newChat = chatmanager.createChat(senderID, mMessageListner);
        try {
            Message newMessage = new Message();
            newMessage.setBody("Send IP");
            newMessage.setSubject(Utils.getLocalIpv4Address());
            newChat.sendMessage(newMessage);

        } catch (XMPPException e) {
            Log.d(TAG, "Error Delivering block");
        }
    }

    private void sendReverseIP() {
        try {
            if (connection.isConnected()) {
                sendMessage(senderID, msg, mMessageListner);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param chat
     * @param message Incoming chat message stored in DB
     */
    void saveParticipantChat(Chat chat, Message message) {
        try {
            RosterEntry rosterEntry = getRosterEntryFromJID(StringUtils.parseBareAddress(message.getFrom()));
            String jid = rosterEntry.getUser();
            name = rosterEntry.getName();
            String msg = message.getBody();

            if (msg.equalsIgnoreCase("What is your ip") || msg.equalsIgnoreCase("Send IP")
                    || msg.equalsIgnoreCase("composing") || msg.equalsIgnoreCase("gone")) {
            } else {
                if (msg.equals("Socket File")) {
                    msg = "I have sent you a file: " + message.getSubject("isFile") + " . Tap to Open.";
                    One2OneChatDb.addMessage(jid, name, msg, message.getSubject("isFile"), FragmentChatScreen.byteArray, DbOpenHelper.USER_TYPE_PARTICIPANT, "");
                    new One2OneChatDb(ThatItApplication.getApplication()).updateFileDownloadstatus(message.getSubject("isFile"));
                } else {
                    One2OneChatDb.addMessage(jid, name, msg, message.getSubject("isFile"), FragmentChatScreen.byteArray, DbOpenHelper.USER_TYPE_PARTICIPANT, "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sendChatBroadast(Chat chat, Message message) {
        try {
            RosterEntry rosterEntry = getRosterEntryFromJID(StringUtils.parseBareAddress(message.getFrom()));

            Intent intent = new Intent();
            intent.putExtra("jid", rosterEntry.getUser());
            intent.putExtra("name", rosterEntry.getName());
            intent.putExtra("msg", message.getBody());
            intent.putExtra("msg_subject", message.getSubject("isFile"));
            intent.setAction(CHAT);
            myApplication.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setIncomingChatListner() {
        if (connection.isConnected()) {
            chatmanager = connection.getChatManager();
            //	chatmanager.removeChatListener(mIncomingChatManagerListener);
            chatmanager.addChatListener(mIncomingChatManagerListener);
        }
    }

    /**
     * @param jid - Roster Entry
     */
    public RosterEntry getRosterEntryFromJID(String jid) {
        Roster roster = connection.getRoster();
        if (roster.contains(jid)) {
            return roster.getEntry(jid);
        }
        return null;
    }

    public FileTransferManager getFileTransferListener() {
        return this.fileTransferManager;
    }

    /**
     * SET FILE TRANSFER LISTENER
     */
    private void setFileTransferListener() {

        ProviderManager.getInstance().addIQProvider("query",
                "http://jabber.org/protocol/bytestreams",
                new BytestreamsProvider());
        ProviderManager.getInstance().addIQProvider("query",
                "http://jabber.org/protocol/disco#items",
                new DiscoverItemsProvider());
        ProviderManager.getInstance().addIQProvider("query",
                "http://jabber.org/protocol/disco#info",
                new DiscoverInfoProvider());

        if (fileTransferManager == null) {
            fileTransferManager = new FileTransferManager(connection);
            fileTransferManager.addFileTransferListener(new FileTransferListener() {
                public void fileTransferRequest(final FileTransferRequest request) {
                }
            });
        }
    }

    /**
     * Set custom tone from gallery for notification sound
     */
    @SuppressLint("NewApi")
    private static void setCustomNotificationTone() {

        Uri soundUri = MediaStore.Audio.Media.getContentUriForPath(FilePath);
        mNotification = new Notification.Builder(ThatItApplication.getApplication()).setSound(soundUri).build();
        mNotification.sound = Uri.parse(FilePath);
        NotificationManager notificationManager = (NotificationManager) ThatItApplication.getApplication().getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, mNotification);
    }

    void clearCredential() {
        mSettings = PreferenceManager.getDefaultSharedPreferences(ThatItApplication.getApplication());
        mSettings.edit().clear().commit();
    }


    @SuppressLint("DefaultLocale")
    private boolean checkForExistingEntry(String sender) {

        if (ThatItApplication.getApplication().getSentInvites().containsKey(sender.toUpperCase())) {

            Presence presence_ = new Presence(Type.subscribed);
            presence_.setTo(sender);

            PresenceAdapter preAdapt = new PresenceAdapter(presence_);
            Presence presence2 = new Presence(PresenceType.getPresenceTypeFrom(preAdapt.getType()));
            presence2.setTo(presence_.getTo());
            presence2.setFrom(connection.getUser());
            connection.sendPacket(presence2);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param fileName         file download from server
     * @param callbackInstance downlaoded successfully
     */
    public void acceptCurrentincomingFileRequest(String fileName, FileDownloadStatusCallback callbackInstance) {
        try {
            new FileReceiveraAsync_(fileName, null, null, mService).doInBackground(fileName, callbackInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public XMPPConnection getConnection() {

        if (connection != null && connection.isConnected()) {
            return connection;
        } else {
            return null;
        }
    }


    /**
     * Group chat helper methods
     */
    private void subscribeToGroupInvitation(final String roomName,
                                            final String invitationSender) {

        if (connection.isConnected() && connection.isAuthenticated()) {
            if (group_name.contains(roomName.split("@")[0])) {
                return;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        String roomName1 = roomName.split("@")[0];
                        RosterEntry entry = getEntryforInvitee(invitationSender);
                        if (!doesGroupExist(roomName)) {
                            try {
                                connection.getRoster().createGroup(roomName.split("@")[0]);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            connection.getRoster().getGroup(roomName.split("@")[0]).addEntry(entry);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * @param jid - jID to whom group invitaion is to be sent
     * @return roster entry
     */
    private RosterEntry getEntryforInvitee(String jid) {
        Collection<RosterEntry> entries = connection.getRoster().getEntries();
        for (RosterEntry iterable_element : entries) {
            return iterable_element;
        }
        return null;
    }

    /**
     * Check for group duplicacy
     */
    private boolean doesGroupExist(String groopName) {
        Collection<RosterGroup> rosterGroups = connection.getRoster().getGroups();
        for (RosterGroup currentGroup : rosterGroups) {
            if (currentGroup.getName().equals(groopName))
                return true;
        }
        return false;
    }

    public boolean isRunningInForeground() {
        ActivityManager manager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = manager.getRunningTasks(1);
        if (tasks.isEmpty()) {
            return false;
        }
        String topActivityName = tasks.get(0).topActivity.getPackageName();
        return topActivityName.equalsIgnoreCase(getPackageName());
    }

    /**
     * Broadcast message if connection error occurs
     */
    private void sendConnectionErrorWhileSignInBroadcast() {
        Intent intent = new Intent();
        intent.setAction("Connection Error While Sign In");
        ThatItApplication.getApplication().sendBroadcast(intent);
    }

    private void sendConnectionErrorBroadcast() {
        Intent intent = new Intent();
        intent.setAction("Connection Error");
        ThatItApplication.getApplication().sendBroadcast(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        boolean_serviceCreatedOnce = false;
        boolean_istTimeLoad = true;
        try {
            if (mNotificationManager != null) {
                mNotificationManager.cancel(MainService.NOTIFICATION_MESSAGE_RECEIVED);
                mNotificationManager.cancel(MainService.NOTIFICATION_FRIEND_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            this.unregisterReceiver(networkChangeReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopForeground(true);
        Intent restartService = new Intent(getApplicationContext(), this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePI);
    }
}
