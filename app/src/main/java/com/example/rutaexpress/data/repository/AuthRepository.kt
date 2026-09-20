package com.example.rutaexpress.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import kotlinx.coroutines.tasks.await

/**
 * Modelo de datos del perfil de usuario en Firestore.
 */
data class UserProfile(
    @get:PropertyName("uid") @set:PropertyName("uid") var uid: String = "",
    @get:PropertyName("dni") @set:PropertyName("dni") var dni: String = "",
    @get:PropertyName("nombres") @set:PropertyName("nombres") var nombres: String = "",
    @get:PropertyName("apellido_paterno") @set:PropertyName("apellido_paterno") var apellidoPaterno: String = "",
    @get:PropertyName("apellido_materno") @set:PropertyName("apellido_materno") var apellidoMaterno: String = "",
    @get:PropertyName("email") @set:PropertyName("email") var email: String = "",
    @get:PropertyName("telefono") @set:PropertyName("telefono") var telefono: String = "",
    @get:PropertyName("role") @set:PropertyName("role") var role: String = "cliente",
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Any? = null,
)

/**
 * Repositorio para la autenticación y registro de usuarios con Firebase.
 */
class AuthRepository {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val currentUser: FirebaseUser?
        get() = runCatching { auth.currentUser }.getOrNull()

    /**
     * Inicia sesión con correo y contraseña y realiza la consulta obligatoria a Firestore
     * para obtener el rol del usuario antes de navegar.
     */
    suspend fun loginAndGetRole(email: String, password: String): Result<Pair<FirebaseUser, String>> {
        return runCatching {
            // 1. Iniciar sesión en Firebase Auth
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("No se obtuvo información del usuario.")

            // 2. Consulta obligatoria a la colección "users" usando el UID del usuario
            val currentUid = auth.currentUser?.uid ?: user.uid
            val docSnapshot = firestore.collection("users").document(currentUid).get().await()

            // 3. Extraer el valor exacto del campo String "role"
            val role = docSnapshot.getString("role") ?: "cliente"

            // 4. Registrar en Logcat para verificación
            Log.d("AUTH_ROLE", "Rol obtenido de Firestore: $role")

            Pair(user, role)
        }
    }

    /**
     * Registra un nuevo usuario en Firebase Auth e inmediatamente guarda su documento en Firestore
     * en la colección 'users' con ID igual al uid del usuario y rol estático "cliente".
     */
    suspend fun signUp(
        dni: String,
        nombres: String,
        apellidoPaterno: String,
        apellidoMaterno: String,
        email: String,
        telefono: String,
        password: String,
        role: String = "cliente",
    ): Result<FirebaseUser> {
        return runCatching {
            // 1. Registro en Firebase Authentication
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("Error al crear usuario en Firebase Auth.")

            // 2. Mapeo explícito de campos exigidos para Firestore
            val userMap = hashMapOf<String, Any?>(
                "uid" to user.uid,
                "email" to email,
                "dni" to dni,
                "nombres" to nombres,
                "apellido_paterno" to apellidoPaterno,
                "apellido_materno" to apellidoMaterno,
                "telefono" to telefono,
                "role" to role,
                "createdAt" to FieldValue.serverTimestamp(),
            )

            // 3. Sincronización obligatoria en Firestore usando el mismo uid como Document ID
            firestore.collection("users")
                .document(user.uid)
                .set(userMap)
                .await()

            user
        }
    }

    /**
     * Cierra la sesión activa.
     */
    fun logout() {
        runCatching { auth.signOut() }
    }
}
