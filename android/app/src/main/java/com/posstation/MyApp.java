package com.posstation;

import android.app.Application;
import android.content.Context;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.zcs.sdk.DriverManager;
import com.zcs.sdk.card.CardInfoEntity;

/**
 * Created by yyzz on 2018/5/18.
 */

public class MyApp extends Application implements ReactApplication {
    public static DriverManager sDriverManager;
    public static CardInfoEntity cardInfoEntity;
    public static Context context;
    private static Application sApp;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        sDriverManager = DriverManager.getInstance();
        cardInfoEntity = new CardInfoEntity();
        context = getApplicationContext();
    }

    public static Application getApp() {
        return sApp;
    }

    @Override
    public ReactNativeHost getReactNativeHost() {
        return null;
    }
}
