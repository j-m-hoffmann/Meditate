package com.gitlab.j_m_hoffmann.meditate.ui.session

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gitlab.j_m_hoffmann.meditate.R
import com.gitlab.j_m_hoffmann.meditate.databinding.SessionFragmentBinding
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class SessionFragment : DaggerFragment() {

    private lateinit var binding: SessionFragmentBinding

    private lateinit var progressListener: OnSessionProgressListener

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val sessionViewModel by viewModels<SessionViewModel> { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
