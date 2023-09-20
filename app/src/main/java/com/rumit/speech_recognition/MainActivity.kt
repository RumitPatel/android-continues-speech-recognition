package com.rumit.speech_recognition

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rumit.speech_recognition.databinding.ActivityMainBinding
import com.rumit.speech_recognition.utility.IS_CONTINUES_LISTEN
import com.rumit.speech_recognition.utility.PERMISSIONS_REQUEST_RECORD_AUDIO
import com.rumit.speech_recognition.utility.RESULTS_LIMIT
import com.rumit.speech_recognition.utility.errorLog
import com.rumit.speech_recognition.utility.getErrorText
import java.util.Locale


class MainActivity : AppCompatActivity(), RecognitionListener {
    private lateinit var mContext: Context
    private lateinit var binding: ActivityMainBinding

    private var speech: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null

    private var selectedLanguage = "en" // Default "en selected"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
        checkPermissions()
        resetSpeechRecognizer()
        setRecogniserIntent()
        prepareSpinner()
    }

    private fun setListeners() {
        binding.btnStartListen.setOnClickListener {
            startListening()
        }
    }

    private fun checkPermissions() {
        val permissionCheck =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_REQUEST_RECORD_AUDIO
            )
            return
        }
    }

    private fun resetSpeechRecognizer() {
        if (speech != null) speech!!.destroy()
        speech = SpeechRecognizer.createSpeechRecognizer(mContext)
        errorLog("isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(mContext))
        if (SpeechRecognizer.isRecognitionAvailable(mContext)) speech!!.setRecognitionListener(this) else finish()
    }

    private fun setRecogniserIntent() {
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent!!.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
            selectedLanguage
        )
        recognizerIntent!!.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            selectedLanguage
        )
        recognizerIntent!!.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        recognizerIntent!!.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, RESULTS_LIMIT)

//        checkForLanguagesAPI33Plus() //Optional causes crash
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
                Toast.makeText(mContext, "Permission Denied!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startListening() {
        speech!!.startListening(recognizerIntent)
        binding.progressBar1.visibility = View.VISIBLE
    }

    public override fun onResume() {
        errorLog("resume")
        super.onResume()
        resetSpeechRecognizer()
        if (IS_CONTINUES_LISTEN) {
            startListening()
        }
    }

    override fun onPause() {
        errorLog("pause")
        super.onPause()
        speech!!.stopListening()
    }

    override fun onStop() {
        errorLog("stop")
        super.onStop()
        if (speech != null) {
            speech!!.destroy()
        }
    }

    override fun onBeginningOfSpeech() {
        errorLog("onBeginningOfSpeech")
        binding.progressBar1.isIndeterminate = false
        binding.progressBar1.max = 10
    }

    override fun onBufferReceived(buffer: ByteArray) {
        errorLog("onBufferReceived: $buffer")
    }

    override fun onEndOfSpeech() {
        errorLog("onEndOfSpeech")
        binding.progressBar1.isIndeterminate = true
        speech!!.stopListening()
    }

    override fun onResults(results: Bundle) {
        errorLog("onResults")
        val matches: ArrayList<String>? = results
            .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        var text = ""
        for (result in matches!!) text += """
     $result
     
     """.trimIndent()
        binding.textView1.text = text
        if (IS_CONTINUES_LISTEN) {
            startListening()
        } else {
            binding.progressBar1.visibility = View.GONE
        }
    }

    override fun onError(errorCode: Int) {
        val errorMessage = getErrorText(errorCode)
        errorLog("FAILED $errorMessage")
        binding.tvError.text = errorMessage

        // rest voice recogniser
        resetSpeechRecognizer()
        startListening()
    }

    override fun onEvent(arg0: Int, arg1: Bundle) {
        errorLog("onEvent")
    }

    override fun onPartialResults(arg0: Bundle) {
        errorLog("onPartialResults")
    }

    override fun onReadyForSpeech(arg0: Bundle) {
        errorLog("onReadyForSpeech")
    }

    override fun onRmsChanged(rmsdB: Float) {
        binding.progressBar1.progress = rmsdB.toInt()
    }

    private fun prepareSpinner() {
        val availableLocales =
            Locale.getAvailableLocales() //Alternatively you can check https://cloud.google.com/speech-to-text/docs/speech-to-text-supported-languages

        val adapterLocalization: ArrayAdapter<Any?> = ArrayAdapter<Any?>(
            mContext,
            android.R.layout.simple_spinner_item,
            availableLocales
        )
        adapterLocalization.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedLanguage = availableLocales[position].toString()

                resetSpeechRecognizer()
                setRecogniserIntent()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
//                TODO("Not yet implemented")
            }
        }

        binding.spinner1.adapter = adapterLocalization

        // Set "en" as selected language
        for (i in availableLocales.indices) {
            val locale = availableLocales[i]
            if (locale.toString().equals("en", true)) {
                binding.spinner1.setSelection(i)
                break
            }
        }


    }
}