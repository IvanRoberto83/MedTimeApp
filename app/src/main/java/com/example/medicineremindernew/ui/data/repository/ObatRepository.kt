package com.example.medicineremindernew.ui.data.repository

import com.example.medicineremindernew.ui.data.model.Obat
import kotlinx.coroutines.tasks.await

class ObatRepository(private val firestoreRepository: FirestoreRepository) {
    private val db = firestoreRepository.db.collection("obat")

    fun getAllObat(callback: (List<Obat>) -> Unit) {
        db.addSnapshotListener { snapshot, _ ->
            val obatList = snapshot?.documents?.mapNotNull { it.toObject(Obat::class.java) } ?: emptyList()
            callback(obatList)
        }
    }

    fun addObat(obat: Obat, onResult: (Boolean) -> Unit) {
        val docRef = db.document()
        val data = obat.copy(id = docRef.id)
        docRef.set(data)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    suspend fun getObatById(id: String): Obat? {
        return firestoreRepository.getDocument("obat", id, Obat::class.java)
    }

    suspend fun deleteObat(id: String) {
        db.document(id).delete().await()
    }

    suspend fun updateObat(obat: Obat): Boolean {
        return try {
            db.document(obat.id).set(obat).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}

