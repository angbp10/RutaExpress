package com.example.rutaexpress.ui.cliente

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rutaexpress.data.model.Encomienda
import com.example.rutaexpress.data.model.Movimiento
import com.example.rutaexpress.viewmodel.RastreoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PrimaryRedHex = Color(0xFFF72907)
private val DarkRedBanner = Color(0xFFB71C1C)
private val SoftBackgroundHex = Color(0xFFFCF9F8)
private val GreenDelivered = Color(0xFF2E7D32)
private val BlueWarehouse = Color(0xFF1976D2)
private val OrangeTransit = Color(0xFFE65100)
private val PurplePickup = Color(0xFF7B1FA2)

private fun formatFechaHoraTracking(timestamp: Long): String {
    if (timestamp <= 0L) return "Reciente"
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (_: Exception) {
        "Reciente"
    }
}

/**
 * Pantalla Pública de Rastreo de Encomiendas por Guía o DNI para Clientes e Invitados (HU22).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RastreoClienteScreen(
    viewModel: RastreoViewModel = viewModel(),
    onBackClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = SoftBackgroundHex,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "RASTREO DE ENCOMIENDAS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1B)
                    )
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. CABECERA CON BANNER (HU22)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkRedBanner),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = DarkRedBanner,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Rastrea tu Encomienda",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Ingresa el número de guía o DNI del destinatario",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 2. BARRA DE BÚSQUEDA REDONDEADA (HU22)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = { viewModel.onQueryChanged(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ej. 72635182 o e-0000001", fontSize = 13.sp) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = PrimaryRedHex
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { viewModel.buscarEncomienda() }),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryRedHex,
                        focusedLabelColor = PrimaryRedHex
                    )
                )

                Button(
                    onClick = { viewModel.buscarEncomienda() },
                    modifier = Modifier.height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRedHex),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Buscar", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Mensaje de Error
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // 3. RESULTADOS Y LÍNEA DE TIEMPO / TIMELINE (HU22)
            if (uiState.searched && !uiState.isLoading && uiState.encomiendas.isEmpty()) {
                // EMPTY STATE (HU22)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = PrimaryRedHex,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No encontramos ninguna encomienda",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No encontramos ninguna encomienda vinculada a este dato. Verifica e intenta nuevamente.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else if (uiState.encomiendas.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.encomiendas, key = { it.id }) { encomienda ->
                        EncomiendaRastreoResultadoCard(encomienda = encomienda)
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de Resultado con la Línea de Tiempo del Tracking de la Encomienda (HU22).
 */
@Composable
fun EncomiendaRastreoResultadoCard(encomienda: Encomienda) {
    val estadoActualEfectivo = encomienda.estadoActual.ifBlank { encomienda.estado.ifBlank { "En almacén" } }

    val colorBadge = when (estadoActualEfectivo) {
        "En almacén", "EN_ALMACEN" -> BlueWarehouse
        "En ruta", "EN_TRANSITO" -> OrangeTransit
        "Disponible para recojo", "DISPONIBLE" -> PurplePickup
        "Entregado", "ENTREGADO" -> GreenDelivered
        else -> PrimaryRedHex
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header del Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GUÍA DE REMITO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = encomienda.guia,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryRedHex
                    )
                }

                Surface(
                    color = colorBadge.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = estadoActualEfectivo,
                        color = colorBadge,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))

            // Información de Remitente y Consignatario
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "REMITENTE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(text = encomienda.remitente, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1B1B))
                    Text(text = "DNI: ${encomienda.dniRemitente}", fontSize = 11.sp, color = Color.Gray)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "DESTINATARIO", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(text = encomienda.destinatario, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1B1B))
                    Text(text = "DNI: ${encomienda.dniDestinatario}", fontSize = 11.sp, color = Color.Gray)
                }
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))

            // LÍNEA DE TIEMPO DEL HISTORIAL (TIMELINE HU22)
            Text(
                text = "HISTORIAL DE SEGUIMIENTO",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.DarkGray,
                letterSpacing = 0.5.sp
            )

            val listaMovimientos = encomienda.historialMovimientos.ifEmpty {
                listOf(
                    Movimiento(
                        estado = estadoActualEfectivo,
                        fechaHora = encomienda.ultimaActualizacion,
                        nombreUsuario = "RutaExpress Tracking",
                        idUsuario = "SISTEMA"
                    )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                listaMovimientos.forEachIndexed { index, movimiento ->
                    TimelineItem(
                        movimiento = movimiento,
                        isFirst = index == 0,
                        isLast = index == listaMovimientos.size - 1
                    )
                }
            }
        }
    }
}

/**
 * Componente @Composable separado para dibujar las líneas y los nodos del historial con buena precisión visual (HU22).
 */
@Composable
fun TimelineItem(
    movimiento: Movimiento,
    isFirst: Boolean,
    isLast: Boolean,
) {
    val isCurrentStep = isFirst

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
        // Columna Izquierda Fija: Círculo de Estado + Línea Vertical Conectora
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (isCurrentStep) 24.dp else 18.dp)
                    .clip(CircleShape)
                    .background(if (isCurrentStep) PrimaryRedHex else Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                if (isCurrentStep) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(38.dp)
                        .background(if (isCurrentStep) PrimaryRedHex.copy(alpha = 0.4f) else Color.LightGray)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Columna Derecha: Nombre del Estado en Negrita + Fecha y Hora Extraída
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 12.dp)) {
            Text(
                text = movimiento.estado,
                fontSize = if (isCurrentStep) 14.sp else 13.sp,
                fontWeight = if (isCurrentStep) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isCurrentStep) Color(0xFF1C1B1B) else Color.DarkGray
            )

            Text(
                text = formatFechaHoraTracking(movimiento.fechaHora),
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
