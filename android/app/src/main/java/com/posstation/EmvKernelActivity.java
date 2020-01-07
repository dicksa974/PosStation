package com.posstation;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.posstation.utils.FileUtils;
import com.zcs.sdk.DriverManager;
import com.zcs.sdk.SdkResult;
import com.zcs.sdk.bluetooth.BluetoothManager;
import com.zcs.sdk.bluetooth.emv.BluetoothHandler;
import com.zcs.sdk.bluetooth.emv.CardDetectedEnum;
import com.zcs.sdk.bluetooth.emv.EmvStatusEnum;
import com.zcs.sdk.bluetooth.emv.NFCCardType;
import com.zcs.sdk.bluetooth.emv.OnBluetoothEmvListener;
import com.zcs.sdk.card.CardInfoEntity;
import com.zcs.sdk.card.CardReaderManager;
import com.zcs.sdk.card.CardReaderTypeEnum;
import com.zcs.sdk.card.CardSlotNoEnum;
import com.zcs.sdk.card.ICCard;
import com.zcs.sdk.card.RfCard;
import com.zcs.sdk.emv.EmvData;
import com.zcs.sdk.emv.EmvHandler;
import com.zcs.sdk.emv.EmvResult;
import com.zcs.sdk.emv.EmvTermParam;
import com.zcs.sdk.emv.EmvTransParam;
import com.zcs.sdk.emv.OnEmvListener;
import com.zcs.sdk.listener.OnSearchCardListener;
import com.zcs.sdk.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Run emv in android
 */
public class EmvKernelActivity extends AppCompatActivity {
    private static final String TAG = "EmvKernelActivity";
    private DriverManager mDriverManager = MyApp.sDriverManager;
    private BluetoothManager mBluetoothManager;
    private static BluetoothHandler mBluetoothHandler;
    private EmvHandler emvHandler;

    protected TextView mTvLog;
    protected ScrollView mScrollView;
    protected ListView mList;

    private ArrayAdapter<String> mAdapter;
    private String[] listItems;
    private StringBuffer sbLog = new StringBuffer();
    private static DateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
    private CardReaderManager mCardReadManager;

