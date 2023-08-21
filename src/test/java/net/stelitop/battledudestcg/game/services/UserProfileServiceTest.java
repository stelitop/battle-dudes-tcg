package net.stelitop.battledudestcg.game.services;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.UserData;
import net.stelitop.battledudestcg.game.database.entities.profile.UserProfile;
import net.stelitop.battledudestcg.game.database.entities.profile.UserSettings;
import net.stelitop.battledudestcg.game.database.repositories.UserCollectionRepository;
import net.stelitop.battledudestcg.game.database.repositories.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepositoryMock;
    @Mock
    private UserCollectionRepository userCollectionRepositoryMock;
    @InjectMocks
    private UserProfileService userProfileService;

    @Test
    void getProfileByIdPresentInDb() {
        long userId = 123;
        UserProfile profile = mock(UserProfile.class);
        when(userProfileRepositoryMock.findById(userId)).thenReturn(Optional.of(profile));

        UserProfile ret = userProfileService.getProfile(userId);
        assertThat(ret).isEqualTo(profile);
        verify(userProfileRepositoryMock, times(1)).findById(userId);
        verifyNoMoreInteractions(userProfileRepositoryMock, userCollectionRepositoryMock);
    }

    @Test
    void getProfileByIdNotInDb() {
        long userId = 123;
        when(userProfileRepositoryMock.findById(userId)).thenReturn(Optional.empty());
        when(userCollectionRepositoryMock.save(any())).thenAnswer(x -> x.getArguments()[0]);
        when(userProfileRepositoryMock.save(any())).thenAnswer(x -> x.getArguments()[0]);

        UserProfile ret = userProfileService.getProfile(userId);
        verify(userProfileRepositoryMock, times(1)).findById(userId);
        verify(userCollectionRepositoryMock, times(1)).save(argThat(x -> x == ret.getUserCollection()));
        verify(userProfileRepositoryMock, times(1)).save(argThat(x -> x.getDiscordId() == userId));
        verifyNoMoreInteractions(userProfileRepositoryMock, userCollectionRepositoryMock);

        assertThat(ret.getDiscordId()).isEqualTo(userId);
        assertThat(ret.getLastMessage()).isEqualTo(Instant.EPOCH);
    }

    @Test
    void getProfileByUser() {
        long userId = 123;
        UserData userData = UserData.builder().id(userId).username("username").discriminator("abcd").build();
        GatewayDiscordClient gatewayDiscordClientMock = mock(GatewayDiscordClient.class);
        User user = new User(gatewayDiscordClientMock, userData);
        userProfileService = Mockito.spy(userProfileService);
        userProfileService.getProfile(user);
        verify(userProfileService, times(1)).getProfile(userId);
    }

    @Test
    void updateLastMessageTime() {
        long userId = 123;
        UserProfile userProfile = new UserProfile();
        userProfile.setLastMessage(Date.from(Instant.EPOCH));
        userProfile.setDiscordId(userId);
        userProfileService = spy(userProfileService);
        when(userProfileService.getProfile(userId)).thenReturn(userProfile);
        when(userProfileRepositoryMock.save(userProfile)).thenAnswer(x -> x.getArguments()[0]);

        Instant timeBefore = Instant.now();
        var ret = userProfileService.updateLastMessageTime(userId);
        Instant timeAfter = Instant.now();

        assertThat(ret.getDiscordId()).isEqualTo(userId);
        assertThat(ret.getLastMessage()).isAfterOrEqualTo(timeBefore);
        assertThat(ret.getLastMessage()).isBeforeOrEqualTo(timeAfter);

        verify(userProfileService, times(1)).getProfile(userId);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void toggleParticipation(boolean startingToggle) {
        long userId = 123;
        UserProfile userProfile = new UserProfile();
        userProfile.setUserSettings(new UserSettings());
        userProfile.setDiscordId(userId);
        userProfile.getUserSettings().setParticipating(startingToggle);
        userProfileService = spy(userProfileService);
        when(userProfileService.getProfile(userId)).thenReturn(userProfile);
        when(userProfileRepositoryMock.save(userProfile)).thenAnswer(x -> x.getArguments()[0]);

        var ret = userProfileService.toggleParticipation(userId);

        assertThat(userProfile.getUserSettings().isParticipating()).isEqualTo(!startingToggle);
        assertThat(ret.getDiscordId()).isEqualTo(userId);
        verify(userProfileService, times(1)).getProfile(userId);
    }
}