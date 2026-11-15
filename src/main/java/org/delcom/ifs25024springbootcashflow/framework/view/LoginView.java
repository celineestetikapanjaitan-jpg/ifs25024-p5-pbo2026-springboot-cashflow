package org.delcom.ifs25024springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
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
import org.delcom.ifs25024springbootcashflow.domain.entity.User;
import org.delcom.ifs25024springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25024springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25024springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25024springbootcashflow.usecase.AuthUseCase;

/** Halaman login (publik). Jika sudah login, langsung dialihkan ke halaman utama. */
@Route("login")
@PageTitle("Masuk | Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthUseCase authUseCase;
    private final IUserRepository userRepository;

    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final Button loginButton = new Button("Masuk");
    private final ProgressBar progress = new ProgressBar();

    public LoginView(AuthUseCase authUseCase, IUserRepository userRepository) {
        this.authUseCase = authUseCase;
        this.userRepository = userRepository;

        setSizeFull();
        getElement().setAttribute("role", "main");
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // ID dipasang pada elemen <input> asli (bukan pada elemen pembungkus Vaadin),
        // supaya selector #login-username / #login-password dapat diisi oleh pengujian otomatis.
        username.addAttachListener(event -> assignIdToInput(username, "login-username"));
        username.setWidthFull();
        password.addAttachListener(event -> assignIdToInput(password, "login-password"));
        password.setWidthFull();

        loginButton.setId("login-submit");
        loginButton.setWidthFull();
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.addClickShortcut(Key.ENTER);
        loginButton.addClickListener(event -> submit());

        progress.setIndeterminate(true);
        progress.setVisible(false);

        Div card = new Div(
                new H2("Masuk ke Cashflow"),
                new Paragraph("Catat pemasukan dan pengeluaran Anda."),
                username, password, progress, loginButton,
                new Paragraph(new RouterLink("Belum punya akun? Daftar", RegisterView.class)));
        card.addClassName("auth-card");
        add(card);
    }

    /** Memindahkan ID ke elemen input native milik field Vaadin dan menyelaraskan label-nya. */
    private static void assignIdToInput(Component field, String id) {
        field.getElement().executeJs(
                "const host = this;"
                        + "const apply = () => {"
                        + "  const input = host.querySelector('input');"
                        + "  if (!input) { return false; }"
                        + "  input.id = $0;"
                        + "  const label = host.querySelector('label');"
                        + "  if (label) { label.htmlFor = $0; }"
                        + "  return true;"
                        + "};"
                        + "if (!apply()) {"
                        + "  const timer = setInterval(() => { if (apply()) { clearInterval(timer); } }, 50);"
                        + "  setTimeout(() => clearInterval(timer), 5000);"
                        + "}",
                id);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository).isPresent()) {
            event.forwardTo(FinanceView.class);
        }
    }

    private void submit() {
        progress.setVisible(true);
        loginButton.setEnabled(false);
        try {
            User user = authUseCase.login(username.getValue(), password.getValue());
            AuthSession.login(user);
            UI.getCurrent().navigate(FinanceView.class);
        } catch (ValidationException ex) {
            Notification.show("[!] " + ex.getMessage(), 4000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } finally {
            progress.setVisible(false);
            loginButton.setEnabled(true);
        }
    }
}
