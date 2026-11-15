package org.delcom.ifs25024springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25024springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25024springbootcashflow.domain.entity.Role;
import org.delcom.ifs25024springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25024springbootcashflow.domain.entity.User;
import org.delcom.ifs25024springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25024springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25024springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25024springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25024springbootcashflow.usecase.UserUseCase;

import java.util.List;
import java.util.Optional;

/** Transaksi milik pengguna lain, hanya-baca (khusus ADMIN). Alamat: /user-transactions?userId=N. */
@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna | Cashflow")
public class UserTransactionsView extends VerticalLayout implements BeforeEnterObserver {

    private final FinanceUseCase financeUseCase;
    private final UserUseCase userUseCase;
    private final IUserRepository userRepository;
    private final FinancePresenter presenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final Paragraph summary = new Paragraph();
    private final PageCard card;

    public UserTransactionsView(FinanceUseCase financeUseCase, UserUseCase userUseCase,
                                IUserRepository userRepository, FinancePresenter presenter) {
        this.financeUseCase = financeUseCase;
        this.userUseCase = userUseCase;
        this.userRepository = userRepository;
        this.presenter = presenter;
        getElement().setAttribute("role", "main");

        grid.addColumn(Transaction::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setFlexGrow(1);
        grid.addColumn(t -> presenter.formatMoney(t.getAmount())).setHeader("Jumlah").setAutoWidth(true);
        grid.addColumn(t -> presenter.formatType(t.getType())).setHeader("Tipe").setAutoWidth(true);
        grid.addComponentColumn(this::proof).setHeader("Bukti").setAutoWidth(true);
        grid.addColumn(presenter::formatCreatedAt).setHeader("Waktu").setAutoWidth(true);
        grid.setEmptyStateComponent(new Span("Belum ada transaksi!"));
        GridStyles.apply(grid);

        card = new PageCard("Transaksi Pengguna", summary, grid);
        add(card);
    }

    private Component proof(Transaction transaction) {
        String url = presenter.uploadUrl(transaction.getProofPath());
        return url == null ? new Span("-") : ImagePreviewDialog.thumbnail(url, "Bukti Transaksi");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<User> viewer = AuthSession.currentUser(userRepository);
        if (viewer.isEmpty()) {
            event.forwardTo(LoginView.class);
            return;
        }
        if (viewer.get().getRole() != Role.ADMIN) {
            event.forwardTo(FinanceView.class);
            return;
        }
        Optional<String> idParam = event.getLocation().getQueryParameters().getSingleParameter("userId");
        try {
            User target = userUseCase.getUser(Integer.parseInt(idParam.orElse("")));
            List<Transaction> transactions = financeUseCase.getTransactions(target.getId());
            summary.setText(target.getFullName() + " (" + target.getUsername() + ") - "
                    + presenter.showBalance(financeUseCase.getBalance(target.getId())) + " - hanya-baca");
            grid.setItems(transactions);
        } catch (NumberFormatException | EntityNotFoundException ex) {
            event.forwardTo(UsersView.class);
        }
    }
}
