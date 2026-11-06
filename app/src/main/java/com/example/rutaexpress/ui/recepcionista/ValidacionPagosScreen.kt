package com.example.rutaexpress.ui.recepcionista

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCardOff
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rutaexpress.data.model.Reserva
import com.example.rutaexpress.viewmodel.ValidacionPagosViewModel

private val PrimaryRedHex = Color(0xFFF72907)
private val SoftBackgroundHex = Color(0xFFFCF9F8)
private val GreenApprove = Color(0xFF2E7D32)
private val RedReject = Color(0xFFD32F2F)
private val YapePurple = Color(0xFF6B1173)
private val PlinCyan = Color(0xFF00A896)

/**
 * Pantalla para la Validación de Pagos por Transferencia (Yape/Plin) por Recepción (HU10).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidacionPagosScreen(
    viewModel: ValidacionPagosViewModel = viewModel(),
    onBackClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        containerColor = SoftBackgroundHex,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "VALIDACIÓN DE PAGOS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1B1B)
                        )
                        Text(
                            text = "Transferencias Yape / Plin (HU10)",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFF1C1B1B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = PrimaryRedHex)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Cargando pagos en revisión...", color = Color.Gray, fontSize = 14.sp)
                }
            } else if (uiState.reservasEnRevision.isEmpty()) {
                // Estado Vacío
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCardOff,
                            contentDescription = null,
                            tint = GreenApprove,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "¡Todo al día!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No hay pagos por transferencia pendientes de validación por el momento.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = uiState.reservasEnRevision,
                        key = { it.id }
                    ) { reserva ->
                        ReservaEnRevisionCard(
                            reserva = reserva,
                            isProcessing = uiState.isProcessingId == reserva.id,
                            onAprobar = { viewModel.aprobarPago(reserva.id) },
                            onRechazar = { viewModel.rechazarPago(reserva.id) },
                            onCopiarOperacion = { opNum ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("N° Operación", opNum)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "N° Operación $opNum copiado", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de Reserva en estado EN_REVISION (HU10).
 */
@Composable
fun ReservaEnRevisionCard(
    reserva: Reserva,
    isProcessing: Boolean,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit,
    onCopiarOperacion: (String) -> Unit,
) {
    val metodoColor = if (reserva.metodoPago.equals("Plin", ignoreCase = true)) PlinCyan else YapePurple

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Código Reserva + Badge Método
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryRedHex.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = reserva.codigoReserva.ifBlank { "REX-----" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryRedHex,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = metodoColor
                ) {
                    Text(
                        text = (reserva.metodoPago.ifBlank { "Yape" }).uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            // Datos del Viaje y Pasajero
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Pasajeros
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val nombresPasajeros = if (reserva.pasajeros.isNotEmpty()) {
                        reserva.pasajeros.joinToString(", ") { "${it.nombres} (${it.dni})" }
                    } else {
                        "Pasajero no especificado"
                    }
                    Text(
                        text = nombresPasajeros,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1B)
                    )
                }

                // Ruta
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${reserva.origen} ➔ ${reserva.destino}",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Fecha/Hora y Asientos
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EventSeat,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${reserva.fechaViaje} • ${reserva.horaSalida}  |  Asientos: ${reserva.asientos.joinToString(", ") { "N° $it" }}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            // Separador
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFEEEEEE))
            )

            // DATO CRÍTICO: BLOQUE RESALTADO CON N° DE OPERACIÓN (HU10)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFF8E1),
                border = BorderStroke(1.dp, Color(0xFFFFE082)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "N° de Operación reportado:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF5D4037)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = reserva.numeroOperacion.ifBlank { "Sin número" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E7D32),
                            letterSpacing = 1.sp
                        )
                    }

                    // Botón Copiar N° de Operación
                    if (reserva.numeroOperacion.isNotBlank()) {
                        IconButton(
                            onClick = { onCopiarOperacion(reserva.numeroOperacion) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar N° Operación",
                                tint = Color(0xFF5D4037),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Total a pagar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monto Total:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Text(
                    text = "S/ ${"%.2f".format(reserva.montoTotal)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryRedHex
                )
            }

            // BOTONES DE DECISIÓN (RECHAZAR / APROBAR PAGO) (HU10)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Botón Rechazar
                OutlinedButton(
                    onClick = onRechazar,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    enabled = !isProcessing,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, RedReject),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedReject)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = RedReject, strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = RedReject,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Rechazar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Botón Aprobar Pago
                Button(
                    onClick = onAprobar,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(44.dp),
                    enabled = !isProcessing,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenApprove)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Aprobar Pago", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
