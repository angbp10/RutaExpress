package com.example.rutaexpress.ui.cliente

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rutaexpress.R
import com.example.rutaexpress.data.model.Asiento
import com.example.rutaexpress.data.model.Viaje
import com.example.rutaexpress.viewmodel.SeleccionAsientosViewModel

private val PrimaryRedHex = Color(0xFFB71C1C)
private val BackgroundSurfaceHex = Color(0xFFFCF9F8)
private val TextPrimaryHex = Color(0xFF1C1B1B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionAsientosScreen(
    viajeId: String,
    viewModel: SeleccionAsientosViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onConfirmSuccess: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viajeId) {
        viewModel.cargarViajeYAsientos(viajeId)
    }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    val viaje = uiState.viaje ?: Viaje(
        id = viajeId,
        origen = "Cajamarca",
        destino = "Celendín",
        fecha = "20/05/2025",
        horaSalida = "08:00 a. m.",
        precio = 25.0
    )

    val seleccionados = uiState.asientosSeleccionadosIds
    val precioTotal = viaje.precio * seleccionados.size

    // Modal de Registro de Pasajeros (HU18)
    if (uiState.mostrarDialogoPasajeros) {
        RegistroPasajerosBottomSheet(
            pasajerosInputs = uiState.pasajerosInputs,
            isLoading = uiState.isLoading,
            onDniChanged = { index, dni -> viewModel.onPasajeroDniChanged(index, dni) },
            onNombresChanged = { index, nombres -> viewModel.onPasajeroNombresChanged(index, nombres) },
            onConsultarDni = { index -> viewModel.consultarDniPasajero(index) },
            onDismiss = { viewModel.cerrarRegistroPasajeros() },
            onConfirmarReserva = { viewModel.procesarReservaTemporal() }
        )
    }

    // Diálogo Resumen de Reserva con Temporizador de 5 minutos (HU18)
    if (uiState.mostrarDialogoResumen && uiState.reservaCreada != null) {
        ResumenReservaDialog(
            reserva = uiState.reservaCreada!!,
            onDismiss = { viewModel.cerrarDialogoResumen() },
            onVolverInicio = {
                viewModel.cerrarDialogoResumen()
                onConfirmSuccess()
            }
        )
    }

    Scaffold(
        containerColor = BackgroundSurfaceHex,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "SELECCIÓN DE ASIENTOS",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRedHex,
                        letterSpacing = 1.2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextPrimaryHex
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundSurfaceHex,
                )
            )
        },
        bottomBar = {
            BarraInferiorConfirmacion(
                cantidadSeleccionada = seleccionados.size,
                precioTotal = precioTotal,
                isLoading = uiState.isLoading,
                onConfirmarClick = {
                    viewModel.abrirRegistroPasajeros()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Banner Resumen
            BannerResumenViajeCard(viaje = viaje)

            // 2. Leyenda de Estados
            LeyendaEstadosRow()

            // 3. Croquis del Vehículo
            if (uiState.isLoading && uiState.asientosList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryRedHex)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cargando disponibilidad...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val cap = if (viaje.asientosTotales > 0) viaje.asientosTotales else uiState.asientosList.size
                CroquisVehiculoCard(
                    capacidadVehiculo = cap,
                    asientos = uiState.asientosList,
                    seleccionadosIds = seleccionados,
                    onAsientoClick = { asiento ->
                        viewModel.toggleSeleccionAsiento(asiento)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun BannerResumenViajeCard(viaje: Viaje) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Capa 1: Fondo
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )

            // Capa 2: Gradiente Oscuro
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color(0xFF7A0000).copy(alpha = 0.85f),
                                Color(0xFFB71C1C).copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            // Capa 3: Información
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = PrimaryRedHex,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "COMBI INTERPROVINCIAL - DIRECTO",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "S/ ${"%.2f".format(viaje.precio)}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "${viaje.origen} ➔ ${viaje.destino}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "2h 45m de viaje estimado",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Fecha: ${viaje.fecha} • Hora: ${viaje.horaSalida}",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun LeyendaEstadosRow() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Libre
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.5.dp, PrimaryRedHex),
                    color = Color.White,
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.EventSeat,
                            contentDescription = null,
                            tint = PrimaryRedHex,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Libre", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimaryHex)
            }

            // Ocupado
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFFBDBDBD)),
                    color = Color(0xFFEEEEEE),
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.EventSeat,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ocupado", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
            }

            // Elegido
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PrimaryRedHex,
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Elegido", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryRedHex)
            }
        }
    }
}

@Composable
fun CroquisVehiculoCard(
    capacidadVehiculo: Int,
    asientos: List<Asiento>,
    seleccionadosIds: Set<String>,
    onAsientoClick: (Asiento) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cabecera del Bus
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Frente del Vehículo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cabina del Piloto",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                    Surface(
                        color = PrimaryRedHex.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Puerta de Ingreso",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRedHex,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Línea separadora
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFE0E0E0))
            )

            // Renderizado Dinámico de Croquis
            if (capacidadVehiculo <= 15) {
                Croquis15Asientos(
                    asientos = asientos,
                    seleccionadosIds = seleccionadosIds,
                    onAsientoClick = onAsientoClick
                )
            } else {
                Croquis18Asientos(
                    asientos = asientos,
                    seleccionadosIds = seleccionadosIds,
                    onAsientoClick = onAsientoClick
                )
            }
        }
    }
}

