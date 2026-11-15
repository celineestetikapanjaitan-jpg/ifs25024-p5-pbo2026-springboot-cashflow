package org.delcom.ifs25024springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25024springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25024springbootcashflow.domain.entity.Role;
import org.delcom.ifs25024springbootcashflow.domain.entity.User;
import org.delcom.ifs25024springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25024springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25024springbootcashflow.usecase.UserUseCase;

import java.util.Map;
import java.util.Optional;

/** Daftar semua pengguna (khusus ADMIN). USER biasa dialihkan ke halaman utama. */
@Route(value = "users", layout = MainLayout.class)
@PageTitle("Pengguna | Cashflow")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {

    private final UserUseCase userUseCase;
    private final IUserRepository userRepository;
    private final FinancePresenter presenter;
    private final Grid<User> grid = new Grid<>(User.class, false);

    public UsersView(UserUseCase userUseCase, IUserRepository userRepository, FinancePresenter presenter) {
        this.userUseCase = userUseCase;
        this.userRepository = userRepository;
        this.presenter = presenter;
        getElement().setAttribute("role", "main");

        grid.addColumn(User::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(User::getUsername).setHeader("Username").setSortable(true);
        grid.addColumn(User::getFullName).setHeader("Nama lengkap").setSortable(true).setFlexGrow(1);
        grid.addColumn(user -> presenter.formatRole(user.getRole())).setHeader("Peran").setAutoWidth(true);
        grid.addComponentColumn(user -> {
            Button open = new Button("Lihat Transaksi", VaadinIcon.EYE.create(), event ->
                    UI.getCurrent().navigate("user-transactions",
                            QueryParameters.simple(Map.of("userId", String.valueOf(user.getId())))));
            open.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            return open;
        }).setHeader("Tindakan").setAutoWidth(true).setFlexGrow(0);
        GridStyles.apply(grid);

        add(new PageCard("Daftar Pengguna", grid));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<User> user = AuthSession.currentUser(userRepository);
        if (user.isEmpty()) {
            event.forwardTo(LoginView.class);
        } else if (user.get().getRole() != Role.ADMIN) {
            event.forwardTo(FinanceView.class);
        } else {
            grid.setItems(userUseCase.getAllUsers());
        }
    }
}
