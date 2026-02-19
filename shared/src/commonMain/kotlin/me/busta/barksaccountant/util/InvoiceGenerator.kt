package me.busta.barksaccountant.util

import me.busta.barksaccountant.model.BusinessInfo
import me.busta.barksaccountant.model.Client
import me.busta.barksaccountant.model.Sale

object InvoiceGenerator {

    fun generateHtml(sale: Sale, client: Client?, businessInfo: BusinessInfo): String {
        val subtotal = sale.totalPrice
        val ivaPct = client?.ivaPct
        val recargoPct = client?.recargoPct
        val ivaAmt = if (ivaPct != null) subtotal * ivaPct / 100.0 else 0.0
        val recargoAmt = if (recargoPct != null) subtotal * recargoPct / 100.0 else 0.0
        val total = subtotal + ivaAmt + recargoAmt

        val hasBankInfo = !businessInfo.bankName.isNullOrBlank() ||
                !businessInfo.iban.isNullOrBlank() ||
                !businessInfo.bankHolder.isNullOrBlank()

        return buildString {
            append("<!DOCTYPE html>")
            append("<html lang=\"es\"><head><meta charset=\"UTF-8\">")
            append("<meta name=\"viewport\" content=\"width=794, initial-scale=0.5, minimum-scale=0.1, maximum-scale=5.0, user-scalable=yes\">")
            append("<title>Factura ${esc(sale.id)}</title>")
            append("<style>")
            append(CSS)
            append("</style></head><body>")
            append("<div class=\"invoice\">")

            // Header
            append("<div class=\"header\">")
            append("<div class=\"logo-icon\">$LOGO_SVG</div>")
            append("<div class=\"invoice-id-block\">")
            append("<div class=\"invoice-label\">Factura</div>")
            append("<div class=\"invoice-number\">${esc(sale.id)}</div>")
            append("<div class=\"invoice-accent-line\"></div>")
            append("</div></div>")

            // Header rule
            append("<div class=\"header-rule\"></div>")

            // Dates
            append("<div class=\"date-row\">")
            append("<div class=\"date-item\">")
            append("<span class=\"date-key\">Fecha de pedido</span>")
            append("<span class=\"date-val\">${esc(sale.orderDate)}</span>")
            append("</div>")
            if (sale.deliveryDate != null) {
                append("<div class=\"date-item\">")
                append("<span class=\"date-key\">Fecha de entrega</span>")
                append("<span class=\"date-val\">${esc(sale.deliveryDate!!)}</span>")
                append("</div>")
            }
            append("</div>")

            // Body
            append("<div class=\"body\">")

            // Parties
            append("<div class=\"parties\">")

            // Vendor
            append("<div class=\"party-block\">")
            append("<div class=\"party-title\">De</div>")
            append("<div class=\"party-name\">${esc(businessInfo.businessName)}</div>")
            if (!businessInfo.nif.isNullOrBlank()) {
                append("<div class=\"party-row\"><span class=\"party-row-label\">NIF</span><span class=\"party-row-val\">${esc(businessInfo.nif!!)}</span></div>")
            }
            if (!businessInfo.address.isNullOrBlank()) {
                append("<div class=\"party-row\"><span class=\"party-row-label\">Dirección</span><span class=\"party-row-val\">${esc(businessInfo.address!!)}</span></div>")
            }
            if (!businessInfo.phone.isNullOrBlank()) {
                append("<div class=\"party-row\"><span class=\"party-row-label\">Teléfono</span><span class=\"party-row-val\">${esc(businessInfo.phone!!)}</span></div>")
            }
            if (!businessInfo.email.isNullOrBlank()) {
                append("<div class=\"party-row\"><span class=\"party-row-label\">Email</span><span class=\"party-row-val\">${esc(businessInfo.email!!)}</span></div>")
            }
            append("</div>")

            // Client
            append("<div class=\"party-block client\">")
            append("<div class=\"party-title\">Facturar a</div>")
            append("<div class=\"party-name\">${esc(sale.clientName)}</div>")
            if (client != null) {
                if (!client.responsible.isNullOrBlank()) {
                    append("<div class=\"party-row\"><span class=\"party-row-label\">Responsable</span><span class=\"party-row-val\">${esc(client.responsible!!)}</span></div>")
                }
                if (!client.nif.isNullOrBlank()) {
                    append("<div class=\"party-row\"><span class=\"party-row-label\">NIF</span><span class=\"party-row-val\">${esc(client.nif!!)}</span></div>")
                }
                if (!client.address.isNullOrBlank()) {
                    append("<div class=\"party-row\"><span class=\"party-row-label\">Dirección</span><span class=\"party-row-val\">${esc(client.address!!)}</span></div>")
                }
            }
            append("</div>")
            append("</div>")

            // Products table
            append("<div class=\"table-section\">")
            append("<div class=\"table-label\">Líneas de factura</div>")
            append("<table><thead><tr>")
            append("<th style=\"width:54%\">Descripción</th>")
            append("<th style=\"width:10%\">Unid.</th>")
            append("<th style=\"width:18%\">Precio unit.</th>")
            append("<th style=\"width:18%\">Total</th>")
            append("</tr></thead><tbody>")
            for (product in sale.products) {
                append("<tr>")
                append("<td class=\"desc\">${esc(product.name)}</td>")
                append("<td>${product.quantity}</td>")
                append("<td>${fmt(product.unitPrice)}</td>")
                append("<td class=\"total-col\">${fmt(product.totalPrice)}</td>")
                append("</tr>")
            }
            append("</tbody></table></div>")

            // Bottom grid
            append("<div class=\"bottom-grid\">")

            // Payment block (only if bank info exists)
            if (hasBankInfo) {
                append("<div class=\"payment-block\">")
                append("<div class=\"payment-title\">Información de pago</div>")
                if (!businessInfo.bankName.isNullOrBlank()) {
                    append("<div class=\"payment-row\"><span class=\"payment-label\">Banco</span><span class=\"payment-val\">${esc(businessInfo.bankName!!)}</span></div>")
                }
                if (!businessInfo.iban.isNullOrBlank()) {
                    append("<div class=\"payment-row\"><span class=\"payment-label\">IBAN</span><span class=\"payment-val\">${esc(businessInfo.iban!!)}</span></div>")
                }
                if (!businessInfo.bankHolder.isNullOrBlank()) {
                    append("<div class=\"payment-row\"><span class=\"payment-label\">Titular</span><span class=\"payment-val\">${esc(businessInfo.bankHolder!!)}</span></div>")
                }
                append("<div class=\"payment-row\"><span class=\"payment-label\">Concepto</span><span class=\"payment-val\">${esc(sale.id)}</span></div>")
                append("<div class=\"payment-alt\">También se acepta pago con tarjeta o en efectivo.</div>")
                append("</div>")
            } else {
                append("<div class=\"payment-block\">")
                append("<div class=\"payment-title\">Información de pago</div>")
                append("<div class=\"payment-alt-only\">Se acepta pago con tarjeta, transferencia o en efectivo.</div>")
                append("</div>")
            }

            // Totals
            append("<div class=\"totals\">")
            append("<div class=\"totals-row\"><span class=\"totals-label\">Subtotal</span><span class=\"totals-value\">${fmt(subtotal)}</span></div>")
            if (ivaPct != null) {
                append("<div class=\"totals-row\"><span class=\"totals-label\">IVA (${fmtPct(ivaPct)}%)</span><span class=\"totals-value\">${fmt(ivaAmt)}</span></div>")
            }
            if (recargoPct != null) {
                append("<div class=\"totals-row\"><span class=\"totals-label\">R. Equiv. (${fmtPct(recargoPct)}%)</span><span class=\"totals-value\">${fmt(recargoAmt)}</span></div>")
            }
            append("<div class=\"totals-row final\"><span class=\"totals-label\">Total</span><span class=\"totals-value\">${fmt(total)}</span></div>")
            append("</div>")
            append("</div>")

            // Notes (only if business has email or phone)
            val contactInfo = listOfNotNull(
                businessInfo.email?.takeIf { it.isNotBlank() },
                businessInfo.phone?.takeIf { it.isNotBlank() }
            ).joinToString(" / ")
            if (contactInfo.isNotEmpty()) {
                append("<div class=\"notes-section\">")
                append("<div class=\"notes-title\">Nota</div>")
                append("<div class=\"notes-text\">El pago deberá realizarse en un plazo máximo de 30 días desde la fecha de emisión. Para cualquier consulta sobre esta factura, contacte con ${esc(contactInfo)}</div>")
                append("</div>")
            }

            // End body
            append("</div>")

            // Footer
            append("<div class=\"footer\">")
            val footerParts = mutableListOf(esc(businessInfo.businessName))
            if (!businessInfo.nif.isNullOrBlank()) footerParts.add(esc(businessInfo.nif!!))
            if (!businessInfo.email.isNullOrBlank()) footerParts.add(esc(businessInfo.email!!))
            append("<span class=\"footer-text\">${footerParts.joinToString(" &nbsp;·&nbsp; ")}</span>")
            append("</div>")

            append("</div>")
            append("</body></html>")
        }
    }

