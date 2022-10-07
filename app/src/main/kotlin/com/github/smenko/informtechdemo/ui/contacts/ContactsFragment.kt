package com.github.smenko.informtechdemo.ui.contacts

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.smenko.informtechdemo.ui.MainActivity
import com.github.smenko.informtechdemo.databinding.FragmentContactsBinding
import com.github.smenko.informtechdemo.models.ContactUiDto
import com.github.smenko.informtechdemo.ui.contacts.adapters.RvContactsAdapter
import com.github.smenko.informtechdemo.utils.*
import com.google.android.material.color.MaterialColors
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil
import kotlin.math.roundToInt

class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    private val contactsViewModel: ContactsViewModel by viewModels({ requireActivity() })
    private val contactsAdapter: RvContactsAdapter by lazy {
        RvContactsAdapter { displayContactToast(it) }
    }
    private var isInitialized = false
    private var isSearching = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkContactPermissions()
        lifecycleScope.launch {
            contactsViewModel.uiState.collectLatest { state ->
                when (state) {
                    is ContactsViewModel.ContactsFragmentState.PermissionResolve -> {
                        if (!state.granted) {
                            binding.mainControls.visibility = View.INVISIBLE
                            binding.permissionControls.visibility = View.VISIBLE
                            binding.btnContactPermRequest.clickWithDebounce(
                                1000
                            ) {
                                requestPermissionLauncher.launch(
                                    android.Manifest.permission.READ_CONTACTS
                                )
                            }
                        }
                    }
                    is ContactsViewModel.ContactsFragmentState.DisplayContacts -> {
                        if (!isInitialized) {
                            isInitialized = true
                            setupViews()
                            contactsViewModel.updateSearchFilterText("")
                            binding.mainControls.visibility = View.VISIBLE
                            binding.permissionControls.visibility = View.GONE
                            binding.scrollerNestedCont.animateShowR2L()
                            binding.rvContacts.animate().alpha(1f).setStartDelay(50)
                                .setInterpolator(AccelerateInterpolator())
                                .setListener(null).duration = 250
                        }
                        state.contactsFlow.collectLatest {
                            contactsAdapter.submitList(it)
                            binding.rvContacts.adapter =
                                contactsAdapter//.withLoadStateFooter(loaderStateAdapter)
                        }
                    }
                }
            }
        }
    }


    @OptIn(FlowPreview::class)
    private fun setupViews() {
        // Search filed block
        val activity = requireActivity()
        if (activity is MainActivity) {
            setupSearchViewListener(
                onBindSearchViewListener = { activity.searchViewListener = it },
                onDetachSearchViewListener = { activity.searchViewListener = null },
                onQueryTextSubmit = {
                    UIUtil.hideKeyboard(activity)
                    activity.getCurrentFocus()?.clearFocus()
                    true
                },
                onSearchViewClose = {
                    isSearching = false
                    binding.scrollerNestedCont.animateShowR2L()
                    false
                },
                onSearchViewOpen = {
                    isSearching = true
                    binding.scrollerNestedCont.animateHideL2R()
                },
            ).debounce(300)
                .filterNotNull()
                .onEach {
                    contactsViewModel.updateSearchFilterText(it)
                }.launchIn(lifecycleScope)
        }

        // Setup recycler view
        binding.rvContacts.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (isSearching) return
                    val offset = recyclerView.computeVerticalScrollOffset()
                    val extent = recyclerView.computeVerticalScrollExtent()
                    val range = recyclerView.computeVerticalScrollRange()
                    val percentage = (1f * offset) / (range - extent)
                    binding.scrollerNestedCont.apply {
                        val adaptedScroll =
                            ((binding.scroller.height - height + 45.px) * percentage)
                        smoothScrollTo(0, adaptedScroll.roundToInt(), 100)
                    }
                }
            })
            setHasFixedSize(true)
            setItemViewCacheSize(8)
            alpha = 0f
        }
        binding.scroller.apply {
            interceptUntilClick()
            setupWithRecyclerView(binding.rvContacts, { position ->
                FastScrollItemIndicator.Text(
                    contactsAdapter.getContactLetter(position)
                )
            })

            val states = arrayOf(
                intArrayOf(android.R.attr.state_activated),
                intArrayOf()
            )
            val colors = intArrayOf(
                MaterialColors.getColor(
                    requireContext(),
                    com.google.android.material.R.attr.colorOutline,
                    Color.MAGENTA,
                ),
                MaterialColors.getColor(
                    requireContext(),
                    com.google.android.material.R.attr.colorOnSurface,
                    Color.BLACK,
                ),
            )
            textColor = ColorStateList(states, colors)
        }
    }

    private fun displayContactToast(contact: ContactUiDto) {
        toast("Selected: ${contact.name},\n${contact.numbers}", Toast.LENGTH_SHORT)
    }

    private fun checkContactPermissions() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_CONTACTS
            ) -> contactsViewModel.updatePermissionState(true)
            else -> contactsViewModel.updatePermissionState(false)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                contactsViewModel.updatePermissionState(true)
            } else {
                toast("Contacts unavailable, permission denied!")
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}