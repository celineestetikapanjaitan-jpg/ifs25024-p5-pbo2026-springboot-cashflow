package org.delcom.ifs25024springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Modal pratinjau gambar. Klik gambar untuk beralih zoom 1,0x dan 1,8x,
 * atau pakai tombol Perkecil dan Perbesar di footer kanan.
 */
public class ImagePreviewDialog extends Dialog {

    private static final double MIN_SCALE = 0.6;
    private static final double MAX_SCALE = 3.0;
    private static final double STEP = 0.4;
    private static final double ZOOMED_SCALE = 1.8;

    private final Image image;
    private double scale = 1.0;

    public ImagePreviewDialog(String url, String title) {
        setHeaderTitle(title);
        setWidth("720px");
        setMaxWidth("95vw");

        image = new Image(url, title);
        image.getStyle().set("max-width", "none").set("cursor", "zoom-in");
        image.addClickListener(event -> applyScale(scale > 1.0 ? 1.0 : ZOOMED_SCALE));
        applyScale(1.0);

        Div viewport = new Div(image);
        viewport.getStyle().set("overflow", "auto").set("max-height", "65vh").set("text-align", "center");
        add(viewport);

        Button zoomOut = new Button("Perkecil", VaadinIcon.SEARCH_MINUS.create(),
                event -> applyScale(Math.max(MIN_SCALE, scale - STEP)));
        Button zoomIn = new Button("Perbesar", VaadinIcon.SEARCH_PLUS.create(),
                event -> applyScale(Math.min(MAX_SCALE, scale + STEP)));
        Button close = new Button("Tutup", VaadinIcon.CLOSE.create(), event -> close());
        getFooter().add(zoomOut, zoomIn, close);
    }

    private void applyScale(double newScale) {
        scale = newScale;
        image.getStyle().set("width", Math.round(newScale * 100) + "%");
        image.getStyle().set("cursor", newScale > 1.0 ? "zoom-out" : "zoom-in");
    }

    /** Thumbnail kecil yang membuka pratinjau saat diklik. */
    public static Component thumbnail(String url, String title) {
        Image thumbnail = new Image(url, title);
        thumbnail.setHeight("40px");
        thumbnail.getStyle().set("cursor", "zoom-in").set("border-radius", "6px");
        thumbnail.addClickListener(event -> new ImagePreviewDialog(url, title).open());
        return thumbnail;
    }
}
