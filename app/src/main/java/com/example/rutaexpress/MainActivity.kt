package com.example.rutaexpress

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rutaexpress.ui.home.ClienteHomeScreen
import com.example.rutaexpress.ui.home.ConductorHomeScreen
import com.example.rutaexpress.ui.home.RecepcionistaHomeScreen
import com.example.rutaexpress.ui.login.LoginScreen
import com.example.rutaexpress.ui.recepcionista.RecepcionistaProfileScreen
import com.example.rutaexpress.ui.recepcionista.VehiculosCrudScreen
import com.example.rutaexpress.ui.signup.SignUpScreen
import com.example.rutaexpress.ui.theme.RutaExpressTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        enableEdgeToEdge()
        setContent {
            RutaExpressTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = "login",
    ) {
        composable("login") {
            LoginScreen(
                onRegisterClick = {
                    navController.navigate("signup")
                },
                onLoginSuccess = { role ->
                    when (role) {
                        "recepcionista" -> {
                            Toast.makeText(
                                context,
                                "Sesión activa: Recepcionista",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigate("home_recepcionista") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        "conductor" -> {
                            Toast.makeText(
                                context,
                                "Sesión activa: Conductor",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigate("home_conductor") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        else -> {
                            Toast.makeText(
                                context,
                                "¡Bienvenido a RUTAEXPRESS!",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigate("home_cliente") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                },
                onGuestClick = {
                    Toast.makeText(context, "Continuando como invitado...", Toast.LENGTH_SHORT).show()
                    navController.navigate("home_cliente") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("signup") {
            SignUpScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSignUpSuccess = {
                    Toast.makeText(context, "¡Cuenta creada con éxito!", Toast.LENGTH_LONG).show()
                    navController.popBackStack()
                }
            )
        }

        composable("home_recepcionista") {
            RecepcionistaHomeScreen(
                onNavigateToFlota = {
                    navController.navigate("vehiculos_crud")
                },
                onNavigateToProfile = {
                    navController.navigate("profile_recepcionista")
                }
            )
        }

        composable("vehiculos_crud") {
            VehiculosCrudScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("profile_recepcionista") {
            RecepcionistaProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLogoutClick = {
                    navController.navigate("login") {
                        popUpTo("home_recepcionista") { inclusive = true }
                    }
                }
            )
        }

        composable("home_cliente") {
            ClienteHomeScreen(
                onLogoutClick = {
                    navController.navigate("login") {
                        popUpTo("home_cliente") { inclusive = true }
                    }
                }
            )
        }

        composable("home_conductor") {
            ConductorHomeScreen(
                onLogoutClick = {
                    navController.navigate("login") {
                        popUpTo("home_conductor") { inclusive = true }
                    }
                }
            )
        }
    }
}
