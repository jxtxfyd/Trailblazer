package com.example.cs446_ece452_android_app.ui.components


import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import com.example.cs446_ece452_android_app.ui.theme.Blue2
import com.example.cs446_ece452_android_app.ui.theme.Blue3
import com.example.cs446_ece452_android_app.ui.theme.DarkBlue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PasswordInputBox(labelVal: String, textboxVal : String = "", valueChanged: (String) -> Unit = {}) {
    var password by remember {
        mutableStateOf(textboxVal)
    }
    var isShowPassword by remember {
        mutableStateOf(false)
    }
    OutlinedTextField(
        value = password,
        onValueChange = {
            password = it
            valueChanged(password)
        },
        textStyle = TextStyle(color = DarkBlue),
        placeholder = { Text(labelVal, color = DarkBlue) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Blue3,
            unfocusedContainerColor = Blue2,
            unfocusedLeadingIconColor = DarkBlue,
            focusedLeadingIconColor = DarkBlue
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "profile"
            )
        },
        trailingIcon = {
            val iconImage =
                if (isShowPassword) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
            val description = if (isShowPassword) "Show Password" else "Hide Password"
            IconButton(onClick = {
                isShowPassword = !isShowPassword
            }) {
                Icon(imageVector = iconImage, contentDescription = description, tint = DarkBlue)
            }
        },

        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (isShowPassword) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true
    )
}

@Preview
@Composable
fun PasswordOutlineBoxPreview() {
    PasswordInputBox(labelVal = "Password")
}

