package com.jsbd.btphone.module.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.jsbd.bluetooth.BTController;
import com.jsbd.bluetooth.constant.BluetoothConstants;
import com.jsbd.btphone.R;
import com.jsbd.btphone.config.WeakHandler;

/**
 * Created by QY on 2018/9/1.
 */

public class SwitchButton extends View {
    private static final String TAG = "SwitchButton";

    private int center;             //左圆半径
    private int rec_x;              //矩形x坐标
    private Paint paint;            //画笔
    private int measuredWidth;      //控件宽
    private int measuredHeight;     //控件高
    private int smallCenter;        //小圆半径
    private float smallCenter_x;    //小圆的x坐标
    private Paint smallPaint;       //小圆画笔
    private float startx;           //按下的x坐标
    private float endx;             //移动的结束坐标
    private int mid_x;              //左圆圆心和右圆圆心中间的坐标

    public static final int TO_LEFT = 11;        //往左
    public static final int TO_RIGHT = 22;       //往右
    public static final int TO_RESPONSE = 33;      //点击有效

    private boolean isRight = false;
    private boolean isResponse = true;

    private boolean isAnimate = false;

    //开启滑动动画
    private boolean isOpenAnimation = false;

    private OnMClickListener onClickListener;

    public SwitchButton(Context context) {
        this(context, null);
    }

    public SwitchButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setOnMbClickListener(OnMClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnMClickListener {
        void onClick(boolean isRight);
    }

    private void init() {
        //初始化一些数据
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        smallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //大圆圈的颜色
        paint.setColor(getResources().getColor(R.color.bt_switch_big_circle_rifht_color));
        //小圆圈的颜色
        smallPaint.setColor(getResources().getColor(R.color.bt_switch_small_circle_rifht_color));
//        paint.setColor(getResources().getColor(R.color.bt_switch_big_circle_left_color));
//        smallPaint.setColor(getResources().getColor(R.color.bt_switch_small_circle_left_color));
    }

    @SuppressLint("HandlerLeak")
    WeakHandler handler = new WeakHandler<SwitchButton>(this) {
        @Override
        public void onHandleMessage(SwitchButton self, Message msg) {
            switch (msg.what) {
                case TO_LEFT:
                    paint.setColor(getResources().getColor(R.color.bt_switch_big_circle_left_color));
                    smallPaint.setColor(getResources().getColor(R.color.bt_switch_small_circle_left_color));
                    if (isOpenAnimation) {
                        if (smallCenter_x > center) {
                            smallCenter_x -= 2;
                            handler.sendEmptyMessage(TO_LEFT);
                            isAnimate = true;
                        } else {
                            smallCenter_x = center;
                            //设置滑动不可点击
                            setEnabled(true);
                            isAnimate = false;
                        }
                    } else {
                        smallCenter_x = center;
                        //设置滑动不可点击
                        setEnabled(true);
                        isAnimate = false;
                    }
                    break;
                case TO_RIGHT:
                    paint.setColor(getResources().getColor(R.color.bt_switch_big_circle_rifht_color));
                    smallPaint.setColor(getResources().getColor(R.color.bt_switch_small_circle_rifht_color));
                    if (isOpenAnimation) {
                        if (smallCenter_x < rec_x) {
                            smallCenter_x += 2;
                            handler.sendEmptyMessage(TO_RIGHT);
                            isAnimate = true;
                        } else {
                            smallCenter_x = rec_x;
                            setEnabled(true);
                            isAnimate = false;
                        }
                    } else {
                        smallCenter_x = rec_x;
                        setEnabled(true);
                        isAnimate = false;
                    }
                    break;
                case TO_RESPONSE:
                    isResponse = true;
                    break;
            }

            //重绘
            invalidate();
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measuredHeight = getMeasuredHeight();
        measuredWidth = getMeasuredWidth();
        //得出左圆，长方形，右圆的坐标
        center = measuredHeight / 2;
        //长方形右边的坐标
        rec_x = measuredWidth - center;
        //小圆的半径 = 大圆半径减2
        smallCenter = center - 2;
        //小圆的圆心x坐标一直在变化
        //L.d(TAG, "SwitchButton >> onMeasure >> getState:" + BTUtil.getState());
        /*在这里设置开关的初始状态*/
        if (BTController.getInstance().getState() == BluetoothConstants.STATE_OFF || BTController.getInstance().getState() == BluetoothConstants.STATE_TURNING_OFF) {
            smallCenter_x = center;
        } else {
            smallCenter_x = rec_x;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(center, center, center, paint);                   //左圆
        canvas.drawRect(center, 0, rec_x, measuredHeight, paint);       //矩形
        canvas.drawCircle(rec_x, center, center, paint);                    //右圆
        canvas.drawCircle(smallCenter_x, center, smallCenter, smallPaint);  //小圆
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //开始的x坐标
                startx = event.getX();
                endx = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                float distance = event.getX() - endx;
                smallCenter_x += distance;
                //控制范围
                if (smallCenter_x > rec_x) {
                    isRight = true;
                    smallCenter_x = rec_x;
                    //Right时画笔颜色
                    paint.setColor(getResources().getColor(R.color.bt_switch_big_circle_rifht_color));
                    smallPaint.setColor(getResources().getColor(R.color.bt_switch_small_circle_rifht_color));
                } else if (smallCenter_x < center) {
                    //最左
                    smallCenter_x = center;
                    isRight = false;
                    //Left时画笔颜色
                    paint.setColor(getResources().getColor(R.color.bt_switch_big_circle_left_color));
                    smallPaint.setColor(getResources().getColor(R.color.bt_switch_small_circle_left_color));
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                //分2种情况，1.点击 2.没滑过中点
                //1.点击
                float up_x = event.getX();
                //按下和抬起的距离小于2确定是点击了
                if (Math.abs(up_x - Math.abs(startx)) < 2) {
                    //不在动画的时候可以点击
                    if (!isAnimate) {
                        setSwitchStatus();
                    }
                } else {
                    //2.没滑过中点
                    //滑到中间的x坐标
                    mid_x = (center + (rec_x - center) / 2);
                    if (smallCenter_x < mid_x) {
                        //最左
                        isRight = false;
                        smallCenter_x = center;
                        paint.setColor(getResources().getColor(R.color.bt_switch_big_circle_left_color));
                        smallPaint.setColor(getResources().getColor(R.color.bt_switch_small_circle_left_color));
                        setEnabled(true);
                        invalidate();
                    } else {
                        //最右
                        isRight = true;
                        smallCenter_x = rec_x;
                        paint.setColor(getResources().getColor(R.color.bt_switch_big_circle_rifht_color));
                        smallPaint.setColor(getResources().getColor(R.color.bt_switch_small_circle_rifht_color));
                        setEnabled(true);
                        invalidate();
                    }
                }

                //到了两端都有点击事件
                if (smallCenter_x == rec_x || smallCenter_x == center) {
                    if (onClickListener != null) {
                        onClickListener.onClick(isRight);
                        if (isRight) {
                            isResponse = false;
                            handler.sendEmptyMessageDelayed(TO_RESPONSE, 5000);
                        }
                    }
                }
                break;
        }

        return isResponse;
    }

    //提供方法调用
    public void setSwitchOff() {
        isRight = false;
        handler.sendEmptyMessage(TO_LEFT);
    }

    public void setSwitch0n() {
        isRight = true;
        handler.sendEmptyMessage(TO_RIGHT);
    }

    public void setSwitchStatus() {
        if (isRight) {
            setSwitchOff();
        } else {
            setSwitch0n();
        }
    }

    public boolean isSwitchStatus() {
        return isRight;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeAll();
    }
}

