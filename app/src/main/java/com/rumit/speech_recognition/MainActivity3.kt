package com.rumit.speech_recognition

import android.Manifest
import android.R
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
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rumit.speech_recognition.databinding.ActivityMain3Binding
import com.rumit.speech_recognition.utility.IS_CONTINUES_LISTEN
import com.rumit.speech_recognition.utility.PERMISSIONS_REQUEST_RECORD_AUDIO
import com.rumit.speech_recognition.utility.RESULTS_LIMIT
import com.rumit.speech_recognition.utility.errorLog
import com.rumit.speech_recognition.utility.getErrorText
import java.util.Locale
import java.util.concurrent.Executors


class MainActivity3 : AppCompatActivity(), RecognitionListener {
    private lateinit var mContext: Context
    private lateinit var binding: ActivityMain3Binding

    private var speech: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null

    private var selectedLanguage = "en-IN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        binding = ActivityMain3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
        checkPermissions()
        resetSpeechRecognizer()
        setRecogniserIntent()
        prepareSpinner()

        checkSupportedLanguage()
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
                this@MainActivity3,
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
//        val language = /*Locale.US.toString()*/ /*"hi"*/ "kn-IN"
        val language = selectedLanguage
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
        //errorLog( "onRmsChanged: " + rmsdB);
        binding.progressBar1.progress = rmsdB.toInt()
    }

    private fun checkSupportedLanguage() {// Optional
        val intent = Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS)
        intent.setPackage("com.google.android.googlequicksearchbox")

        sendOrderedBroadcast(
            intent,
            null,
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (resultCode == Activity.RESULT_OK) {
                        val results = getResultExtras(true)

                        // Supported languages
                        if (results.containsKey(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE)) {
                            val languagePreference =
                                results.getString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE);
                            errorLog("languagePreference = $languagePreference")
                        }
                        if (results.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)) {
                            val supportedLanguages =
                                results.getStringArrayList(
                                    RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES
                                );
                            errorLog("supportedLanguages = $supportedLanguages")
                        }
                    }
                }
            },
            null,
            Activity.RESULT_OK,
            null,
            null
        )
    }

    private fun checkForLanguagesAPI33Plus() {
        //Optional
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                speech?.checkRecognitionSupport(recognizerIntent!!,
                    Executors.newSingleThreadExecutor(),
                    object : RecognitionSupportCallback {
                        override fun onSupportResult(recognitionSupport: RecognitionSupport) {
                            errorLog(
                                "recognitionSupport.supportedOnDeviceLanguages = ${recognitionSupport.supportedOnDeviceLanguages}"
                            )
                            errorLog(
                                "recognitionSupport.installedOnDeviceLanguages = ${recognitionSupport.installedOnDeviceLanguages}"
                            )
                            errorLog(
                                "recognitionSupport.onlineLanguages = ${recognitionSupport.onlineLanguages}"
                            )
                            errorLog("onSupportResult")

                        }

                        override fun onError(error: Int) {
                            Toast.makeText(
                                mContext,
                                "Speech recognition service NOT available",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun prepareSpinner() {
        val availableLocales = Locale.getAvailableLocales()

        val adapterLocalization: ArrayAdapter<Any?> = ArrayAdapter<Any?>(
            mContext,
            R.layout.simple_spinner_item,
            availableLocales
        )
        adapterLocalization.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
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