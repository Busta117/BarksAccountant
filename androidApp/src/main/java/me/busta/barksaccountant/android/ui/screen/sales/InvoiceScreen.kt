package me.busta.barksaccountant.android.ui.screen.sales

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import me.busta.barksaccountant.android.ui.theme.barksColors
import me.busta.barksaccountant.android.ui.theme.omnesStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    invoiceHtml: String,
    saleId: String,
    onBack: () -> Unit,
    documentName: String = "Factura"
) {
    val colors = barksColors()
    val context = LocalContext.current
    val webView = remember { WebView(context) }

    Scaffold(
        containerColor = colors.screenBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "$documentName #$saleId",
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
                actions = {
                    IconButton(onClick = {
                        printInvoice(context, webView, saleId, documentName)
                    }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Compartir",
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
        AndroidView(
            factory = {
                webView.apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = false
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    loadDataWithBaseURL(null, invoiceHtml, "text/html", "UTF-8", null)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}

private fun printInvoice(context: Context, webView: WebView, saleId: String, documentName: String) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
    val printAdapter = webView.createPrintDocumentAdapter("${documentName}_$saleId")
    printManager.print(
        "$documentName #$saleId",
        printAdapter,
        PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .build()
    )
}
