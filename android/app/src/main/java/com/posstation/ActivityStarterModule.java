package com.posstation;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableNativeArray;
import com.posstation.utils.PermissionsManager;
import com.zcs.sdk.DriverManager;
import com.zcs.sdk.bluetooth.BluetoothManager;

import Lib.FWReader.S8.function_S8;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class ActivityStarterModule extends ReactContextBaseJavaModule {

    private LeDeviceListAdapter mAdapter;
    private BluetoothManager mBluetoothManager;
    private Dialog mDialog;

    public static final char PT_USB = 2;
    int struct_portType = PT_USB;
    public char gl_autoRun = 0, gl_autoRunning = 0;
    public char gl_singleTestInAutoRunning = 0;
    public static final char UI_UPDATE_MSG_TEXT_APPEND = 4;
    private function_S8 call_s8;

    AutoTestThread mAutoThread= null;


    ActivityStarterModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }


    @Override
    public String getName() {
        return "ActivityStarter";
    }

    @ReactMethod
    void showPinPadText(@NonNull String text) {
        String devPath = "/dev/ttySAC2";
        int baud = 115200;

        Activity activity = getCurrentActivity();
        call_s8 = new function_S8(activity);
        call_s8.SetTransPara(0x40,1155,22352);

        FuncDispStr(struct_portType, devPath, baud, text);
    }

    @ReactMethod
    void navigeteMpos(@NonNull Callback callback){
        String devPath;
        int baud;

        devPath = "/dev/ttySAC2";
        baud = 115200;

        Activity activity = getCurrentActivity();

        call_s8 = new function_S8(activity);

        call_s8.SetTransPara(0x40,1155,22352);
        callback.invoke(FuncKBPinCode(struct_portType, devPath, baud));

    }

    @ReactMethod
    void FuncM1(@NonNull Callback callback){
        String devPath;
        int baud;

        devPath = "/dev/ttySAC2";
        baud = 115200;
        Activity activity = getCurrentActivity();

        call_s8 = new function_S8(activity);
        call_s8.SetTransPara(0x40,1155,22352);

        callback.invoke(M1(struct_portType, devPath, baud));

    }

    @ReactMethod
    void Kilometrage(@NonNull Callback callback){
        String devPath;
        int baud;

        devPath = "/dev/ttySAC2";
        baud = 115200;

        Activity activity = getCurrentActivity();

        call_s8 = new function_S8(activity);

        call_s8.SetTransPara(0x40,1155,22352);
        callback.invoke(FuncKBKilometrage(struct_portType, devPath, baud));

    }

    private class AutoTestThread extends Thread {

        int hdev=1;
        String devPath;
        int baud;
        int okCnt =0;
        String strNoCardMsg="put on card...";

        @Override
        public void run() {
            try {

                devPath = "/dev/ttySAC2";
                baud = 115200;

                gl_autoRun = 1;

                while(gl_autoRunning == 1)
                {

                    Thread.sleep(500);//50

                    if(1 == gl_singleTestInAutoRunning)
                        continue;

                    gl_singleTestInAutoRunning = 1;

                    Toast.makeText(getReactApplicationContext(), "mpos", Toast.LENGTH_LONG).show();
                    TestM1(struct_portType, devPath, baud);

                    gl_singleTestInAutoRunning = 0;

                }

                gl_autoRun = 0;

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void FuncDispStr(int portType, String path, int baudrate, String text){
        int hdev=1;

        if(portType == PT_USB)hdev = call_s8.fw_init_ex(2, null, 0);
        else hdev = call_s8.fw_init_ex (1, path.toCharArray(), baudrate);

        if (hdev != -1){
            char[] pRBuffer = new char[256];

            for (int i = 0; i < text.length(); i++) {
                pRBuffer[i] = text.charAt(i);
            }

            call_s8.fw_lcd_dispclear(hdev);

            call_s8.fw_lcd_dispstr(hdev, pRBuffer);

            call_s8.fw_exit(hdev);

        }
    }

    public void TestFuncKB(int portType, String path, int baudrate) {
        // TODO Auto-generated method stub
        int result = 0, hdev=1;
        Activity activity = getCurrentActivity();



        if(portType == PT_USB)hdev = call_s8.fw_init_ex(2, null, 0);
        else hdev = call_s8.fw_init_ex (1, path.toCharArray(), baudrate);



        if (hdev != -1) {
            //m_tview.setText(" Get Handle: " + Integer.toString(hdev));

            short status;
            short TIMELEN = 160;
            char[] pRBuffer = new char[256];
            char[] pRlen = new char[1];
            char[] showstr = new char[100];

            String str = "                  Taper code pin";

            // Creating array of string length
            char[] ch = new char[256];

            // Copy character by character into array
            for (int i = 0; i < str.length(); i++) {
                pRBuffer[i] = str.charAt(i);
            }

            call_s8.fw_lcd_dispclear(hdev);

            call_s8.fw_lcd_dispstr(hdev, pRBuffer);


            status = call_s8.fw_PassIn(hdev,(short)TIMELEN);

            if (status != 0) {
                System.out.print("fw_PassIn error!\n");



                if (activity != null) {
                    MainApplication application = (MainApplication) activity.getApplication();
                    ReactNativeHost reactNativeHost = application.getReactNativeHost();
                    ReactInstanceManager reactInstanceManager = reactNativeHost.getReactInstanceManager();
                    ReactContext reactContext = reactInstanceManager.getCurrentReactContext();

                    if (reactContext != null) {
                        CatalystInstance catalystInstance = reactContext.getCatalystInstance();
                        WritableNativeArray params = new WritableNativeArray();
                        params.pushString(String.valueOf(TIMELEN));

                        // AFAIK, this approach to communicate from Java to JavaScript is officially undocumented.
                        // Use at own risk; prefer events.
                        // Note: Here we call 'alert', which shows UI. If this is done from an activity that
                        // doesn't forward lifecycle events to React Native, it wouldn't work.
                        catalystInstance.callFunction("JavaScriptVisibleToJava", "alert", params);
                    }
                }
            } else {
                System.out.println("please input your password in "+ TIMELEN +" seconds![Press ENTER after Input]\n");//strmsg);
            }

            do
            {

                java.util.Arrays.fill(pRBuffer,'\0');

                status = call_s8.fw_PassGet(hdev, pRlen, pRBuffer);

                if(status!= 0Xa1 && status != 0xa2 && status !=0xa5 && status !=0x00)
                {

                    call_s8.fw_CheckKeyValue(hdev, pRlen, pRBuffer);

                    if((byte)(pRlen[0])%2  != 0)
                        pRBuffer[(byte)pRlen[0]] = 0x20;

                    call_s8.fw_lcd_dispstr(hdev,pRBuffer);
                }

            }
            while( status!= 0Xa1 && status != 0xa2 && status !=0xa5 && status !=0x00);


            switch(status)
            {
                case 0x00:

                    int i=0;

                    call_s8.fw_lcd_dispclear(hdev);

                    pRlen[0] -= 1;

                    System.out.println("\nYour password is:");

                    for(i=0; i< (byte)pRlen[0]; i++) System.out.print((char)pRBuffer[i]);
                    System.out.print("\n");

                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"password: "+String.valueOf(pRBuffer) +"\n");

                    break;

                case 0xa1:

                    System.out.print("Your password is too length.(should less then 25).");

                    break;

                case 0xa2:

                    System.out.print("You cancel your input.ReInput Please!\n");

                    break;

                case 0xa5:

                    System.out.print("TimeOUT!\n");

                    break;
            }

            call_s8.fw_exit(hdev);
        }

    }

    public String FuncKBPinCode(int portType, String path, int baudrate) {
        // TODO Auto-generated method stub
        int result = 0, hdev=1;

        String callback = null;
        Activity activity = getCurrentActivity();



        if(portType == PT_USB)hdev = call_s8.fw_init_ex(2, null, 0);
        else hdev = call_s8.fw_init_ex (1, path.toCharArray(), baudrate);



        if (hdev != -1) {
            //m_tview.setText(" Get Handle: " + Integer.toString(hdev));

            short status;
            short TIMELEN = 160;
            char[] pRBuffer = new char[256];
            char[] pRlen = new char[1];
            char[] showstr = new char[100];

            String str = "                  Taper code pin";

            // Creating array of string length
            char[] ch = new char[256];

            // Copy character by character into array
            for (int i = 0; i < str.length(); i++) {
                pRBuffer[i] = str.charAt(i);
            }

            call_s8.fw_lcd_dispclear(hdev);

            call_s8.fw_lcd_dispstr(hdev, pRBuffer);


            status = call_s8.fw_PassIn(hdev,(short)TIMELEN);

            if (status != 0) {
                System.out.print("fw_PassIn error!\n");



                if (activity != null) {
                    MainApplication application = (MainApplication) activity.getApplication();
                    ReactNativeHost reactNativeHost = application.getReactNativeHost();
                    ReactInstanceManager reactInstanceManager = reactNativeHost.getReactInstanceManager();
                    ReactContext reactContext = reactInstanceManager.getCurrentReactContext();

                    if (reactContext != null) {
                        CatalystInstance catalystInstance = reactContext.getCatalystInstance();
                        WritableNativeArray params = new WritableNativeArray();
                        params.pushString(String.valueOf(TIMELEN));

                        // AFAIK, this approach to communicate from Java to JavaScript is officially undocumented.
                        // Use at own risk; prefer events.
                        // Note: Here we call 'alert', which shows UI. If this is done from an activity that
                        // doesn't forward lifecycle events to React Native, it wouldn't work.
                        catalystInstance.callFunction("JavaScriptVisibleToJava", "alert", params);
                    }
                }
            } else {
                System.out.println("please input your password in "+ TIMELEN +" seconds![Press ENTER after Input]\n");//strmsg);
            }

            do
            {

                java.util.Arrays.fill(pRBuffer,'\0');

                status = call_s8.fw_PassGet(hdev, pRlen, pRBuffer);

                if(status!= 0Xa1 && status != 0xa2 && status !=0xa5 && status !=0x00)
                {

                    call_s8.fw_CheckKeyValue(hdev, pRlen, pRBuffer);

                    if((byte)(pRlen[0])%2  != 0)
                        pRBuffer[(byte)pRlen[0]] = 0x20;

                    call_s8.fw_lcd_dispstr(hdev,pRBuffer);
                }

            }
            while( status!= 0Xa1 && status != 0xa2 && status !=0xa5 && status !=0x00);


            switch(status)
            {
                case 0x00:

                    int i=0;

                    call_s8.fw_lcd_dispclear(hdev);

                    pRlen[0] -= 1;

                    System.out.println("\nYour password is:");

                    for(i=0; i< (byte)pRlen[0]; i++) System.out.print((char)pRBuffer[i]);
                    System.out.print("\n");

                    callback = String.valueOf(pRBuffer);

                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"password: "+String.valueOf(pRBuffer) +"\n");

                    break;

                case 0xa1:

                    System.out.print("Your password is too length.(should less then 25).");

                    callback = "null";

                    break;

                case 0xa2:

                    System.out.print("You cancel your input.ReInput Please!\n");

                    callback = "null";

                    break;

                case 0xa5:

                    System.out.print("TimeOUT!\n");

                    callback = "null";

                    break;
            }

            call_s8.fw_exit(hdev);
        }

        return callback;
    }

    public String FuncKBKilometrage(int portType, String path, int baudrate) {
        // TODO Auto-generated method stub
        int result = 0, hdev=1;

        String callback = null;
        Activity activity = getCurrentActivity();



        if(portType == PT_USB)hdev = call_s8.fw_init_ex(2, null, 0);
        else hdev = call_s8.fw_init_ex (1, path.toCharArray(), baudrate);



        if (hdev != -1) {
            //m_tview.setText(" Get Handle: " + Integer.toString(hdev));

            short status;
            short TIMELEN = 160;
            char[] pRBuffer = new char[256];
            char[] pRlen = new char[1];
            char[] showstr = new char[100];

            String str = "                  Taper Kilometrage";

            // Creating array of string length
            char[] ch = new char[256];

            // Copy character by character into array
            for (int i = 0; i < str.length(); i++) {
                pRBuffer[i] = str.charAt(i);
            }

            call_s8.fw_lcd_dispclear(hdev);

            call_s8.fw_lcd_dispstr(hdev, pRBuffer);


            status = call_s8.fw_PassIn(hdev,(short)TIMELEN);

            if (status != 0) {
                System.out.print("fw_PassIn error!\n");



                if (activity != null) {
                    MainApplication application = (MainApplication) activity.getApplication();
                    ReactNativeHost reactNativeHost = application.getReactNativeHost();
                    ReactInstanceManager reactInstanceManager = reactNativeHost.getReactInstanceManager();
                    ReactContext reactContext = reactInstanceManager.getCurrentReactContext();

                    if (reactContext != null) {
                        CatalystInstance catalystInstance = reactContext.getCatalystInstance();
                        WritableNativeArray params = new WritableNativeArray();
                        params.pushString(String.valueOf(TIMELEN));

                        // AFAIK, this approach to communicate from Java to JavaScript is officially undocumented.
                        // Use at own risk; prefer events.
                        // Note: Here we call 'alert', which shows UI. If this is done from an activity that
                        // doesn't forward lifecycle events to React Native, it wouldn't work.
                        catalystInstance.callFunction("JavaScriptVisibleToJava", "alert", params);
                    }
                }
            } else {
                System.out.println("please input your password in "+ TIMELEN +" seconds![Press ENTER after Input]\n");//strmsg);
            }

            do
            {

                java.util.Arrays.fill(pRBuffer,'\0');

                status = call_s8.fw_PassGet(hdev, pRlen, pRBuffer);

                if(status!= 0Xa1 && status != 0xa2 && status !=0xa5 && status !=0x00)
                {

                    call_s8.fw_CheckKeyValue(hdev, pRlen, pRBuffer);

                    if((byte)(pRlen[0])%2  != 0)
                        pRBuffer[(byte)pRlen[0]] = 0x20;

                    call_s8.fw_lcd_dispstr(hdev,pRBuffer);
                }

            }
            while( status!= 0Xa1 && status != 0xa2 && status !=0xa5 && status !=0x00);


            switch(status)
            {
                case 0x00:

                    int i=0;

                    call_s8.fw_lcd_dispclear(hdev);

                    pRlen[0] -= 1;

                    System.out.println("\nYour password is:");

                    for(i=0; i< (byte)pRlen[0]; i++) System.out.print((char)pRBuffer[i]);
                    System.out.print("\n");

                    callback = String.valueOf(pRBuffer);

                    SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND,"password: "+String.valueOf(pRBuffer) +"\n");

                    break;

                case 0xa1:

                    System.out.print("Your password is too length.(should less then 25).");

                    callback = "null";

                    break;

                case 0xa2:

                    System.out.print("You cancel your input.ReInput Please!\n");

                    callback = "null";

                    break;

                case 0xa5:

                    System.out.print("TimeOUT!\n");

                    callback = "null";

                    break;
            }

            call_s8.fw_exit(hdev);
        }

        return callback;
    }

    public boolean AutoTestM1(int hdev, int curCnt) {
        // TODO Auto-generated method stub
        int result = 0;
        char[] pSnrM1 = new char[255];
        boolean st = false;

        if (hdev != -1) {

            result = call_s8.fw_card_str(hdev, (short)0, pSnrM1);
            if(0 == result)
            {
                //call_s8.fw_beep(hdev,5);
                SendUIMessage(UI_UPDATE_MSG_TEXT_APPEND, "_card:ok["+String.valueOf(curCnt) + "] ID:" +String.valueOf(pSnrM1) + ".Move away the card please." );

                call_s8.fw_halt(hdev);
                st = true;
            }
        }

        return st;
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

                    call_s8.fw_lcd_dispclear(hdev);

                    String str = "Merci de patienter";

                    // Creating array of string length
                    char[] ch = new char[256];

                    // Copy character by character into array
                    for (int i = 0; i < str.length(); i++) {
                        ch[i] = str.charAt(i);
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
    public String M1(int portType, String path, int baudrate) {
        // TODO Auto-generated method stub
        String callback = null;
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

                    callback = String.valueOf(pSnrM1);

                    call_s8.fw_lcd_dispclear(hdev);

                    String str = "merci";

                    // Creating array of string length
                    char[] ch = new char[256];

                    // Copy character by character into array
                    for (int i = 0; i < str.length(); i++) {
                        ch[i] = str.charAt(i);
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

        return callback;

    }

    private void SendUIMessage(char toWhat, String text) {
        // Toast.makeText(getReactApplicationContext(), text, Toast.LENGTH_LONG).show();

    }

    @ReactMethod
    void bluetooth(){
        String devPath;
        int baud;

        devPath = "/dev/ttySAC2";
        baud = 115200;

        Activity activity = getCurrentActivity();
        call_s8 = new function_S8(activity);
        TestFuncKB(struct_portType, devPath, baud);
    }


    @ReactMethod
    void navigateToExample() {
        Activity activity = getCurrentActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, NFCActivity.class);
            activity.startActivity(intent);
        }
    }

    @ReactMethod
    void dialNumber(@NonNull String number) {
        Activity activity = getCurrentActivity();
        if (activity != null) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
            activity.startActivity(intent);
        }
    }

    @ReactMethod
    void getActivityName(@NonNull Callback callback) {
        Activity activity = getCurrentActivity();
        if (activity != null) {
            callback.invoke(activity.getClass().getSimpleName());
        } else {
            callback.invoke("No current activity");
        }
    }

    @ReactMethod
    void getActivityNameAsPromise(@NonNull Promise promise) {
        Activity activity = getCurrentActivity();
        if (activity != null) {
            promise.resolve(activity.getClass().getSimpleName());
        } else {
            promise.reject("NO_ACTIVITY", "No current activity");
        }
    }

    @ReactMethod
    void callJavaScript() {
        Activity activity = getCurrentActivity();
        if (activity != null) {
            MainApplication application = (MainApplication) activity.getApplication();
            ReactNativeHost reactNativeHost = application.getReactNativeHost();
            ReactInstanceManager reactInstanceManager = reactNativeHost.getReactInstanceManager();
            ReactContext reactContext = reactInstanceManager.getCurrentReactContext();

            if (reactContext != null) {
                CatalystInstance catalystInstance = reactContext.getCatalystInstance();
                WritableNativeArray params = new WritableNativeArray();
                params.pushString("Hello, JavaScript!");

                // AFAIK, this approach to communicate from Java to JavaScript is officially undocumented.
                // Use at own risk; prefer events.
                // Note: Here we call 'alert', which shows UI. If this is done from an activity that
                // doesn't forward lifecycle events to React Native, it wouldn't work.
                catalystInstance.callFunction("JavaScriptVisibleToJava", "alert", params);
            }
        }
    }

}
