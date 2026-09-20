package com.example.rutaexpress.ui.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val viajes by viajeViewModel.viajesState.collectAsState()
    val vehiculos by vehiculoViewModel.vehiculosState.collectAsState()
    val uiState by viajeViewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var showDialog by remember { mutableStateOf(false) }
    var selectedViajeForEdit by remember { mutableStateOf<Viaje?>(null) }

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

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isLoading && viajes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryRedHex)
                    }
                } else if (viajes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay viajes programados actualmente.")
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(viajes, key = { it.id }) { viaje ->
                            ViajeCardItem(
                                viaje = viaje,
                                onEdit = {
                                    selectedViajeForEdit = viaje
                                    showDialog = true
                                },
                                onDelete = { viajeViewModel.eliminarViaje(viaje.id) }
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
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${viaje.origen} ➔ ${viaje.destino}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryRedHex
                )
                Text(
                    text = "S/ ${"%.2f".format(viaje.precio)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1B)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${viaje.fecha} a las ${viaje.horaSalida}",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Vehículo: ${viaje.vehiculoDescripcion} | Conductor: ${viaje.conductor}",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar Viaje",
                        tint = PrimaryRedHex
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar", color = PrimaryRedHex)
                }

                Button(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar Viaje",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar", color = Color.White)
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
    onDismiss: () -> Unit,
    onSaveNew: (viaje: Viaje, capacidad: Int) -> Unit,
    onUpdate: (viaje: Viaje) -> Unit,
) {
    val context = LocalContext.current

    var origen by remember { mutableStateOf(viajeInicial?.origen ?: "Cajamarca") }
    var destino by remember { mutableStateOf(viajeInicial?.destino ?: "Celendín") }
    var conductor by remember { mutableStateOf(viajeInicial?.conductor ?: "Juan Pérez") }
    var precioText by remember { mutableStateOf(viajeInicial?.precio?.toString() ?: "25.0") }

    var fecha by remember { mutableStateOf(viajeInicial?.fecha ?: "") }
    var horaSalida by remember { mutableStateOf(viajeInicial?.horaSalida ?: "") }

    var vehiculoExpanded by remember { mutableStateOf(false) }
    var vehiculoSeleccionado by remember {
        mutableStateOf<Vehiculo?>(
            vehiculosDisponibles.find { it.id == viajeInicial?.idVehiculo }
                ?: vehiculosDisponibles.firstOrNull()
        )
    }

    // DatePicker nativo
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                fecha = "%02d/%02d/%d".format(dayOfMonth, month + 1, year)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
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
                OutlinedTextField(
                    value = origen,
                    onValueChange = { origen = it },
                    label = { Text("Origen") },
                    colors = tfColors,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = destino,
                    onValueChange = { destino = it },
                    label = { Text("Destino") },
                    colors = tfColors,
                    modifier = Modifier.fillMaxWidth()
                )

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
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehiculoExpanded) },
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

                // Selector Nativo de Fecha
                OutlinedTextField(
                    value = fecha,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha de Salida (dd/MM/yyyy)") },
                    placeholder = { Text("Toca para seleccionar fecha") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Elegir Fecha",
                            modifier = Modifier.clickable { datePickerDialog.show() }
                        )
                    },
                    colors = tfColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                )

                // Selector Nativo de Hora
                OutlinedTextField(
                    value = horaSalida,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Hora de Salida") },
                    placeholder = { Text("Toca para seleccionar hora") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Elegir Hora",
                            modifier = Modifier.clickable { timePickerDialog.show() }
                        )
                    },
                    colors = tfColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { timePickerDialog.show() }
                )

                OutlinedTextField(
                    value = conductor,
                    onValueChange = { conductor = it },
                    label = { Text("Conductor Asignado") },
                    colors = tfColors,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = precioText,
                    onValueChange = { precioText = it },
                    label = { Text("Precio del Pasaje (S/)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = tfColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val precio = precioText.toDoubleOrNull() ?: 20.0
                    val vehiculo = vehiculoSeleccionado

                    if (viajeInicial == null) {
                        val viajeNuevo = Viaje(
                            origen = origen,
                            destino = destino,
                            idVehiculo = vehiculo?.id ?: "",
                            vehiculoDescripcion = vehiculo?.let { "${it.modelo} - ${it.placa}" } ?: "Combi Expreso",
                            conductor = conductor,
                            fecha = fecha.ifBlank { "20/05/2025" },
                            horaSalida = horaSalida.ifBlank { "08:00 a. m." },
                            precio = precio,
                        )
                        onSaveNew(viajeNuevo, vehiculo?.capacidad ?: 15)
                    } else {
                        val viajeEditado = viajeInicial.copy(
                            origen = origen,
                            destino = destino,
                            idVehiculo = vehiculo?.id ?: viajeInicial.idVehiculo,
                            vehiculoDescripcion = vehiculo?.let { "${it.modelo} - ${it.placa}" } ?: viajeInicial.vehiculoDescripcion,
                            conductor = conductor,
                            fecha = fecha.ifBlank { viajeInicial.fecha },
                            horaSalida = horaSalida.ifBlank { viajeInicial.horaSalida },
                            precio = precio,
                        )
                        onUpdate(viajeEditado)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRedHex)
            ) {
                Text(
                    text = if (viajeInicial == null) "Guardar y Generar Asientos" else "Guardar Cambios",
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
