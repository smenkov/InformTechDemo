package com.github.smenko.informtechdemo.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.smenko.informtechdemo.datasource.ContactDao
import com.github.smenko.informtechdemo.datasource.ContactServiceImpl
import com.github.smenko.informtechdemo.models.ContactUiDto
import com.github.smenko.informtechdemo.utils.convertToUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactServiceImpl: ContactServiceImpl,
    private val contactDao: ContactDao,
) : ViewModel() {

    sealed class ContactsFragmentState {
        data class PermissionResolve(val granted: Boolean = false) : ContactsFragmentState()
        data class DisplayContacts(
            val filterText: String,
            val contactsFlow: Flow<List<ContactUiDto>>,
        ) : ContactsFragmentState()
    }

    private val _uiState = MutableStateFlow<ContactsFragmentState>(
        ContactsFragmentState.PermissionResolve()
    )
    val uiState = _uiState.asStateFlow()


    fun updatePermissionState(granted: Boolean) {
        when (val state = _uiState.value) {
            is ContactsFragmentState.PermissionResolve -> {
                _uiState.update { state.copy(granted) }
                if (granted) tryDisplayContacts()
            }
            is ContactsFragmentState.DisplayContacts -> return
        }
    }

    fun updateSearchFilterText(searchFilter: String) {
        tryDisplayContacts(searchFilter)
    }

    private fun tryDisplayContacts(filterText: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            if (state is ContactsFragmentState.PermissionResolve && !state.granted) return@launch

            val baseContactsFlow = flow {
                emit(contactServiceImpl.fetchAllContacts(filterText))
            }.map { contacts ->
                contacts.map(::convertToUI)/*.filter { contact ->
                    if (filterText.isBlank()) return@filter true
                    contact.name.lowercase()
                        .startsWith(filterText.lowercase()) || contact.numbers.lowercase()
                        .split(",").any {
                            it.replace(regexStripPhone, "")
                                .startsWith(filterText.replace(regexStripPhone, ""))
                        }
                }*/
            }

            _uiState.update {
                when (state) {
                    is ContactsFragmentState.PermissionResolve ->
                        ContactsFragmentState.DisplayContacts(filterText, baseContactsFlow)
                    is ContactsFragmentState.DisplayContacts ->
                        state.copy(filterText = filterText, contactsFlow = baseContactsFlow)
                }
            }
        }
    }
}