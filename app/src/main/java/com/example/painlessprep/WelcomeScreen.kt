package com.example.painlessprep

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.annotation.DrawableRes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colors sampled from the mockup
private val Background = Color(0xFFF4F3EE)
private val BrandTeal = Color(0xFF155E75)
private val TitleText = Color(0xFF1C1C1E)
private val BodyText = Color(0xFF5F6368)
private val CardBorder = Color(0xFFE3E0D8)
private val IconBox = Color(0xFFE4EEF1)
private val RadioIdle = Color(0xFF9A9A92)
private val ButtonDisabled = Color(0xFFD9D7CF)
private val ButtonDisabledText = Color(0xFF4A4A4A)

enum class UserType { HOMEOWNER, BUSINESS }

@Composable
fun WelcomeScreen(
    onContinue: (UserType) -> Unit,
    onAlreadyHaveAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by rememberSaveable { mutableStateOf<UserType?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .systemBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 72.dp, bottom = 32.dp)
    ) {
        Text(
            text = "PAINLESSPREP",
            color = BrandTeal,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Welcome! Who are you measuring for?",
            color = TitleText,
            fontSize = 30.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "We'll set things up to fit how you work. You can change this later in Account.",
            color = BodyText,
            fontSize = 16.sp,
            lineHeight = 23.sp,
        )
        Spacer(Modifier.height(28.dp))

        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            UserTypeCard(
                icon = R.drawable.ic_home,
                title = "Homeowner",
                description = "Measuring windows in my own home",
                selected = selected == UserType.HOMEOWNER,
                onSelect = { selected = UserType.HOMEOWNER },
            )
            UserTypeCard(
                icon = R.drawable.ic_building,
                title = "Business",
                description = "Painter or contractor measuring for customers and job sites",
                selected = selected == UserType.BUSINESS,
                onSelect = { selected = UserType.BUSINESS },
            )
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { selected?.let(onContinue) },
            enabled = selected != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandTeal,
                contentColor = Color.White,
                disabledContainerColor = ButtonDisabled,
                disabledContentColor = ButtonDisabledText,
            ),
        ) {
            Text("Continue", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(20.dp))

        TextButton(
            onClick = onAlreadyHaveAccount,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(
                text = "I already have an account",
                color = TitleText,
                fontSize = 15.sp,
                textDecoration = TextDecoration.Underline,
            )
        }
    }
}

@Composable
private fun UserTypeCard(
    @DrawableRes icon: Int,
    title: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)

    Surface(
        shape = shape,
        color = Color.White,
        border = BorderStroke(
            width = 2.dp,
            color = if (selected) BrandTeal else CardBorder,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton,
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(IconBox, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = BrandTeal,
                    modifier = Modifier.size(26.dp),
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TitleText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    color = BodyText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            }

            Spacer(Modifier.width(12.dp))

            // onClick = null so the whole card handles the click (better accessibility)
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = BrandTeal,
                    unselectedColor = RadioIdle,
                ),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun WelcomeScreenPreview() {
    MaterialTheme {
        WelcomeScreen(onContinue = {}, onAlreadyHaveAccount = {})
    }
}