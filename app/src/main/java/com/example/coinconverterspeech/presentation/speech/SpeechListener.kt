package com.example.coinconverterspeech.presentation.speech

interface SpeechListener {
    fun onRmsChanged(rmsdB: Float)
    fun onDoneSpeaking(requestCode: Int?)
    fun onSpeechResults(requestCode: Int?, status: Int, result: String)
    fun onSpeechStarted()
    fun onSpeechFinished()
}