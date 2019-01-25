package com.lambeta;

import org.apache.commons.io.IOUtils;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.profiles.XMLProfileParser;
import org.sonar.api.utils.ValidationMessages;

import java.io.InputStreamReader;

public class CustomProfileDefinition extends ProfileDefinition {
    private final XMLProfileParser xmlProfileParser;

    public CustomProfileDefinition(XMLProfileParser xmlProfileParser) {
        this.xmlProfileParser = xmlProfileParser;
    }

    @Override
    public RulesProfile createProfile(ValidationMessages validation) {
        final InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("/profile.xml"));

        try {
            return xmlProfileParser.parse(reader, validation);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }
}
