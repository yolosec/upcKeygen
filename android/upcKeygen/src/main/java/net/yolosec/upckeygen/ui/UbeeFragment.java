package net.yolosec.upckeygen.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import net.yolosec.upckeygen.R;
import net.yolosec.upckeygen.algorithms.Keygen;
import net.yolosec.upckeygen.algorithms.WiFiNetwork;

import java.util.Locale;

/**
 * Created by miroc on 31.1.16.
 */
public class UbeeFragment extends Fragment {
    private static final String TAG = "UbeeFragment";
    private View loading;
    private View mainView;

    public static UbeeFragment newInstance() {
        Bundle args = new Bundle();
        UbeeFragment fragment = new UbeeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater
                .inflate(R.layout.fragment_ubee, container, false);
        loading = view.findViewById(R.id.loading_spinner);
        mainView = view.findViewById(R.id.main_view);

        final CheckBox freq24 = (CheckBox) view.findViewById(R.id.radio_24);
        final CheckBox freq5 = (CheckBox) view.findViewById(R.id.radio_5);

        final CustomEditText macs[] = new CustomEditText[6];
        macs[0] = (CustomEditText) view.findViewById(R.id.input_mac_pair1);
        macs[1] = (CustomEditText) view.findViewById(R.id.input_mac_pair2);
        macs[2] = (CustomEditText) view.findViewById(R.id.input_mac_pair3);
        macs[3] = (CustomEditText) view.findViewById(R.id.input_mac_pair4);
        macs[4] = (CustomEditText) view.findViewById(R.id.input_mac_pair5);
        macs[5] = (CustomEditText) view.findViewById(R.id.input_mac_pair6);

        for (int i = 0; i < macs.length; i++){
            // remember each mac field number in tag
            macs[i].setTag(i);
        }

        // pre-fill Ubee MAC prefix
        macs[0].setText("64");
        macs[1].setText("7c");
        macs[2].setText("34");

        final InputFilter filterMac = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                if (dstart >= 2)
                    return "";
                if (source.length() > 2)
                    return "";// max 2 chars
                for (int i = start; i < end; i++) {
                    if (Character.digit(source.charAt(i), 16) == -1) {
                        return "";
                    }
                }
                if (source.length() + dstart > 2)
                    return source.subSequence(0, 2 - dstart);
                return null;
            }
        };

//        for (int i = 0; i< macs.length; i++){
//            final EditText mac = macs[i];

//        }
        for (final CustomEditText mac : macs) {
            mac.setFilters(new InputFilter[]{filterMac});
            mac.addTextChangedListener(new TextWatcher() {
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.d(TAG, String.format("onTextChanged; seq=%s, start=%d, before=%d, count=%d", s, start, before, count));
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    Log.d(TAG, String.format("onTextChanged; seq=%s, start=%d, after=%d, count=%d", s, start, after, count));
                }

                public void afterTextChanged(Editable e) {
                    if (e.length() != 2)
                        return;

                    for (int i = 0; i < 6; ++i) {
                        if (macs[i].getText().length() >= 2)
                            continue;

                        macs[i].requestFocus();
                        return;
                    }
                }
            });
        }

        Button calc = (Button) view.findViewById(R.id.bt_calc);
        calc.setOnClickListener(new View.OnClickListener() {

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public void onClick(View v) {
                StringBuilder mac = new StringBuilder();
                boolean warnUnused = false;
                for (EditText m : macs) {
                    final String mText = m.getText().toString();
                    if (mText.length() > 0)
                        warnUnused = true;
                    mac.append(mText);
                    if (!m.equals(macs[5]))
                        mac.append(":"); // do not add this for the
                    // last one
                }
                if (mac.length() < 17) {
                    mac.setLength(0);
                    if (warnUnused){
                        Toast.makeText(getActivity(), R.string.msg_invalid_mac,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                }

                // Mode is not used
                int mode = 0;
                // SSID is not used in the computation
                KeygenMatcherTask matcher = new KeygenMatcherTask("", mac
                        .toString().toUpperCase(Locale.getDefault()), mode);
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
                    matcher.execute();
                } else {
                    matcher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

            }
        });
        macs[3].requestFocus();
        return view;
    }

    private class KeygenMatcherTask extends AsyncTask<Void, Void, WiFiNetwork> {
        private final String ssid;
        private final String mac;
        private int mode;

        public KeygenMatcherTask(String ssid, String mac, int mode) {
            this.ssid = ssid;
            this.mac = mac;
            this.mode = mode;
        }

        @Override
        protected void onPreExecute() {
            mainView.setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(WiFiNetwork wifiNetwork) {
            loading.setVisibility(View.GONE);
            mainView.setVisibility(View.VISIBLE);

            Intent detailIntent = new Intent(getContext(), NetworkActivity.class);
            detailIntent.putExtra(NetworkFragment.NETWORK_ID, wifiNetwork);
            startActivity(detailIntent);
        }

        @Override
        protected WiFiNetwork doInBackground(Void... params) {
            return new WiFiNetwork(ssid, mac, 0, mode, "", null);
        }
    }
}
