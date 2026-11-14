package org.delcom.ifs25024springbootcashflow;

import org.delcom.ifs25024springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25024springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25024springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25024springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25024springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/** Smoke test: konteks Spring naik dengan profil test (H2) dan admin ter-seed. */
@SpringBootTest
@ActiveProfiles("test")
class AppTest {

    @Autowired
    private ITransactionRepository transactionRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private FinanceUseCase financeUseCase;

    @Autowired
    private AuthUseCase authUseCase;

    @Autowired
    private FinancePresenter financePresenter;

    @Test
    void contextLoadsAndWiresBeans() {
        assertNotNull(transactionRepository);
        assertNotNull(financeUseCase);
        assertNotNull(financePresenter);
    }

    @Test
    void rootAdminIsSeeded() throws Exception {
        assertEquals("admin", authUseCase.login("admin", "admin").getUsername());
        assertNotNull(userRepository.findByUsername("admin").orElseThrow());
    }

    @Test
    void mainDelegatesToSpringApplicationRun() throws Exception {
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(eq(App.class), any(String[].class))).thenReturn(context);

            App.main(new String[]{"--server.port=0"});

            mocked.verify(() -> SpringApplication.run(eq(App.class), any(String[].class)));
        }
    }
}
