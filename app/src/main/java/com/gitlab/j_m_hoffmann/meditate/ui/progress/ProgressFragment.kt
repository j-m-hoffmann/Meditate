package com.gitlab.j_m_hoffmann.meditate.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gitlab.j_m_hoffmann.meditate.databinding.ProgressFragmentBinding

class ProgressFragment : Fragment() {

    private lateinit var binding: ProgressFragmentBinding

    private lateinit var progressViewModel: ProgressViewModel

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
        progressViewModel = ViewModelProvider(this)[ProgressViewModel::class.java]

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = progressViewModel
        }
    }

}
