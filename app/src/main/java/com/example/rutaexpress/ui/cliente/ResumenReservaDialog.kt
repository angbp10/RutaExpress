package com.example.rutaexpress.ui.cliente

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rutaexpress.data.model.Reserva
import com.example.rutaexpress.viewmodel.PasajeroInputState
import kotlinx.coroutines.delay

private val PrimaryRedHex = Color(0xFFB71C1C)
private val SoftPinkBackground = Color(0xFFFFEBEE)
private val SoftPinkBorder = Color(0xFFFFCDD2)
private val TextPrimaryHex = Color(0xFF1C1B1B)

/**
 * Diálogo / Modal BottomSheet para ingresar los datos de cada pasajero (DNI y Nombres)
 * correspondientes a los asientos seleccionados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroPasajerosBottomSheet(
    pasajerosInputs: List<PasajeroInputState>,
    isLoading: Boolean,
    onDniChanged: (Int, String) -> Unit,
    onNombresChanged: (Int, String) -> Unit,
    onConsultarDni: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirmarReserva: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cabecera
            Text(
                text = "DATOS DE PASAJEROS",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryRedHex,
                letterSpacing = 1.sp
            )

            // Lista de formularios por cada asiento
            pasajerosInputs.forEachIndexed { index, input ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftPinkBackground.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, SoftPinkBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventSeat,
                                contentDescription = null,
                                tint = PrimaryRedHex,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Pasajero Asiento N° ${input.asientoNumero}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryRedHex
                            )
                        }

                        // Input DNI + Botón consultar API
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = input.dni,
                                onValueChange = { onDniChanged(index, it) },
                                label = { Text("DNI (8 dígitos)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryRedHex,
                                    focusedLabelColor = PrimaryRedHex
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = { onConsultarDni(index) },
                                enabled = input.dni.length == 8 && !input.isLoadingDni,
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (input.dni.length == 8) PrimaryRedHex else Color.Gray.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            ) {
                                if (input.isLoadingDni) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Buscar DNI",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        if (input.errorDni != null) {
                            Text(
                                text = input.errorDni,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        // Input Nombres completos
                        OutlinedTextField(
                            value = input.nombres,
                            onValueChange = { onNombresChanged(index, it) },
                            label = { Text("Nombres y Apellidos") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = PrimaryRedHex
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryRedHex,
                                focusedLabelColor = PrimaryRedHex
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Botón de Confirmación
            Button(
                onClick = onConfirmarReserva,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRedHex)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    val textoBoton = if (pasajerosInputs.size == 1) "Reservar Asiento" else "Reservar Asientos"
                    Text(
                        text = textoBoton,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Diálogo / Pantalla de Resumen y Confirmación de Reserva Temporal (HU18).
 * Muestra el código REX-XXXXX, temporizador en tiempo real de 5 minutos,
 * detalles de asientos/pasajeros, botón para copiar código y botón volver al inicio.
 */
@Composable
fun ResumenReservaDialog(
    reserva: Reserva,
    onDismiss: () -> Unit,
    onVolverInicio: () -> Unit,
) {
    val context = LocalContext.current

    // Temporizador en tiempo real
    var tiempoRestanteMs by remember(reserva.expiresAt) {
        mutableLongStateOf((reserva.expiresAt - System.currentTimeMillis()).coerceAtLeast(0L))
    }

    LaunchedEffect(reserva.expiresAt) {
        while (tiempoRestanteMs > 0) {
            delay(1000L)
            tiempoRestanteMs = (reserva.expiresAt - System.currentTimeMillis()).coerceAtLeast(0L)
        }
    }

    val minutos = (tiempoRestanteMs / 1000) / 60
    val segundos = (tiempoRestanteMs / 1000) % 60
    val timerText = "%02d:%02d".format(minutos, segundos)
    val expirado = tiempoRestanteMs <= 0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono e indicación de éxito
                Surface(
                    color = SoftPinkBackground,
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PrimaryRedHex,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(36.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "¡SOLICITUD DE RESERVA GENERADA!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRedHex,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Presenta este código en la agencia para efectuar el pago",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Tarjeta Destacada con el Código REX y el Temporizador
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftPinkBackground),
                    border = BorderStroke(1.5.dp, SoftPinkBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = PrimaryRedHex,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (expirado) "EXPIRADO" else "RESERVADO - PAGO EN AGENCIA",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = reserva.codigoReserva,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryRedHex,
                            letterSpacing = 2.sp
                        )

                        // Temporizador
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = if (expirado) Color.Red else PrimaryRedHex,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (expirado) "Tiempo expirado" else "Tiempo para pagar: $timerText min",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (expirado) Color.Red else PrimaryRedHex
                            )
                        }
                    }
                }

                // Detalles del Viaje y Pasajeros
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFAFAFA),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Ruta:",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "${reserva.origen} ➔ ${reserva.destino}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryHex
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Fecha y Hora:",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "${reserva.fechaViaje} • ${reserva.horaSalida}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimaryHex
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Asientos:",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = reserva.asientos.joinToString(", ") { "N° $it" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryRedHex
                            )
                        }

                        // Lista de Pasajeros
                        if (reserva.pasajeros.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pasajeros registrados:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryHex
                            )
                            reserva.pasajeros.forEach { pasajero ->
                                Text(
                                    text = "• Asiento ${pasajero.asientoNumero}: ${pasajero.nombres} (DNI: ${pasajero.dni})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFE0E0E0))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Monto Total a Pagar:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryHex
                            )
                            Text(
                                text = "S/ ${"%.2f".format(reserva.montoTotal)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryRedHex
                            )
                        }
                    }
                }

                // Botones de Acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Botón Copiar Código
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Código Reserva", reserva.codigoReserva)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Código ${reserva.codigoReserva} copiado", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, PrimaryRedHex)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = PrimaryRedHex,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Copiar Código",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRedHex
                        )
                    }

                    // Botón Volver al Inicio
                    Button(
                        onClick = onVolverInicio,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRedHex)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Volver al Inicio",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
