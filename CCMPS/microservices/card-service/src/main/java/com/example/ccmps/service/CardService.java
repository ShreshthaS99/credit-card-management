package com.example.ccmps.service;

import com.example.ccmps.entity.CreditCard;
import com.example.ccmps.exception.BusinessException;
import com.example.ccmps.exception.ResourceNotFoundException;
import com.example.ccmps.repository.CreditCardRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CreditCardRepository cardRepository;
    private final AccountService accounts;

    @Transactional(readOnly = true)
    public List<CreditCard> getCards(
            UUID accountId) {

        accounts.getAccount(accountId);
        return cardRepository
                .findByAccountId(accountId);
    }

    @Transactional(readOnly = true)
    public CreditCard getCard(
            UUID cardId) {

        CreditCard card = cardRepository
                .findById(cardId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Card not found: " + cardId
                        )
                );
        accounts.getAccount(card.getAccountId());
        return card;
    }

    @Transactional
    public CreditCard activateCard(
            UUID cardId) {

        CreditCard card = getCard(cardId);

        if (!"ISSUED".equalsIgnoreCase(
                card.getStatus())) {

            throw new BusinessException(
                    "Only an ISSUED card can be activated."
            );
        }

        card.setStatus("ACTIVE");
        card.setFrozen(false);

        return cardRepository.save(card);
    }

    @Transactional
    public CreditCard freezeCard(
            UUID cardId) {

        CreditCard card = getCard(cardId);

        if (Boolean.TRUE.equals(
                card.getFrozen())) {

            throw new BusinessException(
                    "Card is already frozen."
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(
                card.getStatus())) {

            throw new BusinessException(
                    "Only an ACTIVE card can be frozen."
            );
        }

        card.setFrozen(true);
        card.setStatus("BLOCKED");

        return cardRepository.save(card);
    }

    @Transactional
    public CreditCard unfreezeCard(
            UUID cardId) {

        CreditCard card = getCard(cardId);

        if (!Boolean.TRUE.equals(
                card.getFrozen())) {

            throw new BusinessException(
                    "Card is not currently frozen."
            );
        }

        if (!"BLOCKED".equalsIgnoreCase(
                card.getStatus())) {

            throw new BusinessException(
                    "Only a temporarily blocked card can be unfrozen."
            );
        }

        card.setFrozen(false);
        card.setStatus("ACTIVE");

        return cardRepository.save(card);
    }
}