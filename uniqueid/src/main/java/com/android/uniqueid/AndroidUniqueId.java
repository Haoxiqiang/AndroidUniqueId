package com.android.uniqueid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AndroidUniqueId {

    private static AtomicBoolean INIT = new AtomicBoolean(false);

    /**
     * just init in main process
     *
     * @param context
     */
    public static void init(Context context) {
        //msa-sdk oaid
        INIT.set(true);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            MiitManager.attachBaseContext(context);
        }
    }

    public interface OnUniqueIdCallback {

        void onOAID(String oaid);

        void onResult(JSONObject result);
    }

    private static class AndroidUniqueIdLoader {
        static final AndroidUniqueId INSTANCE = new AndroidUniqueId();
    }

    public static AndroidUniqueId getInstance() {
        return AndroidUniqueIdLoader.INSTANCE;
    }

    private AndroidUniqueId() {

    }

    private JSONObject ids = new JSONObject();

    //TODO thread
    public void getIds(final Context context, final OnUniqueIdCallback callback) {
        if (!INIT.get()) {
            init(context);
        }

        if (MiitManager.status && !ids.has("oaid")) {
            MiitDid.detect(context, new MiitDid.OnMiitCallback() {
                @Override
                public void call(int result, String oaid, String vaid, String aaid) {
                    refreshIds(context);
                    try {
                        ids.put("oaid", oaid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    callback.onOAID(ids.optString("oaid", ""));
                    callback.onResult(ids);
                }
            });
        } else {

            if (ids.length() < 1) {
                refreshIds(context);
            }

            callback.onOAID(ids.optString("oaid", ""));
            callback.onResult(ids);
        }
    }

    /**
     * 当 {@link Manifest.permission.READ_PHONE_STATE}权限发生变化的时候,调用此方法可以刷新 imei 等信息
     */
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public void refreshIds(Context context) {
        try {
            //try auto fill imei
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                try {
                    //国际移动设备识别码 (IMEI) 和移动设备识别码 (MEID) 是赋予全球每台移动设备的永久性唯一识别码
                    TelephonyManager telephonyManager =
                            (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephonyManager != null) {

                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                            String id = telephonyManager.getDeviceId();
                            if (!TextUtils.isEmpty(id)) {
                                if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
                                    ids.put("meid", id);
                                } else {
                                    ids.put("imei", id);
                                }
                            }
                            //IMEI1
                            String imei1 = telephonyManager.getImei(0);
                            if (!TextUtils.isEmpty(imei1)) {
                                ids.put("imei1", imei1);
                            }
                            //IMEI2
                            String imei2 = telephonyManager.getImei(1);
                            if (!TextUtils.isEmpty(imei2)) {
                                ids.put("imei2", imei2);
                            }
                            //MEID
                            String meid = telephonyManager.getMeid();
                            if (!TextUtils.isEmpty(meid)) {
                                ids.put("meid", meid);
                            }
                        } else if (
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                                        && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                            //the IMEI for GSM and the MEID or ESN for CDMA phones
                            String id = telephonyManager.getDeviceId();
                            if (!TextUtils.isEmpty(id)) {
                                if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
                                    ids.put("meid", id);
                                } else {
                                    ids.put("imei", id);
                                }
                            }
                            String imei1 = telephonyManager.getDeviceId(0);
                            if (!TextUtils.isEmpty(imei1)) {
                                ids.put("imei1", imei1);
                            }
                            String imei2 = telephonyManager.getDeviceId(1);
                            if (!TextUtils.isEmpty(imei2)) {
                                ids.put("imei2", imei2);
                            }

                        } else if (
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                                        && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                            //Returns the unique device ID of a subscription,
                            // for example, the IMEI for GSM and the MEID for CDMA phones.
                            // Return null if device ID is not available.

                            String id = telephonyManager.getDeviceId();

                            if (!TextUtils.isEmpty(id)) {
                                if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
                                    ids.put("meid", id);
                                } else {
                                    ids.put("imei", id);
                                }
                            }

                            Class<?> clazz = Class.forName("android.telephony.TelephonyManager");
                            Method method = clazz.getMethod("getDeviceId", int.class);
                            method.setAccessible(true);

                            Object imei1 = method.invoke(telephonyManager, 1);
                            if (imei1 != null) {
                                ids.put("imei1", imei1);
                            }
                            Object imei2 = method.invoke(telephonyManager, 2);
                            if (imei2 != null) {
                                ids.put("imei2", imei2);
                            }
                        } else {
                            //the IMEI for GSM and the MEID or ESN for CDMA phones.
                            // Return null if device ID is not available.
                            //GSM的手机上返回的是IMEI，而在CDMA 手机上返回的是MEID或者ESN
                            String id = telephonyManager.getDeviceId();
                            if (!TextUtils.isEmpty(id)) {
                                if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
                                    ids.put("meid", id);
                                } else {
                                    ids.put("imei", id);
                                }
                            }
                        }
                    }
                } catch (Throwable ignore) {
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        //try fill android id
        try {
            ids.put("android_id", Settings.System.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

}
