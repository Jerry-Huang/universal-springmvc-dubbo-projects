package com.universal;

import org.apache.commons.lang3.StringUtils;

public enum Environment {

                         DEV("dev"),
                         TST("tst"),
                         UAT("uat"),
                         PRD("prd");

    private String environment;

    private Environment(final String environment) {
        this.environment = environment;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    @Override
    public String toString() {
        return this.environment;
    }

    private static Environment currentEnvironment;

    public static Environment current() {

        if (currentEnvironment != null) {
            return currentEnvironment;
        }

        final String applicationEnvironment = System.getProperty("application.environment");

        if (StringUtils.isNotBlank(applicationEnvironment)) {

            for (Environment environment : values()) {
                if (environment.getEnvironment().equalsIgnoreCase(applicationEnvironment)) {
                    currentEnvironment = environment;
                    return environment;
                }
            }
        }

        return Environment.DEV;
    }
}
