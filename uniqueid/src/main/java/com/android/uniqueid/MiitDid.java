package com.android.uniqueid;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.bun.miitmdid.core.ErrorCode;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.supplier.IIdentifierListener;
import com.bun.supplier.IdSupplier;

class MiitDid {

    public interface OnMiitCallback {
        void call(int result, String oaid, String vaid, String aaid);
    }

    public static void detect(final Context context, final OnMiitCallback callback) {

        if (!MiitManager.status) {
            Log.d(MiitManager.TAG, "detect failed! init can't get OK");
            callback.call(-999, null, null, null);
            return;
        }

        try {
            long start = SystemClock.elapsedRealtime();

            int nres = MdidSdkHelper.InitSdk(context, true, new IIdentifierListener() {

                @Override
                public void OnSupport(boolean isSupport, IdSupplier _supplier) {
                    String oaid = null, vaid = null, aaid = null;
                    try {
                        if (_supplier != null && _supplier.isSupported()) {

                            oaid = _supplier.getOAID();
                            vaid = _supplier.getVAID();
                            aaid = _supplier.getAAID();

                            Log.d(MiitManager.TAG, "OAID: " + oaid);
                            Log.d(MiitManager.TAG, "VAID: " + vaid);
                            Log.d(MiitManager.TAG, "AAID: " + aaid);
                            return;
                        } else {
                            Log.d(MiitManager.TAG, "IdSupplier support " + (_supplier == null ? "null" : _supplier.isSupported()));
                        }
                        if (_supplier != null) {
                            //_supplier.shutDown();
                        }
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    } finally {
                        callback.call(0, oaid, vaid, aaid);
                    }
                }
            });

            if (nres == ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT) {
                //不支持的设备
                Log.e(MiitManager.TAG, "不支持的设备");
            } else if (nres == ErrorCode.INIT_ERROR_LOAD_CONFIGFILE) {//加载配置文件出错
                Log.e(MiitManager.TAG, "加载配置文件出错");
            } else if (nres == ErrorCode.INIT_ERROR_MANUFACTURER_NOSUPPORT) {//不支持的设备厂商
                Log.e(MiitManager.TAG, "不支持的设备厂商");
            } else if (nres == ErrorCode.INIT_ERROR_RESULT_DELAY) {//获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程
                Log.e(MiitManager.TAG, "INIT_ERROR_RESULT_DELAY");
            } else if (nres == ErrorCode.INIT_HELPER_CALL_ERROR) {//反射调用出错
                Log.e(MiitManager.TAG, "INIT_HELPER_CALL_ERROR");
            } else {
                Log.e(MiitManager.TAG, "INIT_ERROR:" + nres);
            }

            if (nres != ErrorCode.INIT_ERROR_RESULT_DELAY && nres != 0) {
                callback.call(nres, null, null, null);
            }

            long end = SystemClock.elapsedRealtime();
            Log.d(MiitManager.TAG, end - start + " ms");

        } catch (Throwable throwable) {
            throwable.printStackTrace();
            callback.call(-100, null, null, null);
        }
    }

}
