package com.example.rutaexpress.ui.recepcionista

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.rutaexpress.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rutaexpress.data.model.Reserva
import com.example.rutaexpress.data.repository.ReservaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PrimaryRedHex = Color(0xFFB71C1C)
private val SoftPinkBackground = Color(0xFFFFEBEE)
private val SoftPinkBorder = Color(0xFFFFCDD2)
private val TextPrimaryHex = Color(0xFF1C1B1B)

/**
 * Estado UI para la pantalla de Cobro y Activación de Reservas en Taquilla (HU11).
 */
data class CobroReservaUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val haBuscado: Boolean = false,
    val reservaEncontrada: Reserva? = null,
    val medioPagoSeleccionado: String = "Efectivo", // "Efectivo" o "Yape"
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val pasajeEmitido: Reserva? = null,
    val mostrarModalTicket: Boolean = false,
)

/**
 * ViewModel encargado del flujo de Cobro y Activación de Reservas en Taquilla (HU11).
 */
class CobroReservaViewModel : ViewModel() {

    private val reservaRepository by lazy { ReservaRepository() }

    private val _uiState = MutableStateFlow(CobroReservaUiState())
    val uiState: StateFlow<CobroReservaUiState> = _uiState.asStateFlow()

    fun onQueryChanged(nuevoQuery: String) {
        _uiState.update { it.copy(query = nuevoQuery) }
    }

    fun seleccionarMedioPago(medio: String) {
        _uiState.update { it.copy(medioPagoSeleccionado = medio) }
    }

