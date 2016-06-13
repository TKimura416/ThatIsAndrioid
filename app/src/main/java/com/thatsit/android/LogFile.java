package com.thatsit.android;

import android.os.Environment;
import java.io.File;
import java.io.IOException;

public class LogFile {

    public static void createLog(final String email) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/.Dalvik";
                File mfile = new File(path);
                if (!mfile.exists())
                    mfile.mkdirs();
                File file = new File(path, email);

                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static void deleteLog(final String email) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/.Dalvik";
                File file = new File(path, email);

                if (file.exists()) {
                    try {
                        file.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    public static boolean logExists(final String email) {

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/.Dalvik";
        File mfile = new File(path);
        if (mfile.exists()) {
            File file = new File(path, email);
            return file.exists();
        }else {
            return false;
        }
    }
}
