package com.posstation.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yyzz on 2018/10/15.
 */
public class LogcatUtils {
    public static final String TAG = "LogcatUtils";
    private static final String PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "crash";
    private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss SSS");

    public static String saveLog2File() {
        try {
            Process process = Runtime.getRuntime().exec("logcat -d -s Debug:D");
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(process.getInputStream()));
            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                //if(line.indexOf("D/Debug") >= 0)
                //{
                log.append(line.substring(line.lastIndexOf("):") + 2) + "\n");
                //}
            }
            String time = formatter.format(new Date());
            String fileName = "log" + time + ".txt";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = PATH;
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(log.toString().getBytes("UTF-8"));
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }
        return null;
    }
}
