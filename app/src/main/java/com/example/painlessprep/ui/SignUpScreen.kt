package com.example.painlessprep.ui

import android.R.attr.bottom
import android.icu.util.Calendar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.transition.Visibility
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class AccountType {HOMEOWNER, BUSINESS}


/*What data the signup screen will collect*/
data class SignupForm(
    val name: String,
    val email: String,
    val phone: String,
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
    var name by rememberSaveable{mutableStateOf("")}
    var phone by rememberSaveable{mutableStateOf("")}
    var email by rememberSaveable{mutableStateOf("")}
    var businessName by rememberSaveable{mutableStateOf("")}
    var address by rememberSaveable{mutableStateOf("")}
    var businessAddress by rememberSaveable{mutableStateOf("")}
    var dobMillis by rememberSaveable{ mutableStateOf<Long?>(null)}
    var showDobPicker by rememberSaveable{ mutableStateOf(false)}
    var password by rememberSaveable{mutableStateOf("")}
    var confirmPassword by rememberSaveable{mutableStateOf("")}
    var passwordVisible by rememberSaveable {mutableStateOf(false)}
    var confirmPasswordVisible by rememberSaveable {mutableStateOf(false)}


    val isHomeowner = accountType == AccountType.HOMEOWNER
    val dobDisplay = formatDate(dobMillis, "MM/dd/yyyy")
    val passwordsMatch = password == confirmPassword
    val showPasswordError = confirmPassword.isNotEmpty() && !passwordsMatch
    val phoneError = phone.isNotEmpty() && phone.length != 10
    val emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val emailError = email.isNotEmpty() && !emailValid

    /*Password rules, change after getting backend confirmation*/
    val hasMinLength = password.length >= 8
    val hasUppercase = password.any(Char::isUpperCase)
    val hasDigit = password.any(Char::isDigit)
    val passwordValid = hasMinLength && hasUppercase && hasDigit

    val canSubmit = emailValid &&
            passwordValid &&
            passwordsMatch &&
            phone.length == 10 &&
            if (isHomeowner) {
                name.isNotBlank() && dobMillis != null && address.isNotBlank()
            } else {
                businessName.isNotBlank() && businessAddress.isNotBlank()
            }

    /*Dialogue box for the date*/
    if(showDobPicker){
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dobMillis,
            initialDisplayMode = DisplayMode.Input,
            yearRange = 1900..Calendar.getInstance().get(Calendar.YEAR),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= System.currentTimeMillis()
                }
            }
        )
        DatePickerDialog(onDismissRequest = {showDobPicker = false},
            confirmButton = {
                TextButton(
                    enabled = datePickerState.selectedDateMillis != null,
                    onClick = {
                        dobMillis = datePickerState.selectedDateMillis
                        showDobPicker = false
                    }
                ) { Text("OK")}
            },
            dismissButton = {
                TextButton(onClick = {showDobPicker = false}) {Text("Cancel")}
            }) {
            DatePicker(state = datePickerState, showModeToggle = false)
        }
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
                color = MaterialTheme.colorScheme.primary
            )
        } else{
            Text(
                text = "Create Business Account",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(5.dp))
        /*Name field*/
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
        /*Email field*/
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
            isError = emailError,
            supportingText = if (emailError){
                {Text("Enter a valid email address")}
            } else null,
            modifier = Modifier.fillMaxWidth().padding(bottom = (10.dp))
        )
        /*Phone number field*/
        OutlinedTextField(
            value = phone,
            onValueChange = {phone = it.filter(Char::isDigit).take(10)},
            placeholder = { Text("Phone Number", color = MaterialTheme.colorScheme.onBackground)},
            leadingIcon = {Icon(Icons.Default.Smartphone, contentDescription = null)},
            shape = RoundedCornerShape(size = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = phoneError,
            supportingText = if (phoneError){
                {Text("Enter a valid phone number")}
            } else null,
            modifier = Modifier.fillMaxWidth().padding(bottom = (10.dp))
        )
        /*Address field*/
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
        /*Date of birth field*/
        if(isHomeowner) {
            Box(Modifier.fillMaxWidth()){
                OutlinedTextField(
                    value = dobDisplay,
                    onValueChange = {},
                    readOnly = true,
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
                Box(
                    Modifier
                        .matchParentSize()
                        .clickable(onClickLabel = "Pick date of birth") {showDobPicker = true}

                )
            }
        }
        /*Password fields*/
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
        if (password.isNotEmpty()){
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)){
                PasswordRule("Must have at least 8 characters",hasMinLength)
                PasswordRule("Must have at least one uppercase letter",hasUppercase)
                PasswordRule("Must have at least one number",hasDigit)
            }
        }
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
        /*Sign up button*/
        Button(
            onClick = {
                onSignupClick(
                    SignupForm(
                        name = name.trim(),
                        email = email.trim(),
                        phone = phone.trim(),
                        address = address.trim(),
                        password = password,
                        businessAddress = if (isHomeowner) "" else businessAddress.trim(),
                        businessName = if (isHomeowner) "" else businessName.trim(),
                        dateOfBirth = if (isHomeowner) formatDate(dobMillis,"yyyy-MM-dd") else ""
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
@Composable
private fun PasswordRule(text: String, met: Boolean){
    Row(verticalAlignment = Alignment.CenterVertically){
        Icon(
            imageVector = if (met) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = if (met) "Requirement met" else "Requirement not met",
            tint = if (met) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            color = if (met) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/*Function to format date*/
private fun formatDate(millis: Long?, pattern: String): String{
    if (millis == null) return ""
    val formatter = SimpleDateFormat(pattern, Locale.US)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}

@Preview(showBackground = true)
@Composable
private fun HomeOwnerPreview(){
    PainlessPrepTheme {
        Surface{
            SignupScreen(accountType = AccountType.BUSINESS, onSignupClick = {})
        }
    }
}