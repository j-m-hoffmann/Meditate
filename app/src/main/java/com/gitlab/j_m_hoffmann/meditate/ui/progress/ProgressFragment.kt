package com.gitlab.j_m_hoffmann.meditate.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gitlab.j_m_hoffmann.meditate.databinding.ProgressFragmentBinding
import com.gitlab.j_m_hoffmann.meditate.getViewModelFactory

class ProgressFragment : Fragment() {

    private lateinit var binding: ProgressFragmentBinding

    private val progressViewModel by viewModels<ProgressViewModel> { getViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
