package com.example.coinconverterspeech.presentation.coin_converter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.example.coinconverterspeech.core.extensions.*
import com.example.coinconverterspeech.data.Convert
import com.example.coinconverterspeech.data.model.Coin
import com.example.coinconverterspeech.databinding.FragmentCoinConverterBinding
import com.example.coinconverterspeech.presentation.speech.SpeechListener
import com.example.coinconverterspeech.presentation.speech.SpeechUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.ref.WeakReference

class CoinConverterFragment: Fragment(), SpeechListener {

    private val viewModel by viewModel<CoinConverterViewModel>()
    private val dialog by lazy { requireContext().createProgressDialog() }
    private val binding by lazy { FragmentCoinConverterBinding.inflate(layoutInflater) }
    private val speechUtils by lazy { SpeechUtils(WeakReference(this), viewLifecycleOwner) }

    companion object {
        const val MY_PERMISSIONS_RECORD_AUDIO = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindAdapters()
        bindListeners()
        bindObserve()

    }

    private fun bindAdapters() {
        val list = Coin.values()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list)

        binding.tvFrom.setAdapter(adapter)
        binding.tvTo.setAdapter(adapter)

        binding.tvFrom.setText(Coin.USD.name, false)
        binding.tvTo.setText(Coin.BRL.name, false)
    }

    private fun bindListeners() {
        binding.tilValue.editText?.doAfterTextChanged {
            binding.btnConverter.isEnabled = it != null && it.toString().isNotEmpty()
            binding.btnSave.isEnabled = false
        }

        binding.btnConverter.setOnClickListener {
            it.hideSoftKeyboard()
            val search = "${binding.tilFrom.text}-${binding.tilTo.text}"
            viewModel.getExchangeValue(search)
        }

        binding.btnSave.setOnClickListener {
            saveExchange()
        }

        binding.speechInputButton.setOnClickListener {
            permissiomRecordAudio()
        }
    }

    private fun bindObserve() {
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                CoinConverterViewModel.State.Loading -> dialog.show()
                is CoinConverterViewModel.State.Error -> {
                    dialog.dismiss()
                    requireContext().createDialog {
                        setMessage(it.error.message)
                    }.show()
                }
                is CoinConverterViewModel.State.Success -> success(it)
                is CoinConverterViewModel.State.Saved -> {
                    dialog.dismiss()
                    if (viewModel.speechSaved.value == true){
                        viewModel.speechSaved.value = false
                        speechUtils.speak("Ok, Valor Salvo")
                    } else requireContext().createDialog {
                        setMessage("item salvo com sucesso!")
                    }.show()
                }
            }
        }
    }

    private fun consultCoin(result: String, isReal: Boolean){
        viewModel.speechSaved.value = true
        binding.tilValue.text = if (isReal) result.convertValueReal() else result
        val search = "${binding.tilFrom.text}-${binding.tilTo.text}"
        viewModel.getExchangeValue(search)
    }

    private fun success(it: CoinConverterViewModel.State.Success) {
        dialog.dismiss()
        binding.btnSave.isEnabled = true

        val selectedCoin = binding.tilTo.text
        val coin = Coin.getByName(selectedCoin)

        val result = it.exchange.bid * binding.tilValue.text.toDouble()
        if (viewModel.speechSaved.value == true){
            viewModel.speechSaved.value = false
            try {
                speechUtils.speak(
                    Convert.convert(it.exchange).copy(
                        bid = result,coinToConverter = binding.tilValue.text.toDouble()).resultSpeech()
                )
            } catch (e: Exception){
                println("Exception $e Convert Exchan ${Convert.convert(it.exchange).copy(
                    bid = result,coinToConverter = binding.tilValue.text.toDouble())}")
            }
        }
        binding.tvResult.text = result.formatCurrency(coin.locale)
    }

    private fun saveExchange(){
        val value = viewModel.state.value
        (value as? CoinConverterViewModel.State.Success)?.let {
            val exchange = it.exchange.copy(
                bid = it.exchange.bid * binding.tilValue.text.toDouble(),
                coinToConverter = binding.tilValue.text.toDouble()
            )
            viewModel.saveExchange(exchange)
        }
    }

    override fun onSpeechResults(requestCode: Int?, status: Int, result: String) {
        Log.e("RESULT", "$result, REQUESTCODE $requestCode STATUS $status")
        when(requestCode){
            SpeechUtils.SAVE_COIN -> {
                if (binding.tvResult.text.isNotBlank() && binding.tilValue.text.isNotBlank()){
                    viewModel.speechSaved.value = true
                    saveExchange()
                } else speechUtils.speak("Campo vazio!")
            }
            SpeechUtils.VALUE_COIN -> {
                consultCoin(result, false)
            }
            SpeechUtils.VALUE_COIN_REAL -> {
                consultCoin(result, true)
            }
        }
    }

    override fun onRmsChanged(rmsdB: Float) { }

    override fun onDoneSpeaking(requestCode: Int?) { }

    override fun onSpeechStarted() { }

    override fun onSpeechFinished() { }

    private fun permissiomRecordAudio(){
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                    Manifest.permission.RECORD_AUDIO)) {

                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    MY_PERMISSIONS_RECORD_AUDIO)

            } else {
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    MY_PERMISSIONS_RECORD_AUDIO)
            }
        }
        else if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {

            speechUtils.speechInput(SpeechUtils.DEFAULT)

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    speechUtils.speechInput(SpeechUtils.DEFAULT)
                }
            }
        }
    }
}