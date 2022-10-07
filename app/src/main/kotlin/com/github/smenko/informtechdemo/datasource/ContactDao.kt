package com.github.smenko.informtechdemo.datasource

import androidx.room.*
import com.github.smenko.informtechdemo.models.ContactInfoRoom
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(dialogList: List<ContactInfoRoom>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(dialog: ContactInfoRoom)

    @Query("SELECT * FROM contact WHERE contact_id = :contactId")
    suspend fun getContactById(contactId: Long): ContactInfoRoom?

    @Query("SELECT * FROM contact")
    suspend fun getAll(): List<ContactInfoRoom>

    @Query("SELECT count(c.contact_id) FROM contact c")
    suspend fun totalCacheCount(): Int

    @Transaction
    @Query("SELECT * FROM contact c")
    fun getAllAsFlow(): Flow<List<ContactInfoRoom>>

    @Update
    fun update(contact: ContactInfoRoom)

    @Query("DELETE FROM contact")
    fun clearAll()
}