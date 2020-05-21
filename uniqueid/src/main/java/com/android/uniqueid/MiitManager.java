package com.android.uniqueid;

import android.content.Context;

import com.bun.miitmdid.core.JLibrary;

/**
 * 设备唯一标识符(UDID):设备唯一硬件标识，设备生产时根据特定的硬件 信息生成，可用于设备的生产环境及合法性校验。不对第三方应用提供获取接口， 无法通过 SDK 获取。
 * 匿名设备标识符(OAID):可以连接所有应用数据的标识符，移动智能终端 系统首次启动后立即生成，可用于广告业务。可以通过 SDK 获取到接口状态(重 置、关闭)、ID 值。
 * 开发者匿名设备标识符(VAID):用于开放给开发者的设备标识符，可在应 用安装时产生，可用于同一开发者不同应用之间的推荐。可以通过 SDK 获取到 ID值。
 * 应用匿名设备标识符(AAID):第三方应用获取的匿名设备标识，可在应用 安装时产生，可用于用户统计等。可以通过 SDK 获取到 ID 值。
 * oaid 提供给用户关闭和重置的权利，重置后 oaid 值会改变，但通过调研统 计正常情况下重置率很低。
 * oaid 关闭后情况，不同终端企业逻辑不同，一种是跟 iOS 一样，关闭后不可 获取 oaid(返回 null 或全 0)，
 * 另一种是跟 Android 一样，关闭后告知已重置并 可获取重置后的 oaid(此种显示的是禁止跟踪等说法)，
 * 当前两种方法都均存在， 但第二种风险较大不排除后续会有限制的可能。同时，如果选用第二种方案要求 开发者不可将获取到的 ID 用于追踪用户，如果发现违反会有相关处理。
 */
class MiitManager {

    static final String TAG = "MiitDid";
    private static final String JLibraryStatus = "RETURN_OK";

    static boolean status = true;

    static void attachBaseContext(Context base) {
        try {
            Object object = JLibrary.InitEntry(base);
            status = object != null && JLibraryStatus.equals(object.toString());
        } catch (Throwable t) {
            t.printStackTrace();
            status = false;
        }
    }

    ///huawei
    //UDID:
    //OAID: fb373fe1-ffea-043f-dbb7-ffefffff7275
    //VAID:
    //AAID: EC97CE7A10A1369F3D00820E117E359E

    //vivo
    //UDID:
    //OAID: 3018dfe5a647f8272e3914137cc3e319ab572b74901d87df54f271749ee3253e
    //VAID: 2fe2174105b44498f5b7d9cf8659b31ad2dffccbd14afe23a1a3d7a3a0195de6
    //AAID: df585cc30119d105b4daa815e10d75d57077a07ba3b51d114389b7f038255849
}
