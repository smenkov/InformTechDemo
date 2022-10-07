package com.github.smenko.informtechdemo.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContactDto(
    val name: String,
    val imageUri: String,
    val numbers: List<String>
) : Parcelable