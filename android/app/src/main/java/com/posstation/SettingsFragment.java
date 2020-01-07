package com.posstation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.posstation.update.Contants;
import com.posstation.update.HttpUtil;
import com.posstation.update.UpdateUtils;
import com.posstation.utils.MacUtil;
import com.posstation.utils.PermissionsManager;
import com.posstation.utils.SDK_Result;
import com.posstation.utils.SPUtils;
import com.zcs.sdk.Beeper;
import com.zcs.sdk.ConnectTypeEnum;
import com.zcs.sdk.DriverManager;
import com.zcs.sdk.Led;
import com.zcs.sdk.LedLightModeEnum;
import com.zcs.sdk.SdkResult;
import com.zcs.sdk.Sys;
import com.zcs.sdk.bluetooth.BluetoothListener;
import com.zcs.sdk.bluetooth.BluetoothManager;
import com.zcs.sdk.pin.pinpad.PinPadManager;
import com.zcs.sdk.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by yyzz on 2018/5/16.
 */

public class SettingsFragment extends PreferenceFragment {
    private static final String TAG = "SettingsFragment";
    private static final String[] PERMISSIONS = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private DriverManager mDriverManager = MainApplication.sDriverManager;
    private BluetoothManager mBluetoothManager;
    private Sys mSys;
    private Beeper mBeeper;
    private Led mLed;
    private PermissionsManager mPermissionsManager;

    private Activity mActivity;
    private ListView mListView;
    private Dialog mDialog;
    private LeDeviceListAdapter mAdapter;
    private EditText mEditName;
    private EditText mEditPwd;

    private String name;
    private String pwd;
    private boolean isConnect = false;
    private boolean isKeepPwd = false;

