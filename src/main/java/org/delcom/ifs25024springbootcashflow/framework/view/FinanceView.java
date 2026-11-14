package org.delcom.ifs25024springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25024springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25024springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25024springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25024springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25024springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25024springbootcashflow.domain.entity.User;
import org.delcom.ifs25024springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25024springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25024springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25024springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25024springbootcashflow.usecase.FinanceUseCase;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Halaman utama catatan keuangan milik user yang sedang login.
 * View hanya memanggil use case dan presenter; tidak ada aturan bisnis di sini.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Transaksi | Cashflow")
public class FinanceView extends VerticalLayout implements BeforeEnterObserver {

    private static final String EMPTY_MESSAGE = "Belum ada transaksi!";

    private final FinanceUseCase useCase;
    private final FinancePresenter presenter;
    private final IUserRepository userRepository;
    private final FileStorage fileStorage;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<SortOption> sortBox = new ComboBox<>();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span(EMPTY_MESSAGE);
    private final Span totalCount = new Span("0");
    private final Span incomeTotal = new Span();
    private final Span expenseTotal = new Span();
    private final Span balanceLabel = new Span();

    /** User yang sedang login; diisi pada {@link #beforeEnter(BeforeEnterEvent)}. */
    private User currentUser;

    public FinanceView(FinanceUseCase useCase, FinancePresenter presenter,
                       IUserRepository userRepository, FileStorage fileStorage) {
        this.useCase = useCase;
        this.presenter = presenter;
        this.userRepository = userRepository;
        this.fileStorage = fileStorage;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        incomeTotal.setId("income-total");
        expenseTotal.setId("expense-total");
        balanceLabel.setId("balance");

        configureGrid();
        configureToolbar();

        GridStyles.apply(grid);
        add(new H2("Catatan Keuangan"), buildSummaryCards(), buildToolbar(), statusLabel, grid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<User> user = AuthSession.currentUser(userRepository);
        if (user.isEmpty()) {
            event.forwardTo(LoginView.class);
            return;
        }
        currentUser = user.get();
        refreshAll();
    }

    // ---------------------------------------------------------------- ringkasan

    private HorizontalLayout buildSummaryCards() {
        HorizontalLayout summary = new HorizontalLayout(
                createSummaryCard("Total Transaksi", totalCount, "#E0E7FF", "#4338CA"),
                createSummaryCard("Total Pemasukan", incomeTotal, "#DCFCE7", "#16A34A"),
                createSummaryCard("Total Pengeluaran", expenseTotal, "#FEE2E2", "#DC2626"),
                createSummaryCard("Saldo", balanceLabel, "#DBEAFE", "#1D4ED8")
        );
        summary.setWidthFull();
        summary.getStyle().set("align-items", "stretch").set("flex-wrap", "wrap");
        return summary;
    }

    private Div createSummaryCard(String label, Span value, String background, String accent) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.875rem")
                .set("color", accent);

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.35rem")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("color", accent);

        Div card = new Div(caption, value);
        card.getStyle()
                .set("flex", "1")
                .set("min-width", "200px")
                .set("background", background)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem");
        return card;
    }

    private void updateSummary() {
        int userId = currentUser.getId();
        totalCount.setText(String.valueOf(useCase.getTransactions(userId).size()));
        incomeTotal.setText(presenter.formatMoney(useCase.getTotalIncome(userId)));
        expenseTotal.setText(presenter.formatMoney(useCase.getTotalExpense(userId)));
        balanceLabel.setText(presenter.formatMoney(useCase.getBalance(userId)));
    }

    // --------------------------------------------------------------------- grid

