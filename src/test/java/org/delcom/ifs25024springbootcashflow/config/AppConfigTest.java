package org.delcom.ifs25024springbootcashflow.config;

import org.delcom.ifs25024springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25024springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25024springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25024springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25024springbootcashflow.domain.entity.Role;
import org.delcom.ifs25024springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25024springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25024springbootcashflow.usecase.AuthUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppConfigTest {

    private final AppConfig config = new AppConfig();
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
    }

    @Test
    void createsRepositoriesUseCasesAndPresenter(@TempDir Path dir) {
        IUserRepository users = config.userRepository(dataSource);
        ITransactionRepository transactions = config.transactionRepository(dataSource);

        assertInstanceOf(JdbcUserRepository.class, users);
        assertInstanceOf(JdbcTransactionRepository.class, transactions);
        assertNotNull(config.authUseCase(users));
        assertNotNull(config.userUseCase(users));
        assertNotNull(config.financeUseCase(transactions));
        assertInstanceOf(FinancePresenter.class, config.financePresenter());
        assertNotNull(config.fileStorage(dir.toString()));
    }

    @Test
    void seedRootAdminCreatesAdmin() throws Exception {
        IUserRepository users = config.userRepository(dataSource);
        AuthUseCase auth = config.authUseCase(users);

        ApplicationRunner runner = config.seedRootAdmin(auth, "admin", "admin123");
        runner.run(mock(ApplicationArguments.class));

        assertEquals(Role.ADMIN, users.findByUsername("admin").orElseThrow().getRole());
    }

    @Test
    void mapsUploadsToStorageFolder(@TempDir Path dir) {
        FileStorage storage = config.fileStorage(dir.toString());
        WebMvcConfigurer configurer = config.uploadResourceConfigurer(storage);
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler(anyString())).thenReturn(registration);

        configurer.addResourceHandlers(registry);

        verify(registry).addResourceHandler("/uploads/**");
        verify(registration).addResourceLocations(storage.resourceLocation());
    }
}
