package com.gitlab.j_m_hoffmann.meditate.ui.session

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.gitlab.j_m_hoffmann.meditate.databinding.SessionFragmentBinding
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class SessionFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val sessionViewModel by activityViewModels<SessionViewModel> { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = SessionFragmentBinding.inflate(inflater)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sessionViewModel
        }

        return binding.root
    }
}