    /**
     * Busca la reserva por DNI o por Código REX-XXXXX en Firestore.
     */
    fun buscarReserva() {
        val q = _uiState.value.query.trim()
        if (q.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingresa un DNI de 8 dígitos o un Código REX-XXXXX") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, haBuscado = true) }

        viewModelScope.launch {
            val result = reservaRepository.buscarReservaPorCodigoODni(q)
            result.onSuccess { reserva ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        reservaEncontrada = reserva,
                        errorMessage = if (reserva == null) "No se encontró ninguna reserva para '$q'" else null
                    )
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        reservaEncontrada = null,
                        errorMessage = "Error al buscar reserva: ${ex.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * Confirma el pago de la reserva y actualiza su estado a "PAGADO" en Firebase.
     */
    fun confirmarPagoYEmitirPasaje() {
        val reserva = _uiState.value.reservaEncontrada ?: return
        val medio = _uiState.value.medioPagoSeleccionado

        if (reserva.estado.equals("PAGADO", ignoreCase = true)) {
            _uiState.update { it.copy(errorMessage = "Esta reserva ya se encuentra pagada.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = reservaRepository.confirmarPagoReserva(reserva.id, medio)
            result.onSuccess {
                val reservaActualizada = reserva.copy(
                    estado = "PAGADO",
                    medioPago = medio,
                    fechaPago = System.currentTimeMillis()
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        reservaEncontrada = reservaActualizada,
                        pasajeEmitido = reservaActualizada,
                        mostrarModalTicket = true,
                        successMessage = "¡Pago registrado con éxito! Boleto emitido."
                    )
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al registrar el pago: ${ex.localizedMessage}"
                    )
                }
            }
        }
    }

    fun cerrarModalTicket() {
        _uiState.update {
            it.copy(
                mostrarModalTicket = false,
                query = "",
                reservaEncontrada = null,
                haBuscado = false
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}

/**
 * Pantalla de Cobro y Activación de Reservas en Taquilla (HU11).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CobroReservaScreen(
    viewModel: CobroReservaViewModel = viewModel(),
    onBackClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    // Modal de Boleto Emitido con QR
    if (uiState.mostrarModalTicket && uiState.pasajeEmitido != null) {
        TicketBoletoEmitidoDialog(
            reserva = uiState.pasajeEmitido!!,
            onDismiss = { viewModel.cerrarModalTicket() }
        )
    }

    Scaffold(
        containerColor = Color(0xFFFCF9F8),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "TAQUILLA - COBRO DE RESERVAS",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRedHex,
                        letterSpacing = 1.sp
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFCF9F8))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Tarjeta de Búsqueda
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Buscar Reserva de Pasaje",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryHex
                    )
                    Text(
                        text = "Ingresa el DNI del cliente (8 dígitos) o el Código de Reserva (ej: REX-50168)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.query,
                            onValueChange = { viewModel.onQueryChanged(it) },
                            placeholder = { Text("DNI o REX-XXXXX") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = PrimaryRedHex
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryRedHex,
                                focusedLabelColor = PrimaryRedHex,
                                cursorColor = PrimaryRedHex
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = { viewModel.buscarReserva() },
                            enabled = !uiState.isLoading && uiState.query.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRedHex),
                            modifier = Modifier.height(52.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Buscar", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // 2. Resultados de la Búsqueda
            if (uiState.haBuscado && uiState.reservaEncontrada == null && !uiState.isLoading) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No se encontró ninguna reserva asociada al DNI o código '${uiState.query}'.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (uiState.reservaEncontrada != null) {
                val reserva = uiState.reservaEncontrada!!
                val esPagado = reserva.estado.equals("PAGADO", ignoreCase = true)

                // Tarjeta de Detalle de Reserva
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, if (esPagado) Color(0xFFA5D6A7) else SoftPinkBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Cabecera de la Reserva
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "RESERVA: ${reserva.codigoReserva}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryRedHex
                                )
                                Text(
                                    text = "Ruta: ${reserva.origen} ➔ ${reserva.destino}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryHex
                                )
                            }

                            Surface(
                                color = if (esPagado) Color(0xFFE8F5E9) else SoftPinkBackground,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (esPagado) "🟢 PAGADO" else "🔴 RESERVADO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (esPagado) Color(0xFF2E7D32) else PrimaryRedHex,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        // Info del Viaje
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFFAFAFA),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = PrimaryRedHex,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Fecha: ${reserva.fechaViaje} • Hora: ${reserva.horaSalida}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.EventSeat,
                                        contentDescription = null,
                                        tint = PrimaryRedHex,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Asientos: ${reserva.asientos.joinToString(", ") { "N° $it" }}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryRedHex
                                    )
                                }
                            }
                        }

                        // Lista de Pasajeros
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Pasajeros Registrados:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryHex
                            )
                            reserva.pasajeros.forEach { p ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${p.nombres} (DNI: ${p.dni}) - Asiento ${p.asientoNumero}",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }

                        // Monto Total a Pagar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Monto Total a Cobrar:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryHex
                            )
                            Text(
                                text = "S/ ${"%.2f".format(reserva.montoTotal)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryRedHex
                            )
                        }

                        // Formas de Pago y Confirmación si NO está pagado
                        if (!esPagado) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFFE0E0E0))
                            )

                            Text(
                                text = "Selecciona el Medio de Pago en Taquilla:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryHex
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val esEfectivo = uiState.medioPagoSeleccionado == "Efectivo"
                                val esYape = uiState.medioPagoSeleccionado == "Yape"

                                // Opción Efectivo
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.seleccionarMedioPago("Efectivo") },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (esEfectivo) SoftPinkBackground else Color(0xFFF5F5F5),
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (esEfectivo) PrimaryRedHex else Color.Transparent
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AttachMoney,
                                            contentDescription = null,
                                            tint = if (esEfectivo) PrimaryRedHex else Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Efectivo",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (esEfectivo) PrimaryRedHex else Color.DarkGray
                                        )
                                    }
                                }

                                // Opción Yape
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.seleccionarMedioPago("Yape") },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (esYape) SoftPinkBackground else Color(0xFFF5F5F5),
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (esYape) PrimaryRedHex else Color.Transparent
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode2,
                                            contentDescription = null,
                                            tint = if (esYape) PrimaryRedHex else Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Yape QR",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (esYape) PrimaryRedHex else Color.DarkGray
                                        )
                                    }
                                }
                            }

                            // Si eligió Yape, mostrar tarjeta QR de Agencia con imagen real R.drawable.qr_yape
                            if (uiState.medioPagoSeleccionado == "Yape") {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = SoftPinkBackground),
                                    border = BorderStroke(1.5.dp, SoftPinkBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            color = PrimaryRedHex,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "PAGO POR YAPE AGENCIA",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }

                                        // Imagen del Código QR Yape
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.qr_yape),
                                                contentDescription = "Código QR Yape",
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier
                                                    .size(200.dp)
                                                    .padding(8.dp)
                                            )
                                        }

                                        Text(
                                            text = "Gianfranco Ojeda Lachy",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimaryHex
                                        )

                                        Surface(
                                            color = Color.White,
                                            shape = RoundedCornerShape(20.dp),
                                            border = BorderStroke(1.dp, SoftPinkBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Phone,
                                                    contentDescription = null,
                                                    tint = PrimaryRedHex,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "913587767",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = PrimaryRedHex
                                                )
                                            }
                                        }

                                        Surface(
                                            color = PrimaryRedHex.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = "Monto a yapear: S/ ${"%.2f".format(reserva.montoTotal)}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryRedHex,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }

                                        Text(
                                            text = "Escanea el código QR o yapea al número indicado para efectuar el pago.",
                                            fontSize = 11.sp,
                                            color = Color.DarkGray,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // Botón Confirmar Pago y Emitir Pasaje
                            Button(
                                onClick = { viewModel.confirmarPagoYEmitirPasaje() },
                                enabled = !uiState.isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRedHex)
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Confirmar Pago y Emitir Pasaje",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Esta reserva ya fue cobrada (${reserva.medioPago}) y el pasaje está activo.",
                                fontSize = 12.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Diálogo con el Boleto Emitido y su Código QR generado.
 */
@Composable
fun TicketBoletoEmitidoDialog(
    reserva: Reserva,
    onDismiss: () -> Unit,
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val fechaPagoStr = if (reserva.fechaPago > 0) sdf.format(Date(reserva.fechaPago)) else sdf.format(Date())

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
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Surface(
                    color = SoftPinkBackground,
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        tint = PrimaryRedHex,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(36.dp)
                    )
                }

                Text(
                    text = "BOLETO DE VIAJE OFICIAL",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryRedHex
                )

                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "PASAJE ACTIVADO - PAGADO (${reserva.medioPago})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Código QR Generado
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "Código QR del Boleto",
                            tint = Color.Black,
                            modifier = Modifier.size(110.dp)
                        )
                        Text(
                            text = reserva.codigoReserva,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryRedHex
                        )
                    }
                }

                // Detalles
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFAFAFA),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Ruta: ${reserva.origen} ➔ ${reserva.destino}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryHex
                        )
                        Text(
                            text = "Salida: ${reserva.fechaViaje} • ${reserva.horaSalida}",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Asientos: ${reserva.asientos.joinToString(", ") { "N° $it" }}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRedHex
                        )

                        Text(
                            text = "Pasajero(s):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryHex
                        )
                        reserva.pasajeros.forEach { p ->
                            Text(
                                text = "• ${p.nombres} (DNI: ${p.dni})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFE0E0E0))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Fecha Cobro:", fontSize = 11.sp, color = Color.Gray)
                            Text(fechaPagoStr, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Monto Pagado:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "S/ ${"%.2f".format(reserva.montoTotal)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryRedHex
                            )
                        }
                    }
                }

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Imprimir / Cerrar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRedHex)
                    ) {
                        Text("Aceptar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
