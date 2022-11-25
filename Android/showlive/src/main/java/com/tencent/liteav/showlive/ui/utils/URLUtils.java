package com.tencent.liteav.showlive.ui.utils;

import com.tencent.liteav.debug.GenerateTestUserSig;
import com.tencent.qcloud.tuicore.TUILogin;

import java.io.File;

/**
 * MLVB 移动直播地址生成
 * 详情请参考：「https://cloud.tencent.com/document/product/454/7915」
 * <p>
 * <p>
 * Generating Streaming URLs
 * See [https://cloud.tencent.com/document/product/454/7915].
 */
public class URLUtils {

    public static final String WEBRTC      = "webrtc://";
    public static final String RTMP        = "rtmp://";
    public static final String HTTP        = "http://";
    public static final String TRTC        = "trtc://";
    public static final String TRTC_DOMAIN = "qpush.zgfznews.com";
    public static final String RTMP_DOMAIN = "qpush.zgfznews.com";
    public static final String APP_NAME    = "live";

    /**
     * 生成推流地址
     * Generating Publishing URLs
     *
     * @param streamId
     * @param type
     * @return
     */
    public static String generatePushUrl(String streamId, PushType type) {
        String pushUrl = "";
        if (type == PushType.RTC) {
            pushUrl =
                    TRTC + TRTC_DOMAIN +  File.separator +APP_NAME +  File.separator+ streamId +
                            "?sdkappid=" + TUILogin.getSdkAppId() + "&userid="
                    + TUILogin.getUserId() + "&usersig=" + TUILogin.getUserSig();
        }else if (type ==PushType.RTMP){
            pushUrl =
                    RTMP + RTMP_DOMAIN + File.separator + APP_NAME+  File.separator + streamId + "?sdkappid=" + TUILogin.getSdkAppId() + "&userid="
                    + TUILogin.getUserId() + "&usersig=" + TUILogin.getUserSig();
        }
        return pushUrl;
    }

    /**
     * 生成拉流地址
     * Generating Playback URLs
     *
     * @param streamId
     * @param type
     * @return
     */
    public static String generatePlayUrl(String streamId, PlayType type) {
        String playUrl = "";
        if (type == PlayType.RTC) {
            playUrl = "trtc://cloud.tencent.com/play/" + streamId + "?sdkappid=" + TUILogin.getSdkAppId() + "&userid="
                    + TUILogin.getUserId() + "&usersig=" + TUILogin.getUserSig();
        } else if (type == PlayType.RTMP) {
            playUrl = RTMP + GenerateTestUserSig.PLAY_DOMAIN + File.separator + APP_NAME + File.separator + streamId;
        } else if (type == PlayType.HTTP) {
            playUrl = HTTP + GenerateTestUserSig.PLAY_DOMAIN + File.separator + APP_NAME + File.separator + streamId
                    + ".flv";
        } else if (type == PlayType.WEBRTC) {
            playUrl = WEBRTC + GenerateTestUserSig.PLAY_DOMAIN + File.separator + APP_NAME + File.separator + streamId;
        }
        return playUrl;
    }

    public enum PushType {
        RTC,
        RTMP
    }


    public enum PlayType {
        RTC,
        RTMP,
        HTTP,
        WEBRTC
    }
}
