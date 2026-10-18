package com.example.rutaexpress.ui.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rutaexpress.data.model.UsuarioConductor
import com.example.rutaexpress.data.model.Vehiculo
import com.example.rutaexpress.data.model.Viaje
import com.example.rutaexpress.viewmodel.VehiculoViewModel
import com.example.rutaexpress.viewmodel.ViajeViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

private val PrimaryRedHex = Color(0xFFF72907)
private val BackgroundSurfaceHex = Color(0xFFFCF9F8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecepcionistaHomeScreen(
    viajeViewModel: ViajeViewModel = viewModel(),
    vehiculoViewModel: VehiculoViewModel = viewModel(),
    onNavigateToFlota: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToEmision: () -> Unit = {},
    onNavigateToGestion: () -> Unit = {},
    onNavigateToCobroReserva: () -> Unit = {},
    onNavigateToBusquedaSalidas: () -> Unit = {},
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val viajes by viajeViewModel.viajesState.collectAsState()
    val vehiculos by vehiculoViewModel.vehiculosState.collectAsState()
    val conductores by viajeViewModel.conductoresState.collectAsState()
    val uiState by viajeViewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var showDialog by remember { mutableStateOf(false) }
    var selectedViajeForEdit by remember { mutableStateOf<Viaje?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableStateOf("Todas") }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viajeViewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viajeViewModel.clearMessages()
        }
    }

    val viajesFiltrados = viajes.filter { viaje ->
        val matchesSearch = searchQuery.isBlank() ||
            viaje.origen.contains(searchQuery, ignoreCase = true) ||
            viaje.destino.contains(searchQuery, ignoreCase = true) ||
            viaje.vehiculoDescripcion.contains(searchQuery, ignoreCase = true) ||
            viaje.conductor.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilterTab) {
            "Activas" -> true
            "Inactivas" -> false
            else -> true
        }

        matchesSearch && matchesFilter
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = BackgroundSurfaceHex
            ) {
                // Header del Drawer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryRedHex)
                        .padding(24.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBus,
                                contentDescription = null,
                                tint = PrimaryRedHex
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "TURISMO AZAÑERO",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Panel Administrativo",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Items del Drawer
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Route, contentDescription = null, tint = PrimaryRedHex) },
                    label = { Text("Gestión de Rutas / Viajes", fontWeight = FontWeight.Bold) },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = PrimaryRedHex.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = PrimaryRedHex) },
                    label = { Text("Gestión de Flota / Vehículos", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToFlota()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = null, tint = PrimaryRedHex) },
                    label = { Text("Emisión de Encomiendas", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToEmision()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Assignment, contentDescription = null, tint = PrimaryRedHex) },
                    label = { Text("Seguimiento de Encomiendas", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToGestion()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = PrimaryRedHex) },
                    label = { Text("Cobro de Reservas y Pasajes", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToCobroReserva()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryRedHex) },
                    label = { Text("Búsqueda de Salidas Directas", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToBusquedaSalidas()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            containerColor = BackgroundSurfaceHex,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "RECEPCIÓN - RUTAS Y VIAJES",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRedHex,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú Principal",
                                tint = Color(0xFF1C1B1B)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToProfile) {
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
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        selectedViajeForEdit = null
                        showDialog = true
                    },
                    containerColor = PrimaryRedHex,
                    contentColor = Color.White
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Nueva Ruta")
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Programación de Viajes",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1B)
                )
                Text(
                    text = "Crea, edita o elimina salidas con asignación de flota y conductor.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Buscador
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar ruta por ciudad o agencia...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = Color.Gray
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryRedHex,
                        focusedLabelColor = PrimaryRedHex,
                        cursorColor = PrimaryRedHex,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Filtros (Chips/Tabs en Row scrolleable)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val opciones = listOf("Todas", "Activas", "Inactivas")
                    opciones.forEach { opcion ->
                        val esActivo = selectedFilterTab == opcion
                        Surface(
                            modifier = Modifier.clickable { selectedFilterTab = opcion },
                            shape = RoundedCornerShape(20.dp),
                            color = if (esActivo) PrimaryRedHex else Color(0xFFEFEFEF),
                            contentColor = if (esActivo) Color.White else Color(0xFF1C1B1B)
                        ) {
                            Text(
                                text = opcion,
                                fontSize = 13.sp,
                                fontWeight = if (esActivo) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (uiState.isLoading && viajes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryRedHex)
                    }
                } else if (viajesFiltrados.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No se encontraron viajes con los filtros seleccionados.")
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(viajesFiltrados, key = { it.id }) { viaje ->
                            ViajeCardItem(
                                viaje = viaje,
                                onEdit = {
                                    selectedViajeForEdit = viaje
                                    showDialog = true
                                },
                                onDelete = { viajeViewModel.eliminarViaje(viaje.id) },
                                onEstadoChange = { nuevoEstado ->
                                    viajeViewModel.actualizarEstadoViaje(viaje.id, nuevoEstado)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CrearEditarRutaDialog(
            viajeInicial = selectedViajeForEdit,
            vehiculosDisponibles = vehiculos.filter { it.estado == "activo" },
            conductoresDisponibles = conductores,
            onDismiss = { showDialog = false },
            onSaveNew = { viajeNuevo, capacidad ->
                viajeViewModel.crearViajeConAsientos(viajeNuevo, capacidad)
                showDialog = false
            },
            onUpdate = { viajeEditado ->
                viajeViewModel.actualizarViaje(viajeEditado)
                showDialog = false
            }
        )
    }
}

@Composable
fun ViajeCardItem(
    viaje: Viaje,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onEstadoChange: (String) -> Unit = {},
) {
    var estadoMenuExpanded by remember { mutableStateOf(false) }

    val (badgeBgColor, badgeTextColor, badgeTexto) = when (viaje.estado.uppercase()) {
        "AGOTADO" -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "🔴 Agotado")
        "EN RUTA" -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "🚌 En Ruta")
        "FINALIZADO" -> Triple(Color(0xFF424242), Color.White, "⚫ Finalizado")
        else -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "🟢 En Venta")
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // A. Sección Superior (Ruta y Estado)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viaje.origen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = " ➔ ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRedHex,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                    Text(
                        text = viaje.destino,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Badge de Estado Dinámico
                Surface(
                    color = badgeBgColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = badgeTexto,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // B. Contenedor de Detalles (Fondo Gris Claro)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF8F9FA),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Fila 1 (Horarios y Precio)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${viaje.fecha} a las ${viaje.horaSalida}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1C1B1B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "2h 30m de viaje estimado",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "TARIFA BASE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = "S/ ${"%.2f".format(viaje.precio)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryRedHex
                            )
                        }
                    }

                    // Fila 2 (Conductor)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PrimaryRedHex,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Conductor: ${viaje.conductor.ifBlank { "Sin asignar" }}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray
                        )
                    }

                    // Fila 3 (Vehículo)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBus,
                            contentDescription = null,
                            tint = PrimaryRedHex,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Vehículo: ${viaje.vehiculoDescripcion.ifBlank { "Combi Expreso" }}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray
                        )
                    }

                    // Fila 4 (Progreso de Asientos Dinámico)
                    val totales = if (viaje.asientosTotales > 0) viaje.asientosTotales else 15
                    val ocupados = viaje.asientosOcupados.coerceAtLeast(0)
                    val asientosLibres = (totales - ocupados).coerceAtLeast(0)
                    val progreso = if (totales > 0) (ocupados.toFloat() / totales.toFloat()).coerceIn(0f, 1f) else 0f

                    val estaAgotado = asientosLibres == 0
                    val colorAsientos = if (estaAgotado) Color.DarkGray else PrimaryRedHex
                    val textoAsientos = if (estaAgotado) {
                        "0 libres / $totales totales (Agotado)"
                    } else {
                        "$asientosLibres libres / $totales totales"
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Disponibilidad de asientos:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = textoAsientos,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorAsientos
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progreso },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = colorAsientos,
                            trackColor = colorAsientos.copy(alpha = 0.15f),
                        )
                    }
                }
            }

            // C. Sección de Botones Inferiores (33% de ancho cada uno)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Botón Editar
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = PrimaryRedHex,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Editar",
                        color = PrimaryRedHex,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 2. Botón Cambiar Estado (Dropdown)
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { estadoMenuExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Estado",
                            color = Color.DarkGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    DropdownMenu(
                        expanded = estadoMenuExpanded,
                        onDismissRequest = { estadoMenuExpanded = false }
                    ) {
                        val estados = listOf("EN VENTA", "AGOTADO", "EN RUTA", "FINALIZADO")
                        estados.forEach { est ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = when (est) {
                                            "EN VENTA" -> "🟢 En Venta"
                                            "AGOTADO" -> "🔴 Agotado"
                                            "EN RUTA" -> "🚌 En Ruta"
                                            "FINALIZADO" -> "⚫ Finalizado"
                                            else -> est
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = if (viaje.estado.equals(est, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onEstadoChange(est)
                                    estadoMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // 3. Botón Eliminar
                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFFFEBEE)),
                    border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar Viaje",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Eliminar",
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearEditarRutaDialog(
    viajeInicial: Viaje?,
    vehiculosDisponibles: List<Vehiculo>,
    conductoresDisponibles: List<UsuarioConductor>,
    onDismiss: () -> Unit,
    onSaveNew: (viaje: Viaje, capacidad: Int) -> Unit,
    onUpdate: (viaje: Viaje) -> Unit,
) {
    val context = LocalContext.current
    val agencias = listOf("Cajamarca", "Celendín", "Balsas", "Leymebamba", "Chachapoyas")

    var origen by remember { mutableStateOf(viajeInicial?.origen ?: "Cajamarca") }
    var destino by remember { mutableStateOf(viajeInicial?.destino ?: "Celendín") }
    var precioText by remember { mutableStateOf(viajeInicial?.precio?.toString() ?: "25.0") }

    var fecha by remember { mutableStateOf(viajeInicial?.fecha ?: "") }
    var horaSalida by remember { mutableStateOf(viajeInicial?.horaSalida ?: "") }

    var origenExpanded by remember { mutableStateOf(false) }
    var destinoExpanded by remember { mutableStateOf(false) }

    var vehiculoExpanded by remember { mutableStateOf(false) }
    var vehiculoSeleccionado by remember {
        mutableStateOf<Vehiculo?>(
            vehiculosDisponibles.find { it.id == viajeInicial?.idVehiculo }
                ?: vehiculosDisponibles.firstOrNull()
        )
    }

    var conductorExpanded by remember { mutableStateOf(false) }
    var conductorSeleccionado by remember {
        mutableStateOf<UsuarioConductor?>(
            conductoresDisponibles.find {
                (viajeInicial?.idConductor.orEmpty().isNotBlank() && it.uid == viajeInicial?.idConductor) ||
                it.nombreCompleto == viajeInicial?.conductor
            } ?: conductoresDisponibles.firstOrNull()
        )
    }

    // DatePicker nativo corregido con zona horaria UTC
    val calendar = Calendar.getInstance()
    val todayUtcMillis = remember {
        LocalDate.now()
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()
    }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                fecha = "%02d/%02d/%d".format(dayOfMonth, month + 1, year)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = todayUtcMillis
        }
    }

    // TimePicker nativo
    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val amPm = if (hourOfDay >= 12) "p. m." else "a. m."
                val formattedHour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                horaSalida = "%02d:%02d %s".format(formattedHour, minute, amPm)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
    }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PrimaryRedHex,
        focusedLabelColor = PrimaryRedHex
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (viajeInicial == null) "Crear Nueva Ruta" else "Editar Ruta/Viaje",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Dropdown Origen
                ExposedDropdownMenuBox(
                    expanded = origenExpanded,
                    onExpandedChange = { origenExpanded = !origenExpanded }
                ) {
                    OutlinedTextField(
                        value = origen,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Origen") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = PrimaryRedHex
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = origenExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        colors = tfColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    )

                    ExposedDropdownMenu(
                        expanded = origenExpanded,
                        onDismissRequest = { origenExpanded = false }
                    ) {
                        agencias.forEach { ag ->
                            DropdownMenuItem(
                                text = { Text(ag) },
                                onClick = {
                                    origen = ag
                                    origenExpanded = false
                                }
                            )
                        }
                    }
                }

                // Dropdown Destino
                ExposedDropdownMenuBox(
                    expanded = destinoExpanded,
                    onExpandedChange = { destinoExpanded = !destinoExpanded }
                ) {
                    OutlinedTextField(
                        value = destino,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Destino") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = PrimaryRedHex
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destinoExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        colors = tfColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    )

                    ExposedDropdownMenu(
                        expanded = destinoExpanded,
                        onDismissRequest = { destinoExpanded = false }
                    ) {
                        agencias.forEach { ag ->
                            DropdownMenuItem(
                                text = { Text(ag) },
                                onClick = {
                                    destino = ag
                                    destinoExpanded = false
                                }
                            )
                        }
                    }
                }

                // Dropdown de Asignación de Flota
                ExposedDropdownMenuBox(
                    expanded = vehiculoExpanded,
                    onExpandedChange = { vehiculoExpanded = !vehiculoExpanded }
                ) {
                    val displayText = vehiculoSeleccionado?.let {
                        "${it.modelo} - ${it.placa} (Cap: ${it.capacidad})"
                    } ?: viajeInicial?.vehiculoDescripcion ?: "Seleccionar vehículo"

                    OutlinedTextField(
                        value = displayText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Asignar Vehículo / Flota") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DirectionsBus,
                                contentDescription = null,
                                tint = PrimaryRedHex
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehiculoExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        colors = tfColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    )

                    ExposedDropdownMenu(
                        expanded = vehiculoExpanded,
                        onDismissRequest = { vehiculoExpanded = false }
                    ) {
                        vehiculosDisponibles.forEach { v ->
                            DropdownMenuItem(
                                text = { Text("${v.marca} ${v.modelo} - ${v.placa} (Cap: ${v.capacidad})") },
                                onClick = {
                                    vehiculoSeleccionado = v
                                    vehiculoExpanded = false
                                }
                            )
                        }
                    }
                }

                // Fila para Fecha y Hora (Mitad y Mitad)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Selector Nativo de Fecha
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha Salida") },
                        placeholder = { Text("dd/MM/yyyy") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Elegir Fecha",
                                tint = PrimaryRedHex,
                                modifier = Modifier.clickable { datePickerDialog.show() }
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = tfColors,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { datePickerDialog.show() }
                    )

                    // Selector Nativo de Hora
                    OutlinedTextField(
                        value = horaSalida,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hora Salida") },
                        placeholder = { Text("08:00 a.m.") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Elegir Hora",
                                tint = PrimaryRedHex,
                                modifier = Modifier.clickable { timePickerDialog.show() }
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = tfColors,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { timePickerDialog.show() }
                    )
                }

                // Dropdown de Asignación de Conductor
                ExposedDropdownMenuBox(
                    expanded = conductorExpanded,
                    onExpandedChange = { conductorExpanded = !conductorExpanded }
                ) {
                    val displayTextConductor = conductorSeleccionado?.nombreCompleto
                        ?: viajeInicial?.conductor?.ifBlank { null }
                        ?: "Seleccionar conductor"

                    OutlinedTextField(
                        value = displayTextConductor,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Conductor Asignado") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = PrimaryRedHex
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = conductorExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        colors = tfColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    )

                    ExposedDropdownMenu(
                        expanded = conductorExpanded,
                        onDismissRequest = { conductorExpanded = false }
                    ) {
                        if (conductoresDisponibles.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No hay conductores disponibles") },
                                onClick = { conductorExpanded = false }
                            )
                        } else {
                            conductoresDisponibles.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.nombreCompleto) },
                                    onClick = {
                                        conductorSeleccionado = c
                                        conductorExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Precio Pasaje
                OutlinedTextField(
                    value = precioText,
                    onValueChange = { precioText = it },
                    label = { Text("Precio Pasaje") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = PrimaryRedHex
                        )
                    },
                    prefix = {
                        Text("S/ ", fontWeight = FontWeight.Bold, color = PrimaryRedHex)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = tfColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = Color.Gray)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("es-ES"))
                        val fechaSeleccionada = try {
                            LocalDate.parse(fecha.trim(), formatter)
                        } catch (_: Exception) {
                            null
                        }
                        val hoy = LocalDate.now()
                        if (fechaSeleccionada == null || fechaSeleccionada.isBefore(hoy)) {
                            Toast.makeText(context, "No se pueden programar salidas para fechas pasadas", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val precio = precioText.toDoubleOrNull() ?: 20.0
                        val vehiculo = vehiculoSeleccionado
                        val conductorNombre = conductorSeleccionado?.nombreCompleto ?: viajeInicial?.conductor ?: ""
                        val conductorUid = conductorSeleccionado?.uid ?: viajeInicial?.idConductor ?: ""

                        val cap = vehiculoSeleccionado?.capacidad ?: 15

                        if (viajeInicial == null) {
                            val viajeNuevo = Viaje(
                                origen = origen,
                                destino = destino,
                                idVehiculo = vehiculo?.id ?: "",
                                vehiculoDescripcion = vehiculo?.let { "${it.modelo} - ${it.placa}" } ?: "Combi Expreso",
                                idConductor = conductorUid,
                                conductor = conductorNombre,
                                fecha = fecha.ifBlank { "20/05/2025" },
                                horaSalida = horaSalida.ifBlank { "08:00 a. m." },
                                precio = precio,
                                asientosTotales = cap,
                                asientosOcupados = 0,
                                asientosDisponibles = cap,
                                estado = "EN VENTA",
                            )
                            onSaveNew(viajeNuevo, cap)
                        } else {
                            val capEditado = vehiculoSeleccionado?.capacidad ?: viajeInicial.asientosTotales
                            val ocupados = viajeInicial.asientosOcupados
                            val disponibles = (capEditado - ocupados).coerceAtLeast(0)

                            val viajeEditado = viajeInicial.copy(
                                origen = origen,
                                destino = destino,
                                idVehiculo = vehiculo?.id ?: viajeInicial.idVehiculo,
                                vehiculoDescripcion = vehiculo?.let { "${it.modelo} - ${it.placa}" } ?: viajeInicial.vehiculoDescripcion,
                                idConductor = if (conductorUid.isNotBlank()) conductorUid else viajeInicial.idConductor,
                                conductor = if (conductorNombre.isNotBlank()) conductorNombre else viajeInicial.conductor,
                                fecha = fecha.ifBlank { viajeInicial.fecha },
                                horaSalida = horaSalida.ifBlank { viajeInicial.horaSalida },
                                precio = precio,
                                asientosTotales = capEditado,
                                asientosOcupados = ocupados,
                                asientosDisponibles = disponibles,
                            )
                            onUpdate(viajeEditado)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRedHex),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (viajeInicial == null) "Guardar y Generar" else "Guardar Cambios",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {}
    )
}
