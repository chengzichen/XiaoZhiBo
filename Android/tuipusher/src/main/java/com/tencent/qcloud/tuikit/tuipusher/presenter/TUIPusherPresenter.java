package com.tencent.qcloud.tuikit.tuipusher.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.tencent.liteav.audio.TXAudioEffectManager;
import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.beauty.TXBeautyManager;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.qcloud.tuicore.TUILogin;
import com.tencent.qcloud.tuicore.util.ToastUtil;
import com.tencent.qcloud.tuikit.tuipusher.R;
import com.tencent.qcloud.tuikit.tuipusher.model.TUIPusherCallback;
import com.tencent.qcloud.tuikit.tuipusher.model.bean.im.InvitationReqBean;
import com.tencent.qcloud.tuikit.tuipusher.model.bean.im.InvitationResBean;
import com.tencent.qcloud.tuikit.tuipusher.model.listener.ITUIPusherSignallingListener;
import com.tencent.qcloud.tuikit.tuipusher.model.listener.ITUIPusherStreamListener;
import com.tencent.qcloud.tuikit.tuipusher.model.service.ITUIPusherSignallingService;
import com.tencent.qcloud.tuikit.tuipusher.model.service.ITUIPusherStreamService;
import com.tencent.qcloud.tuikit.tuipusher.model.service.impl.TUIPusherSignallingService;
import com.tencent.qcloud.tuikit.tuipusher.model.service.impl.TUIPusherStreamService;
import com.tencent.qcloud.tuikit.tuipusher.model.utils.LinkURLUtils;
import com.tencent.qcloud.tuikit.tuipusher.view.ITUIPusherView;
import com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView;
import com.tencent.rtmp.ui.TXCloudVideoView;

import static com.tencent.live2.V2TXLiveCode.V2TXLIVE_OK;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingCMD.CMD_JOIN_ANCHOR_REQ;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingCMD.CMD_PK_REQ;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingCode.IM_PK_STOP_FAIL;
import static com.tencent.qcloud.tuikit.tuipusher.model.constant.IMProtocol.SignallingCode.IM_PK_STOP_SUCCESS;
import static com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView.LinkState.LINK_CANCEL;
import static com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView.LinkState.LINK_REQ;
import static com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView.LinkState.LINK_START;
import static com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView.LinkState.LINK_STOP;
import static com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView.LinkState.LINK_TIMEOUT;
import static com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView.PKState.PK_ACCAPT;
import static com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView.PKState.PK_CANCEL;
import static com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView.PKState.PK_RECEIVE_REQ;
import static com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView.PKState.PK_REJECT;
import static com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView.PKState.PK_STOP;
import static com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView.PKState.PK_STSRT;
import static com.tencent.qcloud.tuikit.tuipusher.view.TUIPusherView.PKState.PK_TIMEOUT;

public class TUIPusherPresenter implements ITUIPusherPresenter, ITUIPusherStreamListener, ITUIPusherSignallingListener {
    private static final String TAG = "TUIPusherPresent";

    private ITUIPusherSignallingService mSignallingService;
    private ITUIPusherStreamService     mStreamService;
    private TUIPresenterListener        mListener;
    private ITUIPusherView              mTUIPusherView;
    private Context                     mContext;
    private boolean                     mIsPushing;
    private String                      mStreamId;
    private String                      mPKStreamId;
    private String                      mPKUserId;
    private String                      mPKInvteId;
    private String                      mLinkStreamId;
    private String                      mLinkUserId;
    private String                      mLinkInviteId;
    private String                      mPushUrl;
    private LinkURLUtils.PushType       mPushType;

    public TUIPusherPresenter(TUIPusherView pusherView, Context context, LinkURLUtils.PushType pushType) {
        mPushType = pushType;
        mContext = context;
        mSignallingService = new TUIPusherSignallingService(mContext);
        mSignallingService.setListener(this);
        mStreamService = new TUIPusherStreamService(mContext, mPushType);
        mStreamService.setListener(this);
        mTUIPusherView = pusherView;
    }

    @Override
    public void startPreview(boolean isFront, TXCloudVideoView view) {
        mStreamService.startCameraPreview(isFront, view);
    }

    @Override
    public void startVirtualCamera(Bitmap bitmap) {
        mStreamService.startVirtualCamera(bitmap);
    }

