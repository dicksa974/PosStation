package com.posstation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactRootView;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.devsupport.interfaces.DevOptionHandler;
import com.facebook.react.devsupport.interfaces.DevSupportManager;
import com.swmansion.gesturehandler.react.RNGestureHandlerEnabledRootView;


import Lib.FWReader.S8.function_S8;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends ReactActivity {

    private static final String CUSTOM_DEV_OPTION_MESSAGE = "Hello from custom dev option!";

    public static final char PT_USB = 2;
    int struct_portType = PT_USB;
    public char gl_autoRun = 0, gl_autoRunning = 0;
    public char gl_singleTestInAutoRunning = 0;
    public static final char UI_UPDATE_MSG_TEXT_APPEND = 4;
    private function_S8 call_s8;

    AutoTestThread mAutoThread= null;
    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "PosStation";
    }

    @Override
    protected ReactActivityDelegate createReactActivityDelegate() {
      return new ReactActivityDelegate(this, getMainComponentName()) {
        @Override
         protected ReactRootView createRootView() {
            return new RNGestureHandlerEnabledRootView(MainActivity.this);
         }

          @Override
         protected Bundle getLaunchOptions() {
             Bundle launchOptions = new Bundle();
             launchOptions.putString("buildType", BuildConfig.BUILD_TYPE);
             return launchOptions;
         }
       };
    }

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
    //        super.setContentView(R.layout.activity_main);
    //        actionBar = getSupportActionBar();
    //        if (actionBar != null) {
    //            // actionBar.setHomeButtonEnabled(true);
    //            //actionBar.setDisplayHomeAsUpEnabled(true);
    //            actionBar.setTitle(getString(R.string.pref_settings));
    //        }
    //        Fragment fragment = new SettingsFragment();
    //        if (savedInstanceState == null) {
    //            getFragmentManager().beginTransaction().add(R.id.frame_container, fragment).commit();
    //        }
            MainApplication application = (MainApplication) getApplication();
            ReactNativeHost reactNativeHost = application.getReactNativeHost();
            ReactInstanceManager reactInstanceManager = reactNativeHost.getReactInstanceManager();
            DevSupportManager devSupportManager = reactInstanceManager.getDevSupportManager();
            devSupportManager.addCustomDevOption("Custom dev option", new DevOptionHandler() {
                @Override
                public void onOptionSelected() {
                    if (NotificationManagerCompat.from(MainActivity.this).areNotificationsEnabled()) {
                        Toast.makeText(MainActivity.this, CUSTOM_DEV_OPTION_MESSAGE, Toast.LENGTH_LONG).show();
                    } else {
                        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
                        dialog.setTitle("Dev option");
                        dialog.setMessage(CUSTOM_DEV_OPTION_MESSAGE);
                        dialog.show();
                    }
                }
            });

        }

        public class AutoTestThread extends Thread {

            int hdev=1;
            String devPath;
            int baud;
            int okCnt =0;
            String strNoCardMsg="put on card...";

            @Override
            public void run() {
                try {

                    char gl_autoRun = 0, gl_autoRunning = 0;
                    char gl_singleTestInAutoRunning = 0;

                    devPath = "/dev/ttySAC2";
                    baud = 115200;

                    gl_autoRun = 1;

                    while(gl_autoRunning == 1)
                    {

                        Thread.sleep(500);//50

                        if(1 == gl_singleTestInAutoRunning)
                            continue;

                        gl_singleTestInAutoRunning = 1;

                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "ok test");

                        gl_singleTestInAutoRunning = 0;

                    }

                    gl_autoRun = 0;

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        public void TestM1(int portType, String path, int baudrate) {
            // TODO Auto-generated method stub
            int result = 0, hdev=1;
            char[] pModVer = new char[512];
            char[] pSnrM1 = new char[255];
            char[] pCharHex = new char[255];
            char[] pCharSingle = new char[255];
            int lenSingleChar=2, lenHex;
            short tblk = 24;
            short val_blk = 25;
            int[] pCurVal = new int[1];
            short tSec = (short)(tblk/4);
            short keymode = 0;
            char[] defKey = {0xff,0xff,0xff,0xff,0xff,0xff};
            char[] newKey = {0xff,0xff,0xff,0xff,0xff,0xff};
            char[] strNewkey = new char[255];
            char[] tWrite = {0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0a,0x0b,0x0c,0xd,0xe,0xf};
            char[] strHexWrite =new char[1024];
            char[] strHexRead = new char[1024];
            char[] strKeyb = ("ffffffffffff").toCharArray();
            char[] strCtrlW = ("ff078069").toCharArray();


            lenHex = 2*lenSingleChar;
            pCharHex =("090a").toCharArray();

    //		call_s8.a_hex(pCharSingle,pCharHex,   lenSingleChar);
    //		SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," a_hex:");
    //		for(i=0;i<lenSingleChar;i++)
    //		{
    //			SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," "+Integer.toString(pCharSingle[i]));
    //		}

            pCharHex = new char[lenHex];
            call_s8.hex_a(pCharHex, pCharSingle,   lenHex);
            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," hex_a:"+String.valueOf(pCharHex));

            if(portType == PT_USB)hdev = call_s8.fw_init_ex(2, null, 0);
            else hdev = call_s8.fw_init_ex (1, path.toCharArray(), baudrate);
            call_s8.fw_lcd_dispclear(hdev);

            if (hdev != -1) {



                call_s8.fw_beep(hdev, 5);

                //try to get module version
                result = call_s8.fw_getver(hdev, pModVer);
                if(0 == result)
                {

                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"-");
                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"Module Version: " + String.valueOf(pModVer));


                    call_s8.fw_load_key(hdev, keymode, tSec, defKey);

                    result = call_s8.fw_card_str(hdev, (short)1, pSnrM1);
                    if(0 == result)
                    {
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_card:ok ");
                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,String.valueOf(pSnrM1));

                        String str = "                  Taper code pin";

                        // Creating array of string length
                        char[] ch = new char[256];

                        // Copy character by character into array
                        for (int i = 0; i < str.length(); i++) {
                            ch[i] = String.valueOf(pSnrM1).charAt(i);
                        }

                        call_s8.fw_lcd_dispstr(hdev, ch);

                        // authen
                        result = call_s8.fw_authentication(hdev, keymode, tSec );

                        if(0 == result)
                        {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_authen:ok ");

                            //write
                            //result = call.fw_write(hdev, tblk, tWrite);


                            call_s8.hex_a(strHexWrite, tWrite, 2*(tWrite.length));
                            result = call_s8.fw_write_hex(hdev, tblk, strHexWrite);


                            if(0 == result)
                            {
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_write:ok ");

                                //read
                                //result = call.fw_read(hdev, tblk, tRead);


                                result = call_s8.fw_read_hex(hdev, tblk, strHexRead);

                                if(0 == result)
                                {
                                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_read:ok ");

                                    //	for(i=0;i<16;i++)
                                    //	{
                                    //		SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," "+Integer.toHexString(tRead[i]));
                                    //	}

                                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," " +String.valueOf(strHexRead));

                                    //value test
                                    result = call_s8.fw_initval(hdev, val_blk, 1000);
                                    if(0 == result)
                                    {
                                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," _initval:ok");

                                        result = call_s8.fw_increment(hdev, val_blk, 200);
                                        if(0 == result)
                                        {
                                            call_s8.fw_transfer(hdev, val_blk);//make increment valid
                                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," _increment:ok");

                                            result = call_s8.fw_decrement(hdev, val_blk, 100);
                                            if(0 == result)
                                            {
                                                call_s8.fw_transfer(hdev, val_blk);//make decrement valid
                                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," _decrement:ok");

                                                result = call_s8.fw_readval(hdev, val_blk, pCurVal );
                                                if(0 == result)
                                                {
                                                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," _readval ok:" + pCurVal[0]);
                                                }else SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," _readval error");
                                            }else SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," _decrement:error");
                                        }else SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," _increment:error");

                                    }else SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," _initval:error");


                                    //result = call.changeKey(hdev, tSec, newKey, ctrlw, keyb);

                                    call_s8.hex_a(strNewkey, newKey, 2*(newKey.length));
                                    result = call_s8.fw_changeKey_hex(hdev, tSec, strNewkey, strCtrlW, strKeyb);


                                    if(0 ==result)
                                    {
                                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," _changekey:ok");

                                        result = call_s8.fw_halt(hdev);
                                        if(0 == result)
                                        {
                                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," _halt:ok");
                                        }
                                        else
                                        {
                                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," _halt:error");
                                        }
                                    }
                                    else
                                    {
                                        SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND," _changekey:error");
                                    }

                                }
                                else
                                {
                                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_read:error ");
                                }
                            }
                            else
                            {
                                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_write:error ");
                            }

                        }
                        else
                        {
                            SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"_athen:error ");

                        }

                    }
                }

                call_s8.fw_exit(hdev);
            }
            else
            {
                //Log.e("readerlog", "Link reader error");
            }

        }

        private void SendUIMessage(char toWhat, String text) {
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }
}
