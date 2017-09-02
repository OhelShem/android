package com.ohelshem.app.android.utils.view

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

class NoScrollViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {
    private var pagingEnabled: Boolean = false

    override fun onTouchEvent(event: MotionEvent): Boolean = this.pagingEnabled && super.onTouchEvent(event)

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean = this.pagingEnabled && super.onInterceptTouchEvent(event)
}