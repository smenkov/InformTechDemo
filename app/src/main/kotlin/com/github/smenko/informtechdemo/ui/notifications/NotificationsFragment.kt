package com.github.smenko.informtechdemo.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.smenko.informtechdemo.R
import com.github.smenko.informtechdemo.utils.clickWithDebounce
import com.github.smenko.informtechdemo.databinding.FragmentNotificationsBinding
import com.google.android.material.snackbar.Snackbar

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnTest.clickWithDebounce(debounceTime = 2000) {
            Snackbar.make(
                binding.root,
                "Snack OK!",
                Snackbar.LENGTH_LONG
            ).setAnchorView(R.id.nav_view).show()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}