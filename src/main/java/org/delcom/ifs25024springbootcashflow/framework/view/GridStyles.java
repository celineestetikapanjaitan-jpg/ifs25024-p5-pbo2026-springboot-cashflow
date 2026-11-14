package org.delcom.ifs25024springbootcashflow.framework.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

/** Gaya grid yang seragam di seluruh halaman. */
public final class GridStyles {

    private GridStyles() {
    }

    public static void apply(Grid<?> grid) {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setHeight("460px");
    }
}
