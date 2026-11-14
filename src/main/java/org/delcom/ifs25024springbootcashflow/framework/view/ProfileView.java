package org.delcom.ifs25024springbootcashflow.framework.view;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
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
import org.delcom.ifs25024springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25024springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25024springbootcashflow.domain.entity.User;
import org.delcom.ifs25024springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25024springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25024springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25024springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25024springbootcashflow.usecase.UserUseCase;

import java.io.IOException;
import java.util.Optional;

/** Pengaturan profil: ubah nama, password, dan foto. */
@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil | Cashflow")
public class ProfileView extends VerticalLayout implements BeforeEnterObserver {

    private final UserUseCase userUseCase;
    private final IUserRepository userRepository;
    private final FileStorage fileStorage;
    private final FinancePresenter presenter;

    private final TextField username = new TextField("Username");
    private final TextField fullName = new TextField("Nama lengkap");
    private final PasswordField newPassword = new PasswordField("Password baru (kosongkan bila tidak diubah)");
    private final UploadField photoUpload = new UploadField("Foto profil (gambar maks. 2 MB)");
    private final Avatar avatar = new Avatar();
    private final ProgressBar progress = new ProgressBar();
    private final Button saveButton = new Button("Simpan Profil", VaadinIcon.CHECK.create());

    private User currentUser;

    public ProfileView(UserUseCase userUseCase, IUserRepository userRepository,
                       FileStorage fileStorage, FinancePresenter presenter) {
        this.userUseCase = userUseCase;
        this.userRepository = userRepository;
        this.fileStorage = fileStorage;
        this.presenter = presenter;

        username.setReadOnly(true);
        username.setWidthFull();
        fullName.setWidthFull();
        newPassword.setWidthFull();
        avatar.setHeight("80px");
        avatar.setWidth("80px");

        progress.setIndeterminate(true);
        progress.setVisible(false);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(event -> save());

        PageCard card = new PageCard("Profil Saya", avatar, username, fullName, newPassword,
                photoUpload, progress, saveButton);
        card.setMaxWidth("560px");
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<User> user = AuthSession.currentUser(userRepository);
        if (user.isEmpty()) {
            event.forwardTo(LoginView.class);
            return;
        }
        currentUser = user.get();
        fill();
    }

    private void fill() {
        username.setValue(currentUser.getUsername());
        fullName.setValue(currentUser.getFullName());
        newPassword.clear();
        avatar.setName(currentUser.getFullName());
        String photoUrl = presenter.uploadUrl(currentUser.getPhotoPath());
        if (photoUrl != null) {
            avatar.setImage(photoUrl);
        }
    }

    private void save() {
        progress.setVisible(true);
        saveButton.setEnabled(false);
        try {
            currentUser = userUseCase.updateProfile(currentUser.getId(), fullName.getValue(), newPassword.getValue());
            String photoPath = photoUpload.storeIn(fileStorage);
            if (photoPath != null) {
                currentUser = userUseCase.updatePhoto(currentUser.getId(), photoPath);
            }
            fill();
            Notification.show("Profil berhasil disimpan. Muat ulang halaman untuk memperbarui avatar di sidebar.",
                            4000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (ValidationException | EntityNotFoundException ex) {
            error(ex.getMessage());
        } catch (IOException ex) {
            error("Gagal menyimpan foto: " + ex.getMessage());
        } finally {
            progress.setVisible(false);
            saveButton.setEnabled(true);
        }
    }

    private void error(String message) {
        Notification.show("[!] " + message, 4000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
