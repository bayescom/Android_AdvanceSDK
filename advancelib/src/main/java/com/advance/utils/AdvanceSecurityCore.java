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
        // 需要加密的字串
//        String cSrc = "123";
//        String decodeRes = "Ntm1eTfB9TZa16p3CvcPgtT6fIoZWjBmP02Z1fKxA6cY5aXGe-voFyZ1v-p7KaXLeFjfDFY60pj5Pijti1DjWEy7qezQVlUgZDZreIrn4R75HC3IAttWv4eXb9dEvL9PGhuZnxFWk0lunhS6HrviwfunS5UpNAPVLROwM-qv8-bjwI_xqmOBH2_MvPi6q5cHpqYDZAbmmgNZQrlpbuXL02wkGYfW65syyA8ntazmfpxsGT6wEiO1Yu6CYbYqM78SFTjnwzLIqq2-U4zRQSnBFm7cpl9aRmyIB9VVIUh4avzwX-iIjras6qMgXdjw_hFRlDQ_kflVSPBf88cPkDOQo2Ua6Xs1RvoNh8_kUnRJU9TUUuMrRuv0tjtjQ2JnJTpHNG3shUd3-XWPHmadSCTDPsg1pa1EM4E_5s7O5zQ-x4QFLbL5OSP-M2O87Rfr_6KUZmt7iEgExjvDIBM4EK0dcWcXZYKqPPgmkKEqYj3DXkJAYhcpF_LucqdpymAq6TOsZrK1578PDNas6n4CQNOC8XbrCeiGpLGb9vCN67NFMjjNZHdz65KBwqVgO4TZT78d";
//        String decodeRes = "Ntm1eTfB9TZa16p3CvcPgtT6fIoZWjBmP02Z1fKxA6csxqjYLdrT9Op6GcfVF-Axt8bV7VW23TH0qoJWbIXskAfTTlGcwWhAW9-pn4YUPKZYiKi6ZRBNdUIqXY8BtxwC-RwtyALbVr-Hl2_XRLy_TxobmZ8RVpNJbp4Uuh674sH7p0uVKTQD1S0TsDPqr_PmvrIQJwhn9HnlPXv5Q_R8qWTui_3oqZBmiUe6P0RLd2TuK4arvOSNkGqLgzA9YbtkkH67jTkNcrddUYufqx5Y3ULXJJmjdT4in6ynz_vPfv9JpGOY19rZG_XxxJMyTbN9XVFmyJCSLPziYfZ1AxCEmJkDLxMiIl-5fmVsHjMaOXbrh0ueWgPQt0MFEbcBhbW6-IIh7rNSwr9cPcb9h-tfte-FlNC_ccBpz1cfyz97bkCzJ3cSZ0MROK8UVHXY6Luo5NIo4ojGSSoEzRWyLPl4XedPuPKZTscAa906A2oZq_-qOQDu-MRhGybl-i3dObjojfq5k5dA62CBFUobpR-PWiqpqF2xYphFlCwNDCSYxLE62RXSfnZt92IxGE180a1kjufkCY8Mfk9TVY1Mj_UjVnnVK_gxAggpd-SZrtiMse4=";
//        String jsString = BYSecurityCore.getInstance().decrypt(decodeRes);
//        System.out.println("解密后的字串是：" + jsString);
//
//        String json = "{\"ua\":\"Mozilla\\/5.0 (Linux; Android 11; MI 9 Transparent Edition Build\\/RKQ1.200826.002; wv) AppleWebKit\\/537.36 (KHTML, like Gecko) Version\\/4.0 Chrome\\/109.0.5414.117 Mobile Safari\\/537.36\",\"network\":1,\"os\":2,\"osv\":\"11.0\",\"devicetype\":1,\"sw\":1080,\"sh\":2221,\"ppi\":440,\"imei\":\"\",\"mac\":\"60:AB:67:F2:91:9B\",\"oaid\":\"616d570ad76f1563\",\"andr  rier\":\"46007\",\"make\":\"Xiaomi\",\"model\":\"MI 9 Transparent Edition\",\"update_mark\":\"102116.279999990\",\"boot_m";
//        System.out.println("加密前的json：" + json);
//        String enString1 = BYSecurityCore.getInstance().encrypt(json);
//        System.out.println("加密后的json字串是：" + enString1);
//
//
//        // 加密
//        long lStart = System.currentTimeMillis();
//        String enString = BYSecurityCore.getInstance().encrypt(cSrc);
//        System.out.println("加密后的字串是：" + enString);
//
//        long lUseTime = System.currentTimeMillis() - lStart;
//        System.out.println("加密耗时：" + lUseTime + "毫秒");
//        // 解密
//        lStart = System.currentTimeMillis();
//        String DeString = BYSecurityCore.getInstance().decrypt(enString);
//        System.out.println("解密后的字串是：" + DeString);
//        lUseTime = System.currentTimeMillis() - lStart;
//        System.out.println("解密耗时：" + lUseTime + "毫秒");
//        lStart = System.currentTimeMillis();
//        lUseTime = System.currentTimeMillis() - lStart;
//        System.out.println("解密耗时：" + lUseTime + "毫秒");
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