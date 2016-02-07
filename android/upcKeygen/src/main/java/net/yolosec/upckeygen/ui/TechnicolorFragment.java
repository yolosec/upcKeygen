package net.yolosec.upckeygen.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import net.yolosec.upckeygen.R;
import net.yolosec.upckeygen.algorithms.Keygen;
import net.yolosec.upckeygen.algorithms.WiFiNetwork;

/**
 * Created by miroc on 31.1.16.
 */
public class TechnicolorFragment extends Fragment {

    private View loading;
    private View mainView;

    public static TechnicolorFragment newInstance() {
        Bundle args = new Bundle();
        TechnicolorFragment fragment = new TechnicolorFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_technicolor, container, false);

        loading = view.findViewById(R.id.loading_spinner);
        mainView = view.findViewById(R.id.main_view);
        final CheckBox freq24 = (CheckBox) view.findViewById(R.id.radio_24);
        final CheckBox freq5 = (CheckBox) view.findViewById(R.id.radio_5);
        final AutoCompleteTextView edit = (AutoCompleteTextView) view
                .findViewById(R.id.manual_autotext);

        final String[] routers = getResources().getStringArray(
                R.array.supported_routers);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, routers);

        edit.setAdapter(adapter);
        edit.setThreshold(1);
        edit.requestFocus();

        final InputFilter filterSSID = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i))
                            && source.charAt(i) != '-'
                            && source.charAt(i) != '_'
                            && source.charAt(i) != ' ') {
                        return "";
                    }
                }
                return null;
            }
        };
        final InputFilter lengthFilter = new InputFilter.LengthFilter(8); //Filter to 10 characters
        edit.setFilters(new InputFilter[]{filterSSID, lengthFilter});
        edit.setImeOptions(EditorInfo.IME_ACTION_DONE);

        Button calc = (Button) view.findViewById(R.id.bt_calc);
        calc.setOnClickListener(new View.OnClickListener() {

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public void onClick(View v) {
                String ssid = "UPC" + edit.getText().toString().trim();
                if (!freq24.isChecked() && !freq5.isChecked()) {
                    freq24.setChecked(true);
                    freq5.setChecked(true);
                }

                int mode = (freq24.isChecked() ? 1 : 0) | (freq5.isChecked() ? 2 : 0);
                KeygenMatcherTask matcher = new KeygenMatcherTask(ssid, mode);
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
                    matcher.execute();
                } else {
                    matcher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

            }
        });

        return view;
    }

    private class KeygenMatcherTask extends AsyncTask<Void, Void, WiFiNetwork> {
        private final String ssid;
        private int mode;

        public KeygenMatcherTask(String ssid, int mode) {
            this.ssid = ssid;
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
            if (wifiNetwork.getSupportState() == Keygen.UNSUPPORTED) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), R.string.msg_unspported_network,
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }

            Intent detailIntent = new Intent(getContext(), NetworkActivity.class);
            detailIntent.putExtra(NetworkFragment.NETWORK_ID, wifiNetwork);
            startActivity(detailIntent);
        }

        @Override
        protected WiFiNetwork doInBackground(Void... params) {
            String mac = ""; // mac is not used for technicolor password derivation
            return new WiFiNetwork(ssid, mac, 0, mode, "", null);
        }
    }
}
