package com.gitlab.j_m_hoffmann.meditate.ui.session

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gitlab.j_m_hoffmann.meditate.databinding.SessionFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SessionFragment : Fragment() {

    private val sessionViewModel by viewModels<SessionViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        SessionFragmentBinding.inflate(inflater).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sessionViewModel
        }.root
}
