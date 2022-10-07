package com.github.smenko.informtechdemo.utils

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.animation.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.reflect.KProperty


fun Fragment.toast(s: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(
        requireContext(),
        s, duration
    ).show()
}

fun View.clickWithDebounce(debounceTime: Long = 600L, action: () -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
            else action()

            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}

fun View.animateShowR2L() = doOnPreDraw {
    alpha = 0f
    x += width / 2
    visibility = View.VISIBLE
    clearAnimation()
    animate()
        .alpha(1f)
        .translationX(0f)
        .setInterpolator(AccelerateInterpolator())
        .setListener(null)
        .duration = 300
}

fun View.animateHideL2R() = doOnPreDraw {
    visibility = View.GONE
    animate()
        .alpha(0f)
        .translationX(width.toFloat())
        .setInterpolator(DecelerateInterpolator())
        .setListener(null)
        .duration = 300
}

@SuppressLint("ClickableViewAccessibility")
fun View.interceptUntilClick(deviation: Float = 10f, pressTimeMillis: Long = 300) {
    var xDown = -1f
    var yDown = -1f
    var xUp: Float
    var yUp: Float
    setOnTouchListener { _, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                xDown = event.x
                yDown = event.y
            }
            MotionEvent.ACTION_UP -> {
                xUp = event.x
                yUp = event.y
                val nowDown = SystemClock.uptimeMillis()
                if (abs(xDown - xUp) < deviation && abs(yDown - yUp) < deviation) {
                    val cancelEvent = MotionEvent.obtain(
                        nowDown, nowDown,
                        MotionEvent.ACTION_DOWN, xDown, yDown, 0
                    )
                    onTouchEvent(cancelEvent)
                    postDelayed({
                        val nowUp = SystemClock.uptimeMillis()
                        onTouchEvent(
                            MotionEvent.obtain(
                                nowUp, nowUp,
                                MotionEvent.ACTION_CANCEL, 0f, 0f, 0
                            )
                        )
                    }, pressTimeMillis)
                }
            }
        }
        true
    }
}


inline fun LifecycleOwner.setupSearchViewListener(
    crossinline onBindSearchViewListener: (SearchViewListener) -> Unit,
    crossinline onDetachSearchViewListener: (SearchViewListener) -> Unit,
    crossinline onQueryTextSubmit: (String?) -> Boolean = { true },
    crossinline onSearchViewClose: () -> Boolean = { false },
    crossinline onSearchViewOpen: () -> Unit = { },
): Flow<String?> = callbackFlow {
    val searchViewListener = object : SearchViewListener {
        override fun onQueryTextSubmit(query: String?): Boolean = onQueryTextSubmit(query)
        override fun onQueryTextChange(newText: String?): Boolean = trySend(newText).isSuccess
        override fun onClose(): Boolean = onSearchViewClose()
        override fun onClick(v: View?) = onSearchViewOpen()
    }
    onBindSearchViewListener(searchViewListener)
    lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_DESTROY -> {
                    lifecycle.removeObserver(this)
                    onDetachSearchViewListener(searchViewListener)
                    close()
                }
                else -> {}
            }
        }
    })
    awaitClose { cancel() }
}.onStart { emit(null) }


enum class TranslateType(private val type: Int) {
    EXPAND(1), COLLAPSE(-1);

    infix fun xor(direction: TranslateDirection): Int = (this.type xor (direction * 1).toInt()) / 2
    operator fun times(number: Number): Float =
        (number.toFloat() * this.type)
}

enum class TranslateDirection(private val direction: Int) {
    R2L(-1), L2R(1), B2T(-1), T2B(1);

    infix fun xor(type: TranslateType): Int = (this.direction xor (type * 1).toInt()) / 2
    operator fun times(number: Number): Float =
        (number.toFloat() * this.direction)
}

fun View.animateSmoothShift(
    type: TranslateType,
    direction: TranslateDirection,
    dpPerSecond: Float = 60f,
    maxWidthPx: Int = width,
    maxHeightPx: Int = height,
    interpolator: Interpolator = LinearInterpolator(),
    endAction: () -> Unit = {},
    startAction: () -> Unit = {},
) {
    var initialOffset = 0f
    var initialStretch = 0
    var targetShift = 0f
    when (direction) {
        TranslateDirection.R2L, TranslateDirection.L2R -> {
            measure(maxWidthPx, LinearLayout.LayoutParams.MATCH_PARENT)
            targetShift = maxWidthPx.toFloat()
            if (type == TranslateType.EXPAND) {
                initialStretch = 1
                initialOffset = if (direction == TranslateDirection.R2L) x + width.toFloat() else x
                x = initialOffset
                layoutParams.width = initialStretch
            } else {
                initialStretch = layoutParams.width
                initialOffset = x
            }
        }
        TranslateDirection.B2T, TranslateDirection.T2B -> {
            measure(LinearLayout.LayoutParams.MATCH_PARENT, maxHeightPx)
            targetShift = maxHeightPx.toFloat()
            if (type == TranslateType.EXPAND) {
                initialStretch = 1
                initialOffset = if (direction == TranslateDirection.B2T) x + width.toFloat() else x
                y = initialOffset
                layoutParams.height = initialStretch
            } else {
                initialStretch = layoutParams.height
                initialOffset = y
            }
        }
    }
    requestLayout()
    val a: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            val animShiftOffset =
                direction * (type xor direction) * (-1) * (targetShift * interpolatedTime)
            val animStretchOffset = (type * targetShift * interpolatedTime).roundToInt()
            when (direction) {
                TranslateDirection.R2L, TranslateDirection.L2R -> {
                    x = initialOffset + animShiftOffset
                    layoutParams.width = initialStretch + animStretchOffset
                }
                TranslateDirection.B2T, TranslateDirection.T2B -> {
                    y = initialOffset + animShiftOffset
                    layoutParams.height = initialStretch + animStretchOffset
                }
            }
            requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }
    a.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) = startAction.invoke()
        override fun onAnimationEnd(animation: Animation?) = endAction.invoke()
        override fun onAnimationRepeat(animation: Animation?) = Unit
    })

    val duration = ((targetShift * 1000) / (context.resources.displayMetrics.density *
            if (dpPerSecond <= 0) 1f else dpPerSecond)).roundToLong()
    // 1dp/ms
    a.duration = duration
    a.interpolator = interpolator
    startAnimation(a)
}

class QueryTextInputFlow {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Flow<String> = callbackFlow { }
}

fun getPercentHeight(percent: Float) =
    Resources.getSystem().displayMetrics.heightPixels * percent / 100

fun getScreenWidth(): Int = Resources.getSystem().displayMetrics.widthPixels

fun getPixelDensity(): Float = Resources.getSystem().displayMetrics.density

val Int.dp: Float
    get() = (this / Resources.getSystem().displayMetrics.density)
val Int.px: Float
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f)

val Float.dp: Float
    get() = (this / Resources.getSystem().displayMetrics.density)
val Float.px: Float
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f)
