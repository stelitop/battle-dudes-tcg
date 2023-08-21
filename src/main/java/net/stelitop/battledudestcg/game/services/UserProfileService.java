package net.stelitop.battledudestcg.game.services;

import discord4j.core.object.entity.User;
import net.stelitop.battledudestcg.game.database.entities.collection.UserCollection;
import net.stelitop.battledudestcg.game.database.entities.profile.UserProfile;
import net.stelitop.battledudestcg.game.database.repositories.UserCollectionRepository;
import net.stelitop.battledudestcg.game.database.repositories.UserProfileRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Instant;

@Service
public class UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private UserCollectionRepository userCollectionRepository;

    /**
     * Gets the profile of a user by their id. If they didn't have a
     * profile before in the database, a brand new one is created for
     * them.
     *
     * @param userId The discord id of the user.
     * @return Their user profile object.
     */
    public @NotNull UserProfile getProfile(long userId) {

        var profileOpt = userProfileRepository.findById(userId);
        if (profileOpt.isPresent()) {
            return profileOpt.get();
        }

        UserProfile newProfile = new UserProfile();
        UserCollection userCollection = userCollectionRepository.save(new UserCollection());

        newProfile.setDiscordId(userId);
        newProfile.setUserCollection(userCollection);
        newProfile.setLastMessage(Date.from(Instant.EPOCH));
        return userProfileRepository.save(newProfile);
    }

    public @NotNull UserProfile getProfile(User user) {
        return getProfile(user.getId().asLong());
    }

    /**
     * Updates the time of the last registered message by the user to the
     * current time.
     *
     * @param userId The id of the user.
     * @return The user profile object after being updated.
     */
    public @NotNull UserProfile updateLastMessageTime(long userId) {
        UserProfile profile = getProfile(userId);
        profile.setLastMessage(Date.from(Instant.now()));
        return userProfileRepository.save(profile);
    }

    /**
     * Toggles whether the user is participating in the game or not. If they were
     * not participating, they now are and vice-versa.
     *
     * @param userId The id of the user.
     * @return The user profile object after being updated.
     */
    public @NotNull UserProfile toggleParticipation(long userId) {
        UserProfile profile = getProfile(userId);
        profile.getUserSettings().setParticipating(!profile.getUserSettings().isParticipating());
        return userProfileRepository.save(profile);
    }
}
