package com.rumit.speech_recognition.utility

import android.speech.SpeechRecognizer
import android.util.Log

const val LOG_TAG = "rum==##"
const val PERMISSIONS_REQUEST_RECORD_AUDIO = 100
const val RESULTS_LIMIT = 1

const val IS_CONTINUES_LISTEN = false

fun errorLog(msg: String?) {
    Log.e(LOG_TAG, msg!!)
}

fun getErrorText(errorCode: Int): String {
    val message: String = when (errorCode) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
        SpeechRecognizer.ERROR_NETWORK -> "Network error"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
        SpeechRecognizer.ERROR_NO_MATCH -> "No match"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
        SpeechRecognizer.ERROR_SERVER -> "error from server"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED -> "Language Not supported"
        SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> "Language Unavailable"
        else -> "Didn't understand, please try again."
    }
    return message
}