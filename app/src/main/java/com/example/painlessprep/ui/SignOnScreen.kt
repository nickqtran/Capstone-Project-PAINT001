package com.example.painlessprep
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

@Preview(showBackground = true)
@Composable
fun SignInScreen(){
    var email by remember{
        mutableStateOf("")
    }
    var password by remember{
        mutableStateOf("")
    }
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F4EF)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ){
        Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Logo", modifier = Modifier.size(150.dp))
        Text(text = "Painless Prep",
            Modifier.offset(y = (-25).dp),
            fontSize=20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0B5C78))
        /*Sign-in text fields,buttons, and labels*/
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Text(text = "Measure windows.\nOrder sheets cut to fit.",
                modifier = Modifier.align(Alignment.Start).padding(bottom = 20.dp),
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold)

            Text(
                text = "Email", fontWeight = FontWeight.Bold, modifier =
                    Modifier.align(Alignment.Start).padding(bottom = 6.dp)
            )
            OutlinedTextField(value = email, onValueChange = {
                email = it
            }, placeholder = { Text("you@example.com", color = Color.Gray) }, modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0B5C78),
                    unfocusedBorderColor = Color.Gray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White),
                shape = RoundedCornerShape(size = 16.dp))

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Password", fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Forgot?", textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { /*TODO:Handle Button Logic*/ }, color = Color(0xFF0B5C78))
            }
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                },
                placeholder = { Text("••••••••••", color = Color.Gray) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0B5C78),
                    unfocusedBorderColor = Color.Gray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White),
                shape = RoundedCornerShape(size = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {/*TODO:Handle Button Logic*/},
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0B5C78)
                )
            ) {
                Text(text = "Sign in",fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "or", modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center, color = Color.Gray)

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(onClick = {/*TODO:Handle Button Logic*/},
                shape = RoundedCornerShape(size = 16.dp),
                modifier = Modifier.fillMaxWidth().height(64.dp),
                border = BorderStroke(2.dp,Color.Black)){
                    Text(text = "Create an account",fontSize = 18.sp,
                        color = Color.Black)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Measure First, sign in at checkout", textDecoration = TextDecoration.Underline,
                modifier = Modifier.fillMaxWidth().clickable{/*TODO:Handle Button Logic*/},textAlign = TextAlign.Center)
        }
    }
}