    private int iRet;
    CountDownLatch mLatch;
    private int inputPINResult = 0x00;
    CardReaderTypeEnum realCardType;
    String pinBlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_emv);
        initView();
        initSdk();
    }

    private void initView() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.pref_emv);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mTvLog = (TextView) findViewById(R.id.tv_log);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mList = (ListView) findViewById(R.id.list);

        listItems = new String[4];
        listItems[0] = getString(R.string.pref_magnetic);
        listItems[1] = getString(R.string.pref_ic);
        listItems[2] = getString(R.string.pref_rf);
        listItems[3] = getString(R.string.pref_read_all);
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mTvLog.setText("");
                CardReaderTypeEnum cardType = CardReaderTypeEnum.IC_CARD;
                switch (position) {
                    case 0:
                        cardType = CardReaderTypeEnum.MAG_CARD;
                        break;
                    case 1:
                        cardType = CardReaderTypeEnum.IC_CARD;
                        break;
                    case 2:
                        cardType = CardReaderTypeEnum.RF_CARD;
                        break;
                    case 3:
                        cardType = CardReaderTypeEnum.MAG_IC_RF_CARD;
                        break;
                }
                searchCard(cardType);
            }
        });
    }

    private void initSdk() {
        // Config the SDK base info
        mCardReadManager = mDriverManager.getCardReadManager();
        mBluetoothManager = BluetoothManager.getInstance();
        emvHandler = EmvHandler.getInstance();

        mBluetoothHandler = mDriverManager.getBluetoothHandler();
        mBluetoothHandler.addEmvListener(new OnBluetoothEmvListener() {

            @Override
            public void onKeyEnter() {
                String track2[] = new String[1];
                String pan[] = new String[1];
                iRet = emvHandler.getTrack2AndPAN(track2, pan);
                if (iRet == SdkResult.SDK_OK) {
                    pinBlock = mBluetoothHandler.getPinBlock((byte) 0, 0, pan[0]);
                    Log.d("Debug", "pinBlock=" + pinBlock);
                    if (pinBlock == null) {
                        inputPINResult = EmvResult.EMV_NO_PINPAD_OR_ERR;
                    } else {
                        inputPINResult = EmvResult.EMV_OK;
                    }
                } else {
                    inputPINResult = EmvResult.EMV_NO_PINPAD_OR_ERR;
                }
                mBluetoothHandler.closeInputPin();
                mLatch.countDown();
            }

            @Override
            public void onKeyCancel() {
                inputPINResult = EmvResult.EMV_USER_CANCEL;
                mLatch.countDown();
            }

            @Override
            public void onCardDetect(CardDetectedEnum cardDetectedEnum) {

            }

            @Override
            public void onEmvTimeout() {

            }

            @Override
            public void onEnterPasswordTimeout() {
                inputPINResult = EmvResult.EMV_TIME_OUT;
                mLatch.countDown();
            }

            @Override
            public void onEmvStatus(EmvStatusEnum emvStatusEnum) {

            }
        });
    }

    private void getMagData() {
        showLog("Mag card swipe");
        CardInfoEntity magReadData = mCardReadManager.getMAGCard().getMagReadData();
        MyApp.cardInfoEntity = magReadData;
        if (magReadData.getResultcode() == SdkResult.SDK_OK) {
            String tk1 = magReadData.getTk1();
            String tk2 = magReadData.getTk2();
            String tk3 = magReadData.getTk3();
            String expiredDate = magReadData.getExpiredDate();
            String cardNo = magReadData.getCardNo();
            showLog("tk1:  " + tk1);
            showLog("tk2:  " + tk2);
            showLog("tk3:  " + tk3);
            showLog("expiredDate:  " + expiredDate);
            showLog("cardNo:  " + cardNo);
        } else {
            showLog("Mag card read error:  " + magReadData.getResultcode());
        }

    }

    private void getEmvData() {
        String filed = mBluetoothHandler.get55Field();
        String track = mBluetoothHandler.getTrack();
        String cardNo = mBluetoothHandler.getCardNo();
        String cardHolder = mBluetoothHandler.getCardHolder();
        String expDate = mBluetoothHandler.getExpDate(track);
        String icSeq = mBluetoothHandler.getIcSeq();
        NFCCardType cardType = mBluetoothHandler.getNFCCardType();
        String encryptTrack = mBluetoothHandler.getEncrypTrackData(0);

        showLog("cardNo:  " + cardNo);
        showLog("field55:  " + filed);
        showLog("track:  " + track);
        showLog("encryptTrack:  " + encryptTrack);
        showLog("cardHolder:  " + cardHolder);
        showLog("expDate:  " + expDate);
        showLog("icSeq:  " + icSeq);
        showLog("cardType:  " + cardType.name());

    }

    private void searchCard(final CardReaderTypeEnum cardType) {
        OnSearchCardListener listener = new OnSearchCardListener() {
            @Override
            public void onError(int resultCode) {
                mCardReadManager.closeCard();
            }

            @Override
            public void onCardInfo(CardInfoEntity cardInfoEntity) {
                showLog("Deleted card " + cardType.name());
                realCardType = cardInfoEntity.getCardExistslot();
                switch (realCardType) {
                    case RF_CARD:
                        RfCard rfCard = mCardReadManager.getRFCard();
                        byte resetData[] = new byte[EmvData.BUF_LEN];
                        int datalength[] = new int[1];
                        iRet = rfCard.rfReset(resetData, datalength);
                        if (iRet != 0) {
                            Log.d("Debug", "rf reset error!");
                        }
                        break;
                    case MAG_CARD:
                        Log.d("Debug", "MAG_CARD");
                        getMagData();
                        break;
                    case IC_CARD:
                        Log.d("Debug", "ICC_CARD");
                        ICCard iccCard = mCardReadManager.getICCard();
                        iRet = iccCard.icCardReset(CardSlotNoEnum.SDK_ICC_USERCARD);
                        if (iRet != 0) {
                            Log.d("Debug", "ic reset error!");
                            return;
                        }
                        break;
                    default:
                        break;
                }
                if (iRet == 0) {
                    emv(realCardType);
                }
            }

            @Override
            public void onNoCard(CardReaderTypeEnum arg0, boolean arg1) {
            }
        };
        mCardReadManager.searchCard(cardType, 0, listener);

        showLog("Search " + cardType.name() + "....");
    }

    private void emv(CardReaderTypeEnum cardType) {
        // 1. copy aid and capk to '/sdcard/emv/' as the default aid and capk
        try {
            if (!new File(EmvTermParam.emvParamFilePath).exists()) {
                FileUtils.doCopy(EmvKernelActivity.this, "emv", EmvTermParam.emvParamFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 2. set params
        final EmvTransParam emvTransParam = new EmvTransParam();
        if (cardType == CardReaderTypeEnum.IC_CARD) {
            emvTransParam.setTransKernalType(EmvData.KERNAL_EMV_PBOC);
        } else if (cardType == CardReaderTypeEnum.RF_CARD) {
            emvTransParam.setTransKernalType(EmvData.KERNAL_CONTACTLESS_ENTRY_POINT);
        }

        final EmvTermParam emvTermParam = new EmvTermParam();

        // 3. add aid or capk
        //emvHandler.addApp();
        //emvHandler.addCapk()
        emvHandler.kernelInit(emvTermParam);

        // 4. transaction
        byte[] pucIsEcTrans = new byte[1];
        byte[] pucBalance = new byte[6];
        byte[] pucTransResult = new byte[1];

        OnEmvListener onEmvListener = new OnEmvListener() {
            @Override
            public int onSelApp(String[] appLabelList) {
                Log.d("Debug", "onSelApp");
                return iRet;
            }

            @Override
            public int onConfirmCardNo(String cardNo) {
                Log.d("Debug", "onConfirmCardNo");
                String[] track2 = new String[1];
                final String[] pan = new String[1];
                emvHandler.getTrack2AndPAN(track2, pan);
                int index = 0;
                if (track2[0].contains("D")) {
                    index = track2[0].indexOf("D") + 1;
                } else if (track2[0].contains("=")) {
                    index = track2[0].indexOf("=") + 1;
                }
                final String exp = track2[0].substring(index, index + 4);
                showLog("cardmun:" + pan[0]);
                showLog("exp:" + exp);
                return 0;
            }

            @Override
            public int onInputPIN(byte pinType) {
                if (emvTransParam.getTransKernalType() == EmvData.KERNAL_CONTACTLESS_ENTRY_POINT) {
                    String[] track2 = new String[1];
                    final String[] pan = new String[1];
                    emvHandler.getTrack2AndPAN(track2, pan);
                    int index = 0;
                    if (track2[0].contains("D")) {
                        index = track2[0].indexOf("D") + 1;
                    } else if (track2[0].contains("=")) {
                        index = track2[0].indexOf("=") + 1;
                    }
                    final String exp = track2[0].substring(index, index + 4);
                    showLog("cardmun:" + pan[0]);
                    showLog("exp:" + exp);
                }
                showLog("Input pin ");
                Log.d("Debug", "onInputPIN");
                int iRet = 0;
                iRet = inputPIN(pinType);
                Log.d("Debug", "iRet=" + iRet);
                if (iRet == EmvResult.EMV_OK) {
                    emvHandler.setPinBlock(StringUtils.convertHexToBytes(pinBlock));
                }
                return iRet;
            }

            @Override
            public int onCertVerify(int certType, String certNo) {
                Log.d("Debug", "onCertVerify");
                return 0;
            }

            @Override
            public int onlineProc() {
                Log.d("Debug", "onOnlineProc");
                return 0;
            }

            @Override
            public byte[] onExchangeApdu(byte[] send) {
                Log.d("Debug", "onExchangeApdu");
                if (realCardType == CardReaderTypeEnum.IC_CARD) {
                    String recv = mBluetoothManager.icExchangeAPDU(StringUtils.convertBytesToHex(send));
                    if (recv != null)
                        return StringUtils.convertHexToBytes(recv);
                } else {
                    String recv = mBluetoothManager.rfExchangeAPDU(StringUtils.convertBytesToHex(send));
                    if (recv != null)
                        return StringUtils.convertHexToBytes(recv);
                }
                return null;
            }
        };
        showLog("Emv Trans start...");
        // for the emv result, plz refer to emv doc.
        int ret = emvHandler.emvTrans(emvTransParam, onEmvListener, pucIsEcTrans, pucBalance, pucTransResult);
        showLog("Emv trans end, ret = " + ret);
        String str = "Decline";
        if (pucTransResult[0] == EmvData.APPROVE_M) {
            str =  "Approve";
        } else if (pucTransResult[0] == EmvData.ONLINE_M) {
            str = "Online";
        } else if (pucTransResult[0] == EmvData.DECLINE_M) {
            str = "Decline";
        }
        showLog("Emv trans result = " + pucTransResult[0] + ", " + str);

    }


    public int inputPIN(byte pinType) {
        final byte InputPinType = pinType;
        mLatch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("Debug", "InputPinType=" + InputPinType);
                    if (InputPinType == EmvData.ONLINE_ENCIPHERED_PIN) {
                        String track2[] = new String[1];
                        String pan[] = new String[1];
                        iRet = emvHandler.getTrack2AndPAN(track2, pan);
                        mBluetoothHandler.startInputPin((byte) 4, (byte) 12, 0, true);
                    } else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        try {
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return inputPINResult;
    }

    private void setLcdMain() {
        int ret = mBluetoothHandler.LCDMainScreen();
        showLog("Set mpos Lcd main:  " + ret);
    }

    private void setLcdAmount() {
        int ret = mBluetoothHandler.LCDAmount(50 * 100);
        showLog("Set mpos Lcd amount ￥50:  " + ret);
    }

    private void showLcdQR() {
        int ret = mBluetoothHandler.LCDQRCodeShow(100, "www.google.com");
        showLog("Set mpos Lcd qr:  " + ret);
    }


    public void showLog(final String log) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, log);
                Date date = new Date();
                sbLog.append(dateFormat.format(date)).append(":");
                sbLog.append(log);
                String text = mTvLog.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    String[] str = text.split("\r\n");
                    for (int i = 0; i < str.length; i++) {
                        sbLog.append("\r\n");
                        sbLog.append(str[i]);
                    }
                }
                mTvLog.setText(sbLog.toString());
                sbLog.setLength(0);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bt_set, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.menu_item_home:
                setLcdMain();
                break;
            case R.id.menu_item_amount:
                setLcdAmount();
                break;
            case R.id.menu_item_qr:
                showLcdQR();
                break;
            case R.id.menu_item_log:
                mTvLog.setText("");
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