    public static final int BEEP_FREQUENCE = 4000;
    public static final int BEEP_TIME = 600;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 999:
                    update((String) msg.obj);
                    break;
                case 1001:
                    mAdapter.clear();
                    mDialog.dismiss();
                    break;
                case 1002:
                    Toast.makeText(getActivity(), "Connect failed", Toast.LENGTH_SHORT).show();
                    break;
                case 1003:
                    Toast.makeText(getActivity(), (CharSequence) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case 2001:
                    if (isKeepPwd) {
                        Log.e(TAG, "keep pwd: ");
                        SPUtils.getInstance().put("name", SettingsFragment.this.name);
                        SPUtils.getInstance().put("pwd", pwd);
                    }
                    unlockDevice();
                    break;
                case 2002:
                    Toast.makeText(mActivity, msg.obj == null ? "服务器异常" : (CharSequence) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case 2003:
                    Toast.makeText(mActivity, (CharSequence) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
        mActivity = getActivity();
        initView();
        initSdk();
        checkPermission();
    }

    String title = "测试";

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(getString(R.string.key_os)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!mBluetoothManager.isConnected()) {
                    Toast.makeText(mActivity, "Bluetooth is not connected", Toast.LENGTH_SHORT).show();
                    return true;
                }
                title = getString(R.string.pref_os);
                switchFragment(SettingsFragment.this, new InfoFragment());
                return true;
            }
        });
        findPreference(getString(R.string.key_card)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!mBluetoothManager.isConnected()) {
                    Toast.makeText(mActivity, "Bluetooth is not connected", Toast.LENGTH_SHORT).show();
                    return true;
                }
                title = getString(R.string.pref_card);
                switchFragment(SettingsFragment.this, new CardFragment());
                //startActivity(new Intent(getActivity(), PbocActivity.class));
                return true;
            }
        });

        findPreference(getString(R.string.key_pinPad)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!mBluetoothManager.isConnected()) {
                    Toast.makeText(mActivity, "Bluetooth is not connected", Toast.LENGTH_SHORT).show();
                    return true;
                }
                title = getString(R.string.pref_pinPad);
                switchFragment(SettingsFragment.this, new PinpadFragment());
                return true;
            }
        });

        //findPreference(getString(R.string.key_update_app)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        //    @Override
        //    public boolean onPreferenceClick(Preference preference) {
        //        checkUpdate();
        //        return true;
        //    }
        //});

        // set led light
        findPreference(getString(R.string.key_led)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, final Object newValue) {
                if (!mBluetoothManager.isConnected()) {
                    Toast.makeText(mActivity, "Bluetooth is not connected", Toast.LENGTH_SHORT).show();
                    return true;
                }
                setLed((ListPreference) preference, (String) newValue);
                return true;
            }
        });

        findPreference(getString(R.string.key_bt)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (mBluetoothManager.isConnected()) {
                    mBluetoothManager.close();
                    Toast.makeText(mActivity, "Disconnect", Toast.LENGTH_SHORT).show();
                }
                discovery();
                return true;
            }
        });

        findPreference(getString(R.string.key_unlock)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showLoginDialog();
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        mBluetoothManager.close();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        super.onDestroy();
    }

    private void setLed(ListPreference preference, String newValue) {
        if (mLed == null)
            mLed = mDriverManager.getLedDriver();
        final int index = preference.findIndexOfValue(newValue);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mLed.setLed(LedLightModeEnum.ALL, false);
                if (index == 0) {
                    mLed.setLed(LedLightModeEnum.RED, true);
                } else if (index == 1) {
                    mLed.setLed(LedLightModeEnum.GREEN, true);
                } else if (index == 2) {
                    mLed.setLed(LedLightModeEnum.YELLOW, true);
                } else if (index == 3) {
                    mLed.setLed(LedLightModeEnum.BLUE, true);
                } else if (index == 4) {
                    mLed.setLed(LedLightModeEnum.ALL, true);
                }

            }
        }).start();
    }

    /**
     * 登陆验证
     */
    private void showLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("请输入用户名和密码");
        View view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_login, null);
        builder.setView(view);
        final EditText etName = (EditText) view.findViewById(R.id.username);
        final EditText etPwd = (EditText) view.findViewById(R.id.password);
        final CheckBox checkBox = view.findViewById(R.id.check_keep_pwd);
        String nameSp = SPUtils.getInstance().getString("name");
        String pwdSp = SPUtils.getInstance().getString("pwd");
        if (!TextUtils.isEmpty(nameSp)) {
            etName.setText(nameSp);
        }
        if (!TextUtils.isEmpty(pwdSp)) {
            etPwd.setText(pwdSp);
        }
        isKeepPwd = false;
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SettingsFragment.this.name = etName.getText().toString().trim();
                pwd = etPwd.getText().toString().trim();
                if (checkBox.isChecked()) {
                    isKeepPwd = true;
                }
                loginRequest(SettingsFragment.this.name, pwd);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * 登录认证
     *
     * @param name
     * @param pwd
     */
    private void loginRequest(final String name, final String pwd) {
        new Thread(new Runnable() {
            public void run() {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("userId", name);
                    jsonObject.put("userPwd", md5(pwd));
                    String mac = MacUtil.getMac(getActivity()).replace(":", "-");
                    jsonObject.put("sysTerNo", mac);
                    jsonObject.put("loginType", 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final String json = jsonObject.toString();
                String response = HttpUtil.postHttpResponseText(Contants.BASE_URL + "tms/dwKey/userLogin.json", json);
                Log.e(TAG, "userLogin res: " + response);
                if (!TextUtils.isEmpty(response)) {
                    try {
                        JSONObject jsonRes = new JSONObject(response);
                        if ("000000".equals(jsonRes.get("rspcod"))) {
                            mHandler.sendEmptyMessage(2001);
                        } else {
                            String rspmsg = jsonRes.getString("rspmsg");
                            Message message = Message.obtain();
                            message.what = 2002;
                            message.obj = rspmsg;
                            mHandler.sendMessage(message);
                        }
                    } catch (JSONException e) {
                        Message message = Message.obtain();
                        message.what = 2002;
                        message.obj = null;
                        mHandler.sendMessage(message);
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 解锁设备
     */
    private void unlockDevice() {
        new Thread(new Runnable() {
            public void run() {
                String msg = null;
                do {
                    byte[] encData = new byte[256];
                    int requestUnlock = mSys.requestUnlock(encData);
                    Log.e(TAG, "requestUnlock: " + requestUnlock);
                    if (requestUnlock != SdkResult.SDK_OK) {
                        msg = "requestUnlock: " + requestUnlock;
                        break;
                    }
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("agentId", "8160300154");
                        jsonObject.put("deviceType", "Z70");
                        String encDataHex = StringUtils.convertBytesToHex(encData);
                        Log.e(TAG, "requestUnlock encDataHex: " + encDataHex);
                        jsonObject.put("checkValue", encDataHex);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    final String json = jsonObject.toString();
                    String response = HttpUtil.postHttpResponseText(Contants.BASE_URL + "tms/dwKey/termUnlock.json", json);
                    Log.e(TAG, "termUnlock res: " + response);
                    if (!TextUtils.isEmpty(response)) {
                        try {
                            JSONObject jsonRes = new JSONObject(response);
                            if ("000000".equals(jsonRes.get("rspcod"))) {
                                String key = jsonRes.getString("hardWareKey");
                                byte[] bytes = StringUtils.convertHexToBytes(key);
                                int unlock = mSys.unlock(bytes.length, bytes);
                                if (unlock == SdkResult.SDK_OK) {
                                    msg = "Unlock success";
                                } else {
                                    msg = "unlock: " + unlock;
                                }
                            } else {
                                msg = jsonRes.getString("rspmsg");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } while (false);
                Message message = Message.obtain();
                message.what = 2003;
                message.obj = msg;
                mHandler.sendMessage(message);
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionsManager.recheckPermissions(requestCode, permissions, grantResults);
    }

    private void initSdk() {
        // Config the SDK base info
        mSys = mDriverManager.getBaseSysDevice();
        mSys.showLog(getPreferenceManager().getSharedPreferences().getBoolean(getString(R.string.key_show_log), true));
        mBluetoothManager = BluetoothManager.getInstance()
                .setContext(mActivity)
                .setBluetoothListener(new BluetoothListener() {
                    @Override
                    public boolean isReader(BluetoothDevice bluetoothDevice) {
                        mAdapter.addDevice(bluetoothDevice);
                        mAdapter.notifyDataSetChanged();
                        return false;
                    }

                    @Override
                    public void startedConnect(BluetoothDevice device) {
                        Log.e(TAG, "startedConnect: ");
                    }

                    @Override
                    public void connected(BluetoothDevice device) {
                        Log.e(TAG, "connected: ");
                        // 连接成功进行初始化操作
                        int sdkInit = mSys.sdkInit(ConnectTypeEnum.BLUETOOTH);
                        String initRes = (sdkInit == SdkResult.SDK_OK) ? getString(R.string.init_success) : SDK_Result.obtainMsg(mActivity, sdkInit);

                        //upKey();

                        // 如果mBluetoothManager.connect 在子线程中调用，这里ui操作需切换回主线程
                        Message msg = Message.obtain();
                        msg.what = 1003;
                        msg.obj = initRes;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void disConnect() {
                        Log.e(TAG, "disConnect: ");
                    }

                    @Override
                    public void startedDiscovery() {
                        Log.e(TAG, "startedDiscovery: ");
                    }

                    @Override
                    public void finishedDiscovery() {
                        Log.e(TAG, "finishedDiscovery: ");
                    }
                })
                .init();
    }

    /**
     * 指定key和sn
     */
    private void upKey() {
        String mainKeyStr = "0E7DECC0C689E776189D88852B5B3AF9";
        String sn = "1018070000003903";
        String pinKeyStr = "E253176BFDABD1C6E485A4656D4DA321C431DDDD";
        String macKeyStr = "34043B8DB14E9FCC0000000000000000DFF2ECA0";
        String tdkStr = "F01514791AA83F39F0C182680F9D9268D46F63CC";
        // 连接成功下指定秘钥
        byte[] mainKey = StringUtils.convertHexToBytes(mainKeyStr);
        byte[] pinKey = StringUtils.convertHexToBytes(pinKeyStr);
        byte[] macKey = StringUtils.convertHexToBytes(macKeyStr);
        byte[] tdk = StringUtils.convertHexToBytes(tdkStr);
        PinPadManager padManager = mDriverManager.getPadManager();

        // 主密钥
        final int upMastKey = padManager.pinPadUpMastKey(0, mainKey, (byte) mainKey.length);
        Log.e(TAG, "upMastKey: " + upMastKey);
        if (upMastKey != SdkResult.SDK_OK) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mActivity, "Up mastKey error: \t" + upMastKey, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 工作秘钥
        final int upWorkKey = padManager.pinPadUpWorkKey(0, pinKey, (byte) pinKey.length, macKey, (byte) macKey.length, tdk, (byte) tdk.length);
        Log.e(TAG, "upWorkKey: " + upWorkKey);
        if (upWorkKey != SdkResult.SDK_OK) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mActivity, "Up WorkKey error: \t" + upWorkKey, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 设置sn
        final int setCustomSn = mSys.setCustomSn(sn);
        Log.e(TAG, "setCustomSn: " + setCustomSn);
        if (setCustomSn != SdkResult.SDK_OK) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mActivity, "Set CustomSn error: \t" + setCustomSn, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void checkPermission() {
        mPermissionsManager.checkPermissions(0, PERMISSIONS);
    }

    /**
     * 扫描蓝牙
     */
    private void discovery() {
        mDialog.show();
        mBluetoothManager.discovery();
    }

    private void initView() {
        // 6.0 动态权限
        mPermissionsManager = new PermissionsManager(SettingsFragment.this.getActivity()) {
            @Override
            public void authorized(int requestCode) {
                discovery();
            }

            @Override
            public void noAuthorization(int requestCode, String[] lacksPermissions) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsFragment.this.getActivity());
                builder.setTitle("提示");
                builder.setMessage("缺少相关权限！");
                builder.setPositiveButton("设置权限", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionsManager.startAppSettings(SettingsFragment.this.getActivity().getApplicationContext());
                    }
                });
                builder.create().show();
            }

            @Override
            public void ignore() {
                discovery();
            }
        };
        mListView = (ListView) View.inflate(mActivity, R.layout.dialog_list, null);
        mAdapter = new LeDeviceListAdapter(mActivity);
        mListView.setAdapter(mAdapter);
        final ExecutorService sigleThreadExecutor = Executors.newSingleThreadExecutor();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice device = mAdapter.getAllDeivces().get(position);
                sigleThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (mBluetoothManager.isDiscovering()) {
                            mBluetoothManager.stopDiscovery();
                        }
                        if (mBluetoothManager.isConnected()) {
                            mBluetoothManager.disconnect();
                        }
                        isConnect = mBluetoothManager.connect(device);
                        int msgCode = isConnect ? 1001 : 1002;
                        mHandler.sendEmptyMessage(msgCode);
                    }
                });
            }
        });
        mDialog = new AlertDialog.Builder(mActivity)
                .setTitle(getString(R.string.title_connect_bt)).setView(mListView)
                .setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mBluetoothManager.stopDiscovery();
                        mAdapter.clear();
                    }
                }).create();
    }

    /**
     * 更新app
     */
    private void checkUpdate() {
        // String appName = UpdateUtils.getAppName(this);
        String appName = "mpos";
        int versionCode = UpdateUtils.getversionCode(getActivity());
        String versionName = UpdateUtils.getversionName(getActivity());
        if (versionName.length() > 5) {
            versionName = versionName.substring(0, 5);
            Log.d("versionName", versionName + "");
        }
        versionName = "V" + versionName;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("appName", appName);
            jsonObject.put("appVersion", versionName);
            jsonObject.put("sysType", "Android");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String json = jsonObject.toString();
        new Thread(new Runnable() {
            public void run() {
                String response = HttpUtil.postHttpResponseText(Contants.BASE_URL + Contants.UPDATE_APP_URL, json);
                if (!TextUtils.isEmpty(response)) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if ("0".equals(jsonObject.get("checkState"))) {
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Message msg = mHandler.obtainMessage();
                    msg.what = 999;
                    msg.obj = response;
                    mHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void update(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            int code = Integer.parseInt(jsonObject.getString("checkState"));
            String fileUrl = jsonObject.getString("fileUrl");
            String fileDesc = jsonObject.getString("fileDesc");
            if (TextUtils.isEmpty(fileUrl)) {
                return;
            }
            fileUrl = Contants.BASE_URL + fileUrl;
            UpdateUtils updateUtils = UpdateUtils.from(getActivity()).checkBy(UpdateUtils.CHECK_BY_NO).updateInfo(fileDesc)
                    .apkPath(fileUrl);
            if (code == 1) {
                updateUtils.isForce(false).update();
            } else if (code == 3) {
                updateUtils.isForce(true).update();
            } else if (code == 2) {
                Log.d(TAG, "已经是最新版本");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void switchFragment(Fragment from, Fragment to) {
        if (!to.isAdded()) {
            getFragmentManager().beginTransaction().addToBackStack(null).hide(from).add(R.id.frame_container, to).commit();
        } else {
            getFragmentManager().beginTransaction().addToBackStack(null).hide(from).show(to).commit();
        }
    }
}
