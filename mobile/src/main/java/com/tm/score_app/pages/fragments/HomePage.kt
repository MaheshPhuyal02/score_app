package com.tm.score_app.pages.fragments

import ScoreIndicator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tm.score_app.ui.theme.CardColor

@Composable
fun HomePage() {


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 12.dp)
    ) {

        Column {

            Text(
                "Home",
                modifier = Modifier.padding(
                    bottom = 12.dp,
                    top = 12.dp
                ),
                style = TextStyle(
                    fontSize = 24.sp,
                    color = Color.White
                ),
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                "Daily Activity",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color.White
                ),
            )

            Card(
                border = BorderStroke(
                    width = 0.dp,
                    color = Color.Black
                ),

                colors = CardColors(
                    contentColor = CardColor,
                    containerColor = CardColor,
                    disabledContainerColor = CardColor,
                    disabledContentColor = CardColor,
                ),
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            ) {
                ScoreIndicator(
                    score = 0.2f,
                    size = 170,
                    thickness = 30f,
                    modifier = Modifier.padding(24.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardColor),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Heart Rate",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "123 BPM",
                            color = Color.Red,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Today",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.Top)
                    )
                }
            }
        }


    }


}