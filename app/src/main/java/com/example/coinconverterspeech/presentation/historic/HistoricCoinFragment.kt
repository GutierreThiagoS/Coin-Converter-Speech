package com.example.coinconverterspeech.presentation.historic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.coinconverterspeech.core.extensions.createDialog
import com.example.coinconverterspeech.core.extensions.createProgressDialog
import com.example.coinconverterspeech.core.extensions.resultSpeech
import com.example.coinconverterspeech.data.model.ExchangeValue
import com.example.coinconverterspeech.databinding.FragmentHistoricBinding
import com.example.coinconverterspeech.presentation.State
import com.example.coinconverterspeech.presentation.historic.adapter.HistoricListCoinAdapter
import com.example.coinconverterspeech.presentation.speech.SpeechListener
import com.example.coinconverterspeech.presentation.speech.SpeechUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.ref.WeakReference

class HistoricCoinFragment: Fragment(), HistoricHandler, SpeechListener{

    private val adapter by lazy { HistoricListCoinAdapter(this) }
    private val dialog by lazy { requireContext().createProgressDialog() }
    private val viewModel by viewModel<HistoryViewModel>()
    private val binding by lazy { FragmentHistoricBinding.inflate(layoutInflater) }
    private val speechUtils by lazy { SpeechUtils(WeakReference(this), viewLifecycleOwner) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvHistory.adapter = adapter
        binding.rvHistory.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )

        bindObserve()

        lifecycle.addObserver(viewModel)
    }

    private fun bindObserve() {
        viewModel.state.observe(requireActivity()) {
            when (it) {
                State.Loading -> dialog.show()
                is State.Error -> {
                    dialog.dismiss()
                    requireContext().createDialog {
                        setMessage(it.error.message)
                    }.show()
                }
                is State.Success -> {
                    dialog.dismiss()
                    adapter.addAll(it.list)
                }
                else -> { dialog.dismiss() }
            }
        }
    }

    override fun onClickMoveToTrash(item: ExchangeValue, position: Int) {
        viewModel.setMoveToTrash(item)
    }

    override fun onClickSpeech(item: ExchangeValue) {
        speechUtils.speak(item.resultSpeech())
    }

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onDoneSpeaking(requestCode: Int?) {}

    override fun onSpeechResults(requestCode: Int?, status: Int, result: String) {}

    override fun onSpeechStarted() {}

    override fun onSpeechFinished() {}
}