package com.example.rutaexpress.ui.recepcionista

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rutaexpress.viewmodel.EncomiendaViewModel
import java.util.Locale

private val PrimaryRedHex = Color(0xFFF72907)
private val BackgroundSurfaceHex = Color(0xFFFCF9F8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncomiendaScreen(
    viewModel: EncomiendaViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Remitente
    var dniRemitente by remember { mutableStateOf("") }
    var remitente by remember { mutableStateOf("") }
    var celularRemitente by remember { mutableStateOf("") }

    // Destinatario
    var dniDestinatario by remember { mutableStateOf("") }
    var destinatario by remember { mutableStateOf("") }
    var celularDestinatario by remember { mutableStateOf("") }

    // Paquete
    var pesoText by remember { mutableStateOf("") }
    var tipoCarga by remember { mutableStateOf("Estándar") }
    var expanded by remember { mutableStateOf(false) }
    val tipos = listOf("Estándar", "Frágil", "Express")

    val peso = pesoText.toDoubleOrNull() ?: 0.0
    val tarifaTotal = viewModel.calcularTarifa(peso, tipoCarga)

    // Lógica para autocompletar DNI Remitente
    LaunchedEffect(dniRemitente) {
        if (dniRemitente.length == 8) {
            viewModel.buscarDni(dniRemitente, true) { nombre ->
                remitente = nombre
            }
        }
    }

    // Lógica para autocompletar DNI Destinatario
    LaunchedEffect(dniDestinatario) {
        if (dniDestinatario.length == 8) {
            viewModel.buscarDni(dniDestinatario, false) { nombre ->
                destinatario = nombre
            }
        }
    }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
            // Limpiar formulario al éxito
            dniRemitente = ""
            remitente = ""
            celularRemitente = ""
            dniDestinatario = ""
            destinatario = ""
            celularDestinatario = ""
            pesoText = ""
            tipoCarga = "Estándar"
        }
    }

    Scaffold(
        containerColor = BackgroundSurfaceHex,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "EMISIÓN DE ENCOMIENDAS",
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta Remitente
            EncomiendaCard(title = "Datos del Remitente") {
                OutlinedTextField(
                    value = dniRemitente,
                    onValueChange = { if (it.length <= 8) dniRemitente = it },
                    label = { Text("DNI Remitente") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = remitente,
                    onValueChange = { remitente = it },
                    label = { Text("Nombre Completo") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = celularRemitente,
                    onValueChange = { celularRemitente = it },
                    label = { Text("Celular") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
            }

            // Tarjeta Destinatario
            EncomiendaCard(title = "Datos del Destinatario") {
                OutlinedTextField(
                    value = dniDestinatario,
                    onValueChange = { if (it.length <= 8) dniDestinatario = it },
                    label = { Text("DNI Destinatario") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = destinatario,
                    onValueChange = { destinatario = it },
                    label = { Text("Nombre Completo") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = celularDestinatario,
                    onValueChange = { celularDestinatario = it },
                    label = { Text("Celular") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
            }

            // Tarjeta Detalles del Paquete
            EncomiendaCard(title = "Detalles del Paquete") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pesoText,
                        onValueChange = { pesoText = it },
                        label = { Text("Peso (kg)") },
                        leadingIcon = { Icon(Icons.Default.Scale, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = tipoCarga,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                            colors = textFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            tipos.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        tipoCarga = selectionOption
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Tarjeta Tarifa
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryRedHex.copy(alpha = 0.05f)),
                border = BoxShadow(Color.LightGray)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TARIFA TOTAL: S/. ${String.format(Locale.US, "%.2f", tarifaTotal)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryRedHex
                    )
                    Text(
                        text = "Cálculo basado en peso y tipo de carga",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.emitirGuia(
                        remitente = remitente,
                        dniRemitente = dniRemitente,
                        celularRemitente = celularRemitente,
                        destinatario = destinatario,
                        dniDestinatario = dniDestinatario,
                        celularDestinatario = celularDestinatario,
                        peso = peso,
                        tipoCarga = tipoCarga
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRedHex),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.LocalShipping, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EMITIR GUÍA", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EncomiendaCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.DarkGray
            )
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryRedHex,
    focusedLabelColor = PrimaryRedHex
)

@Composable
fun BoxShadow(color: Color) = androidx.compose.foundation.BorderStroke(1.dp, color)
