package com.tm.score_app.pages.fragments

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tm.score_app.pages.ScanDeviceActivity


data class User(
    val name: String,
    val score: Int,
    val rating: Double,
    val color: Color
)

@Composable
fun UsersList(users: List<User>, context: Context) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            text = "Users",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn {
            items(users) { user ->
                UserCard(user)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                context.startActivity(Intent(context, ScanDeviceActivity::class.java))

            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
            modifier = Modifier.align(Alignment.End),
            shape = RoundedCornerShape(50)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Add users", color = Color.White)
        }
    }
}

@Composable
fun UserCard(user: User) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(user.color, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "User Icon",
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = user.name,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = if (user.score == 0) "❌ 0" else "${user.score}",
                color = if (user.score == 0) Color.Red else Color.Green,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "${user.rating}",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun UserPage(context: Context) {
    val users = listOf(
        User("User 1", 2, 7.0, Color(0xFF66FF66)),
        User("User 2", 8, 7.75, Color(0xFF00FFFF)),
        User("User 3", 5, 0.75, Color(0xFFFF6600)),
        User("User 4", 0, 0.0, Color(0xFF9933FF))
    )
    UsersList(users, context)
}
