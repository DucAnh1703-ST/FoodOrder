package com.example.foodorder.listener

import android.os.SystemClock
import android.view.View

abstract class IOnSingleClickListener : View.OnClickListener {

    private var mLastClickTime: Long = 0

    abstract fun onSingleClick(v: View?)

    override fun onClick(v: View) {
        val currentClickTime = SystemClock.uptimeMillis()
        val elapsedTime = currentClickTime - mLastClickTime
        mLastClickTime = currentClickTime
        if (elapsedTime <= MIN_CLICK_INTERVAL) return
        onSingleClick(v)
    }

    companion object {
        private const val MIN_CLICK_INTERVAL: Long = 600
    }
}