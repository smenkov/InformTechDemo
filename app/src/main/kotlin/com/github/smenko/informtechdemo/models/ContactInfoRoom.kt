package com.github.smenko.informtechdemo.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact")
data class ContactInfoRoom(
    @ColumnInfo(name = "contact_id")
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(name = "contact_name")
    var name: String? = null,
    @ColumnInfo(name = "image_uri")
    var imageUri: String? = null,
    var numbers: String? = null
)