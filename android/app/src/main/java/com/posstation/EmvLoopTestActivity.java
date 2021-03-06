package com.posstation;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.posstation.utils.DialogUtils;
import com.zcs.sdk.DriverManager;
import com.zcs.sdk.SdkResult;
import com.zcs.sdk.bluetooth.BluetoothManager;
import com.zcs.sdk.bluetooth.emv.BluetoothHandler;
import com.zcs.sdk.bluetooth.emv.CardDetectedEnum;
import com.zcs.sdk.bluetooth.emv.EmvStatusEnum;
import com.zcs.sdk.bluetooth.emv.OnBluetoothEmvListener;
import com.zcs.sdk.card.CardInfoEntity;
import com.zcs.sdk.card.CardReaderTypeEnum;
import com.zcs.sdk.card.MagCard;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class EmvLoopTestActivity extends AppCompatActivity {
    private static final String TAG = "EmvLoopTestActivity";
    private DriverManager mDriverManager = MyApp.sDriverManager;
    private BluetoothManager mBluetoothManager;
    private static BluetoothHandler mBluetoothHandler;
    private CardReaderTypeEnum mCardType;

    protected TextView mTvLog;
    protected ScrollView mScrollView;
    protected ListView mList;
    private Dialog mEmvDialog;
    //private Dialog mSearchDialog;

    private ArrayAdapter<String> mAdapter;
    private String[] listItems;
    private StringBuffer sbLog = new StringBuffer();
    private static DateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");

    private boolean isRunning = true;
    private int emvCount = 0;
    private int timeoutCount = 0;
    private int errorCount = 0;

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
            actionBar.setTitle(R.string.pref_emv_test);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mTvLog = (TextView) findViewById(R.id.tv_log);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mList = (ListView) findViewById(R.id.list);

        listItems = new String[4];
        listItems[0] = "Emv count: 0";
        listItems[1] = getString(R.string.pref_ic);
        listItems[2] = getString(R.string.pref_rf);
        listItems[3] = getString(R.string.pref_read_all);
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                initCount();
                if (mEmvDialog == null || !mEmvDialog.isShowing()) {
                    mEmvDialog = DialogUtils.showProgress(EmvLoopTestActivity.this, null, "Emving...", new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            isRunning = false;
                        }
                    });
                    mEmvDialog.getWindow().setGravity(Gravity.BOTTOM);
                }
                mTvLog.setText("");
                mCardType = CardReaderTypeEnum.IC_CARD;
                switch (position) {
                    case 0:
                        return;
                    case 1:
                        mCardType = CardReaderTypeEnum.IC_CARD;
                        break;
                    case 2:
                        mCardType = CardReaderTypeEnum.RF_CARD;
                        break;
                    case 3:
                        mCardType = CardReaderTypeEnum.MAG_IC_RF_CARD;
                        break;
                }
                searchCard(mCardType);
            }
        });
        showCount();
    }

    private void initSdk() {
        // Config the SDK base info
        mBluetoothManager = BluetoothManager.getInstance();
        mBluetoothHandler = mDriverManager.getBluetoothHandler();
        mBluetoothHandler.addEmvListener(new OnBluetoothEmvListener() {
            @Override
            public void onKeyEnter() {
                Log.e(TAG, "onKeyEnter: ");
            }

            @Override
            public void onKeyCancel() {
                Log.e(TAG, "onKeyCancel: ");
            }

            @Override
            public void onCardDetect(CardDetectedEnum cardDetectedEnum) {
                Log.e(TAG, "onCardDetect: " + cardDetectedEnum.name());
                showLog("Search " + cardDetectedEnum.name());
                //if (mSearchDialog != null && mSearchDialog.isShowing()) {
                //    mSearchDialog.dismiss();
                //}
                if (!isRunning)
                    return;
                int emvRet = -1;
                switch (cardDetectedEnum) {
                    case INSERTED:
                        showLog("IC card insert");
                        emvRet = mBluetoothHandler.emv(CardReaderTypeEnum.IC_CARD, 100, "20180811121212", 10);
                        showLog("Start emv ret:  " + emvRet);
                        break;
                    case SWIPED:
                        getMagData();
                        break;
                    case CONTACTLESS_FR:
                        showLog("Rf card");
                        emvRet = mBluetoothHandler.emv(CardReaderTypeEnum.RF_CARD, 100, 10);
                        showLog("Start emv ret:  " + emvRet);
                        break;
                    case REMOVED:
                        showLog("IC card remove");
                        break;
                }
            }

            @Override
            public void onEmvTimeout() {
                timeoutCount++;
                Log.e(TAG, "onEmvTimeout: ");
                showLog("onEmvTimeout");
                //if (mSearchDialog != null && mSearchDialog.isShowing()) {
                //    mSearchDialog.dismiss();
                //}
                //if (mEmvDialog != null && mEmvDialog.isShowing()) {
                //    mEmvDialog.dismiss();
                //}
                mBluetoothHandler.LCDMainScreen();
                if (isRunning)
                    searchCard(mCardType);
            }

            @Override
            public void onEnterPasswordTimeout() {
                Log.e(TAG, "onEnterPasswordTimeout: ");
            }

            @Override
            public void onEmvStatus(EmvStatusEnum emvStatusEnum) {
                if (!isRunning)
                    return;
                Log.e(TAG, "onEmvStatus: " + emvStatusEnum.name());
                showLog("onEmvStatus: " + emvStatusEnum.name());
                if (emvStatusEnum == EmvStatusEnum.PBOC_OK || emvStatusEnum == EmvStatusEnum.QPBOC_OK) {
                    getEmvData();
                    emvCount++;
                } else {
                    errorCount++;
                    mBluetoothHandler.LCDMainScreen();
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isRunning)
                    searchCard(mCardType);
            }
        });
    }

    private void getMagData() {
        showLog("Mag card swipe");
        MagCard magCard = mDriverManager.getCardReadManager().getMAGCard();
        CardInfoEntity magReadData = magCard.getMagReadData();
        MyApp.cardInfoEntity = magReadData;
        if (magReadData.getResultcode() == SdkResult.SDK_OK) {
            String tk1 = magReadData.getTk1();
            String tk2 = magReadData.getTk2();
            String tk3 = magReadData.getTk3();
            showLog("tk1:  " + tk1);
            showLog("tk2:  " + tk2);
            showLog("tk3:  " + tk3);
            //searchCard(CardReaderTypeEnum.MAG_CARD);
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
        //if (mEmvDialog != null && mEmvDialog.isShowing()) {
        //    mEmvDialog.dismiss();
        //}
        showLog("cardNo:  " + cardNo);
        showLog("field55:  " + filed);
        showLog("track:  " + track);
        showLog("cardHolder:  " + cardHolder);
        showLog("expDate:  " + expDate);
        showLog("icSeq:  " + icSeq);
    }

    private void searchCard(CardReaderTypeEnum cardType) {
        //if (!isFinishing()) {
        //    mSearchDialog = DialogUtils.showProgress(EmvLoopTestActivity.this, null, "SearchCard...", new DialogInterface.OnCancelListener() {
        //        @Override
        //        public void onCancel(DialogInterface dialog) {
        //            // 发送取消寻卡指令
        //            int cancelSearchCard = mBluetoothHandler.cancelSearchCard();
        //            showLog("cancelSearchCard:  " + cancelSearchCard);
        //        }
        //    });
        //    mSearchDialog.getWindow().setGravity(Gravity.BOTTOM);
        //}
        // 寻卡之前 重新计数
        showCount();
        if (emvCount % 10 == 0) {
            mTvLog.setText("");
        }
        int ret = mBluetoothHandler.searchCard(cardType, 10);
        // 寻卡指令未返回/超时，关闭对话框
        //if (ret != SdkResult.SDK_OK) {
        //    if (mSearchDialog != null && mSearchDialog.isShowing()) {
        //        mSearchDialog.dismiss();
        //    }
        //}
        String msg = "Send search " + cardType.name() + " " + ret;
        showLog(msg);
    }

    private void setLcdMain() {
        int ret = mBluetoothHandler.LCDMainScreen();
        showLog("Set mpos Lcd main:  " + ret);
    }

    private void setLcdAmount() {
        int ret = mBluetoothHandler.LCDAmount(50 * 100);
        showLog("Set mpos Lcd amount ￥50:  " + ret);
    }


    public void showLog(String log) {
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
            case R.id.menu_item_log:
                mTvLog.setText("");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        isRunning = false;
        super.onDestroy();
    }

    /**
     * 初始化计数
     */
    private void initCount() {
        isRunning = true;
        emvCount = 0;
        timeoutCount = 0;
        errorCount = 0;
    }

    /**
     * 显示计数
     */
    private void showCount() {
        listItems[0] = "Emv count: " + emvCount + "\t\t" + "timeout: " + timeoutCount + "\t\t" + "error: " + errorCount;
        mAdapter.notifyDataSetChanged();
    }
}
