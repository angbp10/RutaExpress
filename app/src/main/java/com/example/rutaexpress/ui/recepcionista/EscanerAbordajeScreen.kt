package com.example.rutaexpress.ui.recepcionista

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rutaexpress.viewmodel.EscanerAbordajeViewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

private val PrimaryRedHex = Color(0xFFF72907)
private val DarkRedBanner = Color(0xFFB71C1C)
private val SoftBackgroundHex = Color(0xFFFCF9F8)
private val GreenSuccess = Color(0xFF2E7D32)
private val RedReject = Color(0xFFD32F2F)

/**
 * Pantalla de Escáner y Validación de Abordaje para Personal (HU29).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscanerAbordajeScreen(
    viewModel: EscanerAbordajeViewModel = viewModel(),
    onBackClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scanner = remember { GmsBarcodeScanning.getClient(context) }

    var expandedDropdown by remember { mutableStateOf(false) }
    var codigoManualInput by remember { mutableStateOf("") }

    Scaffold(
        containerColor = SoftBackgroundHex,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ESCÁNER DE ABORDAJE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1B1B)
                        )
                        Text(
                            text = "Control de Turnos y Billetes (HU29)",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. SELECCIÓN DE VIAJE / TURNO ACTUAL (HU29)
            Text(
                text = "1. Selecciona el Turno o Viaje a Despachar:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1B)
            )

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.isLoadingViajes) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PrimaryRedHex)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cargando turnos...", fontSize = 13.sp, color = Color.Gray)
                        }
                    } else if (uiState.viajesDisponibles.isEmpty()) {
                        Text(
                            text = "No hay viajes programados disponibles.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        // Dropdown Selector de Viaje
                        Box(modifier = Modifier.fillMaxWidth()) {
                            val viajeSeleccionado = uiState.viajeSeleccionado

                            OutlinedTextField(
                                value = if (viajeSeleccionado != null)
                                    "${viajeSeleccionado.origen} ➔ ${viajeSeleccionado.destino} (${viajeSeleccionado.horaSalida})"
                                else "Selecciona un turno",
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedDropdown = true },
                                label = { Text("Turno Seleccionado") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsBus,
                                        contentDescription = null,
                                        tint = PrimaryRedHex
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ExpandMore,
                                        contentDescription = "Expandir",
                                        modifier = Modifier.clickable { expandedDropdown = true }
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryRedHex)
                            )

                            DropdownMenu(
                                expanded = expandedDropdown,
                                onDismissRequest = { expandedDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                uiState.viajesDisponibles.forEach { viaje ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = "${viaje.origen} ➔ ${viaje.destino}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = "Salida: ${viaje.fecha} • ${viaje.horaSalida} | Placa: ${viaje.vehiculoDescripcion}",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.seleccionarViaje(viaje)
                                            expandedDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Detalle del Turno Seleccionado
                        uiState.viajeSeleccionado?.let { viaje ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SoftBackgroundHex,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Conductor: ${viaje.conductor.ifBlank { "Sin asignar" }}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.DarkGray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Vehículo: ${viaje.vehiculoDescripcion.ifBlank { "N/A" }}",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = PrimaryRedHex.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = "Abordados: ${uiState.totalAbordadosTurno}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryRedHex,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. BOTÓN PRINCIPAL GRANDE DE ESCÁNER (HU29)
            Text(
                text = "2. Control de Abordaje:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1B)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(PrimaryRedHex.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = PrimaryRedHex,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Presiona para activar el escáner",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1B1B)
                        )
                        Text(
                            text = "Apunta al código QR del boleto digital del cliente",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Botón Principal Lanza Escáner QR GmsBarcodeScanner
                    Button(
                        onClick = {
                            scanner.startScan()
                                .addOnSuccessListener { barcode ->
                                    val qrCodeValue = barcode.rawValue.orEmpty()
                                    if (qrCodeValue.isNotBlank()) {
                                        viewModel.procesarBoletoEscaneado(qrCodeValue)
                                    } else {
                                        Toast.makeText(context, "No se leyó ningún código QR.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { ex ->
                                    Toast.makeText(
                                        context,
                                        "Ingresa el código manualmente si el escáner no está disponible en este dispositivo.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRedHex),
                        enabled = !uiState.isValidating
                    ) {
                        if (uiState.isValidating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Validando boleto...", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📷 Escanear Boleto (QR)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Opción Alternativa: Ingreso Manual de Código / ID
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFEEEEEE))
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "o Ingresa el Código / ID Manualmente:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.DarkGray
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = codigoManualInput,
                                onValueChange = { codigoManualInput = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Ej: REX-50168 o ID") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = null,
                                        tint = PrimaryRedHex,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryRedHex)
                            )

                            Button(
                                onClick = {
                                    if (codigoManualInput.isNotBlank()) {
                                        viewModel.procesarBoletoEscaneado(codigoManualInput)
                                        codigoManualInput = ""
                                    }
                                },
                                enabled = codigoManualInput.isNotBlank() && !uiState.isValidating,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkRedBanner),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Text("Validar", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // 3. DIÁLOGO DE FEEDBACK VISUAL DE VALIDACIÓN DE ABORDAJE (HU29)
        if (uiState.mostrarDialogoResultado) {
            val esExito = uiState.resultadoExitoso != null
            val reserva = uiState.resultadoExitoso

            AlertDialog(
                onDismissRequest = { viewModel.cerrarDialogoResultado() },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(if (esExito) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (esExito) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (esExito) GreenSuccess else RedReject,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = if (esExito) "¡Abordaje Aprobado!" else "¡Abordaje Rechazado!",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (esExito) GreenSuccess else RedReject,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (esExito && reserva != null) {
                            Text(
                                text = "El boleto es válido y ha sido registrado como ABORDADO.",
                                fontSize = 13.sp,
                                color = Color(0xFF424242),
                                textAlign = TextAlign.Center
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF5F5F5),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Código: ${reserva.codigoReserva}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkRedBanner
                                    )
                                    Text(
                                        text = "Ruta: ${reserva.origen} ➔ ${reserva.destino}",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = "Asiento(s): ${reserva.asientos.joinToString(", ") { "N° $it" }}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenSuccess
                                    )
                                    if (reserva.pasajeros.isNotEmpty()) {
                                        Text(
                                            text = "Pasajero: ${reserva.pasajeros.first().nombres} (${reserva.pasajeros.first().dni})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        } else {
                            // Motivo del Rechazo de la Validación Estricta
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFEBEE),
                                border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = RedReject,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = uiState.errorRechazo ?: "Motivo de rechazo desconocido.",
                                        fontSize = 12.sp,
                                        color = RedReject,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.cerrarDialogoResultado() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (esExito) GreenSuccess else RedReject
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (esExito) "Escanear siguiente boleto" else "Reintentar escanear",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                shape = RoundedCornerShape(18.dp),
                containerColor = Color.White
            )
        }
    }
}
