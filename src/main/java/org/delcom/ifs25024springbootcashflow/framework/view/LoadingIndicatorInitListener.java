package org.delcom.ifs25024springbootcashflow.framework.view;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

/**
 * Mengatur indikator loading global Vaadin (muncul setelah 150 ms, berubah setelah 600 ms
 * dan 1500 ms) serta melengkapi halaman awal (bootstrap) agar tidak kosong selama Vaadin
 * belum selesai menggambar tampilan.
 */
@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {

    private static final String DEFAULT_TITLE = "Cashflow";

    /** Kerangka awal: judul, landmark main, dan h1 sudah ada sebelum JavaScript Vaadin selesai. */
    private static final String BOOT_SHELL = """
            <main id="boot-shell" style="position:fixed;inset:0;z-index:2147483000;background:#fff;color:#1b2a3a;\
            font-family:system-ui,sans-serif;padding:2rem;">\
            <h1 style="margin:0 0 .5rem;font-size:1.75rem;">Cashflow</h1>\
            <p style="margin:0;">Memuat aplikasi...</p></main>""";

    /**
     * Berjalan segera setelah halaman dimuat: menandai indikator koneksi sebagai live region
     * (status) dan membuang kerangka awal begitu tampilan sebenarnya (role=main) muncul.
     */
    private static final String BOOT_SCRIPT = """
            (function () {
                var shellRemoved = false;
                var scheduled = false;
                var observer = null;
                var finishShell = function () {
                    shellRemoved = true;
                    observer.disconnect();
                    observer.observe(document.body, { childList: true });
                };
                var run = function () {
                    scheduled = false;
                    document.querySelectorAll('vaadin-connection-indicator').forEach(function (el) {
                        if (el.getAttribute('role') !== 'status') {
                            el.setAttribute('role', 'status');
                            el.setAttribute('aria-live', 'polite');
                        }
                    });
                    if (!shellRemoved) {
                        var shell = document.getElementById('boot-shell');
                        if (!shell) {
                            finishShell();
                        } else if (document.querySelector('[role="main"]:not(#boot-shell)')) {
                            shell.remove();
                            finishShell();
                        }
                    }
                };
                observer = new MutationObserver(function () {
                    if (!scheduled) {
                        scheduled = true;
                        requestAnimationFrame(run);
                    }
                });
                observer.observe(document.body, { childList: true, subtree: true });
                setTimeout(function () {
                    var shell = document.getElementById('boot-shell');
                    if (shell) { shell.remove(); }
                    if (!shellRemoved) { finishShell(); }
                }, 15000);
                run();
            })();
            """;

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addIndexHtmlRequestListener(response -> {
            Document document = response.getDocument();
            if (document.title().isBlank()) {
                document.title(DEFAULT_TITLE);
            }
            document.body().prepend(BOOT_SHELL);
            document.body().appendElement("script").appendChild(new DataNode(BOOT_SCRIPT));
        });

        event.getSource().addUIInitListener(uiEvent -> {
            var config = uiEvent.getUI().getLoadingIndicatorConfiguration();
            config.setFirstDelay(150);
            config.setSecondDelay(600);
            config.setThirdDelay(1500);
        });
    }
}
