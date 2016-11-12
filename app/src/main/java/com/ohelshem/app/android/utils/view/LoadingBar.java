package com.ohelshem.app.android.utils.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class LoadingBar extends View {

    private Paint mPaint, mPaintEye;

    private float mWidth = 0f;
    private float mHigh = 0f;
    private final float mPadding = 5f;

    private final float eatErWidth = 60f;
    private float eatErPositionX = 0f;
    private final int eatSpeed = 5;


    private final float mAngle = 34;
    private float eatErStartAngle = mAngle;
    private float eatErEndAngle = 360 - 2 * eatErStartAngle;


    public LoadingBar(Context context) {
        this(context, null);
    }

    public LoadingBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = getMeasuredWidth();
        mHigh = getMeasuredHeight();
    }

    private final RectF animationRect = new RectF(0,0,0,0);

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float eatRightX = mPadding + eatErWidth + eatErPositionX;
        RectF rectF = animationRect;
        rectF.left = mPadding + eatErPositionX;
        rectF.top = mHigh / 2 - eatErWidth / 2;
        rectF.right = eatRightX;
        rectF.bottom = mHigh / 2 + eatErWidth / 2;

        canvas.drawArc(rectF, eatErStartAngle, eatErEndAngle
                , true, mPaint);
        float beansWidth = 10f;
        canvas.drawCircle(mPadding + eatErPositionX + eatErWidth / 2,
                mHigh / 2 - eatErWidth / 4,
                beansWidth / 2, mPaintEye);

        int beansCount = (int) ((mWidth - mPadding * 2 - eatErWidth) / beansWidth / 2);
        for (int i = 0; i < beansCount; i++) {

            float x = beansCount * i + beansWidth / 2 + mPadding + eatErWidth;
            if (x > eatRightX) {
                canvas.drawCircle(x,
                        mHigh / 2, beansWidth / 2, mPaint);
            }
        }


    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);

        mPaintEye = new Paint();
        mPaintEye.setAntiAlias(true);
        mPaintEye.setStyle(Paint.Style.FILL);
        mPaintEye.setColor(Color.BLACK);

    }

    public void startAnim() {
        stopAnim();
        startViewAnim();
    }

    public void stopAnim() {
        if (valueAnimator != null) {
            clearAnimation();
            valueAnimator.setRepeatCount(0);
            valueAnimator.cancel();
            valueAnimator.end();
            eatErPositionX = 0;
            postInvalidate();
        }
    }

    private ValueAnimator valueAnimator = null;

    private void startViewAnim() {
        valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.setDuration((long) 3500);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);//无限循环
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                float mAnimatedValue = (float) valueAnimator.getAnimatedValue();
                eatErPositionX = (mWidth - 2 * mPadding - eatErWidth) * mAnimatedValue;
                eatErStartAngle = mAngle * (1 - (mAnimatedValue * eatSpeed - (int) (mAnimatedValue * eatSpeed)));
                eatErEndAngle = 360 - eatErStartAngle * 2;
                invalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                if (onAnimationRepeatListener != null) onAnimationRepeatListener.run();
            }
        });
        if (!valueAnimator.isRunning()) {
            valueAnimator.start();

        }
    }

    private Runnable onAnimationRepeatListener;

    public void onRepeat(Runnable onRepeat) {
        this.onAnimationRepeatListener = onRepeat;
    }


}