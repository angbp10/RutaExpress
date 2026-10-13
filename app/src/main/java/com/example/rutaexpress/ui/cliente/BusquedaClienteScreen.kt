package com.example.rutaexpress.ui.cliente

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rutaexpress.R
import com.example.rutaexpress.data.model.Viaje
import com.example.rutaexpress.viewmodel.AGENCIAS_DISPONIBLES
import com.example.rutaexpress.viewmodel.BusquedaClienteUiState
import com.example.rutaexpress.viewmodel.BusquedaClienteViewModel
import com.example.rutaexpress.viewmodel.RUTAS_PROMOCIONALES
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val PrimaryRedHex = Color(0xFFB71C1C)
private val BackgroundSurfaceHex = Color(0xFFFCF9F8)
private val TextPrimaryHex = Color(0xFF1C1B1B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusquedaClienteScreen(
    viewModel: BusquedaClienteViewModel = viewModel(),
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedBottomTab by remember { mutableIntStateOf(0) }

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val sdfShort = SimpleDateFormat("dd/MM", Locale.getDefault())

    val hoyStr = sdf.format(Date())
    val hoyLabel = "Hoy (${sdfShort.format(Date())})"

    val calManana = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    val mananaStr = sdf.format(calManana.time)
    val mananaLabel = "Mañana (${sdfShort.format(calManana.time)})"

    // DatePicker nativo
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formatted = "%02d/%02d/%d".format(dayOfMonth, month + 1, year)
                viewModel.onFechaChanged(formatted)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        containerColor = BackgroundSurfaceHex,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "BÚSQUEDA",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRedHex,
                        letterSpacing = 1.2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menú",
                            tint = TextPrimaryHex
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Perfil",
                            tint = PrimaryRedHex,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundSurfaceHex,
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedBottomTab == 0,
                    onClick = { selectedBottomTab = 0 },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "Schedules") },
                    label = { Text("Schedules", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryRedHex,
                        selectedTextColor = PrimaryRedHex,
                        indicatorColor = PrimaryRedHex.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 1,
                    onClick = { selectedBottomTab = 1 },
                    icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = "My Trips") },
                    label = { Text("My Trips", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 2,
                    onClick = { selectedBottomTab = 2 },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Tickets") },
                    label = { Text("Tickets", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 3,
                    onClick = { selectedBottomTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp) }
                )
            }
        }
    ) { innerPadding ->
        // ÚNICO LazyColumn para toda la pantalla evitando problemas de scroll anidado
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sección 1: Carrusel Automático Superior
            item {
                CarruselBannerSuperior()
            }

            // Sección 2: Formulario de Búsqueda
            item {
                FormularioBusquedaCard(
                    uiState = uiState,
                    hoyStr = hoyStr,
                    hoyLabel = hoyLabel,
                    mananaStr = mananaStr,
                    mananaLabel = mananaLabel,
                    onOrigenChanged = { viewModel.onOrigenChanged(it) },
                    onDestinoChanged = { viewModel.onDestinoChanged(it) },
                    onSwapClick = { viewModel.intercambiarDestinos() },
                    onHoyClick = { viewModel.seleccionarFechaHoy() },
                    onMananaClick = { viewModel.seleccionarFechaManana() },
                    onOtraFechaClick = { datePickerDialog.show() },
                    onBuscarClick = { viewModel.buscarSalidas() }
                )
            }

            // Sección 3: Resultados de la Búsqueda
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = PrimaryRedHex)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Buscando salidas disponibles...",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (uiState.haBuscado && uiState.resultadosBusqueda.isEmpty()) {
                // Empty State
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBus,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No hay salidas programadas para esta ruta en la fecha seleccionada.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else if (uiState.resultadosBusqueda.isNotEmpty()) {
                // Encabezado de Resultados
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Turnos activos encontrados: ${uiState.resultadosBusqueda.size} salidas directas",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryHex
                        )
                        TextButton(onClick = {}) {
                            Text("Filtrar horarios", fontSize = 12.sp, color = PrimaryRedHex)
                        }
                    }
                }

                // Tarjetas de Resultados
                items(uiState.resultadosBusqueda, key = { it.id }) { viaje ->
                    ResultadoViajeCard(viaje = viaje)
                }
            }

            // Sección 4: Pie de Página (Información Importante - Siempre visible)
            item {
                PieDePaginaSeccion()
            }
        }
    }
}

