import SwiftUI
import WebKit

struct InvoiceView: View {
    let invoiceHtml: String
    let saleId: String
    var documentName: String = "Factura"

    @State private var webView: WKWebView?

    var body: some View {
        InvoiceWebView(html: invoiceHtml, webView: $webView)
            .navigationTitle("\(documentName) #\(saleId)")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button(action: sharePdf) {
                        Image(systemName: "square.and.arrow.up")
                    }
                }
            }
    }

    private func sharePdf() {
        guard let webView else { return }

        webView.createPDF { result in
            switch result {
            case .success(let data):
                let tempUrl = FileManager.default.temporaryDirectory
                    .appendingPathComponent("\(documentName)_\(saleId).pdf")
                try? data.write(to: tempUrl)

                DispatchQueue.main.async {
                    let activityVC = UIActivityViewController(
                        activityItems: [tempUrl],
                        applicationActivities: nil
                    )

                    if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                       let rootVC = windowScene.windows.first?.rootViewController {
                        var topVC = rootVC
                        while let presented = topVC.presentedViewController {
                            topVC = presented
                        }
                        if let popover = activityVC.popoverPresentationController {
                            popover.sourceView = topVC.view
                            popover.sourceRect = CGRect(
                                x: topVC.view.bounds.midX,
                                y: topVC.view.bounds.midY,
                                width: 0,
                                height: 0
                            )
                        }
                        topVC.present(activityVC, animated: true)
                    }
                }

            case .failure:
                break
            }
        }
    }
}

private struct InvoiceWebView: UIViewRepresentable {
    let html: String
    @Binding var webView: WKWebView?

    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        let wv = WKWebView(frame: .zero, configuration: config)
        wv.isOpaque = false
        wv.backgroundColor = .white
        wv.scrollView.minimumZoomScale = 0.1
        wv.scrollView.maximumZoomScale = 5.0
        wv.scrollView.bouncesZoom = true
        DispatchQueue.main.async {
            self.webView = wv
        }
        wv.loadHTMLString(html, baseURL: nil)
        return wv
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {}
}
