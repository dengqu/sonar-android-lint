package com.lambeta;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileUtil {
    public final static String PLUGINS_LOG = "D:\\test.txt";
    public final static boolean isDebug = false;

    public static void printLog(String conent) {
        printLog(PLUGINS_LOG, conent);
    }

    public static void printLog(String file, String conent) {
        if (isDebug) {
            BufferedWriter out = null;
            try {
                out = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(file, true)));
                out.write(conent + "\r\n");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
