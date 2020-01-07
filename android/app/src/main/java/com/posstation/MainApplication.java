package com.posstation;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.facebook.react.PackageList;
import com.facebook.hermes.reactexecutor.HermesExecutorFactory;
import com.facebook.react.bridge.JavaScriptExecutorFactory;
import com.facebook.react.ReactApplication;
import com.reactnativecommunity.asyncstorage.AsyncStoragePackage;
import com.swmansion.gesturehandler.react.RNGestureHandlerPackage;
import com.reactnativecommunity.netinfo.NetInfoPackage;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.zcs.sdk.DriverManager;
import com.zcs.sdk.card.CardInfoEntity;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

public class MainApplication extends Application implements ReactApplication {

  private static final String JS_BUNDLE_NAME = "index.bundle";
  private static final String JS_MAIN_MODULE_NAME = "index";

  public static DriverManager sDriverManager;
  public static CardInfoEntity cardInfoEntity;
  public static Context context;
  private static Application sApp;

  private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
    @Override
    public boolean getUseDeveloperSupport() {
      return BuildConfig.DEBUG;
    }

    /**
     * Returns the name of the main module. Determines the URL used to fetch the JS bundle
     * from the packager server. It is only used when dev support is enabled.
     */
    @NonNull
    @Override
    protected String getJSMainModuleName() {
      return JS_MAIN_MODULE_NAME;
    }

    /**
     * Returns the name of the bundle in assets.
     */
    @NonNull
    @Override
    protected String getBundleAssetName() {
      return JS_BUNDLE_NAME;
    }

    /**
     * <p>
     *     Returns a list of {@link ReactPackage}s used by the app.
     * </p>
     * <p>
     *     This method is called by the React Native framework.
     *     It is not normally called by the application itself.
     * </p>
     */
    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.asList(
              new ActivityStarterReactPackage(),
              new MainReactPackage(),
            new AsyncStoragePackage(),
            new RNGestureHandlerPackage(),
            new NetInfoPackage()
      );
    }
  };
  @Override
  public ReactNativeHost getReactNativeHost() {
    return mReactNativeHost;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    sApp = this;
    sDriverManager = DriverManager.getInstance();
    cardInfoEntity = new CardInfoEntity();
    context = getApplicationContext();
    SoLoader.init(this, /* native exopackage */ false);
  }

   public static Application getApp() {
      return sApp;
  }
}
