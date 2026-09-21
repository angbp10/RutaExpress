package com.example.rutaexpress.ui.recepcionista

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rutaexpress.data.model.Encomienda
import com.example.rutaexpress.viewmodel.EncomiendaViewModel
import java.util.Locale

private val PrimaryRedHex = Color(0xFFF72907)
private val BackgroundSurfaceHex = Color(0xFFFCF9F8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionEstadosScreen(
    viewModel: EncomiendaViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val encomiendas by viewModel.encomiendasState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
                        text = "SEGUIMIENTO DE ENCOMIENDAS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRedHex
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundSurfaceHex
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Buscador Dinámico
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Buscar por Guía o DNI...") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                trailingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryRedHex)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryRedHex,
                    focusedLabelColor = PrimaryRedHex
                ),
                singleLine = true
            )

            if (uiState.isLoading && encomiendas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryRedHex)
                }
            } else if (encomiendas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (uiState.searchQuery.isEmpty()) "No hay encomiendas registradas." else "No se encontraron coincidencias.",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(encomiendas, key = { it.id }) { encomienda ->
                        EncomiendaSeguimientoCard(
                            encomienda = encomienda,
                            onUpdateState = { nuevoEstado ->
                                viewModel.actualizarEstado(encomienda.id, nuevoEstado)
                            },
                            isLoading = uiState.isLoading
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncomiendaSeguimientoCard(
    encomienda: Encomienda,
    onUpdateState: (String) -> Unit,
    isLoading: Boolean
) {
    var selectedEstado by remember(encomienda.estado) { mutableStateOf(encomienda.estado) }
    var expanded by remember { mutableStateOf(false) }
    val estadosValidos = listOf("EN_ALMACEN", "EN_TRANSITO", "DISPONIBLE", "ENTREGADO")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Guía: ${encomienda.guia}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PrimaryRedHex
                )
                
                // Chip de estado actual
                Surface(
                    color = PrimaryRedHex.copy(alpha = 0.1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = encomienda.estado,
                        color = PrimaryRedHex,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            
            DetailRow("Remitente:", "${encomienda.remitente} (${encomienda.dniRemitente})")
            DetailRow("Destinatario:", "${encomienda.destinatario} (${encomienda.dniDestinatario})")
            DetailRow("Peso:", "${encomienda.pesoKg} kg | Tarifa: S/. ${String.format(Locale.US, "%.2f", encomienda.tarifaTotal)}")
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedEstado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Nuevo Estado", fontSize = 12.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRedHex,
                            focusedLabelColor = PrimaryRedHex
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        estadosValidos.forEach { est ->
                            DropdownMenuItem(
                                text = { Text(est) },
                                onClick = {
                                    selectedEstado = est
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = { onUpdateState(selectedEstado) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRedHex),
                    enabled = !isLoading && selectedEstado != encomienda.estado,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Update, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("OK", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontWeight = FontWeight.SemiBold, color = Color.Gray, fontSize = 12.sp)
        Text(text = value, fontWeight = FontWeight.Medium, color = Color.Black, fontSize = 14.sp)
    }
}