    fun generateOrderSummaryHtml(sale: Sale, client: Client?, businessInfo: BusinessInfo): String {
        val subtotal = sale.totalPrice
        val ivaPct = client?.ivaPct
        val recargoPct = client?.recargoPct
        val ivaAmt = if (ivaPct != null) subtotal * ivaPct / 100.0 else 0.0
        val recargoAmt = if (recargoPct != null) subtotal * recargoPct / 100.0 else 0.0
        val total = subtotal + ivaAmt + recargoAmt

        val hasBankInfo = !businessInfo.bankName.isNullOrBlank() ||
                !businessInfo.iban.isNullOrBlank() ||
                !businessInfo.bankHolder.isNullOrBlank()

        return buildString {
            append("<!DOCTYPE html>")
            append("<html lang=\"es\"><head><meta charset=\"UTF-8\">")
            append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
            append("<title>Resumen ${esc(sale.id)}</title>")
            append("<style>")
            append(SUMMARY_CSS)
            append("</style></head><body>")
            append("<div class=\"summary\">")

            // Header
            append("<div class=\"header\">")
            append("<div class=\"logo-icon\">$LOGO_SVG</div>")
            append("<div class=\"header-text\">")
            append("<div class=\"title\">Resumen de pedido</div>")
            append("<div class=\"order-number\">${esc(sale.id)}</div>")
            append("</div>")
            append("</div>")

            // Info
            append("<div class=\"info\">")
            append("<div class=\"info-row\"><span class=\"info-label\">Fecha</span><span class=\"info-val\">${esc(sale.orderDate)}</span></div>")
            append("<div class=\"info-row\"><span class=\"info-label\">Cliente</span><span class=\"info-val\">${esc(sale.clientName)}</span></div>")
            append("</div>")

            // Products table
            append("<table><thead><tr>")
            append("<th style=\"width:46%\">Producto</th>")
            append("<th style=\"width:18%\">Precio ud.</th>")
            append("<th style=\"width:12%\">Uds.</th>")
            append("<th style=\"width:24%\">Total</th>")
            append("</tr></thead><tbody>")
            for (product in sale.products) {
                append("<tr>")
                append("<td class=\"desc\">${esc(product.name)}</td>")
                append("<td>${fmt(product.unitPrice)}</td>")
                append("<td class=\"qty\">${product.quantity}</td>")
                append("<td class=\"total-col\">${fmt(product.totalPrice)}</td>")
                append("</tr>")
            }
            append("</tbody></table>")

            // Totals
            append("<div class=\"totals\">")
            append("<div class=\"totals-row\"><span class=\"totals-label\">Subtotal</span><span class=\"totals-value\">${fmt(subtotal)}</span></div>")
            if (ivaPct != null) {
                append("<div class=\"totals-row\"><span class=\"totals-label\">IVA (${fmtPct(ivaPct)}%)</span><span class=\"totals-value\">${fmt(ivaAmt)}</span></div>")
            }
            if (recargoPct != null) {
                append("<div class=\"totals-row\"><span class=\"totals-label\">R. Equiv. (${fmtPct(recargoPct)}%)</span><span class=\"totals-value\">${fmt(recargoAmt)}</span></div>")
            }
            append("<div class=\"totals-row final\"><span class=\"totals-label\">Total</span><span class=\"totals-value\">${fmt(total)}</span></div>")
            append("</div>")

            // Payment footer
            append("<div class=\"payment-footer\">")
            append("<div class=\"payment-footer-title\">Formas de pago</div>")
            val methods = mutableListOf<String>()
            if (hasBankInfo) {
                val bankParts = mutableListOf<String>()
                bankParts.add("Transferencia")
                if (!businessInfo.iban.isNullOrBlank()) {
                    bankParts.add("IBAN: ${esc(businessInfo.iban!!)}")
                }
                if (!businessInfo.bankHolder.isNullOrBlank()) {
                    bankParts.add("Titular: ${esc(businessInfo.bankHolder!!)}")
                }
                bankParts.add("Concepto: ${esc(sale.id)}")
                methods.add(bankParts.joinToString(" &nbsp;·&nbsp; "))
            }
            methods.add("Tarjeta")
            methods.add("Efectivo")
            append("<div class=\"payment-footer-text\">${methods.joinToString(" &nbsp;&nbsp;|&nbsp;&nbsp; ")}</div>")
            append("</div>")

            append("</div>")
            append("</body></html>")
        }
    }

