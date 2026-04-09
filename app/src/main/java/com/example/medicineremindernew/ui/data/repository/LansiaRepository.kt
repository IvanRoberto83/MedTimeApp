package com.example.medicineremindernew.ui.data.repository

import com.example.medicineremindernew.ui.data.model.Lansia
import kotlinx.coroutines.tasks.await

class LansiaRepository(private val firestoreRepository: FirestoreRepository) {
    private val db = firestoreRepository.db.collection("lansia")

    fun addLansia(lansia: Lansia, onResult: (Boolean) -> Unit) {
        val docRef = db.document()
        val data = lansia.copy(id = docRef.id)
        docRef.set(data)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getAllLansia(callback: (List<Lansia>) -> Unit) {
        db.addSnapshotListener { snapshot, _ ->
            val lansiaList = snapshot?.documents?.mapNotNull { it.toObject(Lansia::class.java) } ?: emptyList()
            callback(lansiaList)
        }
    }

    fun updateLansia(lansia: Lansia, onResult: (Boolean) -> Unit) {
        db.document(lansia.id).set(lansia)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    suspend fun updateLansiaSuspend(lansia: Lansia): Boolean {
        return try {
            db.document(lansia.id).set(lansia).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getLansiaById(id: String): Lansia? {
        return firestoreRepository.getDocument("lansia", id, Lansia::class.java)
    }

    suspend fun deleteLansia(id: String) {
        db.document(id).delete().await()
    }
}
