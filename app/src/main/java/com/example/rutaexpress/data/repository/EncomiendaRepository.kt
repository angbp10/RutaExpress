package com.example.rutaexpress.data.repository

import com.example.rutaexpress.data.model.Encomienda
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EncomiendaRepository {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val encomiendasCollection by lazy { firestore.collection("encomiendas") }

    suspend fun registrarEncomienda(encomienda: Encomienda): Result<String> = runCatching {
        val docRef = encomiendasCollection.document()
        val newEncomienda = encomienda.copy(id = docRef.id)
        docRef.set(newEncomienda).await()
        newEncomienda.guia
    }

    fun getEncomiendasFlow(): Flow<List<Encomienda>> = callbackFlow {
        val listener = encomiendasCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.toObjects(Encomienda::class.java)
                trySend(list)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun buscarEncomienda(filtro: String): Result<Encomienda?> = runCatching {
        val snapshot = encomiendasCollection
            .where(
                com.google.firebase.firestore.Filter.or(
                    com.google.firebase.firestore.Filter.equalTo("guia", filtro),
                    com.google.firebase.firestore.Filter.equalTo("dniRemitente", filtro),
                    com.google.firebase.firestore.Filter.equalTo("dniDestinatario", filtro)
                )
            )
            .limit(1)
            .get()
            .await()

        snapshot.toObjects(Encomienda::class.java).firstOrNull()
    }

    suspend fun actualizarEstado(id: String, nuevoEstado: String): Result<Unit> = runCatching {
        encomiendasCollection.document(id).update(
            "estado", nuevoEstado,
            "fechaActualizacion", FieldValue.serverTimestamp()
        ).await()
        Unit
    }

    suspend fun getUltimoNumeroGuia(): Int = runCatching {
        val snapshot = encomiendasCollection
            .orderBy("guia", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()
        
        val ultimaGuia = snapshot.toObjects(Encomienda::class.java).firstOrNull()?.guia
        if (ultimaGuia != null && ultimaGuia.startsWith("e-")) {
            ultimaGuia.substring(2).toIntOrNull() ?: 0
        } else {
            0
        }
    }.getOrDefault(0)
}
