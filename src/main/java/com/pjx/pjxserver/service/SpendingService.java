package com.pjx.pjxserver.service;

import com.pjx.pjxserver.domain.Spending;
import com.pjx.pjxserver.domain.User;
import com.pjx.pjxserver.repository.FriendRepository;
import com.pjx.pjxserver.repository.SpendingRepository;
import com.pjx.pjxserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SpendingService {

    @Autowired
    private SpendingRepository spendingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    private FriendRepository friendRepository;


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

    public SpendingService(SpendingRepository spendingRepository, FriendRepository friendRepository) {
        this.spendingRepository = spendingRepository;
        this.friendRepository = friendRepository;
    }

    public List<Map<String, Object>> getFriendSpendingSummary(Long kakaoId) {
        // 친구 kakaoId 목록 조회
        List<Long> friendKakaoIds = friendRepository.findFriendIdsByKakaoId(kakaoId);

        // 친구들의 지출 내역을 조회하고, 필요한 필드만 포함된 데이터로 변환
        List<Spending> spendings = spendingRepository.findByKakaoIdIn(friendKakaoIds);

        return spendings.stream()
                .map(spending -> {
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("description", spending.getDescription());
                    summary.put("amount", spending.getAmount());
                    summary.put("kakaoId", spending.getKakaoId());
                    summary.put("note", spending.getNote()); // Assuming 'note' is a field in Spending
                    summary.put("images", spending.getImages()); // Assuming 'images' is a field in Spending
                    summary.put("date", spending.getDate()); // Assuming 'data' is a field in Spending
                    return summary;
                })
                .collect(Collectors.toList());

    }
}
