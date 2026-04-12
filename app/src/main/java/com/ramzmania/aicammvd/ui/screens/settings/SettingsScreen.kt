package com.ramzmania.aicammvd.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzmania.aicammvd.R
import com.ramzmania.aicammvd.utils.Constants
import com.ramzmania.aicammvd.utils.PreferencesUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // State for Settings
    var alertType by remember {
        mutableStateOf(PreferencesUtil.getString(context, Constants.PREF_ALERT_TYPE) ?: "sound")
    }
    var postPassNotify by remember {
        mutableStateOf(PreferencesUtil.getBoolean(context, Constants.PREF_POST_PASS_NOTIFY, true))
    }

    val distanceOptions = listOf("250", "500", "750", "1000", "1500", "2000")
    val savedDistances = remember {
        PreferencesUtil.getString(context, Constants.PREF_ALERT_DISTANCES)
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.toMutableStateList()
                ?: mutableStateListOf("500")
    }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Settings", color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                )
                            }
                        },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = colorResource(id = R.color.brown_black)
                                )
                )
            }
    ) { padding ->
        LazyColumn(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(padding)
                                .background(colorResource(id = R.color.brown_black))
                                .padding(16.dp)
        ) {
            item {
                Text(
                        "Alert Options",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                            selected = alertType == "sound",
                            onClick = {
                                alertType = "sound"
                                PreferencesUtil.setString(
                                        context,
                                        "sound",
                                        Constants.PREF_ALERT_TYPE
                                )
                            }
                    )
                    Text("Standard Alert Sound", color = Color.White)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                            selected = alertType == "voice",
                            onClick = {
                                alertType = "voice"
                                PreferencesUtil.setString(
                                        context,
                                        "voice",
                                        Constants.PREF_ALERT_TYPE
                                )
                            }
                    )
                    Text("Voice Guidance (AI Camera Ahead)", color = Color.White)
                }

                HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
            }

            item {
                Text(
                        "Alert Distances",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                )
                Text("Choose when to be notified", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                distanceOptions.forEach { distance ->
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                                checked = savedDistances.contains(distance),
                                onCheckedChange = { checked ->
                                    if (checked) savedDistances.add(distance)
                                    else savedDistances.remove(distance)
                                    PreferencesUtil.setString(
                                            context,
                                            savedDistances.joinToString(","),
                                            Constants.PREF_ALERT_DISTANCES
                                    )
                                }
                        )
                        Text("$distance meters", color = Color.White)
                    }
                }

                HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
            }

            item {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                                "Post-Pass Notification",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                        )
                        Text(
                                "Notify speed after passing camera",
                                color = Color.Gray,
                                fontSize = 14.sp
                        )
                    }
                    Switch(
                            checked = postPassNotify,
                            onCheckedChange = {
                                postPassNotify = it
                                PreferencesUtil.setBoolean(
                                        context,
                                        Constants.PREF_POST_PASS_NOTIFY,
                                        it
                                )
                            }
                    )
                }
            }
        }
    }
}
