package com.example.demo.service;

import com.example.demo.entity.Card;
import com.example.demo.entity.User;
import com.example.demo.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;

    public void setAllNonDefault(List<Card> cards){
        List<Card> nonDefaultCard = cards.stream()
                .map(card ->{
                    card.setIsDefault(false);
                    return card;
                })
                .collect(Collectors.toList());

        cardRepository.saveAll(nonDefaultCard);
    }

    public void setAllNonDefaultForUser(User user){
        List<Card> cards = cardRepository.findByUser(user);
        setAllNonDefault(cards);
    }

    public Card findById(Integer cardId){
        return cardRepository.findById(cardId)
                .orElseThrow(()->new RuntimeException("指定されたIDのカードは見つかりませんでした。"));
    }
}