@Composable
fun CarruselBannerSuperior() {
    val pagerState = rememberPagerState(pageCount = { RUTAS_PROMOCIONALES.size })

    // Automatización: avanza página cada 10 segundos con bucle infinito
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000L) // Espera 10 segundos
            if (RUTAS_PROMOCIONALES.isNotEmpty()) {
                val nextPage = (pagerState.currentPage + 1) % RUTAS_PROMOCIONALES.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth()
    ) { page ->
        val ruta = RUTAS_PROMOCIONALES[page]
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Capa 1: Imagen de fondo
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Capa 2: Gradiente oscuro superpuesto
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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

                // Capa 3: Textos superpuestos
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = ruta.mensajeConfianza,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Column {
                        Text(
                            text = ruta.tituloRuta,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${ruta.descripcion} • Turismo Azañero",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FormularioBusquedaCard(
    uiState: BusquedaClienteUiState,
    hoyStr: String,
    hoyLabel: String,
    mananaStr: String,
    mananaLabel: String,
    onOrigenChanged: (String) -> Unit,
    onDestinoChanged: (String) -> Unit,
    onSwapClick: () -> Unit,
    onHoyClick: () -> Unit,
    onMananaClick: () -> Unit,
    onOtraFechaClick: () -> Unit,
    onBuscarClick: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título y Badge distribuidos correctamente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Buscar Salidas Directas",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryHex,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = PrimaryRedHex.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Salidas cada 45 min",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRedHex,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Fila Origen - Swap - Destino con pesos 50/50 e imprevistos de texto prevenidos
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AgenciaDropdownMenu(
                    label = "Origen",
                    selectedValue = uiState.origen,
                    opciones = AGENCIAS_DISPONIBLES,
                    onValueChange = onOrigenChanged,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onSwapClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(PrimaryRedHex, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Intercambiar Origen y Destino",
                        tint = Color.White
                    )
                }

                AgenciaDropdownMenu(
                    label = "Destino",
                    selectedValue = uiState.destino,
                    opciones = AGENCIAS_DISPONIBLES,
                    onValueChange = onDestinoChanged,
                    modifier = Modifier.weight(1f)
                )
            }

            // Selector de Fecha con distribución exacta de botones
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Fecha de Viaje: ${uiState.fechaSeleccionada}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimaryHex
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val esHoy = uiState.fechaSeleccionada == hoyStr
                    val esManana = uiState.fechaSeleccionada == mananaStr

                    // Botón "Hoy"
                    Button(
                        onClick = onHoyClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (esHoy) PrimaryRedHex else Color(0xFFF0F0F0),
                            contentColor = if (esHoy) Color.White else TextPrimaryHex
                        )
                    ) {
                        Text(
                            text = hoyLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Botón "Mañana"
                    Button(
                        onClick = onMananaClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (esManana) PrimaryRedHex else Color(0xFFF0F0F0),
                            contentColor = if (esManana) Color.White else TextPrimaryHex
                        )
                    ) {
                        Text(
                            text = mananaLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Botón "Otra fecha"
                    OutlinedButton(
                        onClick = onOtraFechaClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Otra fecha",
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Botón Principal Buscar Salidas
            Button(
                onClick = onBuscarClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRedHex)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buscar Salidas Directas",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgenciaDropdownMenu(
    label: String,
    selectedValue: String,
    opciones: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            maxLines = 1,
            label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryRedHex,
                focusedLabelColor = PrimaryRedHex,
                cursorColor = PrimaryRedHex
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    onClick = {
                        onValueChange(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ResultadoViajeCard(viaje: Viaje) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${viaje.origen} ➔ ${viaje.destino}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryHex
                    )
                    Text(
                        text = "Hora de salida: ${viaje.horaSalida}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = PrimaryRedHex.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Directo",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRedHex,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "S/ ${"%.2f".format(viaje.precio)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRedHex
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = viaje.vehiculoDescripcion.ifBlank { "Toyota Hiace / H1" },
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }

            // Indicador de Progreso de Asientos
            val totales = if (viaje.asientosTotales > 0) viaje.asientosTotales else 15
            val disponibles = viaje.asientosDisponibles
            val progreso = (disponibles.toFloat() / totales.toFloat()).coerceIn(0f, 1f)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Disponibilidad de Asientos",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$disponibles libres / $totales totales",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRedHex
                    )
                }
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = PrimaryRedHex,
                    trackColor = PrimaryRedHex.copy(alpha = 0.15f),
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Botón Seleccionar Asientos
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRedHex)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Seleccionar Asientos",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PieDePaginaSeccion() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Tarjeta de Alerta
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryRedHex.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = PrimaryRedHex,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "¿No encuentras el horario ideal?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryHex
                    )
                }
                TextButton(onClick = {}) {
                    Text("Activar alerta", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryRedHex)
                }
            }
        }

        // Información Importante de Viaje
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Información Importante de Viaje",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryHex
                )

                ItemInfoViaje(
                    icon = Icons.Default.Schedule,
                    texto = "Embarque 15 min antes en agencias de Turismo Azañero."
                )
                ItemInfoViaje(
                    icon = Icons.Default.Luggage,
                    texto = "Equipaje permitido: Bolso de mano + maleta de hasta 15kg."
                )
                ItemInfoViaje(
                    icon = Icons.Default.CheckCircle,
                    texto = "Puntualidad garantizada en todas nuestras salidas."
                )
            }
        }
    }
}

@Composable
fun ItemInfoViaje(
    icon: ImageVector,
    texto: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryRedHex,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = texto,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
