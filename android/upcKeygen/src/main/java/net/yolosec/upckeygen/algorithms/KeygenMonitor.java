package net.yolosec.upckeygen.algorithms;

/**
 * Created by dusanklinec on 08.01.16.
 */
public interface KeygenMonitor {
    void onKeyComputed();
    void onKeygenProgressed(double progress);
}
