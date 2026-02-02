package com.advance.utils;


import androidx.annotation.Keep;

import com.bayes.sdk.basic.util.BYBase64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

@Keep
public class AdvanceSecurityCore {
    private static AdvanceSecurityCore instance = null;
    /*
     * 加密用的Key 可以用26个字母和数字组成 此处使用AES-128-CBC加密模式，key需要为16位。
     */
    public String sKey = "bayescom1000000w";

    private AdvanceSecurityCore() {

    }

    public static AdvanceSecurityCore getInstance() {
        if (instance == null)
            instance = new AdvanceSecurityCore();
        return instance;
    }

    public static String webSafeBase64StringEncoding(byte[] sSrc, boolean padded) throws Exception {
        BYBase64.Encoder encoder = BYBase64.getUrlEncoder();
        if (!padded) {
            encoder.withoutPadding();
        }
        return encoder.encodeToString(sSrc);

    }

    public static byte[] webSafeBase64StringDecoding(String sSrc) throws Exception {
        return BYBase64.getUrlDecoder().decode(sSrc.getBytes());
    }

    public static String base64StringEncoding(byte[] sSrc, boolean padded) throws Exception {
        String encodeString = BYBase64.getEncoder().encodeToString(sSrc);// 此处使用BASE64做转码。

        //nopadding base64
        if (!padded) {
            if (encodeString.endsWith("=")) {
                encodeString = encodeString.substring(0, encodeString.length() - 1);
                if (encodeString.endsWith("=")) {
                    encodeString = encodeString.substring(0, encodeString.length() - 1);
                }
            }
        }
        return encodeString;
    }

    public static byte[] base64StringDecoding(String sSrc) throws Exception {
        return BYBase64.getDecoder().decode(sSrc.getBytes("utf-8"));
    }

    //此处改写了加密方式，cbc方式为旧模式，带向量。新方法
    public static byte[] AES128CBCStringEncoding(String encData, String secretKey) throws Exception {

        if (secretKey == null) {
            return null;
        }
        if (secretKey.length() != 16) {
            return null;
        }
//        if (vector != null && vector.length() != 16) {
//            return null;
//        }
        Cipher cipher = Cipher.getInstance("AES");
        byte[] raw = secretKey.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
//        IvParameterSpec iv = new IvParameterSpec(vector.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(encData.getBytes("utf-8"));

        return encrypted;
    }

    public static String AES128CBCStringDecoding(byte[] sSrc, String key) throws Exception {
        try {
            if (key == null) {
                return null;
            }
            if (key.length() != 16) {
                return null;
            }
//            if (ivs != null && ivs.length() != 16) {
//                return null;
//            }
            byte[] raw = key.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");
//            IvParameterSpec iv = new IvParameterSpec(ivs.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] original = cipher.doFinal(sSrc);
            String originalString = new String(original, "utf-8");
            return originalString;
        } catch (Throwable ex) {
            return null;
        }
    }

    //test
    public static void main(String[] args) throws Exception {

    }

    // 加密
    public String encrypt(String sSrc) {
        try {
            String encodeString = webSafeBase64StringEncoding(AES128CBCStringEncoding(sSrc, sKey), true);

            return encodeString;
        } catch (Throwable ex) {
            return sSrc;
        }
    }

    // 解密
    public String decrypt(String sSrc) throws Exception {
        try {
            String decodeString = AES128CBCStringDecoding(webSafeBase64StringDecoding(sSrc), sKey);
            return decodeString;
        } catch (Throwable ex) {
            return sSrc;
        }
    }
}