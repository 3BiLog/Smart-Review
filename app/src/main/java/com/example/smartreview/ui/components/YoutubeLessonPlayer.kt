package com.example.smartreview.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.app.Activity
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.smartreview.data.video.YouTubeVideoUrl
import com.example.smartreview.ui.theme.OnSurfaceVariant
import com.example.smartreview.ui.theme.Primary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YoutubeLessonPlayer(
    videoId: String,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isLoading by remember(videoId) { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val html = remember(videoId) { YouTubeVideoUrl.embedHtml(videoId) }

    val activity = LocalContext.current as Activity
    var isFullscreen by remember { mutableStateOf(false) }
    var customViewRef: View? by remember { mutableStateOf(null) }
    var customViewCallbackRef: WebChromeClient.CustomViewCallback? by remember { mutableStateOf(null) }

    DisposableEffect(lifecycleOwner, videoId) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> webViewRef?.onPause()
                Lifecycle.Event.ON_RESUME -> webViewRef?.onResume()
                Lifecycle.Event.ON_DESTROY -> webViewRef?.destroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            webViewRef?.apply {
                stopLoading()
                loadUrl("about:blank")
                destroy()
            }
            webViewRef = null
            // ensure fullscreen cleared
            if (isFullscreen) {
                hideCustomView(activity, customViewRef, customViewCallbackRef)
            }
        }
    }

    // Back handler to exit fullscreen first
    BackHandler(enabled = isFullscreen) {
        hideCustomView(activity, customViewRef, customViewCallbackRef)
    }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        if (html.isEmpty()) {
            Text("Video không hợp lệ", color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            return@Box
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    webChromeClient = object : WebChromeClient() {
                        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                            if (view == null || callback == null) return
                            // attach to activity decor view
                            try {
                                val decor = activity.window.decorView as ViewGroup
                                decor.systemUiVisibility = (
                                    View.SYSTEM_UI_FLAG_FULLSCREEN
                                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                )
                                decor.addView(view, ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                ))
                                customViewRef = view
                                customViewCallbackRef = callback
                                isFullscreen = true
                            } catch (_: Exception) {
                                callback.onCustomViewHidden()
                            }
                        }

                        override fun onHideCustomView() {
                            hideCustomView(activity, customViewRef, customViewCallbackRef)
                        }
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }
                    }
                    loadDataWithBaseURL(
                        "https://www.youtube-nocookie.com",
                        html,
                        "text/html",
                        "UTF-8",
                        null,
                    )
                    webViewRef = this
                }
            },
            update = { webView ->
                webViewRef = webView
            },
        )

        if (isLoading) {
            CircularProgressIndicator(color = Primary)
        }
    }
}

private fun hideCustomView(activity: Activity, view: View?, callback: WebChromeClient.CustomViewCallback?) {
    if (view == null || callback == null) return
    try {
        val decor = activity.window.decorView as ViewGroup
        decor.removeView(view)
        // restore system UI
        decor.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        callback.onCustomViewHidden()
    } catch (_: Exception) {
        // ignore
    }
}
