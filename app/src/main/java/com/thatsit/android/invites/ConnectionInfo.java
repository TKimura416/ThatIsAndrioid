
package com.thatsit.android.invites;

import org.jivesoftware.smack.XMPPConnection;


public class ConnectionInfo {

    private static ConnectionInfo connectionInfo;
    private String username;
    private String password;
    private boolean isConnectednLoggedIn;
    private boolean isConnected;
    XMPPConnection xmppConnection;
    
    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public static synchronized ConnectionInfo getInstance(){
        if(connectionInfo == null){
            connectionInfo = new ConnectionInfo();
        }
        return connectionInfo;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isConnectednLoggedIn() {
        return isConnectednLoggedIn;
    }

    public void setConnectednLoggedIn(boolean isConnectednLoggedIn) {
        this.isConnectednLoggedIn = isConnectednLoggedIn;
    }

}
