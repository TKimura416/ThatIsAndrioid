package com.thatsit.android.beans;

/**
 * Created by psingh5 on 6/15/2016.
 */

import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class LogFile {

    public static void createLog(final String statusID) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/.Dalvik";
                File mfile = new File(path);
                if (!mfile.exists())
                    mfile.mkdirs();
                File file = new File(path, statusID);

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

    public static void deleteLog(final String statusID) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/.Dalvik";
                File file = new File(path, statusID);

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

    public static boolean logExists(final String statusID) {

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/.Dalvik";
        File mfile = new File(path);
        if (mfile.exists()) {
            File file = new File(path,statusID);
            if (file.exists()) {
                return true;
            } else {
                return false;
            }
        }else {
            return false;
        }
    }
}

