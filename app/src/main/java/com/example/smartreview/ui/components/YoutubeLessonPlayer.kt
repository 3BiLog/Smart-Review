package com.example.smartreview.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.app.Activity
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
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
    onVideoEnded: (() -> Unit)? = null,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isLoading by remember(videoId) { mutableStateOf(true) }
    var webViewRef by remember(videoId) { mutableStateOf<WebView?>(null) }
    val html = remember(videoId) { YouTubeVideoUrl.embedHtml(videoId) }

    val activity = LocalContext.current as Activity
    var isFullscreen by remember(videoId) { mutableStateOf(false) }
    var customViewRef: View? by remember(videoId) { mutableStateOf(null) }
    var customViewCallbackRef: WebChromeClient.CustomViewCallback? by remember(videoId) { mutableStateOf(null) }

    android.util.Log.d("YoutubeLessonPlayer", "Creating/Updating player for videoId: $videoId")

    BackHandler(enabled = isFullscreen) {
        hideCustomView(activity, customViewRef, customViewCallbackRef)
    }

    DisposableEffect(lifecycleOwner, videoId) {
        android.util.Log.d("YoutubeLessonPlayer", "DisposableEffect created for videoId: $videoId")

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    android.util.Log.d("YoutubeLessonPlayer", "ON_PAUSE for videoId: $videoId")
                    webViewRef?.onPause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    android.util.Log.d("YoutubeLessonPlayer", "ON_RESUME for videoId: $videoId")
                    webViewRef?.onResume()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    android.util.Log.d("YoutubeLessonPlayer", "ON_DESTROY for videoId: $videoId")
                    webViewRef?.destroy()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            android.util.Log.d("YoutubeLessonPlayer", "DisposableEffect DISPOSING for videoId: $videoId")
            lifecycleOwner.lifecycle.removeObserver(observer)

            webViewRef?.apply {
                android.util.Log.d("YoutubeLessonPlayer", "Cleaning up WebView for videoId: $videoId")
                stopLoading()
                loadUrl("about:blank")
                destroy()
            }
            webViewRef = null

            if (isFullscreen) {
                hideCustomView(activity, customViewRef, customViewCallbackRef)
            }
        }
    }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        if (html.isEmpty()) {
            Text(
                "Video không hợp lệ",
                color = OnSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            return@Box
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                android.util.Log.d("YoutubeLessonPlayer", "Creating new WebView for videoId: $videoId")

                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true

                    webChromeClient = object : WebChromeClient() {
                        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                            if (view == null || callback == null) return
                            try {
                                android.util.Log.d("YoutubeLessonPlayer", "onShowCustomView for videoId: $videoId")
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
                            } catch (e: Exception) {
                                android.util.Log.e("YoutubeLessonPlayer", "Error showing custom view: ${e.message}")
                                callback.onCustomViewHidden()
                            }
                        }

                        override fun onHideCustomView() {
                            android.util.Log.d("YoutubeLessonPlayer", "onHideCustomView for videoId: $videoId")
                            hideCustomView(activity, customViewRef, customViewCallbackRef)
                            customViewRef = null
                            customViewCallbackRef = null
                            isFullscreen = false
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            android.util.Log.d("YoutubeLessonPlayer", "onPageFinished for videoId: $videoId")
                            isLoading = false
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            android.util.Log.e("YoutubeLessonPlayer", "WebView error for videoId: $videoId - $description")
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
                if (webViewRef != webView) {
                    android.util.Log.d("YoutubeLessonPlayer", "Updating WebView reference for videoId: $videoId")
                    webViewRef = webView
                }
            },
        )

        if (isLoading) {
            CircularProgressIndicator(
                color = Primary,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

private fun hideCustomView(
    activity: Activity,
    view: View?,
    callback: WebChromeClient.CustomViewCallback?
) {
    if (view == null || callback == null) return
    try {
        android.util.Log.d("YoutubeLessonPlayer", "hideCustomView called")
        val decor = activity.window.decorView as ViewGroup
        decor.removeView(view)
        decor.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        callback.onCustomViewHidden()
    } catch (e: Exception) {
        android.util.Log.e("YoutubeLessonPlayer", "Error hiding custom view: ${e.message}")
    }
}