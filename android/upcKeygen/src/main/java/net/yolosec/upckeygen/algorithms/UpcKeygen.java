package net.yolosec.upckeygen.algorithms;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import net.yolosec.upckeygen.R;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/*
 * Copyright 2016 Dusan Klinec, Miroslav Svitok
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
 *
 * Using UPC default key generator implemented by blasty.
 * Source: https://haxx.in/upc_keys.c
 */
public class UpcKeygen extends Keygen {
    private static final String TAG="UpcKeygen";
    private final List<WiFiKey> computedKeys = new LinkedList<>();
    private int isUbee = 0;

    static {
        System.loadLibrary("upc");
    }

    public static final Parcelable.Creator<UpcKeygen> CREATOR = new Parcelable.Creator<UpcKeygen>() {
        public UpcKeygen createFromParcel(Parcel in) {
            return new UpcKeygen(in);
        }
        public UpcKeygen[] newArray(int size) {
            return new UpcKeygen[size];
        }
    };

    public UpcKeygen(String ssid, String mac, int mode) {
        super(ssid, mac, mode);
    }

    public UpcKeygen(String ssid, String mac, int mode, boolean isUbee) {
        super(ssid, mac, mode);
        this.isUbee = isUbee ? 1 :0 ;
    }

    private UpcKeygen(Parcel in) {
        super(in);
        isUbee = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(isUbee);
    }

    @Override
    public int getSupportState() {
        if (isUbee > 0 && (getMacAddress().startsWith("64:7C:34") || getMacAddress().toUpperCase().startsWith("647C34"))){
            return SUPPORTED;
        }

        if (getSsidName().matches("UPC[0-9]{7}")) {
            return SUPPORTED;
        } else if (getSsidName().matches("UPC[0-9]{5,6}")) {
            return UNLIKELY_SUPPORTED;
        } else if (getSsidName().matches("UPC[0-9]{8}")) {
            return UNLIKELY_SUPPORTED;
        }

        return UNSUPPORTED;
    }

    @Override
    public synchronized void setStopRequested(boolean stopRequested) {
        super.setStopRequested(stopRequested);
    }

    public void doComputeKeys() throws UnsupportedEncodingException {
        if (isUbee == 0){
            Log.d(TAG, "Starting a new task for ssid (Thompson): " + getSsidName());
            upcNative(getSsidName().getBytes("US-ASCII"), getMode());
        } else {
            Log.d(TAG, "Starting a new task for mac (Ubee): " + getMacAddress());

            // Ubee extension first, better matching.
            final BigInteger macInt = new BigInteger(getMacAddress(), 16);
            final BigInteger macStart = macInt.subtract(BigInteger.valueOf(6));
            for(int i=0; i<12; i++){
                final BigInteger curMac = macStart.add(BigInteger.valueOf(i));
                final String curSsid = upcUbeeSsid(curMac.toByteArray());
                final String curPass = upcUbeePass(curMac.toByteArray());

                WiFiKey wiFiKey = new WiFiKey(curPass, "", getMode(), computedKeys.size());
                wiFiKey.setSsid(curSsid);

                computedKeys.add(wiFiKey);
                if (monitor != null){
                    monitor.onKeyComputed();
                }
            }
        }
    }

    @Override
    public List<String> getKeys() {
        // not supported.
        return Collections.emptyList();
    }

    public List<WiFiKey> getKeysExt(){
        try {
            doComputeKeys();

        } catch (Exception e) {
            Log.e(TAG, "Exception in native computation", e);
            setErrorCode(R.string.msg_err_native);
        }

        if (isStopRequested() || computedKeys.isEmpty())
            return null;
        for (WiFiKey key : computedKeys)
            addPassword(key);
        if (getResults().size() == 0)
            setErrorCode(R.string.msg_errnomatches);
        return getResults();
    }

    /**
     * Called by native code when a key is computed.
     */
    public void onKeyComputed(String key, String serial, int mode, int type){
        computedKeys.add(new WiFiKey(key, serial, mode, computedKeys.size()));
        if (monitor != null){
            monitor.onKeyComputed();
        }
    }

    /**
     * Called by native code when a progress in computation is made.
     * @param progress
     */
    public void onProgressed(double progress){
        if (monitor != null){
            monitor.onKeygenProgressed(progress);
        }
    }

    @Override
    public boolean keygenSupportsProgress() {
        return true;
    }

    /**
     * Native key generator implementation.
     * @param essid
     * @return
     */
    private native void upcNative(byte[] essid, int mode);

    /**
     * Returns SSID
     * @param macc
     * @return
     */
    private native String upcUbeeSsid(byte[] macc);

    /**
     * Returns passwd for given mac.
     * @param mac
     * @return
     */
    private native String upcUbeePass(byte[] mac);
}
