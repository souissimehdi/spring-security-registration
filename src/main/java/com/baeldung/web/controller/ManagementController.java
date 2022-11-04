package com.baeldung.web.controller;

import com.baeldung.persistence.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ManagementController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping("/management")
    public String getLoggedUsers() {
        final User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
         boolean userHarManagerRole
                = user.getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase("ROLE_MANAGER"));
        if (userHarManagerRole) {
            return "management";
        } else {
            logger.error("User '{} {}' attempted to access unauthorized URL /management", user.getFirstName(), user.getLastName());
            return "accessDenied";
        }
    }
}
