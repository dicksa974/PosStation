package com.posstation;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.View;

import com.posstation.utils.DialogUtils;
import com.posstation.utils.SDK_Result;
import com.zcs.sdk.DriverManager;
import com.zcs.sdk.SdkResult;
import com.zcs.sdk.card.CardInfoEntity;
import com.zcs.sdk.card.CardReaderManager;
import com.zcs.sdk.card.CardReaderTypeEnum;
import com.zcs.sdk.card.CardSlotNoEnum;
import com.zcs.sdk.card.ICCard;
import com.zcs.sdk.card.MagCard;
import com.zcs.sdk.card.RfCard;
import com.zcs.sdk.listener.OnSearchCardListener;
import com.zcs.sdk.util.LogUtils;
import com.zcs.sdk.util.StringUtils;

import java.lang.ref.WeakReference;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;


/**
 * Created by yyzz on 2018/5/24.
 */

public class CardFragment extends PreferenceFragment {
    private static final String TAG = "CardFragment";
    private static DriverManager mDriverManager = MainApplication.sDriverManager;
    private CardHandler mHandler;
    private static CardReaderManager mCardReadManager;
    private ProgressDialog mProgressDialog;

    public static final int READ_TIMEOUT = 60 * 1000;
    public static final int MSG_RESEARCH = 2000;
    public static final int MSG_CARD_OK = 2001;
    public static final int MSG_CARD_ERROR = 2002;
    public static final int MSG_CARD_APDU = 2003;
    public static final int MSG_CARD_M1 = 2004;
    public static final int MSG_CARD_MF_PLUS = 2005;

