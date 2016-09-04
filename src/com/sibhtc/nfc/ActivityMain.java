package com.sibhtc.nfc;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcManager;
import android.nfc.NfcAdapter;
import android.nfc.INfcAdapter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.util.Log;
import java.lang.Exception;
import android.content.BroadcastReceiver;
import android.content.res.Resources;
import android.os.RemoteException;

public class ActivityMain extends Activity {

    private static final String TAG = "NfcSettings";

    private String NFC_STATE_TEMPLATE;
    private String NFC_STATE_ADAPTER_MISSING;
    private String NFC_STATE_SERVICE_DEAD;
    private String NFC_STATE_OFF;
    private String NFC_STATE_TURNING_ON;
    private String NFC_STATE_ON;
    private String NFC_STATE_TURNING_OFF;
    private String NFC_STATE_UNKNOWN;

    private Button   mButton;
    private TextView mTextView;

    private BroadcastReceiver mNfcBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NfcAdapter.ACTION_ADAPTER_STATE_CHANGED.equals(intent.getAction())) {
                onNfcStateChanged(intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF));
            }
        }
    };

    private IntentFilter mIntentFilter = new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);

    private NfcAdapter mNfcAdapter;

    private String mCurrentNfcAdapterState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.rect_activity_main);

        mButton   = (Button)   findViewById(R.id.button);
        mTextView = (TextView) findViewById(R.id.text);

        Resources resources = getResources();

        NFC_STATE_TEMPLATE        = resources.getString(R.string.nfc_stateTemplate);
        NFC_STATE_ADAPTER_MISSING = resources.getString(R.string.nfc_missing);
        NFC_STATE_OFF             = resources.getString(R.string.nfc_off);
        NFC_STATE_TURNING_ON      = resources.getString(R.string.nfc_turningOn);
        NFC_STATE_ON              = resources.getString(R.string.nfc_on);
        NFC_STATE_TURNING_OFF     = resources.getString(R.string.nfc_turningOff);
        NFC_STATE_UNKNOWN         = resources.getString(R.string.nfc_unknown);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        mButton.setEnabled(mNfcAdapter != null);

        updateNfcAdapterState();
        displayNfcAdapterState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.registerReceiver(mNfcBroadcastReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.unregisterReceiver(mNfcBroadcastReceiver);
    }

    private String getAdapterStateString(int serviceState) {
        switch (serviceState) {
            case NfcAdapter.STATE_OFF:
                return NFC_STATE_OFF;
            case NfcAdapter.STATE_TURNING_ON:
                return NFC_STATE_TURNING_ON;
            case NfcAdapter.STATE_ON:
                return NFC_STATE_ON;
            case NfcAdapter.STATE_TURNING_OFF:
                return NFC_STATE_TURNING_OFF;
        }

        return NFC_STATE_UNKNOWN;
    }

    private String getCurrentNfcAdapterState() {
        if (mNfcAdapter == null)
            return NFC_STATE_ADAPTER_MISSING;

        INfcAdapter nfcService = mNfcAdapter.getService();

        int serviceState;

        try {
            serviceState = nfcService.getState();
        } catch (RemoteException e) {
            Log.e(TAG, "getCurrentNfcAdapterState: NfcService is dead. Recovery will be attempted when turning on.");
            return NFC_STATE_SERVICE_DEAD;
        }

        return getAdapterStateString(serviceState);
    }

    private void updateNfcAdapterState() {
        mCurrentNfcAdapterState = getCurrentNfcAdapterState();
    }

    private void onNfcStateChanged(int newState) {
        mCurrentNfcAdapterState = getAdapterStateString(newState);

        switch(newState) {
            case NfcAdapter.STATE_OFF:
                mButton.setEnabled(true);
                break;
            case NfcAdapter.STATE_TURNING_ON:
                mButton.setEnabled(false);
                break;
            case NfcAdapter.STATE_ON:
                mButton.setEnabled(true);
                break;
            case NfcAdapter.STATE_TURNING_OFF:
                mButton.setEnabled(false);
                break;
        }

        displayNfcAdapterState();
    }

    private void displayNfcAdapterState() {
        mTextView.setText(String.format(NFC_STATE_TEMPLATE, this.mCurrentNfcAdapterState));
    }

    public void toggleNfc(View view) {
        try {
            if (mNfcAdapter.isEnabled()) {
                mNfcAdapter.disable();
            } else {
                mNfcAdapter.enable();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

}