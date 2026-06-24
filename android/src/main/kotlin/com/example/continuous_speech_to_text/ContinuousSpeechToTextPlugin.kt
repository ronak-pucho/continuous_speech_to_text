package com.example.continuous_speech_to_text

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry

/** ContinuousSpeechToTextPlugin */
class ContinuousSpeechToTextPlugin: FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler, ActivityAware, PluginRegistry.RequestPermissionsResultListener {

    companion object {
        private const val TAG = "ContinuousSpeechPlugin"
        private const val MIC_PERMISSION_REQUEST = 100
        private const val RESTART_DELAY_MS = 100L
        private const val BUSY_RESTART_DELAY_MS = 1_000L
    }

    private lateinit var methodChannel: MethodChannel
    private lateinit var eventChannel: EventChannel
    private var activity: Activity? = null
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var eventSink: EventChannel.EventSink? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentLocaleId: String = java.util.Locale.getDefault().toLanguageTag()
    @Volatile private var isListeningRequested = false

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        methodChannel = MethodChannel(flutterPluginBinding.binaryMessenger, "continuous_speech_to_text")
        methodChannel.setMethodCallHandler(this)

        eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "continuous_speech_to_text/events")
        eventChannel.setStreamHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        methodChannel.setMethodCallHandler(null)
        eventChannel.setStreamHandler(null)
        fullDestroyRecognizer()
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addRequestPermissionsResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addRequestPermissionsResultListener(this)
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onListen(arguments: Any?, sink: EventChannel.EventSink?) {
        eventSink = sink
        Log.d(TAG, "EventChannel: Flutter is now listening")
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
        Log.d(TAG, "EventChannel: Flutter stopped listening")
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "startListening" -> handleStartListening(call, result)
            "stopListening"  -> handleStopListening(result)
            else             -> result.notImplemented()
        }
    }

    private fun handleStartListening(call: MethodCall, result: Result) {
        val localeId = call.argument<String>("localeId")
        currentLocaleId = if (!localeId.isNullOrEmpty()) localeId else java.util.Locale.getDefault().toLanguageTag()

        if (!hasMicPermission()) {
            requestMicPermission()
            result.error("PERMISSION_DENIED", "Microphone permission not granted", null)
            return
        }
        isListeningRequested = true
        ensureRecognizerAndStart()
        result.success(null)
    }

    private fun handleStopListening(result: Result) {
        isListeningRequested = false
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.post {
            fullDestroyRecognizer()
            sendEventOnMainThread("stopped", "")
        }
        result.success(null)
    }

    private fun hasMicPermission(): Boolean {
        val currentActivity = activity ?: return false
        return ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestMicPermission() {
        val currentActivity = activity ?: return
        ActivityCompat.requestPermissions(currentActivity, arrayOf(Manifest.permission.RECORD_AUDIO), MIC_PERMISSION_REQUEST)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray): Boolean {
        if (requestCode == MIC_PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (isListeningRequested) {
                ensureRecognizerAndStart()
            }
            return true
        }
        return false
    }

    private fun ensureRecognizerAndStart() {
        mainHandler.post {
            if (!isListeningRequested) return@post
            val currentActivity = activity ?: return@post

            if (!SpeechRecognizer.isRecognitionAvailable(currentActivity)) {
                sendEventOnMainThread("error", "Speech recognition not available", errorCode = "no_recognition_service")
                isListeningRequested = false
                return@post
            }

            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(currentActivity).also {
                    it.setRecognitionListener(recognitionListener)
                }
            }

            try {
                speechRecognizer?.startListening(buildRecognizerIntent())
            } catch (e: Exception) {
                fullDestroyRecognizer()
                isListeningRequested = false
                sendEventOnMainThread("error", e.message ?: "Failed to start", errorCode = "5")
            }
        }
    }

    private fun restartOnSameRecognizer(delayMs: Long = RESTART_DELAY_MS) {
        mainHandler.postDelayed({
            if (!isListeningRequested) return@postDelayed
            val sr = speechRecognizer
            if (sr == null) {
                ensureRecognizerAndStart()
                return@postDelayed
            }
            try {
                sr.cancel()
                sr.startListening(buildRecognizerIntent())
            } catch (e: Exception) {
                fullDestroyRecognizer()
                ensureRecognizerAndStart()
            }
        }, delayMs)
    }

    private fun fullDestroyRecognizer() {
        speechRecognizer?.let { sr ->
            try { sr.stopListening() } catch (e: Exception) {}
            try { sr.destroy()       } catch (e: Exception) {}
            speechRecognizer = null
        }
    }

    private fun buildRecognizerIntent(): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLocaleId)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, currentLocaleId)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            sendEventOnMainThread("readyForSpeech", "")
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            val codeStr = error.toString()
            when (error) {
                SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                    sendEventOnMainThread("error", "", errorCode = codeStr)
                    if (isListeningRequested) restartOnSameRecognizer(RESTART_DELAY_MS)
                }
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                    fullDestroyRecognizer()
                    sendEventOnMainThread("error", "", errorCode = codeStr)
                    if (isListeningRequested) {
                        mainHandler.postDelayed({
                            if (isListeningRequested) ensureRecognizerAndStart()
                        }, BUSY_RESTART_DELAY_MS)
                    }
                }
                else -> {
                    sendEventOnMainThread("error", friendlyError(error), errorCode = codeStr)
                    isListeningRequested = false
                }
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: return
            sendEventOnMainThread("partialResult", text)
        }

        override fun onResults(results: Bundle?) {
            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull() ?: ""
            sendEventOnMainThread("result", text)
            if (isListeningRequested) restartOnSameRecognizer(RESTART_DELAY_MS)
        }
    }

    private fun sendEventOnMainThread(event: String, data: String, errorCode: String = "") {
        mainHandler.post {
            eventSink?.success(mapOf("event" to event, "data" to data, "errorCode" to errorCode))
        }
    }

    private fun friendlyError(code: Int): String = when (code) {
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT          -> "Network timeout."
        SpeechRecognizer.ERROR_NETWORK                  -> "Network error."
        SpeechRecognizer.ERROR_AUDIO                    -> "Audio recording error."
        SpeechRecognizer.ERROR_SERVER                   -> "Server error."
        SpeechRecognizer.ERROR_CLIENT                   -> "Client error. Restart the app."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT           -> "Speech timeout."
        SpeechRecognizer.ERROR_NO_MATCH                 -> "No speech detected."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY          -> "Recognizer busy."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission denied."
        13                                              -> "Selected language is not downloaded or supported on this device."
        else                                            -> "Unknown error (\$code)."
    }
}
