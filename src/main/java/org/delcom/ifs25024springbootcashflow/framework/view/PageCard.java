package org.delcom.ifs25024springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/** Kartu konten halaman: judul dan isi dalam satu panel putih. */
public class PageCard extends VerticalLayout {

    public PageCard(String title, Component... content) {
        addClassName("page-card");
        setPadding(true);
        setSpacing(true);
        add(new H2(title));
        add(content);
    }
}
