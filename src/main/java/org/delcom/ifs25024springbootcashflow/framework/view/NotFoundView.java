package org.delcom.ifs25024springbootcashflow.framework.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.HttpStatusCode;

/**
 * Halaman 404 bawaan Vaadin tidak punya judul, landmark main, maupun h1.
 * View ini menggantikannya supaya alamat yang tidak dikenal tetap lolos audit aksesibilitas.
 */
@PageTitle("Halaman Tidak Ditemukan | Cashflow")
public class NotFoundView extends VerticalLayout implements HasErrorParameter<NotFoundException> {

    public NotFoundView() {
        getElement().setAttribute("role", "main");
        setPadding(true);
        add(new H1("Halaman tidak ditemukan"),
                new Paragraph("Alamat yang Anda buka tidak tersedia."),
                new Paragraph(new RouterLink("Kembali ke beranda", FinanceView.class)));
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        return HttpStatusCode.NOT_FOUND.getCode();
    }
}