    public static final byte[] APDU_SEND_IC = {0x00, (byte) 0xA4, 0x04, 0x00, 0x0E, 0x31, 0x50, 0x41, 0x59, 0x2E, 0x53, 0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31, 0X00};
    public static final byte[] APDU_SEND_RF = {0x00, (byte) 0xA4, 0x04, 0x00, 0x0E, 0x32, 0x50, 0x41, 0x59, 0x2E, 0x53, 0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31, 0x00};
    public static final byte[] APDU_SEND_RANDOM = {0x00, (byte) 0x84, 0x00, 0x00, 0x08};
    private static final String KEY_APDU = "APDU";
    private byte[] mReceivedData = new byte[300];
    private int[] mReceivedDataLength = new int[1];
    private static final byte SLOT_USERCARD = 0x00;
    private static final byte SLOT_PSAM1 = 0x01;
    private static final byte SLOT_PSAM2 = 0x02;
    private CardReaderTypeEnum mCardType;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_card);
        mHandler = new CardHandler(this);
        mCardReadManager = mDriverManager.getCardReadManager();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // read mag card
        findPreference(getString(R.string.key_magnetic)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                searchCard(CardReaderTypeEnum.MAG_CARD, READ_TIMEOUT);
                /*mDriverManager.getSingleExecuteThreadExecutor()
                        .execute(new Runnable() {
                            @Override
                            public void run() {
                                int count = 100;
                                while (count > 0) {
                                    int icCardReset = mCardReadManager.getICCCard().icCardPowerDown(CardSlotNoEnum.SDK_ICC_USERCARD);
                                    if (icCardReset == -1004) {
                                        Log.e("Count", count + "");
                                    }
                                    count--;
                                }
                            }
                        });*/
                return true;
            }
        });

        // ic card
        findPreference(getString(R.string.key_ic)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                searchCard(CardReaderTypeEnum.IC_CARD, READ_TIMEOUT);
                return true;
            }
        });

        findPreference(getString(R.string.key_rf)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                searchCard(CardReaderTypeEnum.RF_CARD, READ_TIMEOUT);
                return true;
            }
        });
        findPreference(getString(R.string.key_pboc)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), PbocActivity.class));
                return true;
            }
        });

        findPreference(getString(R.string.key_emv)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), EmvActivity.class));
                return true;
            }
        });
        findPreference(getString(R.string.key_emv_test)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), EmvLoopTestActivity.class));
                return true;
            }
        });
    }

    private void searchCard(final CardReaderTypeEnum cardType, final int timeout) {
        mCardType = cardType;
        int msgStr = R.string.msg_bank_card;
        switch (cardType) {
            case MAG_CARD:
                msgStr = R.string.msg_mag_card;
                break;
            case RF_CARD:
                msgStr = R.string.msg_rf_card;
                break;
            case IC_CARD:
                msgStr = R.string.msg_ic_card;
                break;
        }
        showSearchCardDialog(R.string.title_waiting, msgStr);

        mCardReadManager.searchCard(cardType, timeout, mListener);
    }

    OnSearchCardListener mListener = new OnSearchCardListener() {
        @Override
        public void onCardInfo(CardInfoEntity cardInfoEntity) {
            Message msg = Message.obtain();
            CardReaderTypeEnum cardType = cardInfoEntity.getCardExistslot();
            switch (cardType) {
                case MAG_CARD:
                    readMagCard(msg);
                    break;
                case RF_CARD:
                    readRfCard(msg);
                    break;
                case IC_CARD:
                    readICCard(msg, CardSlotNoEnum.SDK_ICC_USERCARD);
                    break;
            }
            //try {
            //    Thread.sleep(250);
            //} catch (InterruptedException e) {
            //    e.printStackTrace();
            //}
            //mHandler.sendEmptyMessage(MSG_RESEARCH);
        }

        @Override
        public void onError(int i) {
            Log.e(TAG, "search card onError: " + i);
            mHandler.sendEmptyMessage(i);
        }

        @Override
        public void onNoCard(CardReaderTypeEnum cardReaderTypeEnum, boolean b) {

        }
    };

    private void readICCard(Message msg, CardSlotNoEnum slotNo) {
        ICCard iccCard = mCardReadManager.getICCard();
        int icCardReset = iccCard.icCardReset(slotNo);
        Log.e(TAG, "icCardReset: " + icCardReset);
        if (icCardReset == SdkResult.SDK_OK) {
            int icRes;
            byte[] apdu;
            if (slotNo.getType() == SLOT_PSAM1 || slotNo.getType() == SLOT_PSAM2) {
                // test random
                icRes = iccCard.icExchangeAPDU(slotNo, APDU_SEND_RANDOM, mReceivedData, mReceivedDataLength);
                apdu = APDU_SEND_RANDOM;
            } else {
                icRes = iccCard.icExchangeAPDU(slotNo, APDU_SEND_IC, mReceivedData, mReceivedDataLength);
                apdu = APDU_SEND_IC;
            }
            Log.e(TAG, "icRes: " + icRes);
            if (icRes == SdkResult.SDK_OK) {
                msg.what = MSG_CARD_APDU;
                msg.arg1 = icRes;
                Log.e(TAG, "iccCard res: " + StringUtils.convertBytesToHex(mReceivedData));
                msg.obj = StringUtils.convertBytesToHex(mReceivedData).substring(0, mReceivedDataLength[0] * 2);
                Bundle icBundle = new Bundle();
                icBundle.putByteArray(KEY_APDU, apdu);
                msg.setData(icBundle);
                mHandler.sendMessage(msg);

            } else {
                mHandler.sendEmptyMessage(icRes);
            }
        } else {
            mHandler.sendEmptyMessage(icCardReset);
        }
        int icCardPowerDown = iccCard.icCardPowerDown(CardSlotNoEnum.SDK_ICC_USERCARD);
    }

    private void showSearchCardDialog(@StringRes int title, @StringRes int msg) {
        mProgressDialog = (ProgressDialog) DialogUtils.showProgress(getActivity(), getString(title), getString(msg), new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mCardReadManager.cancelSearchCard();
            }
        });
    }


    private void readMagCard(Message msg) {
        CardInfoEntity cardInfo;
        MagCard magCard = mCardReadManager.getMAGCard();
        cardInfo = magCard.getMagReadData();
        MyApp.cardInfoEntity = cardInfo;
        if (cardInfo.getResultcode() == SdkResult.SDK_OK) {
            msg.what = MSG_CARD_OK;
            msg.arg1 = cardInfo.getResultcode();
            msg.obj = cardInfo;
            mHandler.sendMessage(msg);
        } else {
            mHandler.sendEmptyMessage(cardInfo.getResultcode());
        }
        magCard.magCardClose();

    }

    private void readRfCard(Message msg) {
        final RfCard rfCard = mCardReadManager.getRFCard();
        int resetStatus = rfCard.rfReset(new byte[300], new int[10]);
        int rfRes = rfCard.rfExchangeAPDU(APDU_SEND_RF, mReceivedData, mReceivedDataLength);
        int powerDownRes = rfCard.rfCardPowerDown();
        if (rfRes != SdkResult.SDK_OK) {
            mHandler.sendEmptyMessage(rfRes);
        } else {
            Log.e(TAG, "rfAPUDRes: " + rfRes);
            Log.e(TAG, "mReceivedData: " + StringUtils.convertBytesToHex(mReceivedData));
            msg.what = MSG_CARD_APDU;
            msg.arg1 = rfRes;
            msg.obj = StringUtils.convertBytesToHex(mReceivedData).substring(0, mReceivedDataLength[0] * 2);
            Bundle rfBundle = new Bundle();
            rfBundle.putByteArray(KEY_APDU, APDU_SEND_RF);
            msg.setData(rfBundle);
            // led off 再发消息
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void onDestroy() {
        LogUtils.error("关闭activity");
        if (mCardReadManager != null) {
            mCardReadManager.cancelSearchCard();
        }
        super.onDestroy();
    }

    public void closeSearch() {
        LogUtils.error("closeSearch");
        mCardReadManager.cancelSearchCard();
    }

    private Dialog mCardInfoDialog;

    @SuppressLint("HandlerLeak")
    private class CardHandler extends Handler implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
        WeakReference<Fragment> mFragment;
        int code = 9999;


        CardHandler(Fragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            CardFragment fragment = (CardFragment) mFragment.get();
            if (fragment == null || !fragment.isAdded())
                return;
            if (mCardInfoDialog != null) {
                mCardInfoDialog.dismiss();
            }
            if (fragment.mProgressDialog != null) {
                fragment.mProgressDialog.dismiss();
            }
            switch (msg.what) {
                case MSG_CARD_OK:
                    CardInfoEntity cardInfoEntity = (CardInfoEntity) msg.obj;
                    MyApp.cardInfoEntity = cardInfoEntity;
                    mCardInfoDialog = DialogUtils.show(fragment.getActivity(),
                            fragment.getString(R.string.title_card), SDK_Result.obtainCardInfo(fragment.getActivity(), cardInfoEntity),
                            "OK", this, this);
                    break;
                case MSG_CARD_APDU:
                    mCardInfoDialog = DialogUtils.show(fragment.getActivity(),
                            fragment.getString(R.string.title_apdu),
                            SDK_Result.appendMsg("Code", msg.arg1 + "", "APDU send", StringUtils.convertBytesToHex(msg.getData().getByteArray(CardFragment.KEY_APDU)), "APDU response", (String) msg.obj),
                            "OK", this, this);
                    break;
                case MSG_CARD_M1:
                    mCardInfoDialog = DialogUtils.show(fragment.getActivity(),
                            fragment.getString(R.string.title_card), (String) msg.obj,
                            "OK", this, this);
                    break;
                case MSG_CARD_MF_PLUS:
                    mCardInfoDialog = DialogUtils.show(fragment.getActivity(),
                            fragment.getString(R.string.title_card), (String) msg.obj,
                            "OK", this, this);
                    break;
                case MSG_RESEARCH:
                    searchCard(mCardType, READ_TIMEOUT);
                    break;
                default:
                    mCardInfoDialog = DialogUtils.show(fragment.getActivity(),
                            fragment.getString(R.string.title_error),
                            SDK_Result.obtainMsg(fragment.getActivity(), msg.what),
                            "OK", this, this);
                    break;
            }
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            CardFragment fragment = (CardFragment) mFragment.get();
            if (fragment != null && fragment.isAdded()) {
                closeSearch();
            }
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            CardFragment fragment = (CardFragment) mFragment.get();
            if (fragment != null && fragment.isAdded()) {
                closeSearch();
            }
        }
    }
}
