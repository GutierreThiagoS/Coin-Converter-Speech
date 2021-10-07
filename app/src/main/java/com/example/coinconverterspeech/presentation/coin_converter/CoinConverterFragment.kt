package com.example.coinconverterspeech.presentation.coin_converter

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.example.coinconverterspeech.core.extensions.*
import com.example.coinconverterspeech.data.model.Coin
import com.example.coinconverterspeech.databinding.FragmentCoinConverterBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class CoinConverterFragment: Fragment() {


    private val viewModel by viewModel<CoinConverterViewModel>()
    private val dialog by lazy { requireContext().createProgressDialog() }
    private val binding by lazy { FragmentCoinConverterBinding.inflate(layoutInflater) }

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
            val value = viewModel.state.value
            (value as? CoinConverterViewModel.State.Success)?.let {
                val exchange = it.exchange.copy(bid = it.exchange.bid * binding.tilValue.text.toDouble())
                viewModel.saveExchange(exchange)
            }
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
                CoinConverterViewModel.State.Saved -> {
                    dialog.dismiss()
                    requireContext().createDialog {
                        setMessage("item salvo com sucesso!")
                    }.show()
                }
            }
        }
    }

    private fun success(it: CoinConverterViewModel.State.Success) {
        dialog.dismiss()
        binding.btnSave.isEnabled = true

        val selectedCoin = binding.tilTo.text
        val coin = Coin.getByName(selectedCoin)

        val result = it.exchange.bid * binding.tilValue.text.toDouble()

        binding.tvResult.text = result.formatCurrency(coin.locale)
    }
}