package com.java22d.itsakerhet.DTO;

public class BruteForceStatistics {
    private int failedAttempts;
    private boolean isUserCompromised;

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public boolean isUserCompromised() {
        return isUserCompromised;
    }

    public void setUserCompromised(boolean userCompromised) {
        isUserCompromised = userCompromised;
    }
}