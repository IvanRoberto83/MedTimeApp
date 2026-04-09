package com.example.medicineremindernew.ui.data.repository

import com.example.medicineremindernew.ui.data.model.Riwayat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RiwayatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("riwayat")

    suspend fun addRiwayat(riwayat: Riwayat) {
        collection.document(riwayat.idRiwayat)
            .set(riwayat)
            .await()
    }

    suspend fun updateRiwayat(riwayat: Riwayat) {
        collection.document(riwayat.idRiwayat)
            .set(riwayat)
            .await()
    }

    suspend fun deleteRiwayat(id: String) {
        collection.document(id).delete().await()
    }

    suspend fun getAllRiwayat(): List<Riwayat> {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { it.toObject(Riwayat::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRiwayatByLansia(lansiaId: String): List<Riwayat> {
        return try {
            val snapshot = collection.whereEqualTo("lansiaId", lansiaId).get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Riwayat::class.java)?.copy(idRiwayat = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
