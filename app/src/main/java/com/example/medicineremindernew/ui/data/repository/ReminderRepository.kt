package com.example.medicineremindernew.ui.data.repository

import com.example.medicineremindernew.ui.data.model.Reminder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReminderRepository(private val firestoreRepository: FirestoreRepository) {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("reminders")

    suspend fun getAllReminders(): List<Reminder> {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Reminder::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addReminder(reminder: Reminder) {
        collection.document(reminder.id)
            .set(reminder)
            .await()
    }

    suspend fun updateReminder(reminder: Reminder): Boolean {
        return try {
            collection.document(reminder.id).set(reminder).await()
            true
        } catch (e: Exception) {
            false
        }
    }


    suspend fun deleteReminder(id: String) {
        if (id.isNotEmpty()) {
            collection.document(id).delete().await()
        }
    }

    suspend fun getReminderById(id: String): Reminder? {
        return try {
            val snapshot = collection.document(id).get().await()
            snapshot.toObject(Reminder::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