    private void configureGrid() {
        grid.addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Comparator.comparingInt(Transaction::getId));

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        grid.addColumn(transaction -> presenter.formatMoney(transaction.getAmount()))
                .setHeader("Jumlah")
                .setKey("amount")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparingLong(Transaction::getAmount));

        grid.addColumn(transaction -> presenter.formatType(transaction.getType()))
                .setHeader("Tipe")
                .setKey("type")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getType));

        grid.addComponentColumn(this::createProof)
                .setHeader("Bukti")
                .setKey("proof")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.addColumn(presenter::formatCreatedAt)
                .setHeader("Waktu")
                .setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Tindakan")
                .setKey("actions")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.setEmptyStateComponent(emptyState);
    }

    private Component createProof(Transaction transaction) {
        String url = presenter.uploadUrl(transaction.getProofPath());
        return url == null ? new Span("-") : ImagePreviewDialog.thumbnail(url, "Bukti Transaksi");
    }

    private HorizontalLayout createActionButtons(Transaction transaction) {
        Button deleteButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> openDeleteDialog(transaction));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        return new HorizontalLayout(deleteButton);
    }

    // ------------------------------------------------------------------ toolbar

    private void configureToolbar() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);

        sortBox.setPlaceholder("Urutkan...");
        sortBox.setItems(SortOption.values());
        sortBox.setItemLabelGenerator(presenter::formatSortOption);
        sortBox.addValueChangeListener(event -> applySort(event.getValue()));
    }

    private HorizontalLayout buildToolbar() {
        Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(), event -> searchTransactions());
        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), event -> refreshAll());

        Button incomeButton = new Button("Tambah Pemasukan", VaadinIcon.ARROW_DOWN.create(),
                event -> openAddDialog(TransactionType.INCOME));
        incomeButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);

        Button expenseButton = new Button("Tambah Pengeluaran", VaadinIcon.ARROW_UP.create(),
                event -> openAddDialog(TransactionType.EXPENSE));
        expenseButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField, searchButton, sortBox, refreshButton, incomeButton, expenseButton);
        toolbar.setWidthFull();
        toolbar.getStyle().set("flex-wrap", "wrap");
        toolbar.setFlexGrow(1, searchField);
        return toolbar;
    }

    // ------------------------------------------------------------------- dialog

    private void openAddDialog(TransactionType type) {
        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setWidthFull();

        NumberField amountField = new NumberField("Jumlah (Rp)");
        amountField.setWidthFull();
        amountField.setStep(1);
        amountField.setMin(1);

        UploadField proofUpload = new UploadField("Bukti transaksi (opsional, gambar maks. 2 MB)");
        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        Dialog dialog = new Dialog();
        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(event -> {
            progress.setVisible(true);
            saveButton.setEnabled(false);
            try {
                long amount = amountField.getValue() == null ? 0L : Math.round(amountField.getValue());
                // Validasi dulu agar file bukti tidak tersimpan bila input salah.
                useCase.validateInput(descriptionField.getValue(), amount);
                String proofPath = proofUpload.storeIn(fileStorage);
                Transaction created = type == TransactionType.INCOME
                        ? useCase.addIncome(currentUser.getId(), descriptionField.getValue(), amount, proofPath)
                        : useCase.addExpense(currentUser.getId(), descriptionField.getValue(), amount, proofPath);
                showSuccess(presenter.showAddSuccess(created));
                dialog.close();
                refreshAll();
            } catch (ValidationException ex) {
                showError(presenter.showValidationError(ex.getMessage()));
            } catch (IOException ex) {
                showError(presenter.showValidationError("Gagal menyimpan bukti: " + ex.getMessage()));
            } finally {
                progress.setVisible(false);
                saveButton.setEnabled(true);
            }
        });

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        buildDialog(dialog, type == TransactionType.INCOME ? "Tambah Pemasukan" : "Tambah Pengeluaran",
                new VerticalLayout(descriptionField, amountField, proofUpload, progress), cancelButton, saveButton);
        dialog.open();
    }

    private void openDeleteDialog(Transaction transaction) {
        Paragraph message = new Paragraph("Yakin ingin menghapus transaksi \"" + transaction.getDescription()
                + "\" (ID " + transaction.getId() + ")?");

        Dialog dialog = new Dialog();
        Button confirmButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> {
            deleteTransaction(transaction);
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        buildDialog(dialog, "Menghapus Transaksi", message, cancelButton, confirmButton);
        dialog.open();
    }

    /** Merakit dialog: judul di header (berwarna), isi, lalu tombol di footer sebelah kanan. */
    private void buildDialog(Dialog dialog, String title, Component body, Button... footerButtons) {
        dialog.setHeaderTitle(title);
        dialog.setWidth("480px");
        dialog.setMaxWidth("95vw");
        dialog.setResizable(false);
        dialog.add(body);
        dialog.getFooter().add(footerButtons);
    }

    // --------------------------------------------------------------------- aksi

    private void deleteTransaction(Transaction transaction) {
        try {
            useCase.removeTransaction(currentUser.getId(), transaction.getId());
            showSuccess(presenter.showRemoveSuccess());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
        }
    }

    private void searchTransactions() {
        String keyword = searchField.getValue();
        if (keyword.isBlank()) {
            refreshAll();
            return;
        }
        refreshGrid(useCase.searchTransactions(currentUser.getId(), keyword),
                "Hasil Pencarian: \"" + keyword + "\"");
    }

    private void applySort(SortOption option) {
        if (option != null) {
            refreshGrid(useCase.sortTransactions(currentUser.getId(), option),
                    "Daftar Transaksi (" + presenter.formatSortOption(option) + ")");
        }
    }

    private void refreshAll() {
        sortBox.clear();
        searchField.clear();
        refreshGrid(useCase.getTransactions(currentUser.getId()), "Daftar Transaksi");
    }

    private void refreshGrid(List<Transaction> transactions, String title) {
        grid.setItems(transactions);
        updateSummary();
        statusLabel.setText(transactions.isEmpty() ? title + " — " + EMPTY_MESSAGE : title);
    }

    private void showSuccess(String message) {
        Notification.show(message, 3000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification.show(message, 4000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
