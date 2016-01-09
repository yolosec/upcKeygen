package net.yolosec.upckeygen;


import net.yolosec.upckeygen.algorithms.Keygen;
import net.yolosec.upckeygen.algorithms.UpcKeygen;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WirelessMatcher {
    public synchronized static ArrayList<Keygen> getKeygen(String ssid,
                                                           String mac, int mode, ZipInputStream magicInfo) {
        final ArrayList<Keygen> keygens = new ArrayList<>();
        if (ssid.matches("UPC[0-9]{5,8}")) {
            keygens.add(new UpcKeygen(ssid, mac, mode));
        }

        return keygens;
    }

    private static InputStream getEntry(String filename,
                                        ZipInputStream magicInfo) {
        ZipEntry entry = null;
        try {
            do {
                entry = magicInfo.getNextEntry();
            } while (entry != null && !filename.equals(entry.getName()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (entry != null)
            return magicInfo;
        return null;
    }
}
