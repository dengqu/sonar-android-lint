package com.lambeta;

import org.sonar.api.resources.AbstractLanguage;

public class CustomLanguage extends AbstractLanguage {

    public static final String KEY = "custom-key";
    public static final String NAME = "custom-name";

    public CustomLanguage() {
        super(KEY, NAME);
    }

    public String[] getFileSuffixes() {
        return new String[] {"csm.xml"};
    }
}
