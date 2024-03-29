package com.example.lazylist

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Space
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lazylist.ui.theme.LazyListTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import kotlin.time.times


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LazyListTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Navigation(navController)
                }
            }
        }
    }
}

@Composable
fun Navigation(navController: NavHostController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val plates = loadPlates()
        val checkedPlates = remember { mutableStateListOf<Plate>() }

        NavHost(navController = navController, startDestination = "list") {
            composable("list") {
                ListOfPlates(
                    plates = plates,
                    checkedPlates = checkedPlates,
                    navController = navController
                )
            }
            composable("checkout") {
                CheckoutScreen(checkedPlates)
            }
        }
    }
}

@Composable
fun ListOfPlates(
    plates: List<Plate>,
    checkedPlates: SnapshotStateList<Plate>,
    navController: NavController
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(items = plates) { plate ->
                PlateCard(plate = plate) { isChecked ->
                    // Update isChecked state directly
                    plate.isChecked.value = isChecked

                    // Update checkedPlates list (optional)
                    if (isChecked) {
                        checkedPlates.add(plate)
                    } else {
                        checkedPlates.remove(plate)
                    }
                }
            }
        }
        Button(
            onClick = {
                if (checkedPlates.any { it.isChecked.value }) {
                    navController.navigate("checkout")
                } else {
                    Toast.makeText(context, "Veuillez sélectionner au moins un article", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        ) {
            Text(text = "Passer à la caisse")
        }
    }

}

fun calculateTotal(filteredPlates: List<Plate>): Double {
    return filteredPlates.sumOf { it.price * it.quantity.value }
}

fun generateEmailBody(filteredPlates: List<Plate>): String {
    val items = filteredPlates.joinToString("\n") { plate ->
        "${plate.quantity.value}x ${plate.name} - ${"%.2f".format(plate.price * plate.quantity.value)} DH"
    }
    val total = calculateTotal(filteredPlates)
    val totalQuantity = filteredPlates.sumBy { it.quantity.value }
    return """
        Bonjour,

        Voici le récapitulatif de votre commande :

        Articles commandés :
        $items

        Nombre total d'articles : $totalQuantity
        Total : ${"%.2f".format(total)} DH

        Merci pour votre commande et à bientôt!

        Cordialement,
        EsteRestaurant
    """.trimIndent()
}



@Composable
fun PlateRow(plate: Plate, onPlateCheckedChange: (Boolean) -> Unit, onQuantityChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = plate.name)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "${plate.price} DH")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    if (plate.quantity.value > 1) {
                        plate.quantity.value--
                        onQuantityChange(plate.quantity.value)
                    }
                }
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease Quantity")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "${plate.quantity.value}")
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    plate.quantity.value++
                    onQuantityChange(plate.quantity.value)
                }
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase Quantity")
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Checkbox(
            checked = plate.isChecked.value,
            onCheckedChange = { isChecked ->
                plate.isChecked.value = isChecked
                onPlateCheckedChange(isChecked)
            },
            colors = CheckboxDefaults.colors(
                checkedColor = Color.Green,
                uncheckedColor = Color.Gray
            ),
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Composable
fun CheckoutScreen(checkedPlates: MutableList<Plate>) {
    val ctx = LocalContext.current
    val senderEmail = "admin@restaurant.com"
    val emailSubject = "Confirmation de commande"
    val emailBody = generateEmailBody(checkedPlates)

    var total by remember { mutableStateOf(calculateTotal(checkedPlates)) }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Récapitulatif de la commande",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(checkedPlates) { plate ->
                PlateRow(
                    plate = plate,
                    onPlateCheckedChange = { isChecked ->
                        if (!isChecked) {
                            checkedPlates.remove(plate)
                        }
                        total = calculateTotal(checkedPlates)
                    },
                    onQuantityChange = { newQuantity ->
                        plate.quantity.value = newQuantity
                        total = calculateTotal(checkedPlates)
                    }
                )
                Divider(color = Color.Gray, thickness = 1.dp)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total : ${"%.2f".format(total)} DH", // "Total: "
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(senderEmail))
                        putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                        putExtra(Intent.EXTRA_TEXT, emailBody)
                    }
                    val chooser = Intent.createChooser(intent, "Choisir un client de messagerie : ")
                    if (chooser.resolveActivity(ctx.packageManager) != null) {
                        ctx.startActivity(chooser)
                    } else {
                        Toast.makeText(ctx, "Aucun client de messagerie trouvé", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .height(IntrinsicSize.Min)
            ) {
                Text(text = "Confirmer la commande")
            }
        }
    }
}

@Composable
fun PlateCard(plate: Plate, onPlateCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clickable { onPlateCheckedChange(plate.isChecked.value) }
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
            .background(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f))
        ,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
        ) {
            Text(
                text = plate.name,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = MaterialTheme.typography.headlineSmall.fontSize * 0.8f), // Reduce name size
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = painterResource(id = plate.image),
                contentDescription = plate.name,
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.height(20.dp))

            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Catégorie : ${plate.category}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = MaterialTheme.typography.titleMedium.fontSize * 0.8f
                        ),
                        color = Color.White
                    )

                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Description : ${plate.description}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize * 0.8f
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Prix : ${plate.price} DH",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize * 0.8f
                    ),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Checkbox(
                    checked = plate.isChecked.value,
                    onCheckedChange = onPlateCheckedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color.Green,
                        uncheckedColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    )
                )
            }
        }
    }
}




data class Plate(
    val name:String,
    val image:Int,
    val category: String,
    val description: String,
    val price: Double,
    val isChecked: MutableState<Boolean> = mutableStateOf(false),
    val quantity: MutableState<Int> = mutableStateOf(1) // Default quantity is 1
)


fun loadPlates(): List<Plate> {
    return listOf(
        Plate("Pastilla", R.drawable.pastilla, "Main plat", "A savory pastry filled with chicken, almonds, and spices.",  price = 69.99),
    Plate("Méchoui", R.drawable.mechoui, "Main plat", "Slow-roasted lamb or mutton on a spit.",  price = 124.99),
    Plate("Tanjia", R.drawable.tanjiya, "Main plat", "A slow-cooked stew traditionally made in a clay pot.",  price = 89.99),
    Plate("Zaalouk", R.drawable.zaalok, "Salad", "A Moroccan salad made with roasted eggplant, tomatoes, and spices.",  price = 24.99),
    Plate("Baklava", R.drawable.beklava, "Dessert", "A layered pastry with nuts and honey.",  price = 39.99),
    Plate("Chebakia", R.drawable.chbakiya, "Dessert", "Deep-fried pastries drizzled with honey and sesame seeds.",  price = 29.99),
    Plate("Msemen", R.drawable.msemen, "Dessert", "A flatbread filled with butter and honey.",  price = 19.99),
    Plate("Thé à la Menthe", R.drawable.atay, "Drink", "Moroccan mint tea, a refreshing drink made with green tea and mint leaves.",  price = 9.99),
    )
}
