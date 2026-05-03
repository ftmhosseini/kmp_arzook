package ca.arzook.shared.ui

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import ca.arzook.shared.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual suspend fun getRecaptchaToken(action: String): String =
    suspendCancellableCoroutine { cont ->
        Handler(Looper.getMainLooper()).post {
            val webView = WebView(androidAppContext)
            webView.settings.javaScriptEnabled = true

            webView.addJavascriptInterface(object {
                @JavascriptInterface
                fun onToken(token: String) {
                    println("[Recaptcha] token received, length=${token.length}")
                    if (cont.isActive) cont.resume(token)
                }
            }, "Android")

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    view.evaluateJavascript("""
                        grecaptcha.ready(function() {
                            grecaptcha.execute('${BuildConfig.RECAPTCHA_SITE_KEY}', {action: '$action'})
                                .then(function(token) { Android.onToken(token); });
                        });
                    """.trimIndent(), null)
                }
            }

            webView.loadData("""
                <html><head>
                <script src="https://www.google.com/recaptcha/api.js?render=${BuildConfig.RECAPTCHA_SITE_KEY}"></script>
                </head><body></body></html>
            """.trimIndent(), "text/html", "utf-8")
        }
    }

lateinit var androidAppContext: android.content.Context