    @Override
    public void stopVirtualCamera() {
        mStreamService.stopVirtualCamera();
    }

    @Override
    public void startPush(String url) {
        TXCLog.i(TAG, "startPush url:" + url);
        mPushUrl = url;
        if (mIsPushing) {
            if (mListener != null) {
                mListener.onToastMessage(mContext.getResources().getString(R.string.tuipusher_pushing_toast));
            }
            return;
        }
        mStreamId = TUILogin.getUserId();
        mSignallingService.addSignalingListener(new TUIPusherCallback() {
            @Override
            public void onResult(int code, String message) {
                TXCLog.i(TAG, "createGroup code:" + code + " , message: " + message);
                if (code == 0) {
                    int ret = mStreamService.startPush(mPushUrl);
                    if (ret == 0) {
                        mListener.onNotifyState(TUIPusherView.State.PUSH_SUCCESS);
                    }
                    TXCLog.i(TAG, "pushUrl:" + mPushUrl + ", ret: " + ret);
                    mIsPushing = true;
                } else {
                    if (mListener != null) {
                        mListener.onToastMessage(message);
                    }
                }
            }
        });
    }

    @Override
    public void stopPush() {
        TXCLog.i(TAG, "stopPush");
    }

    @Override
    public boolean requestPK(String userId) {
        TXCLog.i(TAG, "requestPK userId:" + userId);
        mPKInvteId = mSignallingService.requestPK(TUILogin.getUserId(), userId, 15);
        if (TextUtils.isEmpty(mPKInvteId)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void cancelPK() {
        TXCLog.i(TAG, "cancelPK");
        mSignallingService.cancelPK(mPKInvteId, mStreamId);
        resetPKData();
    }

    @Override
    public void responsePK(boolean agree, String reason, int timeout) {
        TXCLog.i(TAG, "responsePK agree:" + agree + ", resson:" + reason);
        mSignallingService.responsePK(mPKInvteId, mStreamId, agree, reason, timeout);
        if (!agree) {
            resetPKData();
        }
    }

    @Override
    public void stopPK() {
        TXCLog.i(TAG, "stopPK");
        mSignallingService.stopPK(mStreamId, mPKUserId, 15);
        resetPKData();
    }

    @Override
    public void responseLink(boolean agree, String reason, int timeout) {
        TXCLog.i(TAG, "responseLink agree:" + agree + ", resson:" + reason);
        mSignallingService.responseLink(mLinkInviteId, mStreamId, agree, reason, timeout);
        if (!agree) {
            resetLinkData();
        }
    }

    @Override
    public void destory() {
        TXCLog.i(TAG, "destory");
        mStreamService.destory();
        mSignallingService.destory();
    }

    @Override
    public void startPK(TXCloudVideoView pkVideoView) {
        TXCLog.i(TAG, "startPK");
        int ret = mStreamService.startPlay(pkVideoView, mPKStreamId, TUILogin.getUserId());
        int result = mStreamService.setPKMixTranscodingConfig(TUILogin.getUserId(), mStreamId + "", mPKStreamId, mPKUserId);
        if (result == V2TXLIVE_OK) {
            mListener.onNotifyPKState(PK_STSRT, mPKUserId, "");
        } else {
            mListener.onToastMessage(mContext.getResources().getString(R.string.tuipusher_start_pk_fail));
        }
    }

    @Override
    public void setMirror(boolean isMirror) {
        TXCLog.i(TAG, "setMirror isMirror:" + isMirror);
        mStreamService.setMirror(isMirror);
    }

    @Override
    public void setResolution(int resolution) {
        TXCLog.i(TAG, "setResolution resolution:" + resolution);
        if (mStreamService != null) {
            V2TXLiveDef.V2TXLiveVideoResolution resolutionFlag = V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution640x360;
            if (resolution == 1) {
                resolutionFlag = V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution640x360;
            } else if (resolution == 2) {
                resolutionFlag = V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution960x540;
            } else if (resolution == 3) {
                resolutionFlag = V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution1280x720;
            } else if (resolution == 4) {
                resolutionFlag = V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution1920x1080;
            }
            V2TXLiveDef.V2TXLiveVideoEncoderParam param = new V2TXLiveDef.V2TXLiveVideoEncoderParam(resolutionFlag);
            param.videoResolutionMode = V2TXLiveDef.V2TXLiveVideoResolutionMode.V2TXLiveVideoResolutionModePortrait;
            mStreamService.setVideoQuality(param);
        }
    }

    public void setListener(TUIPresenterListener listener) {
        mListener = listener;
    }

    @Override
    public void switchCamera(boolean isFrontCamera) {
        TXCLog.i(TAG, "switchCamera isFrontCamera:" + isFrontCamera);
        mStreamService.switchCamera(isFrontCamera);
    }

    @Override
    public void startLink(TXCloudVideoView videoView) {
        TXCLog.i(TAG, "startLink");
        mStreamService.startPlay(videoView, mLinkStreamId, TUILogin.getUserId());
        int result = mStreamService.setLinkMixTranscodingConfig(TUILogin.getUserId(), mStreamId + "", mLinkUserId, mLinkUserId);
        if (result == V2TXLIVE_OK) {
            mListener.onNotifyLinkState(LINK_START, mLinkUserId, "");
        } else {
            mListener.onToastMessage(mContext.getResources().getString(R.string.tuipusher_start_link_fail));
        }
    }

    @Override
    public void stopLink(int timeout) {
        TXCLog.i(TAG, "stopLink");
        mSignallingService.stopLink(mStreamId, mLinkUserId, timeout);
        mStreamService.stopPlay();
        mStreamService.setLinkMixTranscodingConfig("", "", "", "");
        resetLinkData();
    }

    @Override
    public void onCommonResult(int code, String message) {
        TXCLog.i(TAG, "onCommonResult code :" + code + ", message" + message);
        if (code == IM_PK_STOP_SUCCESS) {
            mStreamService.stopPlay();
            mStreamService.setPKMixTranscodingConfig("", "", "", "");
        } else if (code == IM_PK_STOP_FAIL) {
            ToastUtil.toastShortMessage(mContext.getString(R.string.tuipusher_stop_pk_error));
        }
    }

    @Override
    public void onRequestPK(@NonNull InvitationReqBean bean) {
        TXCLog.i(TAG, "onRequestPK");
        if (!TextUtils.isEmpty(mPKInvteId) && CMD_PK_REQ.equals(bean.getData().getData().getCmd())) {
            //已经收到了未处理的PK请求，抛弃新的请求
            mSignallingService.responsePK(bean.getInviteID(), mStreamId, false, TUIPusherSignallingService.RejectReason.BUSY.getReason(), 15);
            return;
        }
        mPKInvteId = bean.getInviteID();
        mPKUserId = bean.getInviter();
        mPKStreamId = bean.getData().getData().getStreamID();
        if (mListener != null) {
            mListener.onNotifyPKState(PK_RECEIVE_REQ, mPKUserId, "");
        }
    }

    @Override
    public void onStopPK() {
        TXCLog.i(TAG, "onStopPK");
        resetPKData();
        mStreamService.stopPlay();
        mStreamService.setPKMixTranscodingConfig("", "", "", "");
        if (mListener != null) {
            mListener.onNotifyPKState(PK_STOP, mPKUserId, "");
        }
    }

    @Override
    public void onRequestJoinAnchor(InvitationReqBean bean) {
        TXCLog.i(TAG, "onRequestJoinAnchor");
        if (!TextUtils.isEmpty(mLinkInviteId) && CMD_JOIN_ANCHOR_REQ.equals(bean.getData().getData().getCmd())) {    //已经收到了未处理的连麦请求，抛弃新的请求
            mSignallingService.responseLink(bean.getInviteID(), mStreamId, false, TUIPusherSignallingService.RejectReason.BUSY.getReason(), 15);
            return;
        }
        mLinkInviteId = bean.getInviteID();
        mLinkUserId = bean.getInviter();
        mLinkStreamId = bean.getData().getData().getStreamID();
        if (mListener != null) {
            mListener.onNotifyLinkState(LINK_REQ, mLinkUserId, bean.getData().getData().getCmdInfo());
        }
    }

    @Override
    public void onResponseLink(InvitationResBean bean, LinkResponseState state) {
        TXCLog.i(TAG, "onResponseLink loginUserId:" + TUILogin.getUserId() + ", inviter:" + bean.getInviter());
        if (TUILogin.getUserId().equals(bean.getInviter())) {
            return;
        }
        switch (state) {
            case CANCEL:
                mListener.onNotifyLinkState(LINK_CANCEL, mLinkUserId, "");
                resetLinkData();
                break;
        }
    }

    @Override
    public void onStartLink(InvitationReqBean bean) {
        TXCLog.i(TAG, "onStartLink mLinkInviteId:" + bean.getInviteID() + ",mLinkUserId:" + bean.getInviter());
        mLinkInviteId = bean.getInviteID();
        mLinkUserId = bean.getInviter();
        mLinkStreamId = bean.getData().getData().getStreamID();

        if (mListener != null) {
            mListener.onStartLink();
        }
    }

    @Override
    public void onStopLink(InvitationReqBean bean) {
        TXCLog.i(TAG, "onStopLink");
        resetLinkData();
        mStreamService.stopPlay();
        mStreamService.setLinkMixTranscodingConfig("", "", "", "");
        if (mListener != null) {
            mListener.onToastMessage(mContext.getString(R.string.tuipusher_audience_stop_link));
            mListener.onNotifyLinkState(LINK_STOP, mLinkUserId, "");
        }
    }

    @Override
    public void onTimeOut(String inviteId) {
        TXCLog.i(TAG, "onTimeOut, inviteId : " + inviteId);
        if (TextUtils.isEmpty(inviteId)) {
            return;
        }
        if (!TextUtils.isEmpty(mPKInvteId) && mPKInvteId.equals(inviteId)) {
            mListener.onNotifyPKState(PK_TIMEOUT, mPKUserId, "");
            resetPKData();
        } else if (!TextUtils.isEmpty(mLinkInviteId) && mLinkInviteId.equals(inviteId)) {
            mListener.onNotifyLinkState(LINK_TIMEOUT, mLinkUserId, "");
            resetLinkData();
        }
    }

    @Override
    public void onResponsePK(InvitationResBean bean, PKResponseState result) {
        TXCLog.i(TAG, "onResponsePK loginUserId:" + TUILogin.getUserId() + ", inviter:" + bean.getInviter());
        if (TUILogin.getUserId().equals(bean.getInviter())) {
            return;
        }
        mPKUserId = bean.getInviter();
        mPKStreamId = bean.getData().getData().getStreamID();
        switch (result) {
            case ACCEPT:
                mListener.onNotifyPKState(PK_ACCAPT, mPKUserId, "");
                break;
            case REJECT:
                mListener.onNotifyPKState(PK_REJECT, mPKUserId, bean.getData().getData().getCmdInfo());
                resetPKData();
                break;
            case CANCEL:
                mListener.onNotifyPKState(PK_CANCEL, mPKUserId, "");
                resetPKData();
                break;
        }
    }

    @Override
    public void onPushSuccess() {
        TXCLog.i(TAG, "onPushSuccess");
    }

    public TXBeautyManager getTXBeautyManager() {
        if (mStreamService != null) {
            return mStreamService.getTXBeautyManager();
        }
        return null;
    }

    public TXAudioEffectManager getTXAudioEffectManager() {
        if (mStreamService != null) {
            return mStreamService.getTXAudioEffectManager();
        }
        return null;
    }

    private void resetLinkData() {
        mLinkInviteId = "";
        mLinkStreamId = "";
        mLinkUserId = "";
    }

    private void resetPKData() {
        mPKInvteId = "";
        mPKStreamId = "";
        mPKUserId = "";
    }

    public interface TUIPresenterListener {
        /**
         * UI 吐司提示
         *
         * @param message
         */
        void onToastMessage(String message);

        /**
         * UI 状态改变通知
         *
         * @param state
         */
        void onNotifyState(TUIPusherView.State state);

        /**
         * PK 状态改变通知
         *
         * @param pkState
         * @param pkUserId
         * @param reason
         */
        void onNotifyPKState(TUIPusherView.PKState pkState, String pkUserId, String reason);

        /**
         * 连麦 状态改变通知
         *
         * @param linkState
         */
        void onNotifyLinkState(TUIPusherView.LinkState linkState, String linkUserId, String reason);

        /**
         * 开始连麦回调
         */
        void onStartLink();
    }
}
