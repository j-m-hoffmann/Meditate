package com.gitlab.j_m_hoffmann.meditate.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.gitlab.j_m_hoffmann.meditate.databinding.ProgressFragmentBinding
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class ProgressFragment : DaggerFragment() {

    private lateinit var binding: ProgressFragmentBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val progressViewModel by viewModels<ProgressViewModel> { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ProgressFragmentBinding.inflate(inflater)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = progressViewModel
        }
    }

}
