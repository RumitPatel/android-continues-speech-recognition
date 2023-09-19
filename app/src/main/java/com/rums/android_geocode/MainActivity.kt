package com.rums.android_geocode

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rums.android_geocode.utility.errorLog

class MainActivity : AppCompatActivity() {

    private lateinit var mContext: Context
    private val speechRecognizer: SpeechRecognizer by lazy {
        SpeechRecognizer.createSpeechRecognizer(
            mContext
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mContext = this

        initComponents()
        setListeners()
    }

    private fun initComponents() {
        if (ContextCompat.checkSelfPermission(
                mContext,
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                777377
            )
        }

        if (SpeechRecognizer.isRecognitionAvailable(mContext)) {
            speechRecognizer.setRecognitionListener(mRecognitionListener)
        } else {
            errorLog("error")
        }
    }

    private fun setListeners() {
        findViewById<Button>(R.id.btnClickHere).setOnClickListener {
            btn1Clicked()
        }
    }

    private fun btn1Clicked() {
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mContext.packageName)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            }
        }
        speechRecognizer.startListening(recognizerIntent)
    }

    private val mRecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            errorLog("onReadyForSpeech")
        }

        override fun onBeginningOfSpeech() {
            errorLog("onBeginningOfSpeech")
        }

        override fun onRmsChanged(rmsdB: Float) {
            errorLog("onRmsChanged")
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            errorLog("onBufferReceived")
        }

        override fun onEndOfSpeech() {
            errorLog("onEndOfSpeech")
        }

        override fun onError(error: Int) {
            errorLog("onError =  $error")
        }

        override fun onResults(results: Bundle?) {
            errorLog("onResults")
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val scores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
            if (matches != null) {
                errorLog("matches = $matches,  scores = $scores")
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            errorLog("onPartialResults")
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (matches != null) {
                errorLog("matches = $matches")
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            errorLog("onEvent")
        }
    }
}