package org.delcom.ifs25024springbootcashflow.framework.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25024springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25024springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25024springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25024springbootcashflow.usecase.AuthUseCase;

/** Halaman registrasi (publik). Jika sudah login, langsung dialihkan ke halaman utama. */
@Route("register")
@PageTitle("Daftar | Cashflow")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthUseCase authUseCase;
    private final IUserRepository userRepository;

    private final TextField fullName = new TextField("Nama lengkap");
    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final PasswordField confirm = new PasswordField("Ulangi password");
    private final Button registerButton = new Button("Daftar");
    private final ProgressBar progress = new ProgressBar();

    public RegisterView(AuthUseCase authUseCase, IUserRepository userRepository) {
        this.authUseCase = authUseCase;
        this.userRepository = userRepository;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        fullName.setId("register-fullname");
        username.setId("register-username");
        password.setId("register-password");
        confirm.setId("register-confirm");
        registerButton.setId("register-submit");
        fullName.setWidthFull();
        username.setWidthFull();
        password.setWidthFull();
        confirm.setWidthFull();

        registerButton.setWidthFull();
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickShortcut(Key.ENTER);
        registerButton.addClickListener(event -> submit());

        progress.setIndeterminate(true);
        progress.setVisible(false);

        Div card = new Div(
                new H2("Buat akun Cashflow"),
                fullName, username, password, confirm, progress, registerButton,
                new Paragraph(new RouterLink("Sudah punya akun? Masuk", LoginView.class)));
        card.addClassName("auth-card");
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository).isPresent()) {
            event.forwardTo(FinanceView.class);
        }
    }

    private void submit() {
        if (!password.getValue().equals(confirm.getValue())) {
            showError("Konfirmasi password tidak sama");
            return;
        }
        progress.setVisible(true);
        registerButton.setEnabled(false);
        try {
            authUseCase.register(username.getValue(), password.getValue(), fullName.getValue());
            Notification.show("Registrasi berhasil. Silakan masuk.", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate(LoginView.class);
        } catch (ValidationException ex) {
            showError(ex.getMessage());
        } finally {
            progress.setVisible(false);
            registerButton.setEnabled(true);
        }
    }

    private void showError(String message) {
        Notification.show("[!] " + message, 4000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
