package com.finance.service;

import com.finance.dto.RecordRequest;
import com.finance.dto.RecordResponse;
import com.finance.exception.ResourceNotFoundException;
import com.finance.model.FinancialRecord;
import com.finance.model.FinancialRecord.RecordType;
import com.finance.model.User;
import com.finance.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserService userService;

    @Transactional
    public RecordResponse create(RecordRequest request, String username) {
        User user = userService.findByUsername(username);

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory().trim())
                .date(request.getDate())
                .description(request.getDescription())
                .createdBy(user)
                .build();

        return RecordResponse.from(recordRepository.save(record));
    }

    public Page<RecordResponse> getAll(RecordType type, String category, LocalDate startDate,
                                        LocalDate endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        return recordRepository.findFiltered(type, category, startDate, endDate, pageable)
                .map(RecordResponse::from);
    }

    public RecordResponse getById(Long id) {
        return RecordResponse.from(findActiveById(id));
    }

    @Transactional
    public RecordResponse update(Long id, RecordRequest request) {
        FinancialRecord record = findActiveById(id);
        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory().trim());
        record.setDate(request.getDate());
        record.setDescription(request.getDescription());
        return RecordResponse.from(recordRepository.save(record));
    }

    @Transactional
    public void softDelete(Long id) {
        FinancialRecord record = findActiveById(id);
        record.setDeleted(true);
        recordRepository.save(record);
    }

    private FinancialRecord findActiveById(Long id) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + id));
        if (record.isDeleted()) {
            throw new ResourceNotFoundException("Record not found with id: " + id);
        }
        return record;
    }
}
