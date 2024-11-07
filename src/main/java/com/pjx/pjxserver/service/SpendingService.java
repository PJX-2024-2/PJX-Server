package com.pjx.pjxserver.service;

import com.pjx.pjxserver.domain.Spending;
import com.pjx.pjxserver.repository.FriendRepository;
import com.pjx.pjxserver.repository.SpendingRepository;
import com.pjx.pjxserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void addReaction(Long spendingId, Long kakaoId, String reactionType) {
        Spending spending = spendingRepository.findById(spendingId)
                .orElseThrow(() -> new RuntimeException("Spending not found"));

        spending.addReaction(kakaoId, reactionType);
        spendingRepository.save(spending);
    }

    public List<Map<String, Object>> getFriendSpendingSummary(Long kakaoId) {
        List<Long> friendKakaoIds = friendRepository.findFriendIdsByKakaoId(kakaoId);
        List<Spending> spendings = spendingRepository.findByKakaoIdIn(friendKakaoIds);

        return spendings.stream()
                .map(spending -> {
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("description", spending.getDescription());
                    summary.put("amount", spending.getAmount());
                    summary.put("kakaoId", spending.getKakaoId());
                    summary.put("note", spending.getNote());
                    summary.put("images", spending.getImages());
                    summary.put("date", spending.getDate());
                    summary.put("reactions", spending.getReactions()); // 각 지출 항목에 대한 리액션 포함
                    return summary;
                })
                .collect(Collectors.toList());
    }
    public List<Spending> getSpendingListByDate(Long kakaoId, LocalDate date) {
        return spendingRepository.findByKakaoIdAndDate(kakaoId, date);
    }

    @Transactional
    public void submitReaction(Long kakaoId, LocalDate date, String reactionType) {
        // 특정 날짜와 kakaoId에 해당하는 모든 Spending을 조회
        List<Spending> spendings = spendingRepository.findByKakaoIdAndDate(kakaoId, date);

        // 조회된 지출 목록이 비어 있는 경우 예외 발생
        if (spendings.isEmpty()) {
            throw new IllegalArgumentException("해당 날짜의 지출 정보를 찾을 수 없습니다.");
        }

        // 첫 번째 지출 항목에 리액션 추가
        Spending spending = spendings.get(0);
        spending.addReaction(kakaoId, reactionType);
        spendingRepository.save(spending);
    }

    public List<Spending> getSpendingListByDateRange(Long kakaoId, LocalDate startDate, LocalDate endDate) {
        return spendingRepository.findAllByKakaoIdAndDateBetween(kakaoId, startDate, endDate);
    }

}