    private fun fmt(amount: Double): String {
        val rounded = ((amount * 100).toLong() / 100.0)
        val parts = rounded.toString().split(".")
        val decimals = if (parts.size > 1) parts[1].padEnd(2, '0').take(2) else "00"
        return "€${parts[0]},${decimals}"
    }

    private fun fmtPct(pct: Double): String {
        return if (pct == pct.toLong().toDouble()) {
            pct.toLong().toString()
        } else {
            pct.toString()
        }
    }

    private fun esc(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    private val LOGO_SVG = """<svg viewBox="0 0 3551 3551" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M2894.97 2904.99C2994.44 2821.7 3180.81 2896.38 3254.66 2789.71C3276.14 2758.69 3281.69 2718.21 3305.63 2689.03C3348.01 2637.35 3435.46 2643.84 3484.64 2689.1C3533.81 2734.37 3550.71 2805.44 3551 2872.27C3551.91 3089.61 3385.49 3296.16 3172.93 3341.5C3108.19 3355.3 3041.66 3345.76 2979.15 3324C2778.6 3254.2 2807.83 2977.95 2894.97 2904.99Z" fill="#ED6565"/><path d="M875.294 358.245C756.95 496.694 852.369 650.515 836.523 794.376C1028.18 795.328 1115.54 686.919 1084.82 492.16C1065.19 367.74 998.636 307.56 875.294 358.245Z" fill="#ED6565"/><path d="M2125.76 2807.6C2076.64 2867.22 2023.83 2884.82 1904.28 2884.82H1310.28V2381.64H1890.07C2087.15 2381.64 2185.68 2466.27 2185.68 2638.63C2185.68 2705.35 2164.68 2761.57 2125.76 2807.6ZM2027.22 1674.28C2027.22 1808.04 1932.39 1899.47 1798.64 1899.47H1310.28V1473.81H1777.33C1921.89 1473.81 2027.22 1558.14 2027.22 1674.28ZM2555.11 2209.28C2474.19 2138.85 2407.46 2103.64 2259.5 2061.32C2460.28 1938.38 2537.5 1808.04 2537.5 1593.36C2537.5 1417.6 2467.09 1262.54 2340.44 1153.49C2217.49 1047.85 2080.05 1005.85 1854.86 1005.85H1524.66C1521.88 843.372 1528.05 680.896 1501.18 519.659C1488.83 444.597 1471.53 379.418 1394.93 366.139C1317.08 352.547 1265.81 393.01 1237.08 474.562C1235.23 479.497 1231.84 483.832 1230.28 489.08C1166.97 716.723 1045.27 853.569 823.79 873.024C715.994 882.294 610.04 928.006 575.139 1079.98C560.608 1142.68 497.609 1155.35 442.001 1155.66C340.995 1155.97 239.989 1159.37 138.983 1156.59C25.9254 1153.19 -4.65322 1222.07 11.7197 1334.81C37.0496 1510.88 123.224 1635.67 264.705 1708.57C378.677 1767.26 492.348 1826.88 609.414 1876C643.713 1890.2 669.343 1908.11 687.871 1930.66L689.111 1929.74V2962.36C674.905 3243.76 798.147 3367 1079.54 3352.79H2023.83C2431.87 3352.79 2745.09 3050.39 2745.09 2656.25C2745.09 2473.08 2681.76 2321.72 2555.11 2209.28Z" fill="#ED6565"/></svg>"""

    private const val CSS = """
:root {
  --red: #ED6565;
  --light-blue: #C7D9E1;
  --black: #111225;
  --gray: #8A8890;
  --gray-light: #E8E6E1;
  --gray-bg: #F5F4F1;
  --white: #FFFFFF;
}
* { margin: 0; padding: 0; box-sizing: border-box; }
body {
  background: var(--white);
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Arial, sans-serif;
  color: var(--black);
  padding: 0;
  -webkit-print-color-adjust: exact;
  print-color-adjust: exact;
}
.invoice {
  width: 794px;
  margin: 0 auto;
  background: var(--white);
}
.header {
  background: var(--white);
  padding: 36px 48px 0;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.logo-icon { width: 52px; height: 52px; flex-shrink: 0; }
.invoice-id-block { text-align: right; }
.invoice-label {
  font-size: 9px; font-weight: 600; letter-spacing: 2.5px;
  text-transform: uppercase; color: var(--gray); margin-bottom: 4px;
}
.invoice-number {
  font-family: 'Courier New', monospace; font-size: 22px; font-weight: 500;
  color: var(--black); letter-spacing: 1px;
}
.invoice-accent-line {
  height: 3px; background: var(--red); width: 100%;
  margin-top: 6px; border-radius: 2px;
}
.header-rule {
  margin: 28px 48px 0; position: relative;
  height: 1px; background: var(--gray-light);
}
.header-rule::before {
  content: ''; position: absolute; left: 0; top: -3px;
  width: 36px; height: 7px; background: var(--red);
  border-radius: 0 3px 3px 0;
}
.date-row {
  padding: 18px 48px; display: flex; gap: 0;
  border-bottom: 1px solid var(--gray-light);
}
.date-item {
  display: flex; flex-direction: column; gap: 2px;
  padding-right: 32px; margin-right: 32px;
  border-right: 1px solid var(--gray-light);
}
.date-item:last-child { border-right: none; padding-right: 0; margin-right: 0; }
.date-key {
  font-size: 8px; font-weight: 700; letter-spacing: 1.5px;
  text-transform: uppercase; color: var(--gray);
}
.date-val {
  font-family: 'Courier New', monospace; font-size: 11.5px;
  color: var(--black); letter-spacing: 0.3px;
}
.body { padding: 32px 48px 40px; }
.parties {
  display: grid; grid-template-columns: 1fr 1fr;
  gap: 20px; margin-bottom: 36px;
}
.party-block {
  background: var(--gray-bg); border-radius: 4px;
  padding: 18px 20px; border-left: 3px solid var(--red);
}
.party-block.client { border-left-color: var(--light-blue); }
.party-title {
  font-size: 8px; font-weight: 700; letter-spacing: 2px;
  text-transform: uppercase; color: var(--red); margin-bottom: 10px;
}
.party-block.client .party-title { color: #6899A8; }
.party-name {
  font-size: 14px; font-weight: 600; color: var(--black);
  margin-bottom: 10px; line-height: 1.25;
}
.party-row { display: flex; gap: 8px; margin-bottom: 4px; align-items: baseline; }
.party-row-label {
  font-size: 7.5px; font-weight: 700; letter-spacing: 1px;
  text-transform: uppercase; color: var(--gray);
  min-width: 58px; flex-shrink: 0;
}
.party-row-val { font-size: 10px; color: var(--black); line-height: 1.5; }
.table-section { margin-bottom: 32px; }
.table-label {
  font-size: 8px; font-weight: 700; letter-spacing: 2px;
  text-transform: uppercase; color: var(--gray); margin-bottom: 8px;
}
table { width: 100%; border-collapse: collapse; }
thead tr { background: var(--black); }
thead th {
  font-size: 7.5px; font-weight: 600; letter-spacing: 1.5px;
  text-transform: uppercase; color: #C8C6C0; padding: 10px 12px; text-align: left;
}
thead th:not(:first-child) { text-align: right; }
tbody tr { border-bottom: 1px solid var(--gray-light); }
tbody tr:nth-child(even) { background: var(--gray-bg); }
tbody td {
  padding: 11px 12px; font-size: 10.5px;
  color: var(--black); vertical-align: middle;
}
tbody td.desc { line-height: 1.4; }
tbody td:not(.desc) {
  text-align: right; font-family: 'Courier New', monospace; font-size: 10px;
}
tbody td.total-col { font-weight: 600; }
.bottom-grid {
  display: grid; grid-template-columns: 1fr auto;
  gap: 24px; align-items: start;
}
.payment-block {
  background: var(--gray-bg); border-radius: 4px;
  padding: 18px 20px; border-left: 3px solid var(--light-blue);
}
.payment-title {
  font-size: 8px; font-weight: 700; letter-spacing: 2px;
  text-transform: uppercase; color: #6899A8; margin-bottom: 12px;
}
.payment-row { display: flex; gap: 8px; margin-bottom: 5px; align-items: baseline; }
.payment-label {
  font-size: 7.5px; font-weight: 700; letter-spacing: 1px;
  text-transform: uppercase; color: var(--gray);
  min-width: 58px; flex-shrink: 0;
}
.payment-val {
  font-family: 'Courier New', monospace; font-size: 10px;
  color: var(--black); letter-spacing: 0.3px;
}
.payment-alt {
  margin-top: 10px; padding-top: 8px;
  border-top: 1px solid var(--gray-light);
  font-size: 8.5px; color: var(--gray); line-height: 1.5;
  font-style: italic;
}
.payment-alt-only {
  font-size: 9.5px; color: var(--gray); line-height: 1.6;
}
.totals { min-width: 220px; }
.totals-row {
  display: flex; justify-content: space-between; align-items: baseline;
  padding: 8px 0; border-bottom: 1px solid var(--gray-light); gap: 40px;
}
.totals-row:last-child { border-bottom: none; }
.totals-label { font-size: 10px; color: var(--gray); }
.totals-value {
  font-family: 'Courier New', monospace; font-size: 10.5px; color: var(--black);
}
.totals-row.final {
  margin-top: 6px; padding: 11px 14px; background: var(--black);
  border-radius: 4px; border-bottom: none;
}
.totals-row.final .totals-label {
  font-size: 10px; font-weight: 700; color: #B0AEB8;
  letter-spacing: 1px; text-transform: uppercase;
}
.totals-row.final .totals-value {
  font-size: 15px; font-weight: 700; color: var(--red);
}
.notes-section {
  margin-top: 28px; padding-top: 20px;
  border-top: 1px solid var(--gray-light);
}
.notes-title {
  font-size: 8px; font-weight: 700; letter-spacing: 2px;
  text-transform: uppercase; color: var(--gray); margin-bottom: 6px;
}
.notes-text {
  font-size: 9.5px; color: var(--gray); line-height: 1.65; max-width: 520px;
}
.footer {
  padding: 16px 48px 36px;
  border-top: 1px solid var(--gray-light);
  display: flex; justify-content: space-between;
  align-items: center; position: relative;
}
.footer::before {
  content: ''; position: absolute; top: -1px; left: 48px;
  width: 28px; height: 3px; background: var(--red);
  border-radius: 0 2px 2px 0;
}
.footer-text {
  font-size: 8.5px; color: var(--gray); letter-spacing: 0.3px;
}
@media print {
  body { background: none; padding: 0; }
  .invoice { box-shadow: none; width: 100%; }
}
"""

    private const val SUMMARY_CSS = """
:root {
  --red: #ED6565;
  --black: #111225;
  --gray: #8A8890;
  --gray-light: #E8E6E1;
  --gray-bg: #F5F4F1;
  --white: #FFFFFF;
}
* { margin: 0; padding: 0; box-sizing: border-box; }
body {
  background: var(--white);
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Arial, sans-serif;
  color: var(--black);
  padding: 0;
  -webkit-print-color-adjust: exact;
  print-color-adjust: exact;
}
.summary {
  max-width: 600px;
  margin: 0 auto;
  padding: 24px;
  background: var(--white);
}
.header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid var(--red);
}
.logo-icon { width: 44px; height: 44px; flex-shrink: 0; }
.header-text { flex: 1; }
.title {
  font-size: 18px; font-weight: 700;
  color: var(--black); letter-spacing: 0.3px;
}
.order-number {
  font-family: 'Courier New', monospace; font-size: 14px;
  color: var(--gray); margin-top: 2px;
}
.info {
  margin-bottom: 20px; padding: 14px 16px;
  background: var(--gray-bg); border-radius: 8px;
}
.info-row {
  display: flex; justify-content: space-between;
  align-items: baseline; padding: 4px 0;
}
.info-label {
  font-size: 12px; font-weight: 600; letter-spacing: 0.5px;
  text-transform: uppercase; color: var(--gray);
}
.info-val {
  font-size: 14px; font-weight: 500; color: var(--black);
}
table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
thead tr { background: var(--black); }
thead th {
  font-size: 10px; font-weight: 600; letter-spacing: 1px;
  text-transform: uppercase; color: #C8C6C0;
  padding: 10px 12px; text-align: left;
}
thead th:not(:first-child) { text-align: right; }
tbody tr { border-bottom: 1px solid var(--gray-light); }
tbody tr:nth-child(even) { background: var(--gray-bg); }
tbody td {
  padding: 10px 12px; font-size: 13px;
  color: var(--black); vertical-align: middle;
}
tbody td.desc { font-weight: 500; }
tbody td:not(.desc) {
  text-align: right; font-family: 'Courier New', monospace; font-size: 12px;
}
tbody td.qty { text-align: center; }
tbody td.total-col { font-weight: 600; }
.totals { margin-top: 4px; }
.totals-row {
  display: flex; justify-content: space-between;
  align-items: baseline; padding: 8px 0;
  border-bottom: 1px solid var(--gray-light); gap: 40px;
}
.totals-row:last-child { border-bottom: none; }
.totals-label { font-size: 13px; color: var(--gray); }
.totals-value {
  font-family: 'Courier New', monospace; font-size: 14px; color: var(--black);
}
.totals-row.final {
  margin-top: 6px; padding: 12px 16px;
  background: var(--black); border-radius: 8px; border-bottom: none;
}
.totals-row.final .totals-label {
  font-size: 13px; font-weight: 700; color: #B0AEB8;
  letter-spacing: 1px; text-transform: uppercase;
}
.totals-row.final .totals-value {
  font-size: 18px; font-weight: 700; color: var(--red);
}
.payment-footer {
  margin-top: 24px; padding-top: 16px;
  border-top: 1px solid var(--gray-light);
}
.payment-footer-title {
  font-size: 9px; font-weight: 700; letter-spacing: 1.5px;
  text-transform: uppercase; color: var(--gray); margin-bottom: 6px;
}
.payment-footer-text {
  font-size: 11px; color: var(--gray); line-height: 1.6;
}
"""
}
