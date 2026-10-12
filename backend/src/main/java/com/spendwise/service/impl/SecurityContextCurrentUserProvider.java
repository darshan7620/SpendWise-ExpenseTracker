package com.spendwise.service.impl;

import com.spendwise.entity.User;
import com.spendwise.exception.UnauthorizedException;
import com.spendwise.repository.UserRepository;
import com.spendwise.service.CurrentUserProvider;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextCurrentUserProvider implements CurrentUserProvider {
    private final UserRepository userRepository;

    public SecurityContextCurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("No authenticated user found in the security context");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user no longer exists"));
    }

    @Override
    public Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }
}
