package com.lambeta;

import org.sonar.api.Plugin;

public class AndroidLintPlugin implements Plugin {
    public void define(Context context) {
        context.addExtensions(AndroidLintRulesDefinition.class, AndroidLintSensor.class
        );
    }
}
