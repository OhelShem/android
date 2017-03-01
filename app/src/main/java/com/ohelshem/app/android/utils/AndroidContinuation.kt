package com.ohelshem.app.android.utils

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.AbstractCoroutineContextElement
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.ContinuationInterceptor

/**
 * Android Continuation, guarantees that, when resumed, is on the UI Thread
 * Created by macastiblancot on 2/13/17.
 */
private class AndroidContinuation<in T>(val cont: Continuation<T>) : Continuation<T> by cont {
    override fun resume(value: T) {
        if (Looper.myLooper() == Looper.getMainLooper()) cont.resume(value)
        else Handler(Looper.getMainLooper()).post { cont.resume(value) }
    }

    override fun resumeWithException(exception: Throwable) {
        if (Looper.myLooper() == Looper.getMainLooper()) cont.resumeWithException(exception)
        else Handler(Looper.getMainLooper()).post { cont.resumeWithException(exception) }
    }
}

/**
 * Android context, provides an AndroidContinuation, executes everything on the UI Thread
 */
object Android : AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor {
    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> =
            AndroidContinuation(continuation)
}

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
fun uiThread(func: suspend CoroutineScope.() -> Unit) = launch(Android, true, func)