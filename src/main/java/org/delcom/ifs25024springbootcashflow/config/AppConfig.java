package org.delcom.ifs25024springbootcashflow.config;

import org.delcom.ifs25024springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25024springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25024springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25024springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25024springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25024springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25024springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25024springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25024springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25024springbootcashflow.usecase.UserUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

/**
 * Composition root Spring: merakit dependency antar-layer.
 * Hanya di sini JDBC, storage, dan use case dihubungkan; view cukup meminta bean-nya.
 */
@Configuration
public class AppConfig {

    /** Membuat tabel (bila belum ada) lalu menyediakan repository user berbasis JDBC. */
    @Bean
    public IUserRepository userRepository(DataSource dataSource) {
        SchemaInitializer.initialize(dataSource);
        return new JdbcUserRepository(dataSource);
    }

    @Bean
    public ITransactionRepository transactionRepository(DataSource dataSource) {
        return new JdbcTransactionRepository(dataSource);
    }

    @Bean
    public FileStorage fileStorage(@Value("${app.upload-dir}") String uploadDir) {
        return new FileStorage(uploadDir);
    }

    @Bean
    public AuthUseCase authUseCase(IUserRepository userRepository) {
        return new AuthUseCase(userRepository);
    }

    @Bean
    public UserUseCase userUseCase(IUserRepository userRepository) {
        return new UserUseCase(userRepository);
    }

    @Bean
    public FinanceUseCase financeUseCase(ITransactionRepository transactionRepository) {
        return new FinanceUseCase(transactionRepository);
    }

    @Bean
    public FinancePresenter financePresenter() {
        return new FinancePresenter();
    }

    /** Membuat akun admin dari ROOT_USERNAME dan ROOT_PASSWORD saat aplikasi start. */
    @Bean
    public ApplicationRunner seedRootAdmin(
            AuthUseCase authUseCase,
            @Value("${app.root-username}") String rootUsername,
            @Value("${app.root-password}") String rootPassword) {
        return args -> authUseCase.ensureRootAdmin(rootUsername, rootPassword);
    }

    /** Melayani file upload (foto profil dan bukti transaksi) di path /uploads/**. */
    @Bean
    public WebMvcConfigurer uploadResourceConfigurer(FileStorage fileStorage) {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations(fileStorage.resourceLocation());
            }
        };
    }
}
