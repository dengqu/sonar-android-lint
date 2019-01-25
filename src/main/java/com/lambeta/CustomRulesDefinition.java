package com.lambeta;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;

import java.io.InputStream;

public class CustomRulesDefinition implements RulesDefinition {

    public static final String REPOSITORY_KEY = "custom-repo";
    private final RulesDefinitionXmlLoader xmlLoader;

    public CustomRulesDefinition(RulesDefinitionXmlLoader xmlLoader) {
        this.xmlLoader = xmlLoader;
    }

    public void define(Context context) {

        final InputStream stream = getClass().getResourceAsStream("/rules.xml");
        final NewRepository repository = context.createRepository(REPOSITORY_KEY, CustomLanguage.KEY);

        try {
            if (stream != null) {
                xmlLoader.load(repository, stream, Charsets.UTF_8);
            }
            repository.done();
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }
}
