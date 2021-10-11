package com.example.coinconverterspeech.presentation.deleted

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.coinconverterspeech.core.extensions.createDialog
import com.example.coinconverterspeech.core.extensions.createProgressDialog
import com.example.coinconverterspeech.data.model.ExchangeValue
import com.example.coinconverterspeech.databinding.FragmentDeletedBinding
import com.example.coinconverterspeech.presentation.State
import com.example.coinconverterspeech.presentation.deleted.adapter.DeletedListAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeletedListFragment: Fragment(), DeletedHandler {

    private val adapter by lazy { DeletedListAdapter(this) }
    private val dialog by lazy { requireContext().createProgressDialog() }
    private val viewModel by viewModel<DeletedViewModel>()

    private val binding by lazy { FragmentDeletedBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvDeleted.adapter = adapter
        binding.rvDeleted.addItemDecoration(
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
            }
        }
    }

    override fun onClickRestore(item: ExchangeValue) {
        viewModel.restorePermanent(item)
    }

    override fun onCLickRemove(item: ExchangeValue) {
        viewModel.deletedPermanent(item)
    }
}