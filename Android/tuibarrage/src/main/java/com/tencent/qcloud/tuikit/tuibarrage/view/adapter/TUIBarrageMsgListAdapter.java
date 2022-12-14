package com.tencent.qcloud.tuikit.tuibarrage.view.adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.tencent.qcloud.tuikit.tuibarrage.R;

import java.util.List;

public class TUIBarrageMsgListAdapter extends RecyclerView.Adapter<TUIBarrageMsgListAdapter.ViewHolder> {
    private static final String TAG = "TUIBarrageMsgListAdapter";

    private List<TUIBarrageMsgEntity> mMsgEntityList;
    private OnItemClickListener       mOnItemClickListener;

    public TUIBarrageMsgListAdapter(Context context, List<TUIBarrageMsgEntity> msgEntityList,
                                    OnItemClickListener onItemClickListener) {
        this.mMsgEntityList = msgEntityList;
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.tuibarrage_item_msg, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TUIBarrageMsgEntity item = mMsgEntityList.get(position);
        holder.bind(item, mOnItemClickListener);
    }

    @Override
    public int getItemCount() {
        return mMsgEntityList.size();
    }

    public interface OnItemClickListener {
        void onAgreeClick(int position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvMsgContent;
        private TextView mBtnMsgAgree;

        public ViewHolder(View itemView) {
            super(itemView);
            initView(itemView);
        }

        private void initView(View itemView) {
            mTvMsgContent = (TextView) itemView.findViewById(R.id.tv_msg_content);
            mBtnMsgAgree = (TextView) itemView.findViewById(R.id.btn_msg_agree);
        }

        public void bind(final TUIBarrageMsgEntity model,
                         final OnItemClickListener listener) {
            String userName = TextUtils.isEmpty(model.userName) ? model.userId : model.userName;
            SpannableStringBuilder builder = null;
            String result;
            ForegroundColorSpan redSpan = new ForegroundColorSpan(model.color);
            if (model.type==0){//弹幕消息
                result = userName + ": " + model.content;
                builder = new SpannableStringBuilder(result);
                if (TextUtils.isEmpty(userName)) {
                    userName = "";
                }
                builder.setSpan(redSpan, 0, userName.length()+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }else if (model.type==1){//进入直播间提醒
                result = userName + " " + model.content;
                builder = new SpannableStringBuilder(result);
                if (TextUtils.isEmpty(userName)) {
                    userName = "";
                }
                builder.setSpan(redSpan, 0, userName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }else if (model.type==2){//公告
                result =  model.content;
                builder = new SpannableStringBuilder(result);
                builder.setSpan(redSpan, 0, result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            mTvMsgContent.setText(builder);
            mTvMsgContent.setBackgroundResource(R.drawable.tuibarrage_bg_msg_item);

            mBtnMsgAgree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onAgreeClick(getLayoutPosition());
                    }
                }
            });
        }
    }
}