@Composable
fun Croquis15Asientos(
    asientos: List<Asiento>,
    seleccionadosIds: Set<String>,
    onAsientoClick: (Asiento) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Fila Piloto: [ CHOFER ] [ Spacer ] [ Asiento 1 ] [ Asiento 2 ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChoferSlot(modifier = Modifier.weight(1f))
            SpacerSlot(modifier = Modifier.weight(1f))
            AsientoSlot(1, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            AsientoSlot(2, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
        }

        // Fila 1: [ Asiento 3 ] [ Asiento 4 ] [ Asiento 5 ] [ Spacer ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsientoSlot(3, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            AsientoSlot(4, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            AsientoSlot(5, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            SpacerSlot(modifier = Modifier.weight(1f))
        }

        // Fila 2: [ Asiento 6 ] [ Asiento 7 ] [ PASILLO ] [ Asiento 8 ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsientoSlot(6, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            AsientoSlot(7, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            PasilloSlot(modifier = Modifier.weight(1f))
            AsientoSlot(8, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
        }

        // Fila 3: [ Asiento 9 ] [ Asiento 10 ] [ PASILLO ] [ Asiento 11 ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsientoSlot(9, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            AsientoSlot(10, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            PasilloSlot(modifier = Modifier.weight(1f))
            AsientoSlot(11, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
        }

        // Fila 4 (Última): [ Asiento 12 ] [ Asiento 13 ] [ Asiento 14 ] [ Asiento 15 ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsientoSlot(12, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            AsientoSlot(13, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            AsientoSlot(14, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            AsientoSlot(15, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun Croquis18Asientos(
    asientos: List<Asiento>,
    seleccionadosIds: Set<String>,
    onAsientoClick: (Asiento) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Fila Piloto: [ CHOFER ] [ Spacer ] [ Asiento 1 ] [ Asiento 2 ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChoferSlot(modifier = Modifier.weight(1f))
            SpacerSlot(modifier = Modifier.weight(1f))
            AsientoSlot(1, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            AsientoSlot(2, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
        }

        // Fila 1: [ Asiento 3 ] [ Asiento 4 ] [ Asiento 5 ] [ Spacer ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsientoSlot(3, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            AsientoSlot(4, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            AsientoSlot(5, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            SpacerSlot(modifier = Modifier.weight(1f))
        }

        // Fila 2: [ Asiento 6 ] [ Asiento 7 ] [ PASILLO ] [ Asiento 8 ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsientoSlot(6, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            AsientoSlot(7, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            PasilloSlot(modifier = Modifier.weight(1f))
            AsientoSlot(8, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
        }

        // Fila 3: [ Asiento 9 ] [ Asiento 10 ] [ PASILLO ] [ Asiento 11 ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsientoSlot(9, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            AsientoSlot(10, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            PasilloSlot(modifier = Modifier.weight(1f))
            AsientoSlot(11, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
        }

        // Fila 4: [ Asiento 12 ] [ Asiento 13 ] [ PASILLO ] [ Asiento 14 ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsientoSlot(12, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            AsientoSlot(13, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            PasilloSlot(modifier = Modifier.weight(1f))
            AsientoSlot(14, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
        }

        // Fila 5 (Última): [ Asiento 15 ] [ Asiento 16 ] [ Asiento 17 ] [ Asiento 18 ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsientoSlot(15, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            AsientoSlot(16, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            AsientoSlot(17, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
            AsientoSlot(18, asientos, seleccionadosIds, onAsientoClick, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun AsientoSlot(
    numero: Int,
    asientos: List<Asiento>,
    seleccionadosIds: Set<String>,
    onClick: (Asiento) -> Unit,
    modifier: Modifier = Modifier,
) {
    val asiento = asientos.find { it.numero == numero }
    Box(
        modifier = modifier.padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        if (asiento != null) {
            AsientoItem(
                asiento = asiento,
                esElegido = seleccionadosIds.contains(asiento.id),
                onClick = { onClick(asiento) }
            )
        } else {
            Spacer(modifier = Modifier.fillMaxWidth().height(46.dp))
        }
    }
}

@Composable
fun PasilloSlot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(3.dp)
            .height(46.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "PASILLO",
            fontSize = 9.sp,
            color = Color.LightGray,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SpacerSlot(modifier: Modifier = Modifier) {
    Spacer(
        modifier = modifier
            .padding(3.dp)
            .height(46.dp)
    )
}

@Composable
fun ChoferSlot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(3.dp)
            .height(46.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF333333),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "CHOFER",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AsientoItem(
    asiento: Asiento,
    esElegido: Boolean,
    onClick: () -> Unit,
) {
    val esOcupado = asiento.estado.lowercase() != "disponible"

    val (bgColor, borderColor, contentColor) = when {
        esOcupado -> Triple(Color(0xFFEEEEEE), Color(0xFFBDBDBD), Color.Gray)
        esElegido -> Triple(PrimaryRedHex, PrimaryRedHex, Color.White)
        else -> Triple(Color.White, PrimaryRedHex, PrimaryRedHex)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clickable(enabled = !esOcupado, onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.EventSeat,
                contentDescription = "Asiento ${asiento.numero}",
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "${asiento.numero}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
fun BarraInferiorConfirmacion(
    cantidadSeleccionada: Int,
    precioTotal: Double,
    isLoading: Boolean,
    onConfirmarClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Total a pagar",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Mano incl.",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "S/ ${"%.2f".format(precioTotal)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryRedHex
                )
            }

            Button(
                onClick = onConfirmarClick,
                enabled = cantidadSeleccionada > 0 && !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryRedHex,
                    disabledContainerColor = Color(0xFFFFCDD2),
                    disabledContentColor = Color.White
                ),
                modifier = Modifier.height(46.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (cantidadSeleccionada > 0) "Confirmar ($cantidadSeleccionada) ➔" else "Selecciona Asiento",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
