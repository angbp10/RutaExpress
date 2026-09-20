package com.example.rutaexpress.ui.recepcionista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rutaexpress.data.model.Vehiculo
import com.example.rutaexpress.viewmodel.VehiculoViewModel

private val PrimaryRedHex = Color(0xFFF72907)
private val BackgroundSurfaceHex = Color(0xFFFCF9F8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiculosCrudScreen(
    viewModel: VehiculoViewModel = viewModel(),
    onBackClick: () -> Unit = {},
) {
    val vehiculos by viewModel.vehiculosState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDialog by remember { mutableStateOf(false) }
    var selectedVehiculoForEdit by remember { mutableStateOf<Vehiculo?>(null) }

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
        containerColor = BackgroundSurfaceHex,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "GESTIÓN DE FLOTA (VEHÍCULOS)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRedHex,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFF1C1B1B),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundSurfaceHex,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedVehiculoForEdit = null
                    showDialog = true
                },
                containerColor = PrimaryRedHex,
                contentColor = Color.White,
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nuevo Vehículo")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (uiState.isLoading && vehiculos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryRedHex)
                }
            } else if (vehiculos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay vehículos registrados en la flota.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(vehiculos, key = { it.id }) { vehiculo ->
                        VehiculoCardItem(
                            vehiculo = vehiculo,
                            onEdit = {
                                selectedVehiculoForEdit = vehiculo
                                showDialog = true
                            },
                            onDelete = {
                                viewModel.eliminarVehiculo(vehiculo.id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        VehiculoFormDialog(
            vehiculoInicial = selectedVehiculoForEdit,
            onDismiss = { showDialog = false },
            onSave = { placa, marca, modelo, anio, capacidad, estado ->
                if (selectedVehiculoForEdit == null) {
                    val nuevo = Vehiculo(
                        placa = placa,
                        marca = marca,
                        modelo = modelo,
                        anio = anio,
                        capacidad = capacidad,
                        estado = estado,
                    )
                    viewModel.registrarVehiculo(nuevo)
                } else {
                    val editado = selectedVehiculoForEdit!!.copy(
                        placa = placa,
                        marca = marca,
                        modelo = modelo,
                        anio = anio,
                        capacidad = capacidad,
                        estado = estado,
                    )
                    viewModel.actualizarVehiculo(editado.id, editado)
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun VehiculoCardItem(
    vehiculo: Vehiculo,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = null,
                        tint = PrimaryRedHex,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = vehiculo.placa,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1B)
                    )
                }

                // Chip de Estado
                val (chipColor, chipText) = when (vehiculo.estado.lowercase()) {
                    "activo" -> Color(0xFF2E7D32) to "Activo"
                    "mantenimiento" -> Color(0xFFE65100) to "Mantenimiento"
                    else -> Color(0xFFC62828) to "Inactivo"
                }

                Surface(
                    color = chipColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = chipText,
                        color = chipColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${vehiculo.marca} ${vehiculo.modelo} (${vehiculo.anio})",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EventSeat,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = "Capacidad: ${vehiculo.capacidad} asientos",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
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
                        contentDescription = "Eliminar",
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
fun VehiculoFormDialog(
    vehiculoInicial: Vehiculo?,
    onDismiss: () -> Unit,
    onSave: (
        placa: String,
        marca: String,
        modelo: String,
        anio: Int,
        capacidad: Int,
        estado: String,
    ) -> Unit,
) {
    var placa by remember { mutableStateOf(vehiculoInicial?.placa ?: "") }
    var marca by remember { mutableStateOf(vehiculoInicial?.marca ?: "Toyota") }
    var modelo by remember { mutableStateOf(vehiculoInicial?.modelo ?: "HiAce") }
    var anioText by remember { mutableStateOf(vehiculoInicial?.anio?.toString() ?: "2023") }
    var capacidadText by remember { mutableStateOf(vehiculoInicial?.capacidad?.toString() ?: "15") }

    val estados = listOf("activo", "mantenimiento", "inactivo")
    var estadoExpanded by remember { mutableStateOf(false) }
    var selectedEstado by remember { mutableStateOf(vehiculoInicial?.estado ?: "activo") }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PrimaryRedHex,
        focusedLabelColor = PrimaryRedHex
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (vehiculoInicial == null) "Nuevo Vehículo" else "Editar Vehículo",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = placa,
                    onValueChange = { placa = it.uppercase() },
                    label = { Text("Placa (Ej. M4D-960)") },
                    colors = tfColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = marca,
                    onValueChange = { marca = it },
                    label = { Text("Marca") },
                    colors = tfColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = modelo,
                    onValueChange = { modelo = it },
                    label = { Text("Modelo") },
                    colors = tfColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = anioText,
                    onValueChange = { if (it.length <= 4) anioText = it },
                    label = { Text("Año de fabricación") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = tfColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = capacidadText,
                    onValueChange = { capacidadText = it },
                    label = { Text("Capacidad (N° de Asientos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = tfColors,
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de Estado con ExposedDropdownMenuBox
                ExposedDropdownMenuBox(
                    expanded = estadoExpanded,
                    onExpandedChange = { estadoExpanded = !estadoExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedEstado.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado del Vehículo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoExpanded) },
                        colors = tfColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = estadoExpanded,
                        onDismissRequest = { estadoExpanded = false }
                    ) {
                        estados.forEach { est ->
                            DropdownMenuItem(
                                text = { Text(est.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedEstado = est
                                    estadoExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val anioInt = anioText.toIntOrNull() ?: 2023
                    val capInt = capacidadText.toIntOrNull() ?: 15
                    onSave(placa, marca, modelo, anioInt, capInt, selectedEstado)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRedHex)
            ) {
                Text("Guardar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
