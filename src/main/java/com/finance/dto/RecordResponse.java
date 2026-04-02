package com.finance.dto;

import com.finance.model.FinancialRecord;
import com.finance.model.FinancialRecord.RecordType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RecordResponse {
    private Long id;
    private BigDecimal amount;
    private RecordType type;
    private String category;
    private LocalDate date;
    private String description;
    private String createdBy;
    private LocalDateTime createdAt;

    public static RecordResponse from(FinancialRecord record) {
        RecordResponse dto = new RecordResponse();
        dto.setId(record.getId());
        dto.setAmount(record.getAmount());
        dto.setType(record.getType());
        dto.setCategory(record.getCategory());
        dto.setDate(record.getDate());
        dto.setDescription(record.getDescription());
        dto.setCreatedBy(record.getCreatedBy().getUsername());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }
}
