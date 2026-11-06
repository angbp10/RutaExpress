package com.example.rutaexpress.ui.cliente

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rutaexpress.viewmodel.PagoClienteViewModel

private val PrimaryRedHex = Color(0xFFF72907)
private val DarkRedBanner = Color(0xFFB71C1C)
private val SoftBackgroundHex = Color(0xFFFCF9F8)
private val YapePurple = Color(0xFF6B1173)
private val PlinCyan = Color(0xFF00A896)

/**
 * Pantalla de Pago por Transferencia (Yape/Plin) para el Cliente (HU17).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagoClienteScreen(
    reservaId: String,
    viewModel: PagoClienteViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onPagoEnviadoSuccess: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(reservaId) {
        viewModel.cargarReserva(reservaId)
    }

    Scaffold(
        containerColor = SoftBackgroundHex,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "PAGO POR TRANSFERENCIA",
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
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = PrimaryRedHex)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Cargando datos de la reserva...", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            val reserva = uiState.reserva

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. BANNER SUPERIOR - TOTAL A PAGAR (HU17)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkRedBanner),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TOTAL A PAGAR",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "S/ ${"%.2f".format(reserva?.montoTotal ?: 0.0)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        if (reserva != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.2f))
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Reserva: ${reserva.codigoReserva}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${reserva.origen} ➔ ${reserva.destino}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Asientos: ${reserva.asientos.joinToString(", ") { "N° $it" }}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "${reserva.fechaViaje} • ${reserva.horaSalida}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // 2. SECCIÓN DE QRs (YAPE Y PLIN) (HU17)
                Text(
                    text = "Selecciona tu método de pago:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1B)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tarjeta Yape
                    QrPaymentOptionCard(
                        title = "Yape",
                        phone = "987 189 872",
                        owner = "Turismo Azañero",
                        brandColor = YapePurple,
                        isSelected = uiState.metodoSeleccionado == "Yape",
                        onClick = { viewModel.onMetodoPagoChanged("Yape") },
                        modifier = Modifier.weight(1f)
                    )

                    // Tarjeta Plin
                    QrPaymentOptionCard(
                        title = "Plin",
                        phone = "987 189 872",
                        owner = "Turismo Azañero",
                        brandColor = PlinCyan,
                        isSelected = uiState.metodoSeleccionado == "Plin",
                        onClick = { viewModel.onMetodoPagoChanged("Plin") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 3. FORMULARIO DE REGISTRO (HU17)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = PrimaryRedHex,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Instrucciones de Pago",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1B1B)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF8E1),
                            border = BorderStroke(1.dp, Color(0xFFFFE082))
                        ) {
                            Text(
                                text = "Escanea el QR de arriba o yapea/plinea al 987 189 872, realiza el pago por S/ ${"%.2f".format(reserva?.montoTotal ?: 0.0)} y registra tu N° de Operación aquí abajo.",
                                fontSize = 12.sp,
                                color = Color(0xFF5D4037),
                                modifier = Modifier.padding(12.dp),
                                lineHeight = 16.sp
                            )
                        }

                        OutlinedTextField(
                            value = uiState.numeroOperacion,
                            onValueChange = { viewModel.onNumeroOperacionChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Número de Operación (6 a 8 dígitos)") },
                            placeholder = { Text("Ej: 84920156") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = PrimaryRedHex
                                )
                            },
                            trailingIcon = {
                                if (uiState.numeroOperacion.length in 6..8) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Válido",
                                        tint = Color(0xFF2E7D32)
                                    )
                                }
                            },
                            isError = uiState.errorMessage != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryRedHex,
                                focusedLabelColor = PrimaryRedHex
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (uiState.errorMessage != null) {
                            Text(
                                text = uiState.errorMessage.orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // 4. BOTÓN PRINCIPAL ROJO (HU17)
                Button(
                    onClick = { viewModel.enviarPagoAValidacion() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !uiState.isSubmitting && uiState.numeroOperacion.length in 6..8,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryRedHex,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.4f)
                    )
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviando...", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enviar Pago a Validación",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // DIÁLOGO DE ÉXITO TRAS ENVIAR PAGO A VALIDACIÓN (HU17)
        if (uiState.isSuccess) {
            AlertDialog(
                onDismissRequest = { },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "¡Pago Enviado a Validación!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Tu número de operación (${uiState.numeroOperacion}) ha sido registrado con éxito.",
                            fontSize = 13.sp,
                            color = Color(0xFF424242),
                            textAlign = TextAlign.Center
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF5F5F5),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "El personal de recepción verificará tu pago. Puedes consultar el estado de tu boleto con tu código: ${uiState.reserva?.codigoReserva}",
                                fontSize = 12.sp,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onPagoEnviadoSuccess,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRedHex),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Volver al Inicio", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White
            )
        }
    }
}

/**
 * Tarjeta interactiva para la selección e inspección del QR de Yape o Plin.
 */
@Composable
fun QrPaymentOptionCard(
    title: String,
    phone: String,
    owner: String,
    brandColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    ElevatedCard(
        modifier = modifier
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier.border(2.5.dp, brandColor, RoundedCornerShape(16.dp))
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) brandColor.copy(alpha = 0.05f) else Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header con Chip Marca
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = brandColor,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }

            // QR Gráfico Simulado
            SimulatedQrGraphic(
                brandColor = brandColor,
                modifier = Modifier.size(90.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = phone,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1B)
            )

            Text(
                text = owner,
                fontSize = 10.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Botón Copiar Número
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(brandColor.copy(alpha = 0.1f))
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Celular $title", phone.replace(" ", ""))
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Número $phone copiado", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copiar",
                    tint = brandColor,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Copiar número",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = brandColor
                )
            }
        }
    }
}

/**
 * Renderiza una representación visual limpia de un código QR genérico.
 */
@Composable
fun SimulatedQrGraphic(
    brandColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val cellSize = width / 7f

            // Patrones de esquina (Position Detection Patterns)
            // Esquina superior izquierda
            drawRect(color = brandColor, topLeft = Offset(0f, 0f), size = Size(cellSize * 2.2f, cellSize * 2.2f))
            drawRect(color = Color.White, topLeft = Offset(cellSize * 0.4f, cellSize * 0.4f), size = Size(cellSize * 1.4f, cellSize * 1.4f))
            drawRect(color = brandColor, topLeft = Offset(cellSize * 0.7f, cellSize * 0.7f), size = Size(cellSize * 0.8f, cellSize * 0.8f))

            // Esquina superior derecha
            drawRect(color = brandColor, topLeft = Offset(width - cellSize * 2.2f, 0f), size = Size(cellSize * 2.2f, cellSize * 2.2f))
            drawRect(color = Color.White, topLeft = Offset(width - cellSize * 1.8f, cellSize * 0.4f), size = Size(cellSize * 1.4f, cellSize * 1.4f))
            drawRect(color = brandColor, topLeft = Offset(width - cellSize * 1.5f, cellSize * 0.7f), size = Size(cellSize * 0.8f, cellSize * 0.8f))

            // Esquina inferior izquierda
            drawRect(color = brandColor, topLeft = Offset(0f, height - cellSize * 2.2f), size = Size(cellSize * 2.2f, cellSize * 2.2f))
            drawRect(color = Color.White, topLeft = Offset(cellSize * 0.4f, height - cellSize * 1.8f), size = Size(cellSize * 1.4f, cellSize * 1.4f))
            drawRect(color = brandColor, topLeft = Offset(cellSize * 0.7f, height - cellSize * 1.5f), size = Size(cellSize * 0.8f, cellSize * 0.8f))

            // Módulos aleatorios de datos centro
            val step = cellSize * 0.9f
            for (x in 2..5) {
                for (y in 2..5) {
                    if ((x + y) % 2 == 0) {
                        drawRect(
                            color = brandColor.copy(alpha = 0.85f),
                            topLeft = Offset(x * step, y * step),
                            size = Size(step * 0.8f, step * 0.8f)
                        )
                    }
                }
            }
        }

        Icon(
            imageVector = Icons.Default.QrCode2,
            contentDescription = null,
            tint = brandColor.copy(alpha = 0.3f),
            modifier = Modifier.size(36.dp)
        )
    }
}
