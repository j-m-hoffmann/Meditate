package com.gitlab.j_m_hoffmann.meditate.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gitlab.j_m_hoffmann.meditate.databinding.ProgressFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProgressFragment : Fragment() {

    private val progressViewModel by viewModels<ProgressViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ProgressFragmentBinding.inflate(inflater)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = progressViewModel
            }.root
}
