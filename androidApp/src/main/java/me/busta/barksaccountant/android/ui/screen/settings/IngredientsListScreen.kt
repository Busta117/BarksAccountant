package me.busta.barksaccountant.android.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.android.ui.theme.BarksFab
import me.busta.barksaccountant.android.ui.theme.BarksLightBlue
import me.busta.barksaccountant.android.ui.theme.BarksRed
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
import me.busta.barksaccountant.android.ui.theme.vagRundschriftStyle
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.settings.ingredients.list.IngredientsListMessage
import me.busta.barksaccountant.feature.settings.ingredients.list.IngredientsListStore
import me.busta.barksaccountant.model.Ingredient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsListScreen(
    serviceLocator: ServiceLocator,
    onBack: () -> Unit,
    onIngredientClicked: (String) -> Unit,
    onAddIngredient: () -> Unit
) {
    val store = remember {
        IngredientsListStore(ingredientRepository = serviceLocator.ingredientRepository)
    }
    val state by store.state.collectAsState()
    val colors = barksColors()

    LaunchedEffect(Unit) { store.dispatch(IngredientsListMessage.Started) }
    DisposableEffect(Unit) { onDispose { store.dispose() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ingredientes",
                        style = omnesStyle(20, FontWeight.SemiBold),
                        color = colors.primaryText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colors.primaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.screenBackground,
                    titleContentColor = colors.primaryText,
                    navigationIconContentColor = colors.primaryText
                )
            )
        },
        containerColor = colors.screenBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.ingredients.isEmpty() -> {
                    CircularProgressIndicator(
                        color = BarksRed,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.error != null && state.ingredients.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.error ?: "",
                            style = omnesStyle(17),
                            color = colors.primaryText.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        TextButton(
                            onClick = { store.dispatch(IngredientsListMessage.Started) }
                        ) {
                            Text(
                                "Reintentar",
                                style = omnesStyle(15, FontWeight.SemiBold),
                                color = BarksRed
                            )
                        }
                    }
                }
                state.ingredients.isEmpty() -> {
                    Text(
                        text = "No hay ingredientes",
                        style = vagRundschriftStyle(20),
                        color = colors.primaryText.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.ingredients, key = { it.id }) { ingredient ->
                            IngredientCardRow(
                                ingredient = ingredient,
                                colors = colors,
                                onClick = { onIngredientClicked(ingredient.id) }
                            )
                        }
                    }
                }
            }

            // FAB bottom-right
            BarksFab(
                onClick = onAddIngredient,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = androidx.compose.foundation.shape.CircleShape,
                        ambientColor = Color.Black.copy(alpha = if (colors.isDark) 0.35f else 0.18f),
                        spotColor = Color.Black.copy(alpha = if (colors.isDark) 0.35f else 0.18f)
                    ),
                colors = colors
            )
        }
    }
}

@Composable
private fun IngredientCardRow(
    ingredient: Ingredient,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color.Black.copy(alpha = if (colors.isDark) 0.22f else 0.08f),
                spotColor = Color.Black.copy(alpha = if (colors.isDark) 0.22f else 0.08f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(colors.cardBackground)
            .then(
                if (colors.isDark) {
                    Modifier.border(
                        1.dp,
                        Color.White.copy(alpha = 0.06f),
                        RoundedCornerShape(18.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = ingredient.name,
            style = omnesStyle(17, FontWeight.SemiBold),
            color = colors.primaryText,
            modifier = Modifier.weight(1f, fill = false).padding(end = 12.dp),
            maxLines = 2
        )

        // Unit chip
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
}
