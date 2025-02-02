package com.tencent.qcloud.tuikit.tuibarrage.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.tencent.qcloud.tuikit.tuibarrage.R;
import com.tencent.qcloud.tuikit.tuibarrage.model.TUIBarrageConstants;
import com.tencent.qcloud.tuikit.tuibarrage.model.TUIBarrageModel;
import com.tencent.qcloud.tuikit.tuibarrage.presenter.ITUIBarragePresenter;
import com.tencent.qcloud.tuikit.tuibarrage.presenter.TUIBarragePresenter;
import com.tencent.qcloud.tuikit.tuibarrage.view.adapter.TUIBarrageMsgEntity;
import com.tencent.qcloud.tuikit.tuibarrage.view.adapter.TUIBarrageMsgListAdapter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 弹幕显示界面
 */
public class TUIBarrageDisplayView extends FrameLayout implements ITUIBarrageDisplayView {
    private static final String TAG = "TUIBarrageDisplayView";

    private Context mContext;
    private RecyclerView mRecyclerMsg;
    private TUIBarrageMsgListAdapter mAdapter;
    private ITUIBarragePresenter mPresenter;
    private ArrayList<TUIBarrageMsgEntity> mMsgList;
    private String mGroupId;

    public TUIBarrageDisplayView(Context context) {
        super(context);
    }

    public TUIBarrageDisplayView(Context context, String groupId) {
        this(context);
        this.mContext = context;
        this.mGroupId = groupId;
        initView(context);
        initPresenter();
    }

    private void initPresenter() {
        mPresenter = new TUIBarragePresenter(mContext, mGroupId);
        mPresenter.initDisplayView(this);
    }

    private void initView(Context context) {
        View baseView = LayoutInflater.from(context).inflate(R.layout.tuibarrage_view_display, this);
        mRecyclerMsg = findViewById(R.id.rv_msg);
        mMsgList = new ArrayList<>();
        //弹幕暂时不需要处理点击事件,因此点击回调设置为空
        mAdapter = new TUIBarrageMsgListAdapter(context, mMsgList, null);
        mRecyclerMsg.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerMsg.setHasFixedSize(true);

//设置嵌套滑动是否能用
        mRecyclerMsg.setNestedScrollingEnabled(false);
        mRecyclerMsg.setAdapter(mAdapter);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mPresenter != null) {
            mPresenter.destroyPresenter();
        }
        super.onDetachedFromWindow();
    }

    public void showBarrage(HashMap<String, Object> data) {
        if (data == null||data.size()==0) {
            Log.d(TAG, "showBarrage data is empty");
            return;
        }
        TUIBarrageModel tuiBarrageModel = new TUIBarrageModel();
        tuiBarrageModel.message= data.get("message")==null?null:String.valueOf(data.get("message"));
        Object extInfo = data.get("extInfo");
        if (extInfo !=null&&extInfo instanceof  HashMap){
            tuiBarrageModel.extInfo= (HashMap<String, String>) extInfo;
        }
        receiveBarrage(tuiBarrageModel);
    }

    public void showMeBarrage(Map<String, Object> data) {
        if (data == null||data.size()==0) {
            Log.d(TAG, "showMeBarrage model is empty");
            return;
        }
        TUIBarrageModel tuiBarrageModel = new TUIBarrageModel();
        tuiBarrageModel.message= String.valueOf(data.get("message"));
        Object extInfo = data.get("extInfo");
        if (extInfo !=null&&extInfo instanceof  HashMap){
            tuiBarrageModel.extInfo= (HashMap<String, String>) extInfo;
//            tuiBarrageModel.extInfo.put(TUIBarrageConstants.KEY_USER_NAME, mContext.getString(R.string.tuibarrage_me));
        }
        receiveBarrage(tuiBarrageModel);
    }

    @Override
    public void receiveBarrage(TUIBarrageModel model) {
        if (model == null) {
            Log.d(TAG, "receiveBarrage model is empty");
            return;
        }

        String message = model.message;
        Log.d(TAG, "receiveBarrage message = " + message);
        if (message.length() == 0) {
            Log.d(TAG, "receiveBarrage message is empty");
            return;
        }
        String name = model.extInfo.get(TUIBarrageConstants.KEY_USER_NAME);
        String userId = model.extInfo.get(TUIBarrageConstants.KEY_USER_ID);
        String typeString = model.extInfo.get(TUIBarrageConstants.KEY_MSG_TYPE);
        Integer type = !TextUtils.isEmpty(typeString)?Integer.valueOf(typeString):0;
        String userName = TextUtils.isEmpty(name) ? userId : name;

        TUIBarrageMsgEntity entity = new TUIBarrageMsgEntity();
        entity.userName = userName;
        entity.content = message;
        entity.type=type;

        //用户名显示随机的颜色值
        entity.color =
                mContext.getResources().getColor(TUIBarrageConstants.MESSAGE_USERNAME_COLOR[2]);

        //接收到弹幕后,更新显示界面
        mMsgList.add(entity);
        mAdapter.notifyDataSetChanged();
        mRecyclerMsg.smoothScrollToPosition(mAdapter.getItemCount());
    }
}
