package net.yolosec.upckeygen.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.yolosec.upckeygen.R;
import net.yolosec.upckeygen.algorithms.WiFiKey;

import java.util.List;

/**
 * Created by miroc on 10.01.16.
 */
public class WiFiKeyUbeeAdapter extends ArrayAdapter<WiFiKey> {

    public WiFiKeyUbeeAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public WiFiKeyUbeeAdapter(Context context, int resource, List<WiFiKey> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.key_ubee_row, null);
        }

        final WiFiKey p = getItem(position);

        if (p != null) {
            TextView keyField = (TextView) v.findViewById(R.id.wifi_key);
            TextView ssidField = (TextView) v.findViewById(R.id.wifi_ssid);

            if (keyField != null) {
                keyField.setText(getContext().getString(R.string.router_password, p.getKey()));
            }

            if (ssidField != null){
                final String ssid = p.getSsid();
                if (ssid == null || ssid.length() == 0){
                    ssidField.setVisibility(View.GONE);
                } else {
                    ssidField.setText(getContext().getString(R.string.router_ssid, ssid));
                }
            }
        }

        return v;
    }

}