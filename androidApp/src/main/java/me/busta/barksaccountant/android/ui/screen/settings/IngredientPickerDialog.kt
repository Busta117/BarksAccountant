package me.busta.barksaccountant.android.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.android.ui.theme.BarksLightBlue
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
import me.busta.barksaccountant.model.Ingredient

@Composable
fun IngredientPickerDialog(
    ingredients: List<Ingredient>,
    onSelected: (Ingredient) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = barksColors()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Elegir ingrediente",
                style = omnesStyle(17, FontWeight.SemiBold),
                color = colors.primaryText
            )
        },
        text = {
            if (ingredients.isEmpty()) {
                Text(
                    "No hay ingredientes. Créalos en Settings → Ingredientes.",
                    style = omnesStyle(15),
                    color = colors.secondaryText
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(ingredients, key = { it.id }) { ingredient ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelected(ingredient) }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ingredient.name,
                                style = omnesStyle(16, FontWeight.SemiBold),
                                color = colors.primaryText
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BarksLightBlue.copy(alpha = 0.35f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = ingredient.unit.symbol,
                                    style = omnesStyle(13, FontWeight.SemiBold),
                                    color = colors.primaryText
                                )
                            }
                        }
                        HorizontalDivider(
                            color = colors.primaryText.copy(alpha = 0.08f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cerrar",
                    style = omnesStyle(15, FontWeight.SemiBold)
                )
            }
        }
    )
}
