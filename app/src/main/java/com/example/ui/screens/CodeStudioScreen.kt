package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.CodeGenerationManager
import com.example.service.CodeValidationManager
import com.example.service.GeneratedCodeResult
import com.example.ui.theme.JanuBlack
import com.example.ui.theme.JanuCardBg
import com.example.ui.theme.JanuCardBorder
import com.example.ui.theme.JanuDarkGreen
import com.example.ui.theme.JanuNeonCyan
import com.example.ui.theme.JanuNeonGreen
import com.example.ui.theme.JanuTextMuted
import com.example.ui.theme.JanuTextPrimary
import com.example.ui.theme.JanuTextSecondary
import com.example.ui.theme.JanuWarningAmber

@Composable
fun CodeStudioScreen(
    onSaveToBackups: (String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val codeGen = remember { CodeGenerationManager() }
    val validator = remember { CodeValidationManager() }

    var selectedLang by remember { mutableStateOf("KOTLIN") }
    var userPrompt by remember { mutableStateOf("") }
    var activeCodeResult by remember {
        mutableStateOf<GeneratedCodeResult?>(codeGen.generateTemplate("CALCULATOR"))
    }
    var validationFeedback by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JanuBlack)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(JanuDarkGreen)
                    .border(1.dp, JanuNeonGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "Code Assistant",
                    tint = JanuNeonGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "JANU CODE STUDIO",
                    color = JanuNeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Personal Android & Multi-language Code Generator",
                    color = JanuTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Quick Preset Templates
        Text(
            text = "BUILT-IN TEMPLATES",
            color = JanuTextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Calculator App" to "CALCULATOR",
                "Torch Controller" to "FLASHLIGHT",
                "Room Database" to "ROOM_DB",
                "Python Script" to "PYTHON_SCRIPT",
                "Permissions XML" to "MANIFEST_PERMISSIONS"
            ).forEach { (label, key) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(JanuCardBg)
                        .border(1.dp, JanuCardBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    TextButton(
                        onClick = {
                            activeCodeResult = codeGen.generateTemplate(key)
                            validationFeedback = null
                        },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text(
                            text = label,
                            color = JanuNeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Custom Prompt Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(JanuCardBg)
                .border(1.dp, JanuCardBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userPrompt,
                onValueChange = { userPrompt = it },
                placeholder = {
                    Text(
                        text = "e.g. Janu, mo pain timer app bana...",
                        color = JanuTextMuted,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("code_prompt_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = JanuTextPrimary,
                    unfocusedTextColor = JanuTextPrimary,
                    cursorColor = JanuNeonGreen
                ),
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (userPrompt.isNotBlank()) {
                        activeCodeResult = codeGen.generateTemplate("CUSTOM", userPrompt)
                        validationFeedback = null
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(JanuDarkGreen)
                    .testTag("generate_code_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Generate",
                    tint = JanuNeonGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Code Display Card
        activeCodeResult?.let { res ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, JanuNeonGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070E0B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = res.title,
                                color = JanuNeonGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "File: ${res.targetFileName} (${res.language})",
                                color = JanuTextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Copy Button
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("JANU Code", res.codeSnippet))
                                    Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Code",
                                    tint = JanuNeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Save Backup Button
                            IconButton(
                                onClick = {
                                    onSaveToBackups(
                                        "v-code-${System.currentTimeMillis() % 1000}",
                                        res.title,
                                        res.targetFileName,
                                        res.codeSnippet
                                    )
                                    Toast.makeText(context, "Saved to Project Backups!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save Snapshot",
                                    tint = JanuWarningAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Code snippet view
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF040605))
                            .border(1.dp, JanuCardBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = res.codeSnippet,
                            color = Color(0xFFA7F3D0),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Instructions: ${res.buildInstructions}",
                        color = JanuTextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Syntax Validation Button
                    Button(
                        onClick = {
                            val vResult = validator.validateCode(res.codeSnippet, res.language)
                            validationFeedback = if (vResult.isValid) {
                                "Validation Passed: Code structure, braces, and package declarations are verified."
                            } else {
                                "Issues Found:\n" + vResult.issuesFound.joinToString("\n")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JanuCardBg,
                            contentColor = JanuNeonGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, JanuCardBorder, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Validate Code Syntax", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    validationFeedback?.let { feedback ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0B2117))
                                .border(1.dp, JanuNeonGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = feedback,
                                color = JanuNeonGreen,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
