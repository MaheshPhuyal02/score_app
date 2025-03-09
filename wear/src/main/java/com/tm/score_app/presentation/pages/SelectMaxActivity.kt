
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog


@Composable
fun SelectMaximumScreen() {
    var selectedValue by remember { mutableStateOf(8.0f) }
    var showDialog by remember { mutableStateOf(false) }
    var inputValue by remember { mutableStateOf(selectedValue.toString()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Time at the top
            Text(
                text = "10:18",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // "Select Maximum" heading with green color
            Text(
                text = "Select Maximum",
                color = Color(0xFF00C853),  // Bright green
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Instruction text
            Text(
                text = "Select maximum divisor 1-10, will return to this this number upon reset unless changed:",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Selected value
            Text(
                text = "Selected: $selectedValue",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 13.dp)
            ) {
                Button(
                    onClick = { showDialog = true },
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00C853) // Bright green
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Select Maximum",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (showDialog) {
                Dialog(onDismissRequest = { showDialog = false }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            TextField(
                                value = inputValue,
                                textStyle = TextStyle(
                                    color = Color.White,

                                    ),
                                colors = TextFieldDefaults.colors(
                                    //setting the text field background when it is focused
                                    focusedContainerColor = Color.Black,

                                    //setting the text field background when it is unfocused or initial state
                                    unfocusedContainerColor = Color.Black,

                                    //setting the text field background when it is disabled
                                    disabledContainerColor = Color.Gray,

                                    ),
                                onValueChange = { inputValue = it },
                                label = { Text("Enter value",
                                    style = TextStyle(
                                        color = Color.White
                                    )
                                ) },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00C853) // Bright green
                                ),
                                onClick = {
                                    selectedValue = inputValue.toFloatOrNull() ?: selectedValue
                                    showDialog = false
                                }
                            ) {
                                Text("OK")
                            }
                        }
                    }
                }
            }
        }

    }
}

@Preview(
    widthDp = 200,
    heightDp = 200,
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
fun SelectMaximumScreenPreview() {
    SelectMaximumScreen()
}