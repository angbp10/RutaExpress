package com.example.rutaexpress.ui.recepcionista

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rutaexpress.data.model.Encomienda
import com.example.rutaexpress.data.model.Movimiento
import com.example.rutaexpress.viewmodel.EncomiendaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PrimaryRedHex = Color(0xFFF72907)
private val SoftBackgroundHex = Color(0xFFFCF9F8)
private val GreenDelivered = Color(0xFF2E7D32)
private val BlueWarehouse = Color(0xFF1976D2)
private val OrangeTransit = Color(0xFFE65100)
private val PurplePickup = Color(0xFF7B1FA2)

val ESTADOS_ENCOMIENDA_ESTRICTOS = listOf(
    "En almacén",
    "En ruta",
    "Disponible para recojo",
    "Entregado"
)

fun formatFechaHoraLong(timestamp: Long): String {
    if (timestamp <= 0L) return "Fecha no especificada"
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (_: Exception) {
        "Reciente"
    }
}

/**
 * Pantalla para la Gestión y Seguimiento del Estado de Encomiendas (HU09).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionEstadosScreen(
    viewModel: EncomiendaViewModel = viewModel(),
    onBackClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val encomiendas by viewModel.encomiendasState.collectAsState()
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
                            text = "SEGUIMIENTO DE ENCOMIENDAS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1B1B)
                        )
                        Text(
                            text = "Actualización y Tracking Inmutable (HU09)",
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
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Buscador Dinámico
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Buscar por Guía o DNI...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                trailingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryRedHex)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryRedHex,
                    focusedLabelColor = PrimaryRedHex
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (uiState.isLoading && encomiendas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryRedHex)
                }
            } else if (encomiendas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (uiState.searchQuery.isEmpty()) "No hay encomiendas registradas." else "No se encontraron coincidencias.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(encomiendas, key = { it.id }) { encomienda ->
                        EncomiendaSeguimientoCard(
                            encomienda = encomienda,
                            onUpdateState = { nuevoEstado ->
                                viewModel.actualizarEstadoEncomienda(encomienda.id, nuevoEstado)
                            },
                            isLoading = uiState.isLoading
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta interactiva de encomienda con Selector de Estado y Timeline de Tracking (HU09).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncomiendaSeguimientoCard(
    encomienda: Encomienda,
    onUpdateState: (String) -> Unit,
    isLoading: Boolean,
) {
    val estadoActualEfectivo = encomienda.estadoActual.ifBlank { encomienda.estado.ifBlank { "En almacén" } }
    var selectedEstado by remember(estadoActualEfectivo) { mutableStateOf(estadoActualEfectivo) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var mostrarTrackingHistorial by remember { mutableStateOf(true) }

    val colorEstadoActual = when (estadoActualEfectivo) {
        "En almacén", "EN_ALMACEN" -> BlueWarehouse
        "En ruta", "EN_TRANSITO" -> OrangeTransit
        "Disponible para recojo", "DISPONIBLE" -> PurplePickup
        "Entregado", "ENTREGADO" -> GreenDelivered
        else -> PrimaryRedHex
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Guía + Estado Actual Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Guía: ${encomienda.guia}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = PrimaryRedHex
                )

                Surface(
                    color = colorEstadoActual.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = estadoActualEfectivo,
                        color = colorEstadoActual,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))

            // Detalles de la Encomienda (HU09)
            DetailRow("Remitente:", "${encomienda.remitente} (DNI: ${encomienda.dniRemitente})")
            DetailRow("Consignatario/Destinatario:", "${encomienda.destinatario} (DNI: ${encomienda.dniDestinatario})")
            DetailRow("Carga / Tarifa:", "${encomienda.pesoKg} kg | ${encomienda.tipoCarga} | S/ ${"%.2f".format(Locale.US, encomienda.tarifaTotal)}")

            Spacer(modifier = Modifier.height(2.dp))

            // CONTROLES DE ACTUALIZACIÓN DE ESTADO (HU09)
            Text(
                text = "Cambiar Estado de Encomienda:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1B)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedEstado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Nuevo Estado", fontSize = 11.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRedHex,
                            focusedLabelColor = PrimaryRedHex
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        ESTADOS_ENCOMIENDA_ESTRICTOS.forEach { est ->
                            DropdownMenuItem(
                                text = { Text(est, fontWeight = FontWeight.Medium, fontSize = 13.sp) },
                                onClick = {
                                    selectedEstado = est
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                // BOTÓN DE ACTUALIZAR ESTADO (HU09)
                Button(
                    onClick = { onUpdateState(selectedEstado) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRedHex),
                    enabled = !isLoading && selectedEstado != estadoActualEfectivo,
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Update, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Actualizar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // BOTÓN TOGGLE TIMELINE / HITO DE TRACKING (HU09)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SoftBackgroundHex)
                    .clickable { mostrarTrackingHistorial = !mostrarTrackingHistorial }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = PrimaryRedHex,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Línea de Tiempo / Tracking (${encomienda.historialMovimientos.size} registros)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRedHex
                    )
                }

                Icon(
                    imageVector = if (mostrarTrackingHistorial) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = PrimaryRedHex,
                    modifier = Modifier.size(18.dp)
                )
            }

            // DIBUJO DE LÍNEA DE TIEMPO VERTICAL (TIMELINE TRACKING - HU09)
            AnimatedVisibility(visible = mostrarTrackingHistorial) {
                val listaMovimientos = encomienda.historialMovimientos.ifEmpty {
                    listOf(
                        Movimiento(
                            estado = estadoActualEfectivo,
                            fechaHora = encomienda.ultimaActualizacion,
                            nombreUsuario = "Personal de Agencia",
                            idUsuario = "AGENCIA"
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 4.dp, end = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    listaMovimientos.forEachIndexed { index, movimiento ->
                        TimelineNodeItem(
                            movimiento = movimiento,
                            isLast = index == listaMovimientos.size - 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Renderiza cada nodo individual de la Línea de Tiempo del Tracking (HU09).
 */
@Composable
fun TimelineNodeItem(
    movimiento: Movimiento,
    isLast: Boolean,
) {
    val nodeColor = when (movimiento.estado) {
        "En almacén", "EN_ALMACEN" -> BlueWarehouse
        "En ruta", "EN_TRANSITO" -> OrangeTransit
        "Disponible para recojo", "DISPONIBLE" -> PurplePickup
        "Entregado", "ENTREGADO" -> GreenDelivered
        else -> PrimaryRedHex
    }

    val iconVector = when (movimiento.estado) {
        "En almacén", "EN_ALMACEN" -> Icons.Default.Inventory
        "En ruta", "EN_TRANSITO" -> Icons.Default.LocalShipping
        "Disponible para recojo", "DISPONIBLE" -> Icons.Default.Storefront
        "Entregado", "ENTREGADO" -> Icons.Default.CheckCircle
        else -> Icons.Default.DirectionsBus
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Columna Izquierda: Indicador Circular + Línea Vertical
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(nodeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(nodeColor.copy(alpha = 0.35f))
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Columna Derecha: Nombre del Estado + Timestamp + Usuario
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 12.dp)) {
            Text(
                text = movimiento.estado,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1B)
            )

            Text(
                text = formatFechaHoraLong(movimiento.fechaHora),
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Actualizado por: ${movimiento.nombreUsuario.ifBlank { "Personal de Agencia" }}",
                fontSize = 10.sp,
                color = Color(0xFF616161)
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontWeight = FontWeight.SemiBold, color = Color.Gray, fontSize = 11.sp)
        Text(text = value, fontWeight = FontWeight.Medium, color = Color.Black, fontSize = 13.sp)
    }
}
