package com.example.androidtrafficsigndetection.Classes

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.GestureDetectorCompat
import com.example.androidtrafficsigndetection.Activity.MainActivity
import com.example.androidtrafficsigndetection.Activity.SettingActivity
import com.example.androidtrafficsigndetection.DataModel.SettingParams
import com.example.androidtrafficsigndetection.R

@SuppressLint("StaticFieldLeak")
object NavigationDrawerManager {
    private lateinit var navigationBar: LinearLayout
    private lateinit var gestureDetector: GestureDetectorCompat
    private var isVisible = false
    private var downX: Float = 0f
    private var downY: Float = 0f
    private var velocityTracker: VelocityTracker? = null

    fun init(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        navigationBar = inflater.inflate(R.layout.navigation_drawer, null, false) as LinearLayout
        gestureDetector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val deltaX = e2.x - (e1?.x ?: e2.x)
                Log.d("GestureDebug", "DeltaX: $deltaX, VelocityX: $velocityX, isVisible: $isVisible")
                if (deltaX > SettingParams.SLICE_SETTING && velocityX > SettingParams.SLICE_SETTING &&!isVisible) {
                    showNavigationBar()
                    return true
                } else if (deltaX < -SettingParams.SLICE_SETTING && velocityX < -SettingParams.SLICE_SETTING && isVisible) {
                    hideNavigationBar()
                    return true
                }
                return false
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    fun attachToActivity(activity: Activity) {
        val parent = navigationBar.parent
        if (parent is ViewGroup) {
            parent.removeView(navigationBar)
        }

        val rootView = activity.window.decorView.findViewById<FrameLayout>(android.R.id.content)
        rootView.addView(navigationBar)
        hideNavigationBar()
        // 调整宽度
        val displayMetrics = activity.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val menuWidth = (screenWidth * SettingParams.TAB_BAR_PERCENTAGE).toInt()
        navigationBar.layoutParams.width = menuWidth

        rootView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
        setupButtonClickListeners(activity)
    }
    private fun setupButtonClickListeners(activity: Activity) {
        navigationBar.findViewById<Button>(R.id.main_activity).setOnClickListener {
            activity.startActivity(Intent(activity, MainActivity::class.java))
        }
        navigationBar.findViewById<Button>(R.id.setting_activity).setOnClickListener {
            activity.startActivity(Intent(activity, SettingActivity::class.java))
        }
    }

    @SuppressLint("Recycle")
    internal fun handleTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                velocityTracker = VelocityTracker.obtain()
                velocityTracker?.addMovement(event)
            }
            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.addMovement(event)
            }
            MotionEvent.ACTION_UP -> {
                velocityTracker?.addMovement(event)
                velocityTracker?.computeCurrentVelocity(1000) // 计算每秒的速度
                val velocityX = velocityTracker?.xVelocity ?: 0f
                val deltaX = event.x - downX
                val distance = kotlin.math.abs(deltaX)

                if (distance > 100 && kotlin.math.abs(velocityX) > 100) {
                    Log.d("gesture:", "hello")
                    gestureDetector.onTouchEvent(event)
                }
                velocityTracker?.recycle()
                velocityTracker = null
            }
        }
        return false
    }

    internal fun getGestureDetector(): GestureDetectorCompat {
        return gestureDetector
    }

    private fun showNavigationBar() {
        navigationBar.visibility = View.VISIBLE
        isVisible = true
    }

    private fun hideNavigationBar() {
        navigationBar.visibility = View.GONE
        isVisible = false
    }
}