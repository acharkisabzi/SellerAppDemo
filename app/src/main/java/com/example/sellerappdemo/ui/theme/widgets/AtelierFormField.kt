package com.example.sellerappdemo.ui.theme.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sellerappdemo.ui.theme.*

@Composable
fun AtelierFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = ADAtOnSurfaceVar,
            letterSpacing = 0.4.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = ADAtOnSurface
            ),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = ADAtSurfaceLowest,
                unfocusedContainerColor = ADAtSurfaceLowest,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = ADAtOnSurface,
                unfocusedTextColor = ADAtOnSurface,
                cursorColor = ADAtSecondary,
                focusedLabelColor = ADAtSecondary,
                unfocusedLabelColor = ADAtOnSurfaceVar
            )
        )
    }
}