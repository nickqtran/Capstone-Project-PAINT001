package com.example.painlessprep.ui

import android.R.attr.bottom
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.painlessprep.ui.theme.PainlessPrepTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.painlessprep.ui.theme.Background
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.transition.Visibility

enum class AccountType {HOMEOWNER, BUSINESS}


/*What data the signup screen will collect*/
data class SignupForm(
    val name: String,
    val email: String,
    val address: String,
    val password: String,
    val businessName: String = "",
    val businessAddress: String = "",
    val dateOfBirth: String = ""
)

@Composable
fun SignupScreen(
    accountType: AccountType,
    onSignupClick: (SignupForm) -> Unit
) {
    var name by remember {mutableStateOf("")}
    var email by remember {mutableStateOf("")}
    var businessName by remember {mutableStateOf("")}
    var address by remember {mutableStateOf("")}
    var businessAddress by remember {mutableStateOf("")}
    var dateOfBirth by remember {mutableStateOf("")}
    var password by remember {mutableStateOf("")}
    var confirmPassword by remember {mutableStateOf("")}
    var passwordVisible by remember {mutableStateOf(false)}
    var confirmPasswordVisible by remember {mutableStateOf(false)}


    val isHomeowner = accountType == AccountType.HOMEOWNER
    val passwordsMatch = password == confirmPassword
    val showPasswordError = confirmPassword.isNotEmpty() && !passwordsMatch

    val canSubmit = name.isNotBlank() &&
            email.isNotBlank() &&
            address.isNotBlank() &&
            password.isNotBlank() &&
            passwordsMatch &&
            if (isHomeowner) {
                dateOfBirth.isNotBlank()
            } else {
                businessName.isNotBlank() && businessAddress.isNotBlank()
            }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){
        Text(
            text = "PAINLESS PREP",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 30.dp )
        )
        if(isHomeowner) {
            Text(
                text = "Create Homeowner Account",
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.secondary
            )
        } else{
            Text(
                text = "Create Business Account",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(Modifier.height(5.dp))

        if(isHomeowner) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Full Name", color = MaterialTheme.colorScheme.onBackground) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                shape = RoundedCornerShape(size = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = (10.dp))
            )
        } else{
            OutlinedTextField(
                value = businessName,
                onValueChange = {businessName = it},
                placeholder = { Text("Business Name", color = MaterialTheme.colorScheme.onBackground)},
                leadingIcon = {Icon(Icons.Default.Business, contentDescription = null)},
                shape = RoundedCornerShape(size = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = (10.dp))
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = {email = it},
            placeholder = { Text("Email Address", color = MaterialTheme.colorScheme.onBackground)},
            leadingIcon = {Icon(Icons.Default.Email, contentDescription = null)},
            shape = RoundedCornerShape(size = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = (10.dp))
        )
        if(isHomeowner) {
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                placeholder = { Text("Home Address", color = MaterialTheme.colorScheme.onBackground) },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                shape = RoundedCornerShape(size = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = (10.dp))
            )
        } else{
            OutlinedTextField(
                value = businessAddress,
                onValueChange = {businessAddress = it},
                placeholder = { Text("Business Address", color = MaterialTheme.colorScheme.onBackground)},
                leadingIcon = {Icon(Icons.Default.LocationOn, contentDescription = null)},
                shape = RoundedCornerShape(size = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = (10.dp))
            )
        }
        if(isHomeowner) {
            OutlinedTextField(
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it },
                placeholder = { Text("MM/DD/YYYY", color = MaterialTheme.colorScheme.onBackground) },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                shape = RoundedCornerShape(size = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = (10.dp))
            )
        }
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password", color = MaterialTheme.colorScheme.onBackground) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                VisibilityToggle(
                    visible = passwordVisible,
                    onToggle = {passwordVisible = !passwordVisible}
                )
            },
            shape = RoundedCornerShape(size = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground
            ),
            singleLine = true,
            visualTransformation = if (passwordVisible){
                VisualTransformation.None
            } else{
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().padding(bottom = (10.dp))
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = { Text("Confirm Password", color = MaterialTheme.colorScheme.onBackground) },
            leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
            trailingIcon = {
                VisibilityToggle(
                    visible = confirmPasswordVisible,
                    onToggle = {confirmPasswordVisible = !confirmPasswordVisible}
                )
            },
            shape = RoundedCornerShape(size = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground
            ),
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible){
                VisualTransformation.None
            } else{
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = showPasswordError,
            supportingText = {
                if (showPasswordError) Text("Passwords don't match")
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = (10.dp))
        )
        Button(
            onClick = {
                onSignupClick(
                    SignupForm(
                        name = name.trim(),
                        email = email.trim(),
                        address = address.trim(),
                        password = password,
                        businessAddress = if (isHomeowner) "" else businessAddress.trim(),
                        businessName = if (isHomeowner) "" else businessName.trim(),
                        dateOfBirth = if (isHomeowner) dateOfBirth.trim() else ""
                    )
                )
            },
            enabled = canSubmit,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContentColor = Color.LightGray,
                disabledContainerColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ){
            Text("Sign Up",
                fontSize = 25.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
//Function to let user show/hide password
private fun VisibilityToggle(
    visible: Boolean,
    onToggle: () -> Unit
){
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = if (visible) "Hide password" else "Show password"
        )
    }
}
@Preview(showBackground = true)
@Composable
private fun HomeOwnerPreview(){
    PainlessPrepTheme {
        Surface{
            SignupScreen(accountType = AccountType.HOMEOWNER, onSignupClick = {})
        }
    }
}