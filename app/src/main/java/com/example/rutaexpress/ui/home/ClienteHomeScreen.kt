package com.example.rutaexpress.ui.home

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rutaexpress.data.model.RutaHorario
import com.example.rutaexpress.viewmodel.RutaHorarioViewModel

private val PrimaryRedHex = Color(0xFFF72907)
private val BackgroundSurfaceHex = Color(0xFFFCF9F8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteHomeScreen(
    viewModel: RutaHorarioViewModel = viewModel(),
    onLogoutClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    var origen by rememberSaveable { mutableStateOf("") }
    var destino by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf("") }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PrimaryRedHex,
        focusedLabelColor = PrimaryRedHex,
        cursorColor = PrimaryRedHex,
        focusedLeadingIconColor = PrimaryRedHex
    )

    Scaffold(
        containerColor = BackgroundSurfaceHex,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "TURISMO AZAÑERO",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRedHex,
                    )
                },
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = PrimaryRedHex,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundSurfaceHex,
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            // Tarjeta de Búsqueda
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Buscar Pasajes de Viaje",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1B)
                    )

                    // Campo Origen
                    OutlinedTextField(
                        value = origen,
                        onValueChange = { origen = it },
                        label = { Text("Origen") },
                        placeholder = { Text("Ej. Cajamarca") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                        },
                        singleLine = true,
                        colors = tfColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Campo Destino
                    OutlinedTextField(
                        value = destino,
                        onValueChange = { destino = it },
                        label = { Text("Destino") },
                        placeholder = { Text("Ej. Celendín / Chachapoyas") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                        },
                        singleLine = true,
                        colors = tfColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Campo Fecha
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { fecha = it },
                        label = { Text("Fecha de Viaje") },
                        placeholder = { Text("Ej. 20/05/2025") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null)
                        },
                        singleLine = true,
                        colors = tfColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Botón Principal "Buscar pasajes"
                    Button(
                        onClick = {
                            viewModel.searchPasajes(origen, destino, fecha)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRedHex)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Buscar pasajes",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (uiState.hasSearched) "Resultados de la búsqueda" else "Rutas y Horarios Disponibles",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = PrimaryRedHex)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Buscando salidas disponibles...")
                }
            } else if (uiState.rutas.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No se encontraron rutas con los filtros seleccionados.",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.rutas, key = { it.id }) { ruta ->
                        RutaCardCliente(ruta = ruta)
                    }
                }
            }
        }
    }
}

@Composable
fun RutaCardCliente(ruta: RutaHorario) {
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
                    text = "${ruta.origen} ➔ ${ruta.destino}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryRedHex
                )
                Text(
                    text = "S/ ${"%.2f".format(ruta.costoPasaje)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1B)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = ruta.flotaAsignada, fontSize = 13.sp, color = Color.DarkGray)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${ruta.fecha} ${ruta.horaSalida}",
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EventSeat,
                    contentDescription = null,
                    tint = PrimaryRedHex,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Asientos disponibles: ${ruta.asientosDisponibles} de ${ruta.asientosTotales}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryRedHex
                )
            }
        }
    }
}
