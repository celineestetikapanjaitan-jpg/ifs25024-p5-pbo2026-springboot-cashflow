package org.delcom.ifs25024springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import org.delcom.ifs25024springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25024springbootcashflow.domain.entity.Role;
import org.delcom.ifs25024springbootcashflow.domain.entity.User;
import org.delcom.ifs25024springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25024springbootcashflow.framework.auth.AuthSession;

import java.util.Optional;

/**
 * Layout utama untuk semua halaman yang wajib login: sidebar, menu avatar (Profil dan Keluar),
 * dan pemeriksaan login global. Bila sesi kosong, navigasi dialihkan ke {@link LoginView}.
 */
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final IUserRepository userRepository;
    private final FinancePresenter presenter;

    private final SideNav sideNav = new SideNav();
    private final Div userMenuHost = new Div();

    public MainLayout(IUserRepository userRepository, FinancePresenter presenter) {
        this.userRepository = userRepository;
        this.presenter = presenter;

        setPrimarySection(Section.DRAWER);

        H1 title = new H1("Cashflow");
        title.addClassName("app-title");

        HorizontalLayout navbar = new HorizontalLayout(new DrawerToggle(), title, userMenuHost);
        navbar.setWidthFull();
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);
        navbar.expand(title);
        navbar.addClassName("app-navbar");
        navbar.getElement().setAttribute("role", "banner");

        addToNavbar(navbar);
        addToDrawer(sideNav);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<User> user = AuthSession.currentUser(userRepository);
        if (user.isEmpty()) {
            event.forwardTo(LoginView.class);
            return;
        }
        render(user.get());
    }

    private void render(User user) {
        sideNav.removeAll();
        sideNav.addItem(new SideNavItem("Transaksi", FinanceView.class, VaadinIcon.WALLET.create()));
        sideNav.addItem(new SideNavItem("Profil", ProfileView.class, VaadinIcon.USER.create()));
        if (user.getRole() == Role.ADMIN) {
            sideNav.addItem(new SideNavItem("Pengguna", UsersView.class, VaadinIcon.USERS.create()));
        }

        userMenuHost.removeAll();
        userMenuHost.add(buildUserMenu(user));
    }

    private MenuBar buildUserMenu(User user) {
        Avatar avatar = new Avatar(user.getFullName());
        String photoUrl = presenter.uploadUrl(user.getPhotoPath());
        if (photoUrl != null) {
            avatar.setImage(photoUrl);
        }

        HorizontalLayout trigger = new HorizontalLayout(avatar, new Span(user.getFullName()));
        trigger.setAlignItems(FlexComponent.Alignment.CENTER);

        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        var root = menuBar.addItem(trigger);
        root.getSubMenu().addItem("Profil", event -> UI.getCurrent().navigate(ProfileView.class));
        root.getSubMenu().addItem("Keluar", event -> logout());
        return menuBar;
    }

    private void logout() {
        AuthSession.logout();
        UI.getCurrent().getPage().setLocation("/login");
    }
}
