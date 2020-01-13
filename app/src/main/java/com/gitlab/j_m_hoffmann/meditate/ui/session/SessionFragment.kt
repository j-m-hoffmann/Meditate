package com.gitlab.j_m_hoffmann.meditate.ui.session

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gitlab.j_m_hoffmann.meditate.R
import com.gitlab.j_m_hoffmann.meditate.databinding.SessionFragmentBinding
import com.gitlab.j_m_hoffmann.meditate.ui.util.getViewModelFactory
import com.google.android.material.snackbar.Snackbar

class SessionFragment : Fragment() {

    private lateinit var binding: SessionFragmentBinding

    private lateinit var progressListener: OnSessionProgressListener

    private val sessionViewModel by viewModels<SessionViewModel> { getViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SessionFragmentBinding.inflate(inflater)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sessionViewModel
        }

        //region Observers
        progressListener = requireActivity() as OnSessionProgressListener

        sessionViewModel.apply {
            sessionInProgress.observe(viewLifecycleOwner, Observer { inProgress ->
                if (inProgress) {
                    progressListener.hideNavigation()
                    Snackbar.make(binding.root, R.string.concentrate, Snackbar.LENGTH_LONG).show()
                } else {
                    progressListener.showNavigation()
                }
            })
        }
        //endregion
    }

    interface OnSessionProgressListener {
        fun hideNavigation()
        fun showNavigation()
    }
}
