package com.gitlab.j_m_hoffmann.meditate.ui.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gitlab.j_m_hoffmann.meditate.databinding.TimerFragmentBinding
import com.gitlab.j_m_hoffmann.meditate.ui.util.getViewModelFactory

class TimerFragment : Fragment() {

    private lateinit var binding: TimerFragmentBinding

    private lateinit var progressListener: OnSessionProgressListener

    private val timerViewModel by viewModels<TimerViewModel> { getViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TimerFragmentBinding.inflate(inflater)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = timerViewModel
        }

        //region Observers
        progressListener = requireActivity() as OnSessionProgressListener

        timerViewModel.apply {
            sessionInProgress.observe(viewLifecycleOwner, Observer { inProgress ->
                if (inProgress) {
                    progressListener.hideNavigation()
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
