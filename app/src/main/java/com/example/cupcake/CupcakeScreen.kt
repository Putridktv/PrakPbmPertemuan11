
package com.example.cupcake

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cupcake.data.DataSource
import com.example.cupcake.data.OrderUiState
import com.example.cupcake.ui.OrderSummaryScreen
import com.example.cupcake.ui.OrderViewModel
import com.example.cupcake.ui.SelectOptionScreen
import com.example.cupcake.ui.StartOrderScreen

/**
 * enum values that represent the screens in the app
 */
enum class CupcakeScreen(@StringRes val title: Int) { //enum class digunakan untuk membuat judul dalam header tiap halaman
    Start(title = R.string.header_app1),
    Flavor(title = R.string.choose_flavor),
    Pickup(title = R.string.choose_pickup_date),
    Summary(title = R.string.order_summary)
}

/**
 * Composable that displays the topBar and displays back button if back navigation is possible.
 */
@Composable
fun CupcakeAppBar( //composable yang digunakan untuk menampilkan topBar dan tombol navigasi untuk kembali ke halaman sebelumnya
    currentScreen: CupcakeScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar( //fungsi untuk menampilkan topBar
        title = { Text(stringResource(currentScreen.title)) },//memanggil variable title dalam enum class CupcakeScreen untuk menampilkan judul dalam header halaman
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = { //modifier navigasi untuk menampilkan icon back dengan menggunakan variabel canNavigationBack yang diambil dari composable CupcakeApp yang data didapat ketika aplikasi runnig
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@Composable //Composable yang digunakan sebagai screen utama untuk menjalankan class dan fungsi yang dibutuhkan untuk menjalankan aplikasi
fun CupcakeApp(
    viewModel: OrderViewModel = viewModel(), //menjalankan class OrderViewModel dalam composable CupcakeApp
    navController: NavHostController = rememberNavController() //membuat variabel navController yang akan digunakan untuk NavHost
) {
    // mengambil data back screen untuk navigasi button icon  back aplikasi
    val backStackEntry by navController.currentBackStackEntryAsState()
    // mengambil data nama halaman yang sedang dijalankan
    val currentScreen = CupcakeScreen.valueOf(
        backStackEntry?.destination?.route ?: CupcakeScreen.Start.name
    )

    Scaffold(
        topBar = {
            CupcakeAppBar( //menjalankan fungsi CupcakeAppBar dalam composable CupcakeApp
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        NavHost( //menjalankan NavHost untuk navigasi, menentukan tujuan halaman
            navController = navController,
            startDestination = CupcakeScreen.Start.name, //menentukan halaman awal yang digunakan ketika aplikasi pertama kali dijalankan
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = CupcakeScreen.Start.name) {//membuat rute dalam NavHost untuk enum Start
                StartOrderScreen( //menjalankan class StartOrderScreen dalam composable CupcakeApp untuk rute start
                    quantityOptions = DataSource.quantityOptions, //mengambil data variabel quantityIptions dalam class DataSource
                    onNextButtonClicked = {
                        viewModel.setQuantity(it) //menjalankan funngsi setQuantity dalam class OrderViewModel yang ditampung dalam variabel it
                        navController.navigate(CupcakeScreen.Flavor.name) //membuat navigasi antar rute ketika button di klik, setelah rute start akan menuju rute Flavor
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_medium))
                )
            }
            composable(route = CupcakeScreen.Flavor.name) {//membuat rute dalam NavHost untuk enum Flavor
                val context = LocalContext.current
                SelectOptionScreen( //menjalankan class SelectOptionScreen dalam composable CupcakeApp untuk rute Flavor
                    subtotal = uiState.price,
                    onNextButtonClicked = { navController.navigate(CupcakeScreen.Pickup.name) },//membuat navigasi antar rute ketika button di klik, setelah rute Flavor akan menuju rute Pickup
                    onCancelButtonClicked = { //button cancel menjalankan fungsi composable cancelOrderAndNavigateToStart
                        cancelOrderAndNavigateToStart(viewModel, navController)
                    },
                    options = DataSource.flavors.map { id -> context.resources.getString(id) }, //memamnggil data flavors dalam class DataSource untuk menampilkan pilihan rasa
                    onSelectionChanged = { viewModel.setFlavor(it) }, //menjalankan funngsi setFlavor dalam class OrderViewModel yang ditampung dalam variabel it
                    modifier = Modifier.fillMaxHeight()
                )
            }
            composable(route = CupcakeScreen.Pickup.name) {//membuat rute dalam NavHost untuk enum Pickup
                SelectOptionScreen( //menjalankan class SelectOptionScreen dalam composable CupcakeApp untuk rute Pickup
                    subtotal = uiState.price,
                    onNextButtonClicked = { navController.navigate(CupcakeScreen.Summary.name) }, //membuat navigasi antar rute ketika button di klik, setelah rute Pickup akan menuju rute Summary
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(viewModel, navController) //button cancel menjalankan fungsi composable cancelOrderAndNavigateToStart
                    },
                    options = uiState.pickupOptions, //menjalankan funngsi pickupOptions dalam class OrderViewModel untuk menampilkan pilihan tanggal pickup
                    onSelectionChanged = { viewModel.setDate(it) }, //menjalankan funngsi setDate dalam class OrderViewModel untuk mengambil dan menyimpan tangal yang dipilih
                    modifier = Modifier.fillMaxHeight()
                )
            }
            composable(route = CupcakeScreen.Summary.name) {//membuat rute dalam NavHost untuk enum Summary
                val context = LocalContext.current
                OrderSummaryScreen( //menjalankan class OrderSummaryScreen dalam class SummaryScreen dalam composable CupcakeApp untuk rute Pickup
                    orderUiState = uiState, //menampilkan rangkuman data dengan menjalankan clas OrderUiState
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(viewModel, navController) //button cancel menjalankan fungsi composable cancelOrderAndNavigateToStart
                    },
                    onSendButtonClicked = { subject: String, summary: String ->
                        shareOrder(context, subject = subject, summary = summary)
                    },
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    }
}

/**
 * Resets the [OrderUiState] and pops up to [CupcakeScreen.Start]
 */
private fun cancelOrderAndNavigateToStart( //composable untuk button cancel dan akan mereset data yang sebelumnya sudah disimpan dengan mutable state dan aplikasi kembali ke rute Start
    viewModel: OrderViewModel,
    navController: NavHostController
) {
    viewModel.resetOrder()
    navController.popBackStack(CupcakeScreen.Start.name, inclusive = false)
}

/**
 * Creates an intent to share order details
 */
private fun shareOrder(context: Context, subject: String, summary: String) { //Composable untuk meneruskan/ mengcopy data rangkuman order cupcake yang sudah dipilih untuk diteruskan ke aplikasi lain
    // Create an ACTION_SEND implicit intent with order details in the intent extras
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, summary)
    }
    context.startActivity(
        Intent.createChooser(
            intent,
            context.getString(R.string.new_cupcake_order)
        )
    )
}
