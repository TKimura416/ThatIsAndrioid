
package com.thatsit.android.invites;

import org.jivesoftware.smack.packet.Presence;

import android.os.Parcel;
import android.os.Parcelable;

import com.thatsit.android.xmpputils.Status;

/**
 * this class contain contact presence informations.
 */
public class PresenceAdapter implements Parcelable {

    /** Parcelable.Creator needs by Android. */
    public static final Parcelable.Creator<PresenceAdapter> CREATOR = new Parcelable.Creator<PresenceAdapter>() {

    @Override
    public PresenceAdapter createFromParcel(Parcel source) {
        return new PresenceAdapter(source);
    }

    @Override
    public PresenceAdapter[] newArray(int size) {
        return new PresenceAdapter[size];
    }
    };

    private int mType;
    private int mStatus;
    private String mTo;
    private String mFrom;
    private String mStatusText;

    /**
     * constructor from Parcel.
     * @param source parcelable presence.
     */
    public PresenceAdapter(final Parcel source) {
    mType = source.readInt();
    mStatus = source.readInt();
    mTo = source.readString();
    mFrom = source.readString();
    mStatusText = source.readString();
    }

    /**
     * constructor from smack Presence.
     * @param presence smack presence.
     */
    public PresenceAdapter(final Presence presence) {
    mType = PresenceType.getPresenceType(presence);
    mStatus = Status.getStatusFromPresence(presence);
    mTo = presence.getTo();
    mFrom = presence.getFrom();
    mStatusText = presence.getStatus();
    }

    /* (non-Javadoc)
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
    return 0;
    }

    /**
     * mFrom getter.
     * @return the mFrom
     */
    public String getFrom() {
    return mFrom;
    }

    /**
     * mStatus getter.
     * @return the mStatus
     */
    public int getStatus() {
    return mStatus;
    }

    /**
     * mStatusText getter.
     * @return the mStatusText
     */
    public String getStatusText() {
    return mStatusText;
    }

    /**
     * mTo getter.
     * @return the mTo
     */
    public String getTo() {
    return mTo;
    }

    /**
     * mType getter.
     * @return the mType
     */
    public int getType() {
    return mType;
    }

    /**
     * mFrom setter.
     * @param from the mFrom to set
     */
    public void setFrom(final String from) {
    this.mFrom = from;
    }

    /**
     * mStatus setter.
     * @param status the mStatus to set
     */
    public void setStatus(final int status) {
    this.mStatus = status;
    }

    /**
     * mStatusText setter.
     * @param statusText the mStatusText to set
     */
    public void setStatusText(final String statusText) {
    this.mStatusText = statusText;
    }

    /**
     * mTo setter.
     * @param to the mTo to set
     */
    public void setTo(final String to) {
    this.mTo = to;
    }

    /**
     * mType setter.
     * @param type the type to set
     */
    public void setType(int type) {
    this.mType = type;
    }

    /* (non-Javadoc)
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(mType);
    dest.writeInt(mStatus);
    dest.writeString(mTo);
    dest.writeString(mFrom);
    dest.writeString(mStatusText);
    }
}
