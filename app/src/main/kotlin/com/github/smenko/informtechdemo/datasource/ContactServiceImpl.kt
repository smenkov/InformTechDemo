package com.github.smenko.informtechdemo.datasource

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.telephony.PhoneNumberUtils
import com.github.smenko.informtechdemo.di.ApplicationScope
import com.github.smenko.informtechdemo.models.ContactDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ContactServiceImpl @Inject constructor(
    @ApplicationScope private val externalScope: CoroutineScope,
    @ApplicationContext private val appContext: Context
) {
    suspend fun fetchAllContacts(filterText: String = ""): List<ContactDto> {
        val uri = if (filterText.isBlank()) Phone.CONTENT_URI
        else Uri.withAppendedPath(Phone.CONTENT_FILTER_URI, Uri.encode(filterText))
        val selection = "${Phone.HAS_PHONE_NUMBER} = True"
        val sortOrder = "${Phone.DISPLAY_NAME} ASC "
        val gropingMap: Deferred<Map<Pair<String, String?>, MutableList<String>>> =
            externalScope.async {
                val cursor = appContext.contentResolver.query(
                    uri,
                    arrayOf(
                        Phone.DISPLAY_NAME,
                        Phone.NUMBER,
                        Phone.PHOTO_THUMBNAIL_URI,
                    ),
                    selection,
                    null,
                    sortOrder
                ) ?: return@async emptyMap()

                val gropingMap = mutableMapOf<Pair<String, String?>, MutableList<String>>()

                val locale = Locale.getDefault().country
                while (cursor.moveToNext()) {
                    val nameIdx = cursor.getColumnIndex(Phone.DISPLAY_NAME)
                    val numberIdx = cursor.getColumnIndex(Phone.NUMBER)
                    val imageIdx = cursor.getColumnIndex(Phone.PHOTO_THUMBNAIL_URI)
                    val contactName = cursor.getString(nameIdx)
                    val contactNumber = PhoneNumberUtils.formatNumber(
                        cursor.getString(numberIdx), locale
                    )
                    val contactImage = cursor.getString(imageIdx)
                    if (contactName == null || contactNumber == null) continue

                    val pair = contactName to contactImage

                    if (gropingMap[pair] == null) gropingMap[pair] = mutableListOf()
                    gropingMap[pair]?.add(contactNumber)
                }
                cursor.close()
                gropingMap
            }
        return try {
            gropingMap.await().map {
                ContactDto(
                    it.key.first,
                    it.key.second ?: "no_image",
                    it.value
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}