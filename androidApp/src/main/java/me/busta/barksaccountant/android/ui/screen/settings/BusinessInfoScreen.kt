package me.busta.barksaccountant.android.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.busta.barksaccountant.android.ui.theme.BarksCard
import me.busta.barksaccountant.android.ui.theme.BarksRed
import me.busta.barksaccountant.android.ui.theme.BarksWhite
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle
import me.busta.barksaccountant.di.ServiceLocator
import me.busta.barksaccountant.feature.settings.businessinfo.BusinessInfoMessage
import me.busta.barksaccountant.feature.settings.businessinfo.BusinessInfoStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessInfoScreen(
    serviceLocator: ServiceLocator,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val store = remember {
        BusinessInfoStore(businessInfoRepository = serviceLocator.businessInfoRepository)
    }
    val state by store.state.collectAsState()
    val colors = barksColors()

    LaunchedEffect(Unit) { store.dispatch(BusinessInfoMessage.Started) }
    DisposableEffect(Unit) { onDispose { store.dispose() } }
    LaunchedEffect(state.savedSuccessfully) { if (state.savedSuccessfully) onSaved() }

    Scaffold(
        containerColor = colors.screenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Datos del negocio",
                        style = omnesStyle(17, FontWeight.SemiBold),
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
                    containerColor = colors.screenBackground
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.screenBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                BarksCard(title = "Información", colors = colors) {
                    Column {
                        InfoFieldLabel(text = "Nombre del negocio", colors = colors)
                        Spacer(Modifier.height(6.dp))
                        InfoTextField(
                            value = state.businessName,
                            onValueChange = { store.dispatch(BusinessInfoMessage.BusinessNameChanged(it)) },
                            placeholder = "Ej: Mi Empresa S.L.",
                            colors = colors,
                            hasError = state.businessName.isEmpty(),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words
                            )
                        )

                        Spacer(Modifier.height(14.dp))

                        InfoFieldLabel(text = "NIF (opcional)", colors = colors)
                        Spacer(Modifier.height(6.dp))
                        InfoTextField(
                            value = state.nif,
                            onValueChange = { store.dispatch(BusinessInfoMessage.NifChanged(it)) },
                            placeholder = "Ej: B12345678",
                            colors = colors,
                            hasError = false
                        )

                        Spacer(Modifier.height(14.dp))

                        InfoFieldLabel(text = "Dirección (opcional)", colors = colors)
                        Spacer(Modifier.height(6.dp))
                        InfoTextField(
                            value = state.address,
                            onValueChange = { store.dispatch(BusinessInfoMessage.AddressChanged(it)) },
                            placeholder = "Dirección del negocio",
                            colors = colors,
                            hasError = false,
                            multiline = true,
                            maxLines = 3
                        )

                        Spacer(Modifier.height(14.dp))

                        InfoFieldLabel(text = "Teléfono (opcional)", colors = colors)
                        Spacer(Modifier.height(6.dp))
                        InfoTextField(
                            value = state.phone,
                            onValueChange = { store.dispatch(BusinessInfoMessage.PhoneChanged(it)) },
                            placeholder = "Teléfono",
                            colors = colors,
                            hasError = false,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )

                        Spacer(Modifier.height(14.dp))

                        InfoFieldLabel(text = "Email (opcional)", colors = colors)
                        Spacer(Modifier.height(6.dp))
                        InfoTextField(
                            value = state.email,
                            onValueChange = { store.dispatch(BusinessInfoMessage.EmailChanged(it)) },
                            placeholder = "Email",
                            colors = colors,
                            hasError = false,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                BarksCard(title = "Información bancaria (opcional)", colors = colors) {
                    Column {
                        InfoFieldLabel(text = "Banco", colors = colors)
                        Spacer(Modifier.height(6.dp))
                        InfoTextField(
                            value = state.bankName,
                            onValueChange = { store.dispatch(BusinessInfoMessage.BankNameChanged(it)) },
                            placeholder = "Ej: Banco Santander",
                            colors = colors,
                            hasError = false,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words
                            )
                        )

                        Spacer(Modifier.height(14.dp))

                        InfoFieldLabel(text = "IBAN", colors = colors)
                        Spacer(Modifier.height(6.dp))
                        InfoTextField(
                            value = state.iban,
                            onValueChange = { store.dispatch(BusinessInfoMessage.IbanChanged(it)) },
                            placeholder = "Ej: ES12 1234 5678 9012 3456 7890",
                            colors = colors,
                            hasError = false
                        )

                        Spacer(Modifier.height(14.dp))

                        InfoFieldLabel(text = "Titular", colors = colors)
                        Spacer(Modifier.height(6.dp))
                        InfoTextField(
                            value = state.bankHolder,
                            onValueChange = { store.dispatch(BusinessInfoMessage.BankHolderChanged(it)) },
                            placeholder = "Titular de la cuenta",
                            colors = colors,
                            hasError = false,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words
                            )
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                BarksCard(colors = colors) {
                    Column {
                        Button(
                            onClick = { store.dispatch(BusinessInfoMessage.SaveTapped) },
                            enabled = state.canSave && !state.isSaving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BarksRed,
                                contentColor = BarksWhite,
                                disabledContainerColor = BarksRed.copy(alpha = 0.6f),
                                disabledContentColor = BarksWhite.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = BarksWhite,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Guardar",
                                    style = omnesStyle(16, FontWeight.SemiBold)
                                )
                            }
                        }

                        state.error?.let { error ->
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = error,
                                style = omnesStyle(13, FontWeight.Normal),
                                color = BarksRed,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoFieldLabel(
    text: String,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors
) {
    Text(
        text = text,
        style = omnesStyle(13, FontWeight.Normal),
        color = colors.secondaryText
    )
}

@Composable
private fun InfoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    colors: me.busta.barksaccountant.android.ui.theme.BarksColors,
    hasError: Boolean,
    modifier: Modifier = Modifier,
    multiline: Boolean = false,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = omnesStyle(
            size = if (multiline) 16 else 17,
            weight = if (multiline) FontWeight.Normal else FontWeight.SemiBold
        ).copy(color = colors.primaryText),
        keyboardOptions = keyboardOptions,
        singleLine = !multiline,
        maxLines = if (multiline) maxLines else 1,
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (multiline) {
                    Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                } else {
                    Modifier
                        .height(48.dp)
                        .padding(horizontal = 12.dp)
                }
            )
            .background(colors.fieldBackground, RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = if (hasError) BarksRed.copy(alpha = 0.4f) else colors.fieldBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .then(
                if (!multiline) {
                    Modifier.padding(horizontal = 12.dp)
                } else {
                    Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                }
            ),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = if (multiline) Alignment.TopStart else Alignment.CenterStart,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = omnesStyle(
                            size = if (multiline) 16 else 17,
                            weight = if (multiline) FontWeight.Normal else FontWeight.SemiBold
                        ),
                        color = colors.secondaryText.copy(alpha = 0.5f)
                    )
                }
                innerTextField()
            }
        }
    )
}
