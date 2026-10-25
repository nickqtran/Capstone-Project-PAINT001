package com.example.painlessprep

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview



import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// milestone 2 to do: implement create house functionality
// sponsor wants house name customization
private val BackgroundColor = Color(0xFFF6F5F1)
private val PrimaryColor = Color(0xFF0B5D75)
private val TextColor = Color(0xFF202124)
private val SecondaryColor = Color(0xFF60666F)
private val BorderColor = Color(0xFFE2E0D8)

private data class Job(
    val name: String,
    val address: String = "",
    val windows: Int,
    val status: String,
    val estimate: String = "",
    val shipping: String = ""
)

@Composable
fun JobsScreen() {
    var searchQuery by remember { mutableStateOf("") }

    // placeholder variables to test UI
    val jobs = remember {
        listOf(
            Job(
                name = "House 1",
                address = "123 A Street Omaha NE 68132",
                windows = 4,
                status = "Draft",
                estimate = "$[TOTAL]"
            ),
            Job(
                name = "House 2",
                address = "123 A Street Omaha NE 68132",
                windows = 11,
                status = "Draft",
                estimate = "$[TOTAL]"
            ),
            Job(
                name = "House 3",
                windows = 6,
                status = "Paid",
                shipping = "Shipped\nArriving October 13"
            )
        )
    }

    // filter through jobs based on status, name, and address
    val filteredJobs = jobs.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.address.contains(searchQuery, ignoreCase = true)
    }

    // filtering for draft/paid orders needs fixing
    val draftJobs = filteredJobs.filter { it.status == "Draft" }
    val orderedJobs = filteredJobs.filter { it.status == "Paid" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 48.dp,
                bottom = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "PAINLESS PREP",
                            color = PrimaryColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Jobs",
                            color = TextColor,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, BorderColor, CircleShape)
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("♙", fontSize = 27.sp, color = TextColor)
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(
                            Color.White,
                            RoundedCornerShape(14.dp)
                        )
                        .border(
                            1.dp,
                            BorderColor,
                            RoundedCornerShape(14.dp)
                        )
                        .padding(17.dp)
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            "Search jobs or addresses",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = TextColor,
                            fontSize = 16.sp
                        )
                    )
                }
            }

            if (draftJobs.isNotEmpty()) {
                item {
                    SectionTitle("In progress")
                }

                items(draftJobs) { job ->
                    JobCard(job)
                }
            }

            if (orderedJobs.isNotEmpty()) {
                item {
                    SectionTitle("Ordered")
                }

                items(orderedJobs) { job ->
                    JobCard(job)
                }
            }

            if (filteredJobs.isEmpty()) {
                item {
                    Text(
                        "No jobs found",
                        color = SecondaryColor,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
            }
        }

        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .height(58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryColor
            )
        ) {
            Text(
                "⊞  Measure a House",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        BottomNavigationBar()
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = SecondaryColor,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun JobCard(job: Job) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            BorderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = job.name,
                    color = TextColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                StatusBadge(job.status)
            }

            if (job.address.isNotEmpty()) {
                Text(
                    text = job.address,
                    color = SecondaryColor,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (job.status == "Draft") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        "${job.windows} windows",
                        color = TextColor,
                        fontSize = 15.sp
                    )
                    Text(
                        "Est. ${job.estimate}",
                        color = SecondaryColor,
                        fontSize = 15.sp
                    )
                }
            } else {
                Text(
                    text = "${job.windows} windows · ${job.shipping}",
                    color = SecondaryColor,
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val isDraft = status == "Draft"

    Box(
        modifier = Modifier
            .background(
                if (isDraft) Color(0xFFFBE8DD)
                else Color(0xFFDDEFF4),
                RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = status,
            color = if (isDraft) Color(0xFF934018)
            else Color(0xFF10536A),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun BottomNavigationBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        NavigationItem("▦", "Jobs", true)
        NavigationItem("▱", "Orders", false)
        NavigationItem("♙", "Account", false)
    }
}

@Composable
private fun RowScope.NavigationItem(
    symbol: String,
    label: String,
    selected: Boolean
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable { },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            symbol,
            fontSize = 25.sp,
            color = if (selected) PrimaryColor else SecondaryColor
        )
        Text(
            label,
            fontSize = 13.sp,
            color = if (selected) PrimaryColor else SecondaryColor,
            fontWeight = if (selected) FontWeight.Bold
            else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun JobsScreenPreview() {
    JobsScreen()
}
