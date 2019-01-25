package com.lambeta;

import org.sonar.api.Plugin;

public class CustomPlugin implements Plugin {
    public void define(Context context) {
        context.addExtension(CustomLanguage.class)
                .addExtension(CustomRulesDefinition.class)
                .addExtension(CustomProfileDefinition.class)
                .addExtension(CustomSensor.class);
    }
}
