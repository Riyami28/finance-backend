package com.finance.service;

import com.finance.dto.DashboardSummary;
import com.finance.dto.DashboardSummary.MonthlyTrend;
import com.finance.dto.RecordResponse;
import com.finance.model.FinancialRecord.RecordType;
import com.finance.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository recordRepository;

    public DashboardSummary getSummary() {
        BigDecimal totalIncome = recordRepository.sumByType(RecordType.INCOME);
        BigDecimal totalExpenses = recordRepository.sumByType(RecordType.EXPENSE);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        Map<String, BigDecimal> incomeByCategory = toMap(
                recordRepository.sumByCategoryAndType(RecordType.INCOME));
        Map<String, BigDecimal> expenseByCategory = toMap(
                recordRepository.sumByCategoryAndType(RecordType.EXPENSE));

        List<RecordResponse> recent = recordRepository
                .findRecentActivity(PageRequest.of(0, 10))
                .stream()
                .map(RecordResponse::from)
                .collect(Collectors.toList());

        List<MonthlyTrend> trends = buildMonthlyTrends();

        return DashboardSummary.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .totalRecords(recordRepository.countByDeletedFalse())
                .incomeByCategory(incomeByCategory)
                .expenseByCategory(expenseByCategory)
                .recentActivity(recent)
                .monthlyTrends(trends)
                .build();
    }

    private List<MonthlyTrend> buildMonthlyTrends() {
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6).withDayOfMonth(1);
        List<Object[]> raw = recordRepository.monthlyTrends(sixMonthsAgo);

        // Group: month -> { INCOME: x, EXPENSE: y }
        Map<String, Map<RecordType, BigDecimal>> grouped = new LinkedHashMap<>();
        for (Object[] row : raw) {
            String month = (String) row[0];
            RecordType type = (RecordType) row[1];
            BigDecimal sum = (BigDecimal) row[2];
            grouped.computeIfAbsent(month, k -> new EnumMap<>(RecordType.class))
                    .put(type, sum);
        }

        List<MonthlyTrend> trends = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            BigDecimal inc = entry.getValue().getOrDefault(RecordType.INCOME, BigDecimal.ZERO);
            BigDecimal exp = entry.getValue().getOrDefault(RecordType.EXPENSE, BigDecimal.ZERO);
            trends.add(MonthlyTrend.builder()
                    .month(entry.getKey())
                    .income(inc)
                    .expense(exp)
                    .net(inc.subtract(exp))
                    .build());
        }
        return trends;
    }

    private Map<String, BigDecimal> toMap(List<Object[]> rows) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((String) row[0], (BigDecimal) row[1]);
        }
        return map;
    }
}
