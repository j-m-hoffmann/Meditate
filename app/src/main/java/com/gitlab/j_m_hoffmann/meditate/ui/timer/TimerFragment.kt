package com.gitlab.j_m_hoffmann.meditate.ui.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

        listener = requireActivity() as OnSessionChangeListener

        binding.apply {
            startSession.setOnClickListener { listener.disableNavigation() }
            stopSession.setOnClickListener { listener.enableNavigation() }
        }
    }

    interface OnSessionChangeListener {
        fun disableNavigation()
        fun enableNavigation()
    }
/*
    fun startSession() {
        listener.disableNavigation()
    }

    fun stopSession() {
        listener.enableNavigation()
    }
*/
}
