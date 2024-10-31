package com.pjx.pjxserver.service;

import com.pjx.pjxserver.domain.Spending;
import com.pjx.pjxserver.repository.SpendingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SpendingService {

    @Autowired
    private SpendingRepository spendingRepository;

    // 날짜를 포함하도록 메서드 시그니처 수정
    public Spending createSpending(Long kakaoId, LocalDate date, BigDecimal amount, String description, String note, List<String> images) {
        Spending spending = Spending.builder()
                .kakaoId(kakaoId)
                .date(date) // 특정 날짜 설정
                .amount(amount)
                .description(description)
                .note(note)
                .images(images)
                .build();
        return spendingRepository.save(spending);
    }

    public Spending updateSpending(Long spendingId, BigDecimal amount, String description, String note, List<String> images) {
        Spending spending = spendingRepository.findById(spendingId)
                .orElseThrow(() -> new RuntimeException("Spending not found"));

        // Update fields only if they are provided
        if (amount != null) spending.setAmount(amount);
        if (description != null) spending.setDescription(description);
        if (note != null) spending.setNote(note);
        if (images != null) spending.setImages(images);

        return spendingRepository.save(spending);
    }

    public void deleteSpending(Long spendingId) {
        spendingRepository.deleteById(spendingId);
    }

    public Optional<Spending> getSpendingDetail(Long spendingId) {
        return spendingRepository.findById(spendingId);
    }
}
