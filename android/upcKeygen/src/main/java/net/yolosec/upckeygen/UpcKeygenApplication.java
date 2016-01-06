package net.yolosec.upckeygen;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

public class UpcKeygenApplication extends Application {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                StrictMode
                        .setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                                .detectAll().penaltyLog().build());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog()
                            .build());
                } else {
                    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                            .detectLeakedSqlLiteObjects().penaltyLog().build());

                }
            }
        }
    }
}