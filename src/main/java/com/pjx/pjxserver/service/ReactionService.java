package com.pjx.pjxserver.service;

import com.pjx.pjxserver.domain.Reaction;
import com.pjx.pjxserver.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReactionService {

    @Autowired
    private final ReactionRepository reactionRepository;

    @Transactional
    public void submitReaction(Long kakaoId, LocalDate date, String reactionType) {
        Optional<Reaction> existingReaction = reactionRepository.findByKakaoIdAndDate(kakaoId, date);

        if (existingReaction.isPresent()) {
            // 기존 리액션 업데이트
            Reaction reaction = existingReaction.get();
            reaction.setReactionType(reactionType);
            reactionRepository.save(reaction);
        } else {
            // 새로운 리액션 생성
            Reaction reaction = Reaction.builder()
                    .kakaoId(kakaoId)
                    .date(date)
                    .reactionType(reactionType)
                    .build();
            reactionRepository.save(reaction);
        }
    }

    public List<Map<String, Object>> getReactionsByDateRange(Long kakaoId, LocalDate startDate, LocalDate endDate) {
        List<Reaction> reactions = reactionRepository.findAllByKakaoIdAndDateBetween(kakaoId, startDate, endDate);

        return reactions.stream()
                .map(reaction -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("date", reaction.getDate());
                    data.put("reactionType", reaction.getReactionType());
                    return data;
                })
                .collect(Collectors.toList());
    }
}
