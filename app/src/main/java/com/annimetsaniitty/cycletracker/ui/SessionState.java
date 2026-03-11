package com.annimetsaniitty.cycletracker.ui;

import com.annimetsaniitty.cycletracker.dto.UserResponse;

public class SessionState {
    private UserResponse currentUser;

    public UserResponse getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(UserResponse currentUser) {
        this.currentUser = currentUser;
    }

    public void clear() {
        currentUser = null;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }
}
