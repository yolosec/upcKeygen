package net.yolosec.upckeygen.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;

import net.yolosec.upckeygen.R;

/**
 * Created by dusanklinec on 11.01.16.
 */
public class AboutTabHostActivity extends AppCompatActivity {
    private static final String TAG = "AboutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_dialog);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch(Exception e){
            Log.e(TAG, "Exception", e);
        }

        TabHost tabs = (TabHost) findViewById(R.id.tabhost);
        tabs.setup();

        TabHost.TabSpec tspec1 = tabs.newTabSpec("about");
        tspec1.setIndicator(getString(R.string.pref_2section));
        tspec1.setContent(R.id.text_about_scroll);
        TextView text = ((TextView) findViewById(R.id.text_about));
        text.setMovementMethod(LinkMovementMethod.getInstance());
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            text.append(version);
        } catch(Exception e){
            Log.e(TAG, "Exception in getting app version", e);
        }
        tabs.addTab(tspec1);

        TabHost.TabSpec tspec2 = tabs.newTabSpec("credits");
        tspec2.setIndicator(getString(R.string.dialog_about_credits));
        tspec2.setContent(R.id.about_credits_scroll);
        ((TextView) findViewById(R.id.about_credits))
                .setMovementMethod(LinkMovementMethod.getInstance());
        tabs.addTab(tspec2);
        TabHost.TabSpec tspec3 = tabs.newTabSpec("license");
        tspec3.setIndicator(getString(R.string.dialog_about_license));
        tspec3.setContent(R.id.about_license_scroll);
        ((TextView) findViewById(R.id.about_license))
                .setMovementMethod(LinkMovementMethod.getInstance());
        tabs.addTab(tspec3);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpTo(
                        this, new Intent(this, ManualInputActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                );
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
