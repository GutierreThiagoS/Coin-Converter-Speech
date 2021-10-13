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
                speak("Ocorreu um erro! Estou tendo problemas de conexão com a internet.", NO_SAVE)
            }
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT->{
                speak("Ocorreu um erro! Por favor.. Verifique conexão com a internet.", NO_SAVE)
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
        return result.contains("real para dólar") && !result.contains("canadense") && result.wordCount() < 6 ||
                result.contains("reais para dólar") && !result.contains("canadense") && result.wordCount() < 6 ||
                result.contains("real pra dólar") && !result.contains("canadense") && result.wordCount() < 6 ||
                result.contains("reais pra dólar") && !result.contains("canadense") && result.wordCount() < 6 ||
                result.contains("real em dólar") && !result.contains("canadense") && result.wordCount() < 6 ||
                result.contains("reais em dólar") && !result.contains("canadense") && result.wordCount() < 6 ||
                result.contains("réis para dona") && !result.contains("canadense") && result.wordCount() < 6 ||
                result.contains("réis pra dona") && !result.contains("canadense") && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("em dólar") && !result.contains("canadense") && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("pra dólar") && !result.contains("canadense") && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("para dólar") && !result.contains("canadense") && result.wordCount() < 6
    }

    private fun containsConvertDollarToReal(result: String): Boolean {
        return result.contains("dólar para real") && !result.contains("canadense") && result.wordCount() < 6 ||
                result.contains("dólares para real") && !result.contains("canadense") && result.wordCount() < 6 ||
                result.contains("dólar pra real") && !result.contains("canadense") && result.wordCount() < 6 ||
                result.contains("dólares pra real") && !result.contains("canadense") && result.wordCount() < 6 ||
                result.contains("dólar em real") && !result.contains("canadense") && result.wordCount() < 6 ||
                result.contains("dólares em real") && !result.contains("canadense") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("em real") && !result.contains("canadense") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("pra real") && !result.contains("canadense") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("para real") && !result.contains("canadense") && result.wordCount() < 6
    }

    private fun containsConvertRealToPeso(result: String): Boolean {
        return result.contains("real para peso") && result.wordCount() < 6 ||
                result.contains("real para peso argentino") && result.wordCount() < 6 ||
                result.contains("reais para peso") && result.wordCount() < 6 ||
                result.contains("reais para peso argentino") && result.wordCount() < 6 ||
                result.contains("real pra peso") && result.wordCount() < 6 ||
                result.contains("real pra peso argentino") && result.wordCount() < 6 ||
                result.contains("reais pra peso") && result.wordCount() < 6 ||
                result.contains("reais pra peso argentino") && result.wordCount() < 6 ||
                result.contains("real em peso") && result.wordCount() < 6 ||
                result.contains("real em peso argentino") && result.wordCount() < 6 ||
                result.contains("reais em peso") && result.wordCount() < 6 ||
                result.contains("reais em peso argentino") && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("em peso") && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("em peso argentino") && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("pra peso") && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("pra peso argentino") && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("para peso") && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("para peso argentino") && result.wordCount() < 6
    }

    private fun containsConvertDollarToPeso(result: String): Boolean {
        return result.contains("dólar para peso") && result.wordCount() < 6 ||
                result.contains("dólar para peso argentino") && result.wordCount() < 6 ||
                result.contains("dólares para peso") && result.wordCount() < 6 ||
                result.contains("dólares para peso argentino") && result.wordCount() < 6 ||
                result.contains("dólar pra peso") && result.wordCount() < 6 ||
                result.contains("dólar pra peso argentino") && result.wordCount() < 6 ||
                result.contains("dólares pra peso") && result.wordCount() < 6 ||
                result.contains("dólares pra peso argentino") && result.wordCount() < 6 ||
                result.contains("dólar em peso") && result.wordCount() < 6 ||
                result.contains("dólar em peso argentino") && result.wordCount() < 6 ||
                result.contains("dólares em peso") && result.wordCount() < 6 ||
                result.contains("dólares em peso argentino") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("em peso") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("em peso argentino") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("pra peso") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("pra peso argentino") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("para peso") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("para peso argentino") && result.wordCount() < 6
    }

    private fun containsConvertRealToCanada(result: String): Boolean {
        return result.contains("real para dólar canadense") && result.wordCount() < 6 ||
                result.contains("real para canadense") && result.wordCount() < 6 ||
                result.contains("reais para dólar canadense") && result.wordCount() < 6 ||
                result.contains("reais para canadense") && result.wordCount() < 6 ||
                result.contains("real pra dólar canadense") && result.wordCount() < 6 ||
                result.contains("real pra canadense") && result.wordCount() < 6 ||
                result.contains("reais pra dólar canadense") && result.wordCount() < 6 ||
                result.contains("reais pra canadense") && result.wordCount() < 6 ||
                result.contains("real em dólar canadense") && result.wordCount() < 6 ||
                result.contains("real em canadense") && result.wordCount() < 6 ||
                result.contains("reais em dólar canadense") && result.wordCount() < 6 ||
                result.contains("reais em canadense") && result.wordCount() < 6 ||
                result.contains("réis para dona canadense") && result.wordCount() < 6 ||
                result.contains("réis para canadense") && result.wordCount() < 6 ||
                result.contains("réis pra dona canadense") && result.wordCount() < 6 ||
                result.contains("réis pra canadense") && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("em dólar canadense") && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("em canadense") && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("pra dólar canadense") && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("pra canadense") && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("para dólar canadense") && result.wordCount() < 6 ||
                result.contains("r$") && result.contains("para canadense") && result.wordCount() < 6
    }

    private fun containsConvertDollarToCanada(result: String): Boolean {
        return result.contains("dólar para canadense") && result.wordCount() < 6 ||
                result.contains("dólar para dólar canadense") && result.wordCount() < 6 ||
                result.contains("dólares para canadense") && result.wordCount() < 6 ||
                result.contains("dólares para dólar canadense") && result.wordCount() < 6 ||
                result.contains("dólar pra canadense") && result.wordCount() < 6 ||
                result.contains("dólar pra dólar canadense") && result.wordCount() < 6 ||
                result.contains("dólares pra canadense") && result.wordCount() < 6 ||
                result.contains("dólares pra dólar canadense") && result.wordCount() < 6 ||
                result.contains("dólar em canadense") && result.wordCount() < 6 ||
                result.contains("dólar em dólar canadense") && result.wordCount() < 6 ||
                result.contains("dólares em canadense") && result.wordCount() < 6 ||
                result.contains("dólares em dólar canadense") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("em canadense") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("em dólar canadense") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("pra canadense") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("pra dólar canadense") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("para canadense") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("para dólar canadense") && result.wordCount() < 6
    }

    private fun containsConvertCanadaToReal(result: String): Boolean {
        return result.contains("dólar canadense para real") && result.wordCount() < 6 ||
                result.contains("canadense para real") && result.wordCount() < 6 ||
                result.contains("dólares canadense para real") && result.wordCount() < 6 ||
                result.contains("canadense para real") && result.wordCount() < 6 ||
                result.contains("dólar canadense pra real") && result.wordCount() < 6 ||
                result.contains("canadense pra real") && result.wordCount() < 6 ||
                result.contains("dólares canadense pra real") && result.wordCount() < 6 ||
                result.contains("canadense pra real") && result.wordCount() < 6 ||
                result.contains("dólar canadense em real") && result.wordCount() < 6 ||
                result.contains("canadense em real") && result.wordCount() < 6 ||
                result.contains("dólares canadense em real") && result.wordCount() < 6 ||
                result.contains("canadense em real") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("canadense em real") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("canadense pra real") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("canadense para real") && result.wordCount() < 6
    }

    private fun containsConvertCanadaToDollar(result: String): Boolean {
        return result.contains("dólar canadense para dólar") && result.wordCount() < 6 ||
                result.contains("canadense para dólar") && result.wordCount() < 6 ||
                result.contains("dólares canadense para dólar") && result.wordCount() < 6 ||
                result.contains("canadense para dólar") && result.wordCount() < 6 ||
                result.contains("dólar canadense pra dólar") && result.wordCount() < 6 ||
                result.contains("canadense pra dólar") && result.wordCount() < 6 ||
                result.contains("dólares canadense pra dólar") && result.wordCount() < 6 ||
                result.contains("canadense pra dólar") && result.wordCount() < 6 ||
                result.contains("dólar canadense em dólar") && result.wordCount() < 6 ||
                result.contains("canadense em dólar") && result.wordCount() < 6 ||
                result.contains("dólares canadense em dólar") && result.wordCount() < 6 ||
                result.contains("canadense em dólar") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("canadense em dólar") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("canadense pra dólar") && result.wordCount() < 6 ||
                result.contains("$") && result.contains("canadense para dólar") && result.wordCount() < 6
    }

/*
    const val CONVERT_CAD_TO_USD = 414
    const val CONVERT_CAD_TO_BRL = 415*/

    private fun containsValueCoin(result: String): Boolean{
        return try {
            result.toDouble()
            !result.contains("r$") &&
                    !result.contains("$") &&
                    !result.contains("dólar") &&
                    !result.contains("real") &&
                    !result.contains("canadense") &&
                    !result.contains("peso")
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
                    !result.contains("dólar") &&
                    !result.contains("real") &&
                    !result.contains("canadense") &&
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