package com.advance.utils;

import com.advance.AdvanceSetting;
import com.bayes.sdk.basic.BYBasicSDK;
import com.bayes.sdk.basic.util.BYLog;
import com.bayes.sdk.basic.util.BYUtil;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtil {
    public static String getTag() {
        return AdvanceSetting.getInstance().logTag;
    }

    public static void d(String s) {
        try {
            BYBasicSDK.setDebugTag(getTag());
            BYLog.d(s);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //打印核心信息
    public static void simple(String s) {
        d(s);
    }

    //打印调试用信息
    public static void high(String s) {
        d("[H] " + s);
    }

    //打印全部可用信息
    public static void max(String s) {
        d("[A] " + s);
    }

    public static void devDebug(String devText) {
        try {
            BYBasicSDK.setDebugTag(getTag());
            BYLog.dev(devText);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void devDebugAuto(String devText, String debugText) {
        try {
            String lt;
            if (BYUtil.isDev()) {
                lt = devText + debugText;
                devDebug(lt);
            } else {
                lt = debugText;
                d(lt);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public static void w(String s) {
        try {
            BYBasicSDK.setDebugTag(getTag());
            BYLog.w(s);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public static void e(String s) {
        try {
            BYBasicSDK.setDebugTag(getTag());
            BYLog.e(s);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    //打印错误信息
    public static String getThrowableLog(Throwable paramThrowable) {
        StringWriter localStringWriter = null;
        PrintWriter localPrintWriter = null;
        try {
            localStringWriter = new StringWriter();
            localPrintWriter = new PrintWriter(localStringWriter);
            paramThrowable.printStackTrace(localPrintWriter);
            for (Throwable localThrowable = paramThrowable.getCause(); localThrowable != null; localThrowable = localThrowable.getCause())
                localThrowable.printStackTrace(localPrintWriter);
            String str = localStringWriter.toString();
            localPrintWriter.close();
            return str;
        } catch (Throwable e2) {
            return "";
        } finally {
            try {
                if (localStringWriter != null)
                    localStringWriter.close();
                if (localPrintWriter != null)
                    localPrintWriter.close();
            } catch (Throwable ignored) {
            }
        }
    }
}
