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
 * Created by dusanklinec on 10.01.16.
 */
public class WiFiKeyAdapter extends ArrayAdapter<WiFiKey> {

    public WiFiKeyAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public WiFiKeyAdapter(Context context, int resource, List<WiFiKey> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.key_row, null);
        }

        final WiFiKey p = getItem(position);

        if (p != null) {
            TextView keyField = (TextView) v.findViewById(R.id.wifi_key);
            TextView keyBandField = (TextView) v.findViewById(R.id.wifi_key_band);
            TextView serialField = (TextView) v.findViewById(R.id.wifi_serial);

            if (keyField != null) {
                keyField.setText(p.getKey());
            }

            if (keyBandField != null) {
                if (p.getBandType() == WiFiKey.BAND_24){
                    keyBandField.setText(R.string.freq_24);
                } else if (p.getBandType() == WiFiKey.BAND_5){
                    keyBandField.setText(R.string.freq_5);
                } else {
                    keyBandField.setVisibility(View.GONE);
                }
            }

            if (serialField != null){
                final String serial = p.getSerial();
                if (serial == null || serial.length() == 0){
                    serialField.setVisibility(View.GONE);
                } else {
                    serialField.setText(getContext().getString(R.string.serial, serial));
                }
            }
        }

        return v;
    }

}