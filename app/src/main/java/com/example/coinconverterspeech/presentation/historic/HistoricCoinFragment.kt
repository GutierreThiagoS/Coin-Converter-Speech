package com.example.coinconverterspeech.presentation.historic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.coinconverterspeech.core.extensions.createDialog
import com.example.coinconverterspeech.core.extensions.createProgressDialog
import com.example.coinconverterspeech.databinding.FragmentHistoricBinding
import com.example.coinconverterspeech.presentation.State
import com.example.coinconverterspeech.presentation.historic.adapter.HistoricListCoinAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoricCoinFragment: Fragment() {

    private val adapter by lazy { HistoricListCoinAdapter() }
    private val dialog by lazy { requireContext().createProgressDialog() }
    private val viewModel by viewModel<HistoryViewModel>()
    private val binding by lazy { FragmentHistoricBinding.inflate(layoutInflater) }

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
            DividerItemDecoration(requireContext(), DividerItemDecoration.HORIZONTAL)
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
            }
        }
    }
}