package org.delcom.ifs25024springbootcashflow.framework.view;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/**
 * Mengatur indikator loading global Vaadin agar responsif:
 * muncul setelah 150 ms, berubah setelah 600 ms, dan 1500 ms.
 */
@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {

    private static final String MARK_CONNECTION_INDICATOR = """
            const mark = () => document.querySelectorAll('vaadin-connection-indicator').forEach(el => {
                el.setAttribute('role', 'status');
                el.setAttribute('aria-live', 'polite');
            });
            mark();
            new MutationObserver(mark).observe(document.body, { childList: true });
            """;

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            var config = uiEvent.getUI().getLoadingIndicatorConfiguration();
            config.setFirstDelay(150);
            config.setSecondDelay(600);
            config.setThirdDelay(1500);

            // Indikator koneksi bawaan Vaadin ditandai sebagai live region (status) agar
            // tidak dianggap konten di luar landmark oleh pemeriksa aksesibilitas.
            uiEvent.getUI().getPage().executeJs(MARK_CONNECTION_INDICATOR);
        });
    }
}
