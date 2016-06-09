package com.myquick.groupchat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.MultiUserChat;

import com.thatsit.android.xmpputils.Constants;

/**
 * contains the helper methods associated with group chat functionality.
 */


class GroupChatClient {
	public static void createNewChatGroup(XMPPConnection connection , String groupQualifiedName, String ownerJid) throws XMPPException{
		
		// Create a MultiUserChat using a XMPPConnection for a room
	      MultiUserChat muc = new MultiUserChat(connection, ownerJid+"__"+groupQualifiedName+"@conference."+Constants.HOST);

	      // Create the room
	      muc.create(connection.getUser());

	      // Get the the room's configuration form
	      Form form = muc.getConfigurationForm();
	      // Create a new form to submit based on the original form
	      Form submitForm = form.createAnswerForm();
	      // Add default answers to the form to submit
	      for (Iterator fields = form.getFields(); fields.hasNext();) {
	          FormField field = (FormField) fields.next();
	          if (!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null) {
	              // Sets the default value as the answer
	              submitForm.setDefaultAnswer(field.getVariable());
	          }
	      }
	      // Sets the new owner of the room
	      List<String> owners = new ArrayList<>();
	      owners.add(connection.getUser());
	      submitForm.setAnswer("muc#roomconfig_roomowners", owners);
	      // Send the completed form (with default values) to the server to configure the room
	      muc.sendConfigurationForm(submitForm);
	     muc.join(ownerJid+"@"+Constants.HOST);
	}
}
