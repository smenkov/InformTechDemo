package com.github.smenko.informtechdemo.utils

import android.graphics.Color
import android.net.Uri
import com.github.smenko.informtechdemo.models.ContactDto
import com.github.smenko.informtechdemo.models.ContactInfoRoom
import com.github.smenko.informtechdemo.models.ContactUiDto
import kotlin.random.Random

fun ContactInfoRoom.convertToDto(): ContactDto = ContactDto(
    name = name ?: "",
    imageUri = imageUri ?: "",
    numbers = numbers?.split(", ") ?: emptyList()
)

fun convertToUI(contact: ContactDto): ContactUiDto {
    val rnd = Random(contact.hashCode())
    return ContactUiDto(
        name = contact.name,
        letter = contact.name.first().uppercase(),
        bgColor = Color.argb(
            255,
            rnd.nextInt(120, 200),
            rnd.nextInt(120, 200),
            rnd.nextInt(120, 200)
        ),
        image = Uri.parse(contact.imageUri),
        numbers = contact.numbers.joinToString(", ")
    )
}