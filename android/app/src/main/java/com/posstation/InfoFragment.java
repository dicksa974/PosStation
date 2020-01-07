package com.posstation;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.posstation.utils.DialogUtils;
import com.posstation.utils.SDK_Result;
import com.zcs.sdk.ConnectTypeEnum;
import com.zcs.sdk.DriverManager;
import com.zcs.sdk.SdkResult;
import com.zcs.sdk.Sys;
import com.zcs.sdk.bluetooth.BluetoothManager;
import com.zcs.sdk.bluetooth.emv.BluetoothHandler;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 系统功能模块
 * Created by yyzz on 2018/5/18.
 */

public class InfoFragment extends PreferenceFragment {
    private static final String TAG = "InfoFragment";
    private String mSdkVer;
    private String[] mBaseSdkVer;
    private String[] mFirmwareVer;
    private String[] mCustomerSN;
    private String[] mTerminalSN;
    private String[] mMachineModel;
    private Activity mActivity;
    private ProgressDialog mProgressDialog;
    private static Handler mHandler;
    private DriverManager mDriverManager = MainApplication.sDriverManager;
    private Sys mSys;
    private BluetoothManager mBluetoothManager;


    private static final int MSG_SN = 1001;
    private static final int MSG_VER = 1002;
    private static final int MSG_FIRM_VER = 1003;
    private static final int MSG_CUSTOMER_SN = 1004;
    private static final int MSG_BASE_VER = 1005;
    private static final int MSG_MACHINE_MODEL = 1006;
    private BluetoothHandler mBluetoothHandler;


    static class InfoHandler extends Handler {
        WeakReference<InfoFragment> mFragment;

        InfoHandler(InfoFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            InfoFragment infoFragment = mFragment.get();
            if (infoFragment == null || !infoFragment.isAdded())
                return;
            switch (msg.what) {
                case MSG_SN:
                    infoFragment.refreshSummary(infoFragment.getString(R.string.key_pid), infoFragment.mTerminalSN[0]);
                    break;
                case MSG_FIRM_VER:
                    infoFragment.refreshSummary(infoFragment.getString(R.string.key_firmware_ver), infoFragment.mFirmwareVer[0]);
                    break;
                case MSG_CUSTOMER_SN:
                    infoFragment.refreshSummary(infoFragment.getString(R.string.key_sn), infoFragment.mCustomerSN[0]);
                    break;
                case MSG_VER:
                    infoFragment.refreshSummary(infoFragment.getString(R.string.key_sdk_ver), infoFragment.mSdkVer);
                    break;
                case MSG_BASE_VER:
                    infoFragment.refreshSummary(infoFragment.getString(R.string.key_base_sdk_ver), infoFragment.mBaseSdkVer[0]);
                    break;
                //case MSG_MACHINE_MODEL:
                //    infoFragment.refreshSummary(infoFragment.getString(R.string.key_machine_model), infoFragment.mMachineModel[0]);
                //    break;
                default:
                    DialogUtils.show(infoFragment.getActivity(), SDK_Result.obtainMsg(infoFragment.getActivity(), msg.what));
                    break;
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_info);
        mActivity = getActivity();

        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mBaseSdkVer = new String[1];
        mFirmwareVer = new String[1];
        mCustomerSN = new String[1];
        mTerminalSN = new String[1];
        mMachineModel = new String[1];
        mSys = mDriverManager.getBaseSysDevice();
        mBluetoothHandler = mDriverManager.getBluetoothHandler();
        mBluetoothManager = BluetoothManager.getInstance();
        mHandler = new InfoHandler(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // pid
                int resPid = mSys.getPid(mTerminalSN);
                Log.e(TAG, "getPid res: " + resPid);
                if (resPid == SdkResult.SDK_OK) {

                    Log.e(TAG, "getPid: " + mTerminalSN[0]);
                    mHandler.sendEmptyMessage(MSG_SN);
                }

                // base sdk version
                int resBaseVer = mSys.getBaseSdkVer(mBaseSdkVer);
                Log.e(TAG, "Read base ver res:  " + resBaseVer);
                if (resBaseVer == SdkResult.SDK_OK) {

                    Log.e(TAG, "getBaseSdkVer: " + mBaseSdkVer[0]);
                    mHandler.sendEmptyMessage(MSG_BASE_VER);
                }

                // sdk ver
                mSdkVer = mSys.getSdkVersion();
                if (mSdkVer != null)
                    mHandler.sendEmptyMessage(MSG_VER);

                // firmware ver
                int resFireVer = mSys.getFirmwareVer(mFirmwareVer);
                Log.e(TAG, "Read firm ver res: " + resFireVer);
                if (resFireVer == SdkResult.SDK_OK) {

                    Log.e(TAG, "getFirmwareVer: " + mFirmwareVer[0]);
                    mHandler.sendEmptyMessage(MSG_FIRM_VER);
                }

                // sn
                byte[] sn = new byte[12];
                for (int i = 0; i < 12; i++) {
                    sn[i] = (byte) 0x31;
                }
                //   mSys.setCustomSn(sn);
                int resSN = mSys.getCustomSn(mCustomerSN);
                Log.e(TAG, "Read sn res: " + resSN);
                if (resSN == SdkResult.SDK_OK) {
                    Log.e(TAG, "getCustomSn: " + mCustomerSN[0]);
                    mHandler.sendEmptyMessage(MSG_CUSTOMER_SN);
                }
              /*  int resMachine = mSys.getDevName(mMachineModel);
                Log.e(TAG, "Read sn res: " + resSN);
                if (resSN == SdkResult.SDK_OK) {
                    Log.e(TAG, "getCustomSn: " + mMachineModel[0]);
                    mHandler.sendEmptyMessage(MSG_MACHINE_MODEL);
                }*/
            }
        }).start();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(getString(R.string.key_show_log)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mSys.showLog((Boolean) newValue);
                return true;
            }
        });
        // init sdk
        findPreference(getString(R.string.key_init)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.d(TAG, "key_init click: ");
                initSdk();
                return true;
            }
        });

        findPreference(getString(R.string.key_disconnect)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.d(TAG, "key_disconnect click: ");
                if (mBluetoothManager.isConnected()) {
                    mBluetoothManager.close();
                } else {
                    Toast.makeText(mActivity, "Bluetooth is not connected", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        findPreference(getString(R.string.key_update_time)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.d(TAG, "key_update_time click: ");
                int ret = mBluetoothHandler.updateClock();
                Toast.makeText(mActivity, "Update time:  " + (ret == SdkResult.SDK_OK ? "success" : "fail"), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        findPreference(getString(R.string.key_update_firmware)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                updateFirmware();
                //Toast.makeText(mActivity, "暂不支持", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void updateFirmware() {
        final ProgressDialog progressDialog = new ProgressDialog(mActivity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Updating...");
        progressDialog.setMax(100);
        progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.btn_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.dismiss();
            }
        });
        progressDialog.show();
        progressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.GONE);
        //progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.btn_confirm), new DialogInterface.OnClickListener() {
        //    @Override
        //    public void onClick(DialogInterface dialog, int which) {
        //        progressDialog.dismiss();
        //    }
        //});
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream open = mActivity.getAssets().open("Z70_V1.0.9R180818.bin");
                    mSys.updateFirmware(open, new Sys.UpdateListener() {
                        @Override
                        public void onSuccess() {
                            Log.e(TAG, "onSuccess: ");
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.setTitle("Update success");
                                    progressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                                }
                            });
                        }

                        @Override
                        public void onProcessChange(long cur, long max) {
                            Log.e(TAG, "onProcessChange: " + cur + "\t" + max);
                            progressDialog.setProgress((int) ((float) cur / max * 100));
                        }

                        @Override
                        public void onError(int i, String s) {
                            Log.e(TAG, "onError: " + i + "\t" + s);
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.setTitle("Update fail");
                                    progressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initSdk() {
        mProgressDialog = (ProgressDialog) DialogUtils.showProgress(mActivity, getString(R.string.title_waiting), getString(R.string.msg_init));
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int i = mDriverManager.getBaseSysDevice().sdkInit(ConnectTypeEnum.BLUETOOTH);
                if (mActivity != null) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressDialog != null)
                                mProgressDialog.dismiss();
                            String initRes = (i == SdkResult.SDK_OK) ? getString(R.string.init_success) : SDK_Result.obtainMsg(mActivity, i);

                            Toast.makeText(getActivity(), initRes, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void refreshSummary(@NonNull String key, String summary) {
        refreshSummary(findPreference(key), summary);
    }

    private void refreshSummary(Preference preference, String summary) {
        SpannableString spannableString = new SpannableString(summary);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), 0, summary.length(), 0);
        preference.setSummary(spannableString);
    }
}
