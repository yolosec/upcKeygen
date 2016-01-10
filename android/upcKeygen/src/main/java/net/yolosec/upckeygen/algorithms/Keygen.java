/*
 * Copyright 2012 Rui Araújo, Luís Fonseca
 *
 * This file is part of Router Keygen.
 *
 * Router Keygen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Router Keygen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Router Keygen.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.yolosec.upckeygen.algorithms;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public abstract class Keygen implements Parcelable {
    public static final int SUPPORTED = 2;
    public static final int UNLIKELY_SUPPORTED = 1;
    public static final int UNSUPPORTED = 0;
    public static final Parcelable.Creator<Keygen> CREATOR = new Parcelable.Creator<Keygen>() {

        public Keygen[] newArray(int size) {
            return new Keygen[size];
        }

        @Override
        public Keygen createFromParcel(Parcel source) {
            return null;
        }
    };
    final private String ssidName;
    final private String macAddress;
    final private int mode;
    private final List<WiFiKey> pwList = new ArrayList<>();
    private boolean stopRequested = false;
    private int errorCode;
    protected KeygenMonitor monitor;

    public Keygen(final String ssid, final String mac, int mode) {
        this.ssidName = ssid;
        this.macAddress = mac.replace(":", "").toUpperCase(Locale.getDefault());
        this.mode = mode;
    }

    Keygen(Parcel in) {
        ssidName = in.readString();
        if (in.readInt() == 1)
            macAddress = in.readString();
        else
            macAddress = "";
        errorCode = in.readInt();
        mode = in.readInt();
        stopRequested = in.readInt() == 1;
        in.readTypedList(pwList, WiFiKey.CREATOR);
    }

    static String incrementMac(String mac, int increment) {
        String incremented = Long.toHexString(Long.parseLong(mac, 16) + increment)
                .toLowerCase(Locale.getDefault());
        //Any leading zeros will disappear in this process.
        //TODO: add tests for this.
        final int leadingZerosCount = mac.length() - incremented.length();
        for (int i = 0; i < leadingZerosCount; ++i) {
            incremented = "0" + incremented;
        }
        return incremented;
    }

    synchronized boolean isStopRequested() {
        return stopRequested;
    }

    public synchronized void setStopRequested(boolean stopRequested) {
        this.stopRequested = stopRequested;
    }

    String getMacAddress() {
        return macAddress;
    }

    String getSsidName() {
        return ssidName;
    }

    void addPassword(final WiFiKey key) {
        if (!pwList.contains(key))
            pwList.add(key);
    }

    List<WiFiKey> getResults() {
        return pwList;
    }

    abstract public List<String> getKeys();

    /**
     * Override if want to support extended features.
     * @return
     */
    public List<WiFiKey> getKeysExt(){
        final List<String> keys = getKeys();
        final List<WiFiKey> keysExt = new LinkedList<>();
        for(String key : keys){
            keysExt.add(new WiFiKey(key, null, WiFiKey.BAND_24 | WiFiKey.BAND_5));
        }

        return keysExt;
    }

    /**
     * True if keygen supports progress reporting.
     * @return
     */
    public boolean keygenSupportsProgress(){
        return false;
    }

    public int getErrorCode() {
        return errorCode;
    }

    void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public KeygenMonitor getMonitor() {
        return monitor;
    }

    public void setMonitor(KeygenMonitor monitor) {
        this.monitor = monitor;
    }

    public int getMode() {
        return mode;
    }

    public int getSupportState() {
        return SUPPORTED;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ssidName);
        dest.writeInt(macAddress != null ? 1 : 0);
        if (macAddress != null)
            dest.writeString(macAddress);
        dest.writeInt(errorCode);
        dest.writeInt(mode);
        dest.writeInt(stopRequested ? 1 : 0);
        dest.writeTypedList(pwList);
        //dest.writeParcelableArray(pwList.toArray(new Parcelable[pwList.size()]), 0);
    }

}
