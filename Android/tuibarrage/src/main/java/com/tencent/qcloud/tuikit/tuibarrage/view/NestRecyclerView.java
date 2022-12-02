package com.tencent.qcloud.tuikit.tuibarrage.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @Modified by 王骏杰
 * @data 2019/8/18
 * @des 解决RecyclerView嵌套ecyclerView竖向滑动冲突
 */
public class NestRecyclerView extends RecyclerView {
 
    private int lastVisibleItemPosition;
    private int firstVisibleItemPosition;
    private float mLastY = 0;// 记录上次Y位置
    private boolean isTopToBottom = false;
    private boolean isBottomToTop = false;
    public NestRecyclerView(Context context) {
        this(context, null);
    }
 
    public NestRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
 
    public NestRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
 
    /*@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }*/
 
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        Log.d("TAG", "dispatchTouchEvent: "+action);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastY = event.getY();
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_SCROLL:
                float nowY = event.getY();
                isIntercept(nowY);
                mLastY = nowY;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(true);
//                float nowY1 = event.getY();
//                isIntercept(nowY1);
                break;
            default:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
        }
        return super.dispatchTouchEvent(event);
    }
 
    private void isIntercept(float nowY){
 
        isTopToBottom = false;
        isBottomToTop = false;
 
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            //得到当前界面，最后一个子视图对应的position
            lastVisibleItemPosition = ((GridLayoutManager) layoutManager)
                    .findLastVisibleItemPosition();
            //得到当前界面，第一个子视图的position
            firstVisibleItemPosition = ((GridLayoutManager) layoutManager)
                    .findFirstVisibleItemPosition();
        }else if (layoutManager instanceof LinearLayoutManager){
            //得到当前界面，最后一个子视图对应的position
            lastVisibleItemPosition = ((LinearLayoutManager) layoutManager)
                    .findLastVisibleItemPosition();
            //得到当前界面，第一个子视图的position
            firstVisibleItemPosition = ((LinearLayoutManager) layoutManager)
                    .findFirstVisibleItemPosition();
        }
        //得到当前界面可见数据的大小
        int visibleItemCount = layoutManager.getChildCount();
        Log.d("nestScrolling","onScrollStateChanged");
        if (visibleItemCount>0) {

            /**
             * 注意这里有非常关键的两点，也是我修改完善之前哥们博客的有坑的两点，
             * 第一点是canScrollVertically传的正负值问题，判断向上用正值1，向下则反过来用负值-1，
             * 第二点是canScrollVertically返回值的问题，true时是代表可以滑动，false时才代表划到顶部或者底部不可以再滑动了，所以这个判断前要加逻辑非!运算符
             * 补充了这两点基本效果就很完美了。
             */
            if (!NestRecyclerView.this.canScrollVertically(1) && nowY < mLastY
                    ||!NestRecyclerView.this.canScrollVertically(-1) && nowY > mLastY) {
                // 不能向上滑动
                Log.d("nestScrolling", "不能滑动");
                isBottomToTop = true;
                getParent().requestDisallowInterceptTouchEvent(false);
            }else{
                Log.d("nestScrolling", "滑动");
                getParent().requestDisallowInterceptTouchEvent(true);
            }
        }
    }
 
}