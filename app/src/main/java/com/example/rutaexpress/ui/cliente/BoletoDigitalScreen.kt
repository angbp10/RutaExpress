package com.example.rutaexpress.ui.cliente

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rutaexpress.R
import com.example.rutaexpress.data.model.Reserva
import com.example.rutaexpress.viewmodel.BoletoDigitalViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream

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
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(contenido, BarcodeFormat.QR_CODE, width, height)
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
 * Dibuja y genera un Bitmap PNG completo del Boleto Digital para Guardar o Compartir.
 */
fun generarBoletoCompletoBitmap(reserva: Reserva): Bitmap {
    val width = 800
    val height = 1100
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Fondo blanco
    canvas.drawColor(AndroidColor.WHITE)

    // Header Rojo
    paint.color = AndroidColor.parseColor("#B71C1C")
    canvas.drawRect(0f, 0f, width.toFloat(), 180f, paint)

    paint.color = AndroidColor.WHITE
    paint.textSize = 40f
    paint.isFakeBoldText = true
    paint.textAlign = Paint.Align.CENTER
    canvas.drawText("TURISMO AZAÑERO", width / 2f, 85f, paint)

    paint.textSize = 24f
    paint.isFakeBoldText = false
    canvas.drawText("PASE DE ABORDAJE DIGITAL", width / 2f, 135f, paint)

    // Código de Reserva
    paint.color = AndroidColor.parseColor("#B71C1C")
    paint.textSize = 38f
    paint.isFakeBoldText = true
    paint.textAlign = Paint.Align.LEFT
    canvas.drawText("CÓDIGO: ${reserva.codigoReserva}", 60f, 260f, paint)

    // Ruta
    paint.color = AndroidColor.parseColor("#1C1B1B")
    paint.textSize = 34f
    paint.isFakeBoldText = true
    canvas.drawText("${reserva.origen} ➔ ${reserva.destino}", 60f, 330f, paint)

    // Fecha / Hora y Asientos
    paint.textSize = 26f
    paint.isFakeBoldText = false
    paint.color = AndroidColor.DKGRAY
    canvas.drawText("Fecha y Hora: ${reserva.fechaViaje} • ${reserva.horaSalida}", 60f, 390f, paint)
    canvas.drawText("Asiento(s): ${reserva.asientos.joinToString(", ") { "N° $it" }}", 60f, 440f, paint)

    // Pasajeros
    if (reserva.pasajeros.isNotEmpty()) {
        val pStr = reserva.pasajeros.joinToString(", ") { "${it.nombres} (${it.dni})" }
        canvas.drawText("Pasajero(s): $pStr", 60f, 490f, paint)
    }

    // Línea divisoria
    paint.strokeWidth = 3f
    paint.color = AndroidColor.LTGRAY
    canvas.drawLine(60f, 540f, width - 60f, 540f, paint)

    // Código QR
    val qrBmp = generarQrBitmap(reserva.id, 400, 400)
    if (qrBmp != null) {
        val qrLeft = (width - 400) / 2f
        canvas.drawBitmap(qrBmp, qrLeft, 580f, null)
    }

    // Pie de página
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = 22f
    paint.color = AndroidColor.GRAY
    canvas.drawText("Muestra este código QR al momento de abordar", width / 2f, 1030f, paint)

    return bitmap
}

/**
 * Guarda el boleto en la Galería de Fotos del dispositivo usando MediaStore.
 */
fun guardarBoletoEnGaleria(context: Context, bitmap: Bitmap, codigoReserva: String): Boolean {
    return try {
        val resolver = context.contentResolver
        val filename = "Boleto_RutaExpress_$codigoReserva.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/RutaExpress")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: return false

        resolver.openOutputStream(imageUri)?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }

        contentValues.clear()
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(imageUri, contentValues, null, null)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

/**
 * Comparte la imagen del Boleto Digital a través de WhatsApp / Aplicaciones Externas.
 */
fun compartirBoletoImagen(context: Context, bitmap: Bitmap, codigoReserva: String) {
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "boleto_$codigoReserva.png")
        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Boleto Digital RutaExpress - $codigoReserva")
            putExtra(Intent.EXTRA_TEXT, "Adjunto mi pase de abordaje digital RutaExpress ($codigoReserva).")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Compartir Boleto Digital via"))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al compartir boleto: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Pantalla de Boleto Digital con Código QR y Opciones para Invitados (HU19).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoletoDigitalScreen(
    reservaId: String,
    viewModel: BoletoDigitalViewModel = viewModel(),
    onBackClick: () -> Unit = {},
) {
    val context = LocalContext.current
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
                // ADVERTENCIA CRÍTICA PARA CLIENTES INVITADOS (HU19 PERSISTENCIA)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF3E0),
                    border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "¡Atención! Guarda o toma captura de este boleto. Al no tener cuenta, no podrás recuperarlo si cierras la aplicación.",
                            fontSize = 12.sp,
                            color = Color(0xFF5D4037),
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp
                        )
                    }
                }

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
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_largo),
                                    contentDescription = "RutaExpress Logo",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(45.dp),
                                    contentScale = ContentScale.Fit
                                )
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
                                        border = BorderStroke(1.5.dp, GreenAbordado)
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
                                        border = BorderStroke(1.dp, Color(0xFFFFB74D))
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

                // ACCIONES DE PERSISTENCIA Y COMPARTIR (HU19 PERSISTENCIA)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Botón Descargar Imagen en Galería
                    Button(
                        onClick = {
                            val ticketBmp = generarBoletoCompletoBitmap(reserva)
                            val guardadoExitoso = guardarBoletoEnGaleria(context, ticketBmp, reserva.codigoReserva)
                            if (guardadoExitoso) {
                                Toast.makeText(context, "¡Boleto guardado en tu galería de fotos!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Error al guardar el boleto en la galería.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkRedBanner)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Descargar (PNG)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Botón Compartir Boleto
                    OutlinedButton(
                        onClick = {
                            val ticketBmp = generarBoletoCompletoBitmap(reserva)
                            compartirBoletoImagen(context, ticketBmp, reserva.codigoReserva)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, PrimaryRedHex)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = PrimaryRedHex,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compartir", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryRedHex)
                    }
                }

                // Botón Volver
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f))
                ) {
                    Text("Volver", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                }
            }
        }
    }
}
