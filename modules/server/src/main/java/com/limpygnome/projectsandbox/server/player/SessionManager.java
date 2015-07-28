package com.limpygnome.projectsandbox.server.player;

import com.limpygnome.projectsandbox.shared.jpa.provider.GameProvider;
import com.limpygnome.projectsandbox.shared.model.GameSession;
import com.limpygnome.projectsandbox.shared.model.PlayerMetrics;
import com.limpygnome.projectsandbox.shared.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by limpygnome on 26/07/15.
 */
public class SessionManager
{
    private final static Logger LOG = LogManager.getLogger(SessionManager.class);
    private GameProvider gameProvider;
    private List<GameSession> trackedGameSessions;

    public SessionManager()
    {
        this.gameProvider = new GameProvider();
        this.trackedGameSessions = new LinkedList<>();

        try
        {
            gameProvider.begin();
            gameProvider.setAllSessionsToDisconnected();
            gameProvider.commit();
        }
        catch (Exception e)
        {
            LOG.error("Failed to set all pre-existing connected sessions to disconnected", e);
        }
    }

    public synchronized GameSession load(UUID token)
    {
        if (gameProvider != null)
        {
            GameSession gameSession = gameProvider.fetchGameSessionByToken(token);

            // Track session for periodic DB updates
            if (gameSession != null)
            {
                trackedGameSessions.add(gameSession);
            }

            return gameSession;
        }
        else
        {
            LOG.error("Unable to load game session, provider not set - token: {}", token);

            return null;
        }
    }

    public synchronized void unload(GameSession gameSession)
    {
        // Set session to unconnected
        gameSession.getPlayerMetrics().setLastUpdatedNow();
        gameSession.setConnected(false);

        // Transfer score/kills/death to user's profile/account
        User user = gameSession.getUser();

        if (user != null)
        {
            PlayerMetrics gameSessionMetrics = gameSession.getPlayerMetrics();
            PlayerMetrics userMetrics = user.getPlayerMetrics();

            // Transfer metrics
            userMetrics.transferFromGameSession(gameSessionMetrics);

            persist(gameSession, user);
        }
        else
        {
            persist(gameSession);
        }

        // Remove from tracked list
        trackedGameSessions.remove(gameSession);
    }

    public synchronized void persist(GameSession gameSession)
    {
        persist(gameSession, null);
    }

    private synchronized void persist(GameSession gameSession, User user)
    {
        // Check valid game session passed
        if (gameSession == null)
        {
            throw new IllegalArgumentException("Invalid/null game session provided");
        }

        // Check valid game provider available
        if (gameProvider != null)
        {
            gameProvider.begin();

            // Persist session
            gameProvider.updateGameSession(gameSession);

            // Persist user, if specified
            if (user != null)
            {
                gameProvider.updateUser(user);
            }

            gameProvider.commit();

            LOG.debug("Persisted game session - token: {}", gameSession.getToken());
        }
        else
        {
            LOG.error("Unable to persist game session, provider not set - token: {}", gameSession.getToken());
        }
    }

    public synchronized void logic()
    {
        try
        {
            gameProvider.begin();

            // Persist all tracked sessions
            for (GameSession gameSession : trackedGameSessions)
            {
                if (gameSession.getPlayerMetrics().isDirtyDatabaseFlag())
                {
                    gameSession.getPlayerMetrics().setLastUpdatedNow();
                    gameProvider.updateGameSession(gameSession);
                }
            }

            gameProvider.commit();
        }
        catch (Exception e)
        {
            LOG.error("Failed to perform periodic game session sync", e);
        }
    }

    // TODO: call on shutdown
    public synchronized void dispose()
    {
        if (gameProvider != null)
        {
            gameProvider.close();
            gameProvider = null;

            LOG.info("Disposed database provider");
        }
    }

}
