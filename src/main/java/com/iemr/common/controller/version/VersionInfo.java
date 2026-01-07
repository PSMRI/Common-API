package com.iemr.common.controller.version;

public class VersionInfo {

    private String commitHash;
    private String buildTime;

    public VersionInfo(String commitHash, String buildTime) {
        this.commitHash = commitHash;
        this.buildTime = buildTime;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public String getBuildTime() {
        return buildTime;
    }
}
