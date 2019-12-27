package com.gitlab.j_m_hoffmann.meditate.ui.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gitlab.j_m_hoffmann.meditate.databinding.TimerFragmentBinding

class TimerFragment : Fragment() {

    private lateinit var binding: TimerFragmentBinding

    private lateinit var listener: OnSessionChangeListener

    private lateinit var timerViewModel: TimerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TimerFragmentBinding.inflate(inflater)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        timerViewModel = ViewModelProvider(this)[TimerViewModel::class.java]

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = timerViewModel
        }

        //region Observers
        listener = requireActivity() as OnSessionChangeListener

        timerViewModel.apply {
            sessionInProgress.observe(viewLifecycleOwner, Observer { inProgress ->
                if (inProgress) {
                    listener.disableNavigation()
                } else {
                    listener.enableNavigation()
                }
            })
        }
        //endregion
    }

    interface OnSessionChangeListener {
        fun disableNavigation()
        fun enableNavigation()
    }
}
