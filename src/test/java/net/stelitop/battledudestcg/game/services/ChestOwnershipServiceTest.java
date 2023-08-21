package net.stelitop.battledudestcg.game.services;

import net.stelitop.battledudestcg.game.database.entities.chests.ChannelChest;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.database.entities.collection.ChestOwnership;
import net.stelitop.battledudestcg.game.database.entities.collection.UserCollection;
import net.stelitop.battledudestcg.game.database.entities.collection.UserCollectionChestKey;
import net.stelitop.battledudestcg.game.database.repositories.ChestOwnershipRepository;
import net.stelitop.battledudestcg.game.database.repositories.ChestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChestOwnershipServiceTest {

    @Mock
    private ChestOwnershipRepository chestOwnershipRepositoryMock;
    @Mock
    private ChestRepository chestRepositoryMock;
    @Mock
    private CollectionService collectionServiceMock;
    @InjectMocks
    private ChestOwnershipService chestOwnershipService;

    @Test
    void getChestOwnershipAlreadyInDb() {
        long userId = 123;
        long collectionId = 1234;
        int chestCount = 3;
        Chest chest = ChannelChest.builder().name("Chest ABC").chestId(1).build();
        UserCollection userCollection = new UserCollection();
        userCollection.setCollectionId(collectionId);
        ChestOwnership co = new ChestOwnership(new UserCollectionChestKey(collectionId,
                chest.getChestId()), chest, userCollection, chestCount);
        when(chestOwnershipRepositoryMock.getChestOwnership(userId, chest.getName())).thenReturn(Optional.of(co));

        var result = chestOwnershipService.getChestOwnership(userId, chest.getName());
        assertThat(result).isPresent();
        verify(chestOwnershipRepositoryMock, times(1)).getChestOwnership(userId, chest.getName());
        verifyNoMoreInteractions(chestOwnershipRepositoryMock, chestRepositoryMock, collectionServiceMock);
    }

    @Test
    void getChestOwnershipNotInDbExistingChest() {
        long userId = 123;
        long collectionId = 1234;
        Chest chest = ChannelChest.builder().name("Chest ABC").chestId(1).build();
        UserCollection userCollection = new UserCollection();
        userCollection.setCollectionId(collectionId);
        when(chestOwnershipRepositoryMock.getChestOwnership(userId, chest.getName())).thenReturn(Optional.empty());
        when(chestRepositoryMock.findByName(chest.getName())).thenReturn(Optional.of(chest));
        UserCollection collection = new UserCollection();
        collection.setCollectionId(collectionId);
        when(collectionServiceMock.getUserCollection(userId)).thenReturn(collection);

        var resultOpt = chestOwnershipService.getChestOwnership(userId, chest.getName());
        assertThat(resultOpt).isPresent();
        var result = resultOpt.get();
        assertThat(result.getId().getCollectionId()).isEqualTo(userCollection.getCollectionId());
        assertThat(result.getId().getChestId()).isEqualTo(chest.getChestId());
        assertThat(result.getChest()).isEqualTo(chest);
        assertThat(result.getUserCollection()).isEqualTo(collection);
        assertThat(result.getCount()).isZero();

        verify(chestOwnershipRepositoryMock, times(1)).getChestOwnership(userId, chest.getName());
        verify(chestRepositoryMock, times(1)).findByName(chest.getName());
        verify(collectionServiceMock, times(1)).getUserCollection(userId);
        verifyNoMoreInteractions(chestOwnershipRepositoryMock, chestRepositoryMock, collectionServiceMock);
    }

    @Test
    void getChestOwnershipNotInDbNonExistingChest() {
        long userId = 123;
        long collectionId = 1234;
        Chest chest = ChannelChest.builder().name("Chest ABC").chestId(1).build();
        UserCollection userCollection = new UserCollection();
        userCollection.setCollectionId(collectionId);
        when(chestOwnershipRepositoryMock.getChestOwnership(userId, chest.getName())).thenReturn(Optional.empty());
        when(chestRepositoryMock.findByName(chest.getName())).thenReturn(Optional.empty());
        UserCollection collection = new UserCollection();
        collection.setCollectionId(collectionId);
        when(collectionServiceMock.getUserCollection(userId)).thenReturn(collection);

        var resultOpt = chestOwnershipService.getChestOwnership(userId, chest.getName());
        assertThat(resultOpt).isEmpty();

        verify(chestOwnershipRepositoryMock, times(1)).getChestOwnership(userId, chest.getName());
        verify(chestRepositoryMock, times(1)).findByName(chest.getName());
        verify(collectionServiceMock, times(1)).getUserCollection(userId);
        verifyNoMoreInteractions(chestOwnershipRepositoryMock, chestRepositoryMock, collectionServiceMock);
    }
}