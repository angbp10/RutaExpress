package com.example.rutaexpress.ui.cliente

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rutaexpress.viewmodel.BoletoDigitalViewModel

private val PrimaryRedHex = Color(0xFFF72907)
private val DarkRedBanner = Color(0xFFB71C1C)
private val SoftBackgroundHex = Color(0xFFFCF9F8)
private val GreenAbordado = Color(0xFF2E7D32)

/**
 * Función de utilidad para generar una imagen Bitmap de un código QR mediante ZXing.
 */
fun generarQrBitmap(contenido: String, width: Int = 512, height: Int = 512): Bitmap? {
    if (contenido.isBlank()) return null
    return try {
        val writer = com.google.zxing.qrcode.QRCodeWriter()
        val bitMatrix = writer.encode(contenido, com.google.zxing.BarcodeFormat.QR_CODE, width, height)
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE)
            }
        }
        bmp
    } catch (_: Exception) {
        null
    }
}

/**
 * Pantalla de Boleto Digital con Código QR para Abordaje (HU19).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoletoDigitalScreen(
    reservaId: String,
    viewModel: BoletoDigitalViewModel = viewModel(),
    onBackClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(reservaId) {
        viewModel.cargarBoleto(reservaId)
    }

    Scaffold(
        containerColor = SoftBackgroundHex,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "BOLETO DIGITAL",
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
                    Text(text = "Generando boleto digital...", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else if (uiState.reserva == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.errorMessage ?: "No se encontró la información del boleto.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }
        } else {
            val reserva = uiState.reserva!!
            val esAbordado = reserva.estadoAbordaje.equals("ABORDADO", ignoreCase = true)
            val qrBitmap = remember(reserva.id) { generarQrBitmap(reserva.id) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // TICKET ELEVATED CARD (HU19)
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header del Ticket (Sólido Corporativo)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkRedBanner)
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsBus,
                                            contentDescription = null,
                                            tint = DarkRedBanner,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "TURISMO AZAÑERO",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "PASE DE ABORDAJE DIGITAL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.85f),
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Cuerpo del Ticket
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Código Reserva + Estado Abordaje Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "CÓDIGO DE BOLETO", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = reserva.codigoReserva.ifBlank { "REX-XXXXX" },
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = DarkRedBanner
                                    )
                                }

                                // BADGE SELLO "ABORDADO" (HU19)
                                if (esAbordado) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFE8F5E9),
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, GreenAbordado)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = GreenAbordado,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "✓ ABORDADO",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = GreenAbordado
                                            )
                                        }
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFFFF3E0),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D))
                                    ) {
                                        Text(
                                            text = "PENDIENTE DE ABORDAJE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE65100),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Ruta principal
                            Column {
                                Text(text = "RUTA DE VIAJE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${reserva.origen}  ➔  ${reserva.destino}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1C1B1B)
                                )
                            }

                            // Fecha, Hora y Asientos
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "FECHA Y HORA", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${reserva.fechaViaje}\n${reserva.horaSalida}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1C1B1B)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "ASIENTO(S)", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = reserva.asientos.joinToString(", ") { "N° $it" },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkRedBanner
                                    )
                                }
                            }

                            // Pasajero(s)
                            if (reserva.pasajeros.isNotEmpty()) {
                                Column {
                                    Text(text = "PASAJERO(S)", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    reserva.pasajeros.forEach { p ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${p.nombres} (DNI: ${p.dni})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF1C1B1B)
                                            )
                                        }
                                    }
                                }
                            }

                            // Línea Divisoria Decorativa
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFFE0E0E0))
                            )

                            // SECCIÓN QR EN EL CENTRO (HU19)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "CÓDIGO QR DE ABORDAJE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    letterSpacing = 0.5.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Box(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .border(2.dp, if (esAbordado) GreenAbordado else DarkRedBanner, RoundedCornerShape(12.dp))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (qrBitmap != null) {
                                        Image(
                                            bitmap = qrBitmap.asImageBitmap(),
                                            contentDescription = "Código QR de Abordaje",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.QrCode2,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(120.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = if (esAbordado)
                                        "Este boleto ya fue registrado y abordado."
                                    else
                                        "Muestra este código QR al chofer o recepcionista para abordar.",
                                    fontSize = 11.sp,
                                    color = if (esAbordado) GreenAbordado else Color.DarkGray,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Botón Volver
                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRedHex)
                ) {
                    Text("Volver", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
