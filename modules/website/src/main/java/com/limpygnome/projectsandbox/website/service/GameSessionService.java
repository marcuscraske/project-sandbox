package com.limpygnome.projectsandbox.website.service;

import com.limpygnome.projectsandbox.shared.jpa.provider.GameProvider;
import com.limpygnome.projectsandbox.shared.model.User;

/**
 * Created by limpygnome on 17/07/15.
 */
public interface GameSessionService
{
    /**
     * Generates a session token.
     *
     * @param nickname
     * @return A session token, or null if failed.
     */
    String generateSessionToken(GameProvider gameProvider, String nickname);

    String generateSessionToken(GameProvider gameProvider, User user);

    /**
     * Validates the game session token exists.
     *
     * @param gameSessionToken The token
     * @return True = valid, false = invalid session token
     */
    boolean validateExists(GameProvider gameProvider, String gameSessionToken);
}
