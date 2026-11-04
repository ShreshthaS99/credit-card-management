package com.example.ccmps.controller;

import com.example.ccmps.entity.CreditCard;
import com.example.ccmps.service.CardService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public List<CreditCard> getCards(
            @RequestParam UUID accountId) {

        return cardService.getCards(
                accountId
        );
    }

    @GetMapping("/{cardId}")
    public CreditCard getCard(
            @PathVariable UUID cardId) {

        return cardService.getCard(
                cardId
        );
    }

    @PostMapping("/{cardId}/activate")
    public CreditCard activateCard(
            @PathVariable UUID cardId) {

        return cardService.activateCard(
                cardId
        );
    }

    @PostMapping("/{cardId}/freeze")
    public CreditCard freezeCard(
            @PathVariable UUID cardId) {

        return cardService.freezeCard(
                cardId
        );
    }

    @PostMapping("/{cardId}/unfreeze")
    public CreditCard unfreezeCard(
            @PathVariable UUID cardId) {

        return cardService.unfreezeCard(
                cardId
        );
    }
}


