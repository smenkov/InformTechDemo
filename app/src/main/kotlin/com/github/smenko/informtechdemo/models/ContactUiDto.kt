package com.github.smenko.informtechdemo.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContactUiDto(
    val name: String,
    val letter: String,
    val bgColor: Int,
    val image: Uri,
    val numbers: String
) : Parcelable