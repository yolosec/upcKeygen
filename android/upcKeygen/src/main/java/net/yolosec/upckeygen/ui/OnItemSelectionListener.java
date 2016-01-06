package net.yolosec.upckeygen.ui;

import net.yolosec.upckeygen.algorithms.WiFiNetwork;

/**
 * Created by dusanklinec on 06.01.16.
 */
public interface OnItemSelectionListener {

    void onItemSelected(WiFiNetwork id);

    void onItemSelected(String mac);
}
