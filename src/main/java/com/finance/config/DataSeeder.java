package com.finance.config;

import com.finance.model.FinancialRecord;
import com.finance.model.FinancialRecord.RecordType;
import com.finance.model.Role;
import com.finance.model.User;
import com.finance.repository.FinancialRecordRepository;
import com.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds the database with sample data for development and testing.
 * Creates default users for each role and sample financial records.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FinancialRecordRepository recordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return; // skip if data exists

        log.info("Seeding database with sample data...");

        // Create default users
        User admin = userRepository.save(User.builder()
                .username("admin")
                .email("admin@finance.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build());

        userRepository.save(User.builder()
                .username("analyst")
                .email("analyst@finance.com")
                .password(passwordEncoder.encode("analyst123"))
                .role(Role.ANALYST)
                .build());

        userRepository.save(User.builder()
                .username("viewer")
                .email("viewer@finance.com")
                .password(passwordEncoder.encode("viewer123"))
                .role(Role.VIEWER)
                .build());

        // Create sample financial records
        String[][] records = {
            {"50000", "INCOME", "Salary", "2026-03-01", "March salary"},
            {"12000", "INCOME", "Freelance", "2026-03-10", "Web project payment"},
            {"1500", "EXPENSE", "Utilities", "2026-03-05", "Electricity bill"},
            {"3000", "EXPENSE", "Groceries", "2026-03-08", "Monthly groceries"},
            {"800", "EXPENSE", "Transport", "2026-03-12", "Fuel and metro"},
            {"5000", "EXPENSE", "Rent", "2026-03-01", "Monthly rent"},
            {"2000", "INCOME", "Dividends", "2026-02-15", "Stock dividends"},
            {"45000", "INCOME", "Salary", "2026-02-01", "February salary"},
            {"4500", "EXPENSE", "Shopping", "2026-02-20", "Electronics purchase"},
            {"1200", "EXPENSE", "Healthcare", "2026-02-18", "Doctor visit"},
            {"50000", "INCOME", "Salary", "2026-01-01", "January salary"},
            {"6000", "EXPENSE", "Travel", "2026-01-15", "Weekend trip"},
        };

        for (String[] r : records) {
            recordRepository.save(FinancialRecord.builder()
                    .amount(new BigDecimal(r[0]))
                    .type(RecordType.valueOf(r[1]))
                    .category(r[2])
                    .date(LocalDate.parse(r[3]))
                    .description(r[4])
                    .createdBy(admin)
                    .build());
        }

        log.info("Database seeded: 3 users, {} records", records.length);
    }
}
