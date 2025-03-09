
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tm.score_app.R

// Define your navigation items
enum class BottomNavItem(
    val title: String,
    val selectedIcon: Int,
    val defaultIcon: Int
) {
    HOME("Home", R.drawable.home_selected, R.drawable.home),
    USER("Users", R.drawable.people_selected, R.drawable.people),
    WATCH("Watch", R.drawable.watch_selected, R.drawable.watch)
}

/**
 * Reusable Bottom Navigation Component
 *
 * @param selectedItem Current selected navigation item
 * @param onItemSelected Callback when an item is selected
 */
@Composable
fun AppBottomNavigation(
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit
) {
    NavigationBar(
        containerColor = Color.Black,
        modifier = Modifier
            .fillMaxWidth()

    ) {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = selectedItem == item,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        painter = painterResource(
                            id = if (selectedItem == item) item.selectedIcon else item.defaultIcon
                        ),
                        contentDescription = item.title,
                        tint = if (selectedItem == item) Color.Cyan else Color.Gray
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontWeight = if (selectedItem == item) FontWeight.Medium else FontWeight.Normal,
                        fontSize = 12.sp,
                        color = if (selectedItem == item) Color.Cyan else Color.Gray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Cyan,
                    selectedTextColor = Color.Cyan,
                    indicatorColor = Color.Black,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

// Example usage in a parent composable
@Composable
fun MainScreen() {
    // State should be hoisted to the parent composable or ViewModel
    var selectedItem by remember { mutableStateOf(BottomNavItem.HOME) }

    // Your page content here

    // Add bottom navigation at the bottom of your layout
    AppBottomNavigation(
        selectedItem = selectedItem,
        onItemSelected = { selectedItem = it }
    )
}

