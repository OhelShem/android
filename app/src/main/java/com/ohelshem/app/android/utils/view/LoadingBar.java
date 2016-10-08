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
    private float mPadding = 5f;

    private float eatErWidth = 60f;
    private float eatErPositionX = 0f;
    int eatSpeed = 5;


    private float mAngle = 34;
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

    RectF animationRect = new RectF(0,0,0,0);

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
        startViewAnim(0f, 1f, 3500);
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

    ValueAnimator valueAnimator = null;

    private ValueAnimator startViewAnim(float startF, final float endF, long time) {
        valueAnimator = ValueAnimator.ofFloat(startF, endF);
        valueAnimator.setDuration(time);
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
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
            }
        });
        if (!valueAnimator.isRunning()) {
            valueAnimator.start();

        }

        return valueAnimator;
    }


}