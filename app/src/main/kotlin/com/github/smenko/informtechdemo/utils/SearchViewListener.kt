package com.github.smenko.informtechdemo.utils

import android.view.View
import androidx.appcompat.widget.SearchView

interface SearchViewListener :
    SearchView.OnQueryTextListener,
    SearchView.OnCloseListener,
    View.OnClickListener {

    override fun onQueryTextSubmit(query: String?): Boolean

    override fun onQueryTextChange(newText: String?): Boolean

    override fun onClose(): Boolean

    override fun onClick(v: View?)
}