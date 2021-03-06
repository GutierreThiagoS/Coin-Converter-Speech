package com.example.coinconverterspeech.presentation.speech

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.example.coinconverterspeech.App
import com.example.coinconverterspeech.core.extensions.convertValueReal
import com.example.coinconverterspeech.core.extensions.wordCount
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.HashMap

class SpeechUtils(private val speakeListner: WeakReference<SpeechListener>, owner: LifecycleOwner):
    RecognitionListener, LifecycleObserver {

    private var  currentRequestCode: Int? = null
    private var  previousRequestCode = DEFAULT
    private var lastSpeech: String? = null
    private var lastRequestCode: Int? = null
    private var lastCallInput: Boolean = false
    private var textToSpeach: TextToSpeech? = null
    private var speechTimeOutCount = 0
    private var repeatCount = 0
    private var waitingForResult = false
    private var recognizer = SpeechRecognizer.createSpeechRecognizer(App.context)

    init {
        owner.lifecycle.addObserver(this)
        Handler(Looper.getMainLooper()).post {
            recognizer.setRecognitionListener(this)
        }
    }

    companion object{
        const val NO_SAVE = 0
        const val STATUS_SUCCESS = 400
        const val SPEECH_TIMEOUT = 401
        const val STATUS_ERROR = 402
        const val DEFAULT = 403
        const val SAVE_COIN = 405
        const val VALUE_COIN = 406
        const val VALUE_COIN_REAL = 407
        const val CONVERT_BRL_TO_USD = 408
        const val CONVERT_USD_TO_BRL = 409
        const val CONVERT_BRL_TO_ARS = 410
        const val CONVERT_USD_TO_ARS = 411
        const val CONVERT_BRL_TO_CAD = 412
        const val CONVERT_USD_TO_CAD = 413
        const val CONVERT_CAD_TO_USD = 414
        const val CONVERT_CAD_TO_BRL = 415
        const val CONVERT_ARS_TO_BRL = 416
        const val CONVERT_ARS_TO_USD = 417
        const val CONVERT_TO_BRL = 418
        const val CONVERT_TO_USD = 419
        const val CONVERT_TO_CAD = 420
        const val CONVERT_TO_ARS = 421
    }

    fun speechInput(requestCode: Int?) {
        Handler(Looper.getMainLooper()).post {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            if(requestCode != DEFAULT) {
                currentRequestCode = requestCode
            }

            recognizer.stopListening()
            recognizer.startListening(intent)
            waitingForResult = true
        }
    }
    fun speak(text: String, requestCode: Int) {
        when (requestCode) {
            NO_SAVE -> {
                speak(text, lastRequestCode, lastCallInput, false)
            }
            else -> speak(text, requestCode, true, saveSpeech = true)
        }
    }

    fun speak(text: String) {
        speak(text, null,  false, saveSpeech = true)
    }

    fun speak(text: String, requestCode: Int?, callInput: Boolean, saveSpeech: Boolean) {
        Handler(Looper.getMainLooper()).post {
            if(requestCode != DEFAULT) {
                currentRequestCode = requestCode
            }
            if(saveSpeech) {
                if(requestCode != DEFAULT) {
                    lastRequestCode = requestCode
                }
                lastCallInput = callInput
                lastSpeech = text
            }

            if(textToSpeach == null) {
                initSpeech(text)
            }else{
                startSpeech(text)
            }

            if (Build.VERSION.SDK_INT >= 23) {
                textToSpeach?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onDone(utteranceId: String?) {
                        Handler(Looper.getMainLooper()).post {
                            if (callInput) {
                                speechInput(requestCode)
                            }
                            speakeListner.get()?.onDoneSpeaking(requestCode)
                        }
                    }
                    override fun onError(utteranceId: String?) {}
                    override fun onStart(utteranceId: String?) {}
                })
            }
        }
    }
    private fun initSpeech(text: String) {
        textToSpeach = TextToSpeech(App.context) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeach?.language = Locale("Pt", "BR")
                textToSpeach?.setSpeechRate(1.4f)
                startSpeech(text)
            }
        }
    }
    @Suppress("DEPRECATION")
    private fun startSpeech(text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
            textToSpeach?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                params,
                Calendar.getInstance().timeInMillis.toString()
            )
        }else{
            val speakMap = HashMap<String, String>()
            speakMap[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] =
                Calendar.getInstance().timeInMillis.toString()
            textToSpeach?.speak(text, TextToSpeech.QUEUE_FLUSH, speakMap)
        }
    }
    fun stopSpeach(){
        textToSpeach?.stop()
    }
    fun stopRecognizer(){
        recognizer.stopListening()
    }

    override fun onReadyForSpeech(params: Bundle?) {
        println("Ready for speech")
        stopSpeach()
    }
    override fun onRmsChanged(rmsdB: Float) {
        speakeListner.get()?.onRmsChanged(rmsdB)
        println("onRmsChanged, $rmsdB")
    }
    override fun onBufferReceived(buffer: ByteArray?) {
        println("onBufferReceived, $buffer")
    }
    override fun onPartialResults(partialResults: Bundle?) {
        println("onPartialResults $partialResults")
    }
    override fun onEvent(eventType: Int, params: Bundle?) {
        println("onEvent $params")
    }
    override fun onBeginningOfSpeech() {
        speakeListner.get()?.onSpeechStarted()
        println("### Speech starting")
    }
    override fun onEndOfSpeech() {
        speakeListner.get()?.onSpeechFinished()
        println("### onEndOfSpeech")
    }
    override fun onError(error: Int) {
        when(error){
            SpeechRecognizer.ERROR_NETWORK->{
                speak("Ocorreu um erro! Estou tendo problemas de conex??o com a internet.", NO_SAVE)
            }
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT->{
                speak("Ocorreu um erro! Por favor.. Verifique conex??o com a internet.", NO_SAVE)
            }
            SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT->{
                speakeListner.get()?.onSpeechResults(SPEECH_TIMEOUT, STATUS_ERROR, "")
            }
            else -> { }
        }
        System.err.println("Error listening for speech: $error")
    }

    override fun onResults(results: Bundle?) {
        if(results != null && waitingForResult) {
            waitingForResult = false
            val voiceResults = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (voiceResults == null) {
                println("No voice results")
            } else {
                val result = voiceResults[0]
                repeatCount = 0
                speechTimeOutCount = 0
                when {

                    containsSavedCoin(result) -> {
                        speakeListner.get()?.onSpeechResults(SAVE_COIN, STATUS_SUCCESS, result)
                    }

                    containsValueCoin(result) -> {
                        speakeListner.get()?.onSpeechResults(VALUE_COIN, STATUS_SUCCESS, result)
                    }

                    containsValueCoinReal(result) -> {
                        speakeListner.get()?.onSpeechResults(VALUE_COIN_REAL, STATUS_SUCCESS, result)
                    }

                    containsConvertRealToDollar(result) -> {
                        speakeListner.get()?.onSpeechResults(CONVERT_BRL_TO_USD, STATUS_SUCCESS, result)
                    }

                    containsConvertDollarToReal(result) -> {
                        speakeListner.get()?.onSpeechResults(CONVERT_USD_TO_BRL, STATUS_SUCCESS, result)
                    }

                    containsConvertRealToPeso(result) -> {
                        speakeListner.get()?.onSpeechResults(CONVERT_BRL_TO_ARS, STATUS_SUCCESS, result)
                    }

                    containsConvertDollarToPeso(result) -> {
                        speakeListner.get()?.onSpeechResults(CONVERT_USD_TO_ARS, STATUS_SUCCESS, result)
                    }

                    containsConvertRealToCanada(result) -> {
                        speakeListner.get()?.onSpeechResults(CONVERT_BRL_TO_CAD, STATUS_SUCCESS, result)
                    }

                    containsConvertDollarToCanada(result) -> {
                        speakeListner.get()?.onSpeechResults(CONVERT_USD_TO_CAD, STATUS_SUCCESS, result)
                    }

                    containsConvertCanadaToReal(result) -> {
                        speakeListner.get()?.onSpeechResults(CONVERT_CAD_TO_BRL, STATUS_SUCCESS, result)
                    }

                    containsConvertCanadaToDollar(result) -> {
                        speakeListner.get()?.onSpeechResults(CONVERT_CAD_TO_USD, STATUS_SUCCESS, result)
                    }

                    containsConvertArgentinoToReal(result) -> {
                        speakeListner.get()?.onSpeechResults(CONVERT_ARS_TO_BRL, STATUS_SUCCESS, result)
                    }

                    containsConvertArgentinoToDollar(result) -> {
                        speakeListner.get()?.onSpeechResults(CONVERT_ARS_TO_USD, STATUS_SUCCESS, result)
                    }

                    containsConvertToDollar(result) -> {
                        speakeListner.get()?.onSpeechResults(CONVERT_TO_USD, STATUS_SUCCESS, result)
                    }

                    containsConvertToReal(result) -> {
                        speakeListner.get()?.onSpeechResults(CONVERT_TO_BRL, STATUS_SUCCESS, result)
                    }

                    containsConvertToCanada(result) -> {
                        speakeListner.get()?.onSpeechResults(CONVERT_TO_CAD, STATUS_SUCCESS, result)
                    }

                    containsConvertToArgentine(result) -> {
                        speakeListner.get()?.onSpeechResults(CONVERT_TO_ARS, STATUS_SUCCESS, result)
                    }

                    else -> speakeListner.get()?.onSpeechResults(currentRequestCode, STATUS_SUCCESS, result)
                }
            }
        }
        if(previousRequestCode != currentRequestCode ){
            currentRequestCode?.let {
                previousRequestCode = it
            }
        }
    }

    private fun containsSavedCoin(result: String): Boolean {
        return result.contains("salvar", true) ||
                result.contains("salve", true) ||
                result.contains("gravar", true) ||
                result.contains("grava", true)
    }

    private fun containsConvertRealToDollar(result: String): Boolean {
        return result.contains("real para d??lar", true) && !result.contains("canadense", true) && result.wordCount() < 6 ||
                result.contains("reais para d??lar", true) && !result.contains("canadense", true) && result.wordCount() < 6 ||
                result.contains("real pra d??lar", true) && !result.contains("canadense", true) && result.wordCount() < 6 ||
                result.contains("reais pra d??lar", true) && !result.contains("canadense", true) && result.wordCount() < 6 ||
                result.contains("real em d??lar", true) && !result.contains("canadense", true) && result.wordCount() < 6 ||
                result.contains("reais em d??lar", true) && !result.contains("canadense", true) && result.wordCount() < 6 ||
                result.contains("r??is para dona", true) && !result.contains("canadense", true) && result.wordCount() < 6 ||
                result.contains("r??is pra dona", true) && !result.contains("canadense", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("em d??lar") && !result.contains("canadense", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("pra d??lar") && !result.contains("canadense", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("para d??lar") && !result.contains("canadense", true) && result.wordCount() < 6
    }

    private fun containsConvertRealToPeso(result: String): Boolean {
        return result.contains("real para peso", true) && result.wordCount() < 6 ||
                result.contains("real para peso argentino", true) && result.wordCount() < 6 ||
                result.contains("real para peso argentina", true) && result.wordCount() < 6 ||
                result.contains("real para peso argentino", true) && result.wordCount() < 6 ||
                result.contains("real para argentina", true) && result.wordCount() < 6 ||
                result.contains("reais para peso", true) && result.wordCount() < 6 ||
                result.contains("reais para peso argentino", true) && result.wordCount() < 6 ||
                result.contains("reais para peso argentina", true) && result.wordCount() < 6 ||
                result.contains("reais para argentino", true) && result.wordCount() < 6 ||
                result.contains("reais para argentina", true) && result.wordCount() < 6 ||
                result.contains("real pra peso", true) && result.wordCount() < 6 ||
                result.contains("real pra peso argentino", true) && result.wordCount() < 6 ||
                result.contains("real pra peso argentina", true) && result.wordCount() < 6 ||
                result.contains("real pra argentino", true) && result.wordCount() < 6 ||
                result.contains("real pra argentina", true) && result.wordCount() < 6 ||
                result.contains("reais pra peso", true) && result.wordCount() < 6 ||
                result.contains("reais pra peso argentino", true) && result.wordCount() < 6 ||
                result.contains("reais pra peso argentina", true) && result.wordCount() < 6 ||
                result.contains("reais pra argentino", true) && result.wordCount() < 6 ||
                result.contains("reais pra argentina", true) && result.wordCount() < 6 ||
                result.contains("real em peso", true) && result.wordCount() < 6 ||
                result.contains("real em peso argentino", true) && result.wordCount() < 6 ||
                result.contains("real em peso argentina", true) && result.wordCount() < 6 ||
                result.contains("real em argentino", true) && result.wordCount() < 6 ||
                result.contains("real em argentina", true) && result.wordCount() < 6 ||
                result.contains("reais em peso", true) && result.wordCount() < 6 ||
                result.contains("reais em peso argentino", true) && result.wordCount() < 6 ||
                result.contains("reais em peso argentina", true) && result.wordCount() < 6 ||
                result.contains("reais em argentino", true) && result.wordCount() < 6 ||
                result.contains("reais em argentina", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("em peso", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("em peso argentino", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("em peso argentina", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("em argentino", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("em argentina", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("pra peso", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("pra peso argentino", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("pra peso argentina", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("pra argentino", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("pra argentina", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("para peso", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("para peso argentino", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("para peso argentina", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("para argentino", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("para argentina", true) && result.wordCount() < 6
    }


    private fun containsConvertDollarToReal(result: String): Boolean {
        return result.contains("d??lar para real", true) && !result.contains("canadense", true) && result.wordCount() < 6 ||
                result.contains("d??lares para real", true) && !result.contains("canadense", true) && result.wordCount() < 6 ||
                result.contains("d??lar pra real", true) && !result.contains("canadense", true) && result.wordCount() < 6 ||
                result.contains("d??lares pra real", true) && !result.contains("canadense", true) && result.wordCount() < 6 ||
                result.contains("d??lar em real", true) && !result.contains("canadense", true) && result.wordCount() < 6 ||
                result.contains("d??lares em real", true) && !result.contains("canadense", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("em real", true) && !result.contains("canadense", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("pra real", true) && !result.contains("canadense", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("para real", true) && !result.contains("canadense", true) && result.wordCount() < 6
    }

    private fun containsConvertDollarToPeso(result: String): Boolean {
        return result.contains("d??lar para peso", true) && result.wordCount() < 6 ||
                result.contains("d??lar para peso argentino", true) && result.wordCount() < 6 ||
                result.contains("d??lar para argentino", true) && result.wordCount() < 6 ||
                result.contains("d??lar para peso argentina", true) && result.wordCount() < 6 ||
                result.contains("d??lar para argentina", true) && result.wordCount() < 6 ||
                result.contains("d??lares para peso", true) && result.wordCount() < 6 ||
                result.contains("d??lares para peso argentino", true) && result.wordCount() < 6 ||
                result.contains("d??lares para argentino", true) && result.wordCount() < 6 ||
                result.contains("d??lares para peso argentina", true) && result.wordCount() < 6 ||
                result.contains("d??lares para argentina", true) && result.wordCount() < 6 ||
                result.contains("d??lar pra peso", true) && result.wordCount() < 6 ||
                result.contains("d??lar pra peso argentino", true) && result.wordCount() < 6 ||
                result.contains("d??lar pra argentino", true) && result.wordCount() < 6 ||
                result.contains("d??lar pra peso argentina", true) && result.wordCount() < 6 ||
                result.contains("d??lar pra argentina", true) && result.wordCount() < 6 ||
                result.contains("d??lares pra peso", true) && result.wordCount() < 6 ||
                result.contains("d??lares pra peso argentino", true) && result.wordCount() < 6 ||
                result.contains("d??lares pra argentino", true) && result.wordCount() < 6 ||
                result.contains("d??lares pra peso argentina", true) && result.wordCount() < 6 ||
                result.contains("d??lares pra argentina", true) && result.wordCount() < 6 ||
                result.contains("d??lar em peso", true) && result.wordCount() < 6 ||
                result.contains("d??lar em peso argentino", true) && result.wordCount() < 6 ||
                result.contains("d??lar em argentino", true) && result.wordCount() < 6 ||
                result.contains("d??lar em peso argentina", true) && result.wordCount() < 6 ||
                result.contains("d??lar em argentina", true) && result.wordCount() < 6 ||
                result.contains("d??lares em peso", true) && result.wordCount() < 6 ||
                result.contains("d??lares em peso argentino", true) && result.wordCount() < 6 ||
                result.contains("d??lares em argentino", true) && result.wordCount() < 6 ||
                result.contains("d??lares em peso argentina", true) && result.wordCount() < 6 ||
                result.contains("d??lares em argentina", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("em peso", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("em peso argentino", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("em argentino", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("em peso argentina", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("em argentina", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("pra peso", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("pra peso argentino", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("pra argentino", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("pra peso argentina", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("pra argentina", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("para peso", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("para peso argentino", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("para argentino", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("para peso argentina", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("para argentina", true) && result.wordCount() < 6
    }

    private fun containsConvertRealToCanada(result: String): Boolean {
        return result.contains("real para d??lar canadense", true) && result.wordCount() < 6 ||
                result.contains("real para canad??", true) && result.wordCount() < 6 ||
                result.contains("real para canadense", true) && result.wordCount() < 6 ||
                result.contains("reais para d??lar canadense", true) && result.wordCount() < 6 ||
                result.contains("reais para canad??", true) && result.wordCount() < 6 ||
                result.contains("reais para canadense", true) && result.wordCount() < 6 ||
                result.contains("real pra d??lar canadense", true) && result.wordCount() < 6 ||
                result.contains("real pra canad??") && result.wordCount() < 6 ||
                result.contains("real pra canadense", true) && result.wordCount() < 6 ||
                result.contains("reais pra d??lar canadense", true) && result.wordCount() < 6 ||
                result.contains("reais pra canad??", true) && result.wordCount() < 6 ||
                result.contains("reais pra canadense") && result.wordCount() < 6 ||
                result.contains("real em d??lar canadense", true) && result.wordCount() < 6 ||
                result.contains("real em canad??", true) && result.wordCount() < 6 ||
                result.contains("real em canadense", true) && result.wordCount() < 6 ||
                result.contains("reais em d??lar canadense", true) && result.wordCount() < 6 ||
                result.contains("reais em canad??", true) && result.wordCount() < 6 ||
                result.contains("reais em canadense", true) && result.wordCount() < 6 ||
                result.contains("r??is para dona canadense", true) && result.wordCount() < 6 ||
                result.contains("r??is para canad??", true) && result.wordCount() < 6 ||
                result.contains("r??is para canadense", true) && result.wordCount() < 6 ||
                result.contains("r??is pra dona canadense", true) && result.wordCount() < 6 ||
                result.contains("r??is pra canad??", true) && result.wordCount() < 6 ||
                result.contains("r??is pra canadense", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("em d??lar canadense", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("em canad??", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("em canadense", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("pra d??lar canadense", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("pra canad??", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("pra canadense", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("para d??lar canadense", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("para canad??", true) && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("para canadense", true) && result.wordCount() < 6
    }

    private fun containsConvertDollarToCanada(result: String): Boolean {
        return result.contains("d??lar para canadense", true) && result.wordCount() < 6 ||
                result.contains("d??lar para canad??", true) && result.wordCount() < 6 ||
                result.contains("d??lar para d??lar canadense", true) && result.wordCount() < 6 ||
                result.contains("d??lares para canadense", true) && result.wordCount() < 6 ||
                result.contains("d??lares para canad??", true) && result.wordCount() < 6 ||
                result.contains("d??lares para d??lar canadense", true) && result.wordCount() < 6 ||
                result.contains("d??lar pra canadense", true) && result.wordCount() < 6 ||
                result.contains("d??lar pra canad??", true) && result.wordCount() < 6 ||
                result.contains("d??lar pra d??lar canadense", true) && result.wordCount() < 6 ||
                result.contains("d??lares pra canadense", true) && result.wordCount() < 6 ||
                result.contains("d??lares pra canad??", true) && result.wordCount() < 6 ||
                result.contains("d??lares pra d??lar canadense", true) && result.wordCount() < 6 ||
                result.contains("d??lar em canadense", true) && result.wordCount() < 6 ||
                result.contains("d??lar em canad??", true) && result.wordCount() < 6 ||
                result.contains("d??lar em d??lar canadense", true) && result.wordCount() < 6 ||
                result.contains("d??lares em canadense", true) && result.wordCount() < 6 ||
                result.contains("d??lares em canad??", true) && result.wordCount() < 6 ||
                result.contains("d??lares em d??lar canadense", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("em canad??", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("em canadense", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("em d??lar canadense", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("pra canad??", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("pra canadense", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("pra d??lar canadense", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("para canad??", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("para canadense", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("para d??lar canadense", true) && result.wordCount() < 6
    }

    private fun containsConvertCanadaToReal(result: String): Boolean {
        return result.contains("d??lar canadense para real", true) && result.wordCount() < 6 ||
                result.contains("canadense para real", true) && result.wordCount() < 6 ||
                result.contains("canad?? para real", true) && result.wordCount() < 6 ||
                result.contains("d??lares canadense para real", true) && result.wordCount() < 6 ||
                result.contains("canadense para real", true) && result.wordCount() < 6 ||
                result.contains("canad?? para real", true) && result.wordCount() < 6 ||
                result.contains("d??lar canadense pra real", true) && result.wordCount() < 6 ||
                result.contains("canadense pra real", true) && result.wordCount() < 6 ||
                result.contains("canad?? pra real", true) && result.wordCount() < 6 ||
                result.contains("d??lares canadense pra real", true) && result.wordCount() < 6 ||
                result.contains("canadense pra real", true) && result.wordCount() < 6 ||
                result.contains("canad?? pra real", true) && result.wordCount() < 6 ||
                result.contains("d??lar canadense em real", true) && result.wordCount() < 6 ||
                result.contains("canadense em real", true) && result.wordCount() < 6 ||
                result.contains("canad?? em real", true) && result.wordCount() < 6 ||
                result.contains("d??lares canadense em real", true) && result.wordCount() < 6 ||
                result.contains("canadense em real", true) && result.wordCount() < 6 ||
                result.contains("canad?? em real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("canadense em real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("canad?? em real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("canadense pra real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("canad?? pra real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("canadense para real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("canad?? para real", true) && result.wordCount() < 6
    }

    private fun containsConvertCanadaToDollar(result: String): Boolean {
        return result.contains("d??lar canadense para d??lar", true) && result.wordCount() < 6 ||
                result.contains("canadense para d??lar", true) && result.wordCount() < 6 ||
                result.contains("canadense para d??lar", true) && result.wordCount() < 6 ||
                result.contains("d??lares canadense para d??lar", true) && result.wordCount() < 6 ||
                result.contains("canadense para d??lar", true) && result.wordCount() < 6 ||
                result.contains("canadense para d??lar", true) && result.wordCount() < 6 ||
                result.contains("d??lar canadense pra d??lar", true) && result.wordCount() < 6 ||
                result.contains("canadense pra d??lar", true) && result.wordCount() < 6 ||
                result.contains("canadense pra d??lar", true) && result.wordCount() < 6 ||
                result.contains("d??lares canadense pra d??lar", true) && result.wordCount() < 6 ||
                result.contains("canadense pra d??lar", true) && result.wordCount() < 6 ||
                result.contains("canadense pra d??lar", true) && result.wordCount() < 6 ||
                result.contains("d??lar canadense em d??lar", true) && result.wordCount() < 6 ||
                result.contains("canadense em d??lar", true) && result.wordCount() < 6 ||
                result.contains("canadense em d??lar", true) && result.wordCount() < 6 ||
                result.contains("d??lares canadense em d??lar", true) && result.wordCount() < 6 ||
                result.contains("canadense em d??lar", true) && result.wordCount() < 6 ||
                result.contains("canadense em d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("canadense em d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("canad?? em d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("canadense pra d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("canad?? pra d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("canadense para d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("canad?? para d??lar", true) && result.wordCount() < 6
    }

    private fun containsConvertArgentinoToReal(result: String): Boolean {
        return result.contains("peso para real", true) && result.wordCount() < 6 ||
                result.contains("argentino para real", true) && result.wordCount() < 6 ||
                result.contains("argentina para real", true) && result.wordCount() < 6 ||
                result.contains("peso argentino para real", true) && result.wordCount() < 6 ||
                result.contains("peso argentina para real", true) && result.wordCount() < 6 ||
                result.contains("peso pra real", true) && result.wordCount() < 6 ||
                result.contains("argentino pra real", true) && result.wordCount() < 6 ||
                result.contains("argentina pra real", true) && result.wordCount() < 6 ||
                result.contains("peso argentino pra real", true) && result.wordCount() < 6 ||
                result.contains("peso argentina pra real", true) && result.wordCount() < 6 ||
                result.contains("peso em real", true) && result.wordCount() < 6 ||
                result.contains("argentino em real", true) && result.wordCount() < 6 ||
                result.contains("argentina em real", true) && result.wordCount() < 6 ||
                result.contains("peso argentino em real", true) && result.wordCount() < 6 ||
                result.contains("peso argentina em real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("peso em real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("argentino em real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("argentina em real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("peso argentino em real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("peso argentina em real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("peso pra real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("argentino pra real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("argentina pra real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("peso argentino pra real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("peso argentina pra real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("peso para real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("argentina para real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("argentino para real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("peso argentino para real", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("peso argentina para real", true) && result.wordCount() < 6
    }

    private fun containsConvertArgentinoToDollar(result: String): Boolean {
        return result.contains("peso para d??lar", true) && result.wordCount() < 6 ||
                result.contains("argentino para d??lar", true) && result.wordCount() < 6 ||
                result.contains("argentina para d??lar", true) && result.wordCount() < 6 ||
                result.contains("peso argentino para d??lar", true) && result.wordCount() < 6 ||
                result.contains("peso argentina para d??lar", true) && result.wordCount() < 6 ||
                result.contains("peso pra d??lar", true) && result.wordCount() < 6 ||
                result.contains("argentino pra d??lar", true) && result.wordCount() < 6 ||
                result.contains("argentina pra d??lar", true) && result.wordCount() < 6 ||
                result.contains("peso argentino pra d??lar", true) && result.wordCount() < 6 ||
                result.contains("peso argentina pra d??lar", true) && result.wordCount() < 6 ||
                result.contains("peso em d??lar", true) && result.wordCount() < 6 ||
                result.contains("argentino em d??lar", true) && result.wordCount() < 6 ||
                result.contains("argentina em d??lar", true) && result.wordCount() < 6 ||
                result.contains("peso argentino em d??lar", true) && result.wordCount() < 6 ||
                result.contains("peso argentina em d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("peso em d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("argentino em d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("argentina em d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("peso argentino em d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("peso argentina em d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("peso pra d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("argentino pra d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("argentina pra d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("peso argentino pra d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("peso argentina pra d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("peso para d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("argentina para d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("argentino para d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("peso argentino para d??lar", true) && result.wordCount() < 6 ||
                result.contains("$") && result.contains("peso argentina para d??lar", true) && result.wordCount() < 6
    }

    private fun containsConvertToDollar(result: String): Boolean {
        return  !result.contains("real", true) &&
                !result.contains("canadense", true) &&
                !result.contains("peso", true) &&
                !result.contains("$") &&
                !result.contains("r$", true) &&
                (
                        result.contains("para d??lar", true) && result.wordCount() < 6 ||
                        result.contains("pra d??lar", true) && result.wordCount() < 6 ||
                        result.contains("em d??lar", true) && result.wordCount() < 6
                        )
    }

    private fun containsConvertToReal(result: String): Boolean {
        return  !result.contains("d??lar", true) &&
                !result.contains("canadense", true) &&
                !result.contains("peso", true) &&
                !result.contains("$") &&
                !result.contains("r$") &&
                (
                        result.contains("para real", true) && result.wordCount() < 6 ||
                        result.contains("pra real", true) && result.wordCount() < 6 ||
                        result.contains("em real", true) && result.wordCount() < 6
                        )
    }

    private fun containsConvertToCanada(result: String): Boolean {
        return  !result.contains("real", true) &&
                !result.contains("peso", true) &&
                !result.contains("$") &&
                !result.contains("r$") &&
                (
                        result.contains("para canada", true) && result.wordCount() < 6 ||
                        result.contains("pra canada", true) && result.wordCount() < 6 ||
                        result.contains("em canada", true) && result.wordCount() < 6 ||
                        result.contains("para canadense", true) && result.wordCount() < 6 ||
                        result.contains("pra canadense", true) && result.wordCount() < 6 ||
                        result.contains("em canadense", true) && result.wordCount() < 6
                        )
    }

    private fun containsConvertToArgentine(result: String): Boolean {
        return  !result.contains("real", true) &&
                !result.contains("d??lar", true) &&
                !result.contains("canadense", true) &&
                !result.contains("$", true) &&
                !result.contains("r$", true) &&
                (
                        result.contains("para peso", true) && result.wordCount() < 6 ||
                                result.contains("pra peso", true) && result.wordCount() < 6 ||
                                result.contains("em peso", true) && result.wordCount() < 6 ||
                                result.contains("para peso argentino", true) && result.wordCount() < 6 ||
                                result.contains("pra peso argentino", true) && result.wordCount() < 6 ||
                                result.contains("em peso argentino", true) && result.wordCount() < 6 ||
                                result.contains("para peso argentina", true) && result.wordCount() < 6 ||
                                result.contains("pra peso argentina", true) && result.wordCount() < 6 ||
                                result.contains("em peso argentina", true) && result.wordCount() < 6
                        )
    }

/*
    const val CONVERT_CAD_TO_USD = 414
    const val CONVERT_CAD_TO_BRL = 415*/

    private fun containsValueCoin(result: String): Boolean{
        return try {
            result.toDouble()
            !result.contains("r$") &&
                    !result.contains("$") &&
                    !result.contains("d??lar", true) &&
                    !result.contains("real", true) &&
                    !result.contains("canadense", true) &&
                    !result.contains("canada", true) &&
                    !result.contains("argentino", true) &&
                    !result.contains("argentina", true) &&
                    !result.contains("peso", true)
        }catch (e: Exception){
            Log.e("containsValueCoin", "$result Exception $e")
            false
        }
    }

    private fun containsValueCoinReal(result: String): Boolean{
        return try {
            result.convertValueReal().toDouble()
            !result.contains("r$") &&
                    !result.contains("$") &&
                    !result.contains("d??lar") &&
                    !result.contains("real") &&
                    !result.contains("canadense") &&
                    !result.contains("canada", true) &&
                    !result.contains("argentino", true) &&
                    !result.contains("argentina", true) &&
                    !result.contains("peso")
        }catch (e: Exception){
            Log.e("ValueCoinValue", "$result Exception $e")
            false
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause(){
        stopSpeach()
        stopRecognizer()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun desroy(){
        stopSpeach()
        stopRecognizer()
        recognizer.setRecognitionListener(null)
        recognizer.destroy()
        textToSpeach?.let {
            if(it.isSpeaking) {
                it.shutdown()
            }
            textToSpeach = null
        }
    }
}