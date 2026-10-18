package com.spendwise.service;

import com.spendwise.entity.User;

public interface CurrentUserProvider {
    User getCurrentUser();
    Long getCurrentUserId();
}
