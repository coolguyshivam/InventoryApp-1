package com.example.inventoryapp.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionScreen(navController: NavHostController, serialArg: String? = null) {
    var model by remember { mutableStateOf("") }
    var serial by remember { mutableStateOf(serialArg ?: "") }
    var phone by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = serial, onValueChange = { serial = it }, label = { Text("Serial") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = aadhaar, onValueChange = { aadhaar = it }, label = { Text("Aadhaar") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = date, onValueChange = {}, label = { Text("Date") }, readOnly = true,
            modifier = Modifier.fillMaxWidth().clickable {
                val cal = Calendar.getInstance()
                DatePickerDialog(context, { _, y, m, d ->
                    cal.set(y, m, d)
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            })
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val data = hashMapOf(
                "model" to model,
                "serial" to serial,
                "phone" to phone,
                "aadhaar" to aadhaar,
                "description" to description,
                "date" to date,
                "timestamp" to System.currentTimeMillis(),
                "type" to if (serialArg != null) "Sale" else "Purchase"
            )
            FirebaseFirestore.getInstance().collection("transactions").add(data)
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Submit Transaction")
        }
    }
}
