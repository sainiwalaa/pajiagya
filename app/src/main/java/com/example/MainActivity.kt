package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private var gameWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                gameWebView?.evaluateJavascript("if (window.uiManager) window.uiManager.togglePauseMenu();", null)
            }
        })

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BattleZoneWebView(
                        onWebViewCreated = { webView ->
                            gameWebView = webView
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gameWebView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        gameWebView?.onPause()
    }

    override fun onDestroy() {
        gameWebView?.destroy()
        gameWebView = null
        super.onDestroy()
    }
}

class AndroidHapticBridge(private val context: Context) {
    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Exception) {
        null
    }

    @JavascriptInterface
    fun vibrate(durationMs: Long) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            durationMs.coerceIn(10L, 500L),
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BattleZoneWebView(onWebViewCreated: (WebView) -> Unit) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // High-performance hardware acceleration
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                isHapticFeedbackEnabled = true
                isSoundEffectsEnabled = true

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    mediaPlaybackRequiresUserGesture = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                }

                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()

                addJavascriptInterface(AndroidHapticBridge(ctx), "AndroidBridge")

                loadUrl("file:///android_asset/game/index.html")

                onWebViewCreated(this)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
