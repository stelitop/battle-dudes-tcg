package net.stelitop.battledudestcg.game.services;

import discord4j.core.GatewayDiscordClient;
import net.stelitop.battledudestcg.game.database.entities.collection.CardDeck;
import net.stelitop.battledudestcg.game.database.repositories.DeckRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeckServiceTest {

    @Mock
    private DeckRepository deckRepositoryMock;
    @Mock
    private CollectionService collectionServiceMock;
    @Mock
    private GatewayDiscordClient clientMock;
    @InjectMocks
    private DeckService deckService;

    @Test
    void getDecksOfUser() {
        long userId = 1234;
        List<CardDeck> decks = List.of(mock(CardDeck.class), mock(CardDeck.class));
        when(deckRepositoryMock.findCardDecksByUserId(userId))
                .thenReturn(decks);
        List<CardDeck> ret = deckService.getDecksOfUser(userId);
        assertThat(ret).containsExactlyInAnyOrder(decks.toArray(new CardDeck[0]));
        verify(deckRepositoryMock, times(1)).findCardDecksByUserId(userId);
        verifyNoMoreInteractions(deckRepositoryMock, collectionServiceMock, clientMock);
    }

    @Test
    void getUserDeckNames() {
        long userId = 1234;
        List<String> deckNames = List.of("name1", "NamE23", "Last Name");
        when(deckRepositoryMock.findNamesOfDecksOfUser(userId)).thenReturn(deckNames);
        List<String> ret = deckService.getUserDeckNames(userId);
        assertThat(ret).containsExactlyInAnyOrder(deckNames.toArray(new String[0]));
        verify(deckRepositoryMock, times(1)).findNamesOfDecksOfUser(userId);
        verifyNoMoreInteractions(deckRepositoryMock, collectionServiceMock, clientMock);
    }

    @Test
    void createNewDeck() {

    }

    @Test
    void getSelectedDeck() {
    }

    @Test
    void saveDeck() {
    }

    @Test
    void getDeckOfUser() {
    }
}