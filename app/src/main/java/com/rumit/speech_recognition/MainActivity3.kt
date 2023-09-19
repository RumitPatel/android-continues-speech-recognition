package com.rumit.speech_recognition

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognitionSupport
import android.speech.RecognitionSupportCallback
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rumit.speech_recognition.utility.LOG_TAG
import com.rumit.speech_recognition.utility.PERMISSIONS_REQUEST_RECORD_AUDIO
import com.rumit.speech_recognition.utility.RESULTS_LIMIT
import com.rumit.speech_recognition.utility.getErrorText
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity3 : AppCompatActivity(), RecognitionListener {
    private var returnedText: TextView? = null
    private var returnedError: TextView? = null
    private var progressBar: ProgressBar? = null
    private var speech: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        // UI initialisation
        returnedText = findViewById(R.id.textView1)
        returnedError = findViewById(R.id.tvError)
        progressBar = findViewById(R.id.progressBar1)

        resetSpeechRecognizer()

        // check for permission
        val permissionCheck =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_REQUEST_RECORD_AUDIO
            )
            return
        }
        setRecogniserIntent()
        startListening()

        checkSupportedLanguage()
    }

    private fun resetSpeechRecognizer() {
        if (speech != null) speech!!.destroy()
        speech = SpeechRecognizer.createSpeechRecognizer(this)
        Log.i(LOG_TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this))
        if (SpeechRecognizer.isRecognitionAvailable(this)) speech!!.setRecognitionListener(this) else finish()
    }

    private fun setRecogniserIntent() {
        val language = Locale.US.toString()
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent!!.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
            language
        )
        recognizerIntent!!.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            language
        )
        recognizerIntent!!.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        recognizerIntent!!.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, RESULTS_LIMIT)

        //Optional
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            speech?.checkRecognitionSupport(recognizerIntent!!,
                Executors.newSingleThreadExecutor(),
                object : RecognitionSupportCallback {
                    override fun onSupportResult(recognitionSupport: RecognitionSupport) {
                        Log.e(
                            LOG_TAG,
                            "recognitionSupport.supportedOnDeviceLanguages = ${recognitionSupport.supportedOnDeviceLanguages}"
                        )
                        Log.e(
                            LOG_TAG,
                            "recognitionSupport.installedOnDeviceLanguages = ${recognitionSupport.installedOnDeviceLanguages}"
                        )
                        Log.e(
                            LOG_TAG,
                            "recognitionSupport.onlineLanguages = ${recognitionSupport.onlineLanguages}"
                        )
                        Log.e(LOG_TAG, "onSupportResult")

                    }

                    override fun onError(error: Int) {
                        Toast.makeText(
                            this@MainActivity3,
                            "Speech recognition service NOT available",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening()
            } else {
                Toast.makeText(this@MainActivity3, "Permission Denied!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startListening() {
        speech!!.startListening(recognizerIntent)
        progressBar!!.visibility = View.VISIBLE
    }

    public override fun onResume() {
        Log.i(LOG_TAG, "resume")
        super.onResume()
        resetSpeechRecognizer()
        startListening()
    }

    override fun onPause() {
        Log.i(LOG_TAG, "pause")
        super.onPause()
        speech!!.stopListening()
    }

    override fun onStop() {
        Log.i(LOG_TAG, "stop")
        super.onStop()
        if (speech != null) {
            speech!!.destroy()
        }
    }

    override fun onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech")
        progressBar!!.isIndeterminate = false
        progressBar!!.max = 10
    }

    override fun onBufferReceived(buffer: ByteArray) {
        Log.i(LOG_TAG, "onBufferReceived: $buffer")
    }

    override fun onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech")
        progressBar!!.isIndeterminate = true
        speech!!.stopListening()
    }

    override fun onResults(results: Bundle) {
        Log.i(LOG_TAG, "onResults")
        val matches: ArrayList<String>? = results
            .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        var text = ""
        for (result in matches!!) text += """
     $result
     
     """.trimIndent()
        returnedText!!.text = text
        startListening()
    }

    override fun onError(errorCode: Int) {
        val errorMessage = getErrorText(errorCode)
        Log.i(LOG_TAG, "FAILED $errorMessage")
        returnedError!!.text = errorMessage

        // rest voice recogniser
        resetSpeechRecognizer()
        startListening()
    }

    override fun onEvent(arg0: Int, arg1: Bundle) {
        Log.i(LOG_TAG, "onEvent")
    }

    override fun onPartialResults(arg0: Bundle) {
        Log.i(LOG_TAG, "onPartialResults")
    }

    override fun onReadyForSpeech(arg0: Bundle) {
        Log.i(LOG_TAG, "onReadyForSpeech")
    }

    override fun onRmsChanged(rmsdB: Float) {
        //Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar!!.progress = rmsdB.toInt()
    }

    private fun checkSupportedLanguage() {// Optional
        val intent = Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS)

        sendOrderedBroadcast(intent, null, object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (resultCode == Activity.RESULT_OK) {
                    val results = getResultExtras(true)

                    // Supported languages
                    val prefLang: String? =
                        results.getString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE)
                    val allLangs: ArrayList<CharSequence>? =
                        results.getCharSequenceArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)
                    Log.e(
                        LOG_TAG,
                        "prefLang = $prefLang allLangs.toString() = ${allLangs.toString()}"
                    )
                }
            }
        }, null, Activity.RESULT_OK, null, null)
    }
}