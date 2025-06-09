package com.example.inventoryapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportsScreen() {
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    // Filters
    var searchQuery by remember { mutableStateOf("") }
    var typeFilter by remember { mutableStateOf("All") }
    val types = listOf("All", "Purchase", "Sale")

    // Date filter
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }

    var transactions by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        runCatching {
            db.collection("transactions").orderBy("timestamp").get().await()
        }.onSuccess { snapshot ->
            val list = snapshot.documents.mapNotNull { it.toObject(TransactionRecord::class.java) }
            transactions = list.reversed()
            errorMsg = null
        }.onFailure {
            errorMsg = "Failed to load transactions:\n${it.message}"
        }
        loading = false
    }

    Column(Modifier.fillMaxSize().padding(8.dp)) {
        // --- FILTERS ---
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search serial/item") },
                modifier = Modifier.weight(1f)
            )
            DropdownMenuWrapper(
                options = types,
                selected = typeFilter,
                onSelect = { typeFilter = it },
                label = "Type"
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = fromDate,
                onValueChange = { fromDate = it },
                label = { Text("From (yyyy-MM-dd)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = toDate,
                onValueChange = { toDate = it },
                label = { Text("To (yyyy-MM-dd)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))

        if (loading) {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
        } else if (errorMsg != null) {
            Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
        } else {
            // Apply filters
            val filtered = transactions.filter { tx ->
                (typeFilter == "All" || tx.transactionType == typeFilter) &&
                (searchQuery.isBlank() ||
                    tx.serialNumber.contains(searchQuery, true) ||
                    tx.itemName.contains(searchQuery, true)
                ) &&
                runCatching {
                    val t = Date(tx.timestamp)
                    val fd = if (fromDate.isBlank()) null else dateFormat.parse(fromDate)
                    val td = if (toDate.isBlank()) null else dateFormat.parse(toDate)
                    (fd == null || t >= fd) && (td == null || t <= td)
                }.getOrDefault(true)
            }

            LazyColumn {
                items(filtered) { tx ->
                    TransactionCard(tx)
                }
            }
        }
    }
}

@Composable
fun DropdownMenuWrapper(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier
                .clickable { expanded = true }
                .width(120.dp)
        )
        DropdownMenu(expanded, { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        onSelect(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}
