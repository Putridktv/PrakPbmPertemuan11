
package com.example.cupcake.data

import com.example.cupcake.R

object DataSource {
    val flavors = listOf(
        R.string.vanilla,
        R.string.chocolate,
        R.string.red_velvet,
        R.string.salted_caramel,
        R.string.coffee,
        R.string.caramel_latte,
        R.string.stawberry,
        R.string.matcha,
    )

    val quantityOptions = listOf(
        Pair(R.string.one_cupcake, 1),
        Pair(R.string.three_cupcake, 3),
        Pair(R.string.six_cupcakes, 6),
        Pair(R.string.twelve_cupcakes, 12),
        Pair(R.string.twenty_four_cupcakes, 24),
        Pair(R.string.fourty_eight_cupcakes, 48)
    )
}
