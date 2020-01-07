package com.posstation;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.posstation.utils.DialogUtils;
import com.posstation.utils.SDK_Result;
import com.zcs.sdk.DriverManager;
import com.zcs.sdk.SdkResult;
import com.zcs.sdk.bluetooth.emv.BluetoothHandler;
import com.zcs.sdk.bluetooth.emv.CardDetectedEnum;
import com.zcs.sdk.bluetooth.emv.EmvStatusEnum;
import com.zcs.sdk.bluetooth.emv.OnBluetoothEmvListener;
import com.zcs.sdk.pin.MagEncryptTypeEnum;
import com.zcs.sdk.pin.PinMacTypeEnum;
import com.zcs.sdk.pin.pinpad.PinPadManager;
import com.zcs.sdk.util.LogUtils;
import com.zcs.sdk.util.StringUtils;

import androidx.annotation.Nullable;

/**
 * Created by yyzz on 2018/5/18.
 */

public class PinpadFragment extends PreferenceFragment {

    private static final String TAG = "InfoFragment";


    private DriverManager mDriverManager = MainApplication.sDriverManager;
    private PinPadManager pinPadManager;
    private BluetoothHandler mBluetoothHandler;


    public static int master_key = 0;
    public static int work_key = 0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pinpad_info);

        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        pinPadManager = mDriverManager.getPadManager();
        mBluetoothHandler = mDriverManager.getBluetoothHandler();
        mBluetoothHandler.addEmvListener(new OnBluetoothEmvListener() {
            @Override
            public void onKeyEnter() {
                Log.e(TAG, "onKeyEnter: ");
                String cardNo = "6229180030458014";
                String pinBlock = mBluetoothHandler.getPinBlock((byte) 0, 60, cardNo);
                Log.e(TAG, "pinBlock: " + pinBlock);
                DialogUtils.show(getActivity(), "CardNo:  " + cardNo + "\n" + "PinBlock data:  " + pinBlock);
                mBluetoothHandler.LCDMainScreen();
            }

            @Override
            public void onKeyCancel() {
                Log.e(TAG, "onKeyCancel: ");
            }

            @Override
            public void onCardDetect(CardDetectedEnum cardDetectedEnum) {

            }

            @Override
            public void onEmvTimeout() {

            }

            @Override
            public void onEnterPasswordTimeout() {

            }

            @Override
            public void onEmvStatus(EmvStatusEnum emvStatusEnum) {

            }
        });
        // 设置主密钥
        findPreference(getString(R.string.key_setmaster_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                master_key = 0;
                setMasterKey();
                return true;
            }
        });
        // 磁道数据加密
        findPreference(getString(R.string.key_track_encrypt)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    if (MyApp.cardInfoEntity.getTk2() != null || MyApp.cardInfoEntity.getTk3() != null) {
                        byte[] track2data = (MyApp.cardInfoEntity.getTk2()).getBytes();
                        byte[] track3data = (MyApp.cardInfoEntity.getTk3()).getBytes();
                        byte[] out2data = new byte[track2data.length];
                        byte[] out3data = new byte[track3data.length];
                        int status2 = pinPadManager.pinPadEncryptTrackData(index_all, MagEncryptTypeEnum.UNION_ENCRYPT, track2data, (byte) track2data.length, out2data);
                        int status3 = pinPadManager.pinPadEncryptTrackData(index_all, MagEncryptTypeEnum.UNION_ENCRYPT, track3data, (byte) track3data.length, out3data);
                        LogUtils.error("磁道二加密状态码:" + status2 + "磁道三加密状态码:" + status3);

                        DialogUtils.show(getActivity(), getString(R.string.show_second_track) + new String(out2data) + " \n " + getString(R.string.show_second_track_cipher) + new String(out3data));
                    } else {
                        DialogUtils.show(getActivity(), getString(R.string.no_mag_card_data));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        // 设置工作密钥
        findPreference(getString(R.string.key_setworkKey)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                work_key = 0;
                setWorkKey();
                return true;
            }
        });
        // 获取pinBlock信息
        findPreference(getString(R.string.key_getpinblock)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int startPinRet = mBluetoothHandler.startInputPin((byte) 6, (byte) 12, 60, true);
                Toast.makeText(getActivity(), "Start input pin" + (startPinRet == SdkResult.SDK_OK ? "success" : "fail"), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        // 计算mac
        findPreference(getString(R.string.key_getmac)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getMac();
                return true;
            }
        });
    }

    //计算mac
    private void getMac() {
        //处理更新工作密钥的操作
        String mac_data = "0210000990000010000000010005240705775867115D2C881003AD13CEB772995240705775867115D1561560000000005275003000000034343420110DDD000000000575809742500000000000782A00044140000000277CC2B21DE08C204c92f4311bd76496c86a438afb4c052e0";
        byte[] mac_data_byte = mac_data.getBytes();

        byte[] outdata = new byte[8];
        int status = pinPadManager.pinPadMac(index_all, PinMacTypeEnum.ECB, mac_data_byte, mac_data_byte.length, outdata);
        if (status == SdkResult.SDK_OK) {
            DialogUtils.show(getActivity(), getString(R.string.calc_mac_encrypt_success) + "\n" + "Data:  " + mac_data + "\n" + "Res:  " + StringUtils.convertBytesToHex(outdata));
        } else {
            DialogUtils.show(getActivity(), getString(R.string.calc_mac_encrypt_failure) + ":" + SDK_Result.obtainMsg(getActivity(), status));
        }
    }

    //更新工作密钥方法
    private void setWorkKey() {
        //处理更新工作密钥的操作
        String pin_key = "BF1CA957FE63B286E2134E08A8F3DDA903E0686F";
        String mac_key = "8670685795c8d2ea0000000000000000d2db51f1";
        String tdk_key = "00A0ABA733F2CBB1E61535EDCFDC34A93AA3EA2D";
        LogUtils.error("pin_key:" + pin_key + "\t" + "mac_key:" + mac_key + "\t" + "tdk_key:" + tdk_key);

        //popwindow 弹出关闭
        byte[] pin_key_byte = StringUtils.convertHexToBytes(pin_key);
        byte[] mac_key_byte = StringUtils.convertHexToBytes(mac_key);
        byte[] tdk_key_byte = StringUtils.convertHexToBytes(tdk_key);

        int status = pinPadManager.pinPadUpWorkKey(index_all, pin_key_byte, (byte) pin_key_byte.length,
                mac_key_byte, (byte) mac_key_byte.length, tdk_key_byte, (byte) tdk_key_byte.length);
        if (status == SdkResult.SDK_OK) {
            DialogUtils.show(getActivity(), getString(R.string.update_work_key_success));
        } else {
            DialogUtils.show(getActivity(), getString(R.string.update_work_key_failure) + ":" + SDK_Result.obtainMsg(getActivity(), status));
        }

    }

    //设置主密钥方法
    private void setMasterKey() {
        showPopupWindow();
    }

    int index_all = 0;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private PopupWindow mPopWindow;

    /**
     * 更新主密钥的popwindows弹出
     */
    private void showPopupWindow() {
        final EditText masterKeyIndex;
        final EditText masterKey1;
        final EditText masterKey2;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View contentView = getActivity().getLayoutInflater().inflate(
                R.layout.pinpad_master_layout, null);
        masterKeyIndex = contentView.findViewById(R.id.masterKeyIndex);
        masterKey1 = contentView.findViewById(R.id.masterKey1);
        masterKey2 = contentView.findViewById(R.id.masterKey2);
        builder.setTitle(getString(R.string.input_master_key_meg));
        builder.setView(contentView);
        builder.setPositiveButton(getString(R.string.set_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String index = masterKeyIndex.getText().toString().trim();
                String key1 = masterKey1.getText().toString().trim();
                String key2 = masterKey2.getText().toString().trim();
                int int_index = 0;
                if ("".equals(index) || "".equals(key1) || "".equals(key2)) {

                } else {
                    index = index.toUpperCase();
                    switch (index) {
                        case "F":
                            int_index = 15;
                            break;
                        case "E":
                            int_index = 14;
                            break;
                        case "D":
                            int_index = 13;
                            break;
                        case "C":
                            int_index = 12;
                            break;
                        case "B":
                            int_index = 11;
                            break;
                        case "A":
                            int_index = 10;
                            break;
                        default:
                            int_index = Integer.valueOf(index);
                            break;
                    }
                    byte[] key_byte = StringUtils.convertHexToBytes(key1 + key2);
                    LogUtils.error("显示的是信息 key:" + key1 + key2);
                    LogUtils.debugHexMsg("显示信息", key_byte);
                    index_all = int_index;
                    int status = pinPadManager.pinPadUpMastKey(int_index, key_byte, (byte) key_byte.length);
                    LogUtils.debug("更新主密钥的返回状态 status:" + status);
                    if (status == SdkResult.SDK_OK) {
                        DialogUtils.show(getActivity(), getString(R.string.update_master_key_success));

                    } else {
                        DialogUtils.show(getActivity(), getString(R.string.update_master_key_failure) + ":" + SDK_Result.obtainMsg(getActivity(), status));
                    }

                }
            }
        }).setNegativeButton(getString(R.string.set_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).create().show();
    }
}
