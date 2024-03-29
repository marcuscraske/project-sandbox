package com.projectsandbox.components.website.controller.page.main.profile;

import com.projectsandbox.components.shared.jpa.repository.GameRepository;
import com.projectsandbox.components.shared.jpa.repository.UserRepository;
import com.projectsandbox.components.shared.model.GameSession;
import com.projectsandbox.components.shared.model.User;
import com.projectsandbox.components.website.controller.BaseController;
import com.projectsandbox.components.website.service.AuthenticationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.regex.Pattern;

/**
 * Created by limpygnome on 07/08/15.
 */
@Controller
@RequestMapping(value = "/profile")
public class ProfileViewController extends BaseController
{
    private final static Logger LOG = LogManager.getLogger(ProfileViewController.class);

    private static final Pattern UUID_REGEX_PATTERN = Pattern.compile("^([a-fA-F0-9]{8})\\-(([a-fA-F0-9]{4})\\-){3}([a-fA-F0-9]{12})$");

    private static final int SECONDS_LAST_UPDATED_METRICS_DISPLAY_ONLINE = 60;

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GameRepository gameRepository;

    @RequestMapping(value = "")
    public ModelAndView viewCurrentUser(HttpSession httpSession)
    {
        // Fetch and render profile for current user
        User currentUser = authenticationService.retrieveCurrentUser(httpSession);

        return viewProfile(currentUser);
    }

    @RequestMapping(value = "{userPath}")
    public ModelAndView viewUser(@PathVariable("userPath") String userPath)
    {
        User profileUser;

        // Fetch user and render their profile
        if (UUID_REGEX_PATTERN.matcher(userPath).matches())
        {
            // Load by UUID
            profileUser = userRepository.fetchUserByUserId(userPath);
        }
        else
        {
            // Load by nickname
            profileUser = userRepository.fetchUserByNickname(userPath);
        }

        return viewProfile(profileUser);
    }

    private ModelAndView viewProfile(User profileUser)
    {
        // Check a user has been found
        if (profileUser == null)
        {
            return createMvPage404();
        }

        // Fetch the user's game session (may not exist)
        GameSession gameSession = null;

        try
        {
            gameSession = gameRepository.fetchGameSessionByUser(profileUser);
        }
        catch (Exception e)
        {
            LOG.error("Failed to retrieve game session for profile - user id: {}", profileUser.getUserId());
        }

        // Setup mv
        ModelAndView modelAndView = createMV("main/profile", "profile - " + profileUser.getNickname());

        // Check if the user is online
        boolean online = false;

        if (gameSession != null)
        {
            int secondsSinceOnline = Seconds.secondsBetween(gameSession.getPlayerMetrics().getLastUpdated(), DateTime.now()).getSeconds();

            if (secondsSinceOnline < SECONDS_LAST_UPDATED_METRICS_DISPLAY_ONLINE)
            {
                online = true;
            }
        }

        // Attach objects
        modelAndView.addObject("profile_user", profileUser);
        modelAndView.addObject("game_session", gameSession);
        modelAndView.addObject("online", online);

        return modelAndView;
    }

}
