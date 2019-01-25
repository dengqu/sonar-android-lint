/*
 * SonarQube Android Lint Plugin
 * Copyright (C) 2013-2016 SonarSource SA and Jerome Van Der Linden, Stephane Nicolas, Florian Roncari, Thomas Bores
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.lambeta;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.sslr.internal.matchers.TextUtils;

import java.io.File;
import java.util.List;

public class AndroidLintProcessor {
    private static Logger LOGGER = Loggers.get(Sensor.class);
    private static final String LINT_PATH = "\\build\\reports\\lint-results.xml";

    public AndroidLintProcessor() {
    }

    public void process(SensorContext context) {
        LOGGER.warn("Processing File workDir {} for baseDir {}", context.fileSystem().workDir().getAbsolutePath(), context.fileSystem().baseDir().getAbsolutePath());
        File lintXml = new File(context.fileSystem().baseDir() + LINT_PATH);
        process(context, lintXml);
    }

    public void process(SensorContext context, File lintXml) {
        Serializer serializer = new Persister();
        if (lintXml.exists()) {
            try {
                LOGGER.info("Processing android lint report: " + lintXml.getPath());
                FileUtil.printLog("Processing android lint report: " + lintXml.getPath());
                LintIssues lintIssues = serializer.read(LintIssues.class, lintXml);
                for (LintIssue lintIssue : lintIssues.issues) {
                    processIssue(context, lintIssue);
                }
            } catch (Exception e) {
                LOGGER.error("Exception reading " + lintXml.getPath(), e);
                FileUtil.printLog("Exception reading " + lintXml.getPath());
            }
        } else {
            LOGGER.warn("Processing android lint report: " + lintXml.getPath() + "is not exists");
        }
    }

    private void processIssue(SensorContext context, LintIssue lintIssue) {
        org.sonar.api.batch.rule.ActiveRule rule = context.activeRules().find(RuleKey.of(AndroidLintRulesDefinition.REPOSITORY_KEY, lintIssue.id));
        if (rule != null) {
            // LOGGER.warn("Processing Issue: {}", lintIssue.id);
            //FileUtil.printLog("Processing Issue: {}" + lintIssue.id);
            for (LintLocation lintLocation : lintIssue.locations) {
                processIssueForLocation(context, rule, lintIssue, lintLocation);
            }
        } else {
            //LOGGER.warn("Unable to find rule for {}", lintIssue.id);
            //FileUtil.printLog("Unable to find rule for {}" + lintIssue.id);
        }
    }

    private void processIssueForLocation(SensorContext context, ActiveRule rule, LintIssue lintIssue, LintLocation lintLocation) {
        InputFile inputFile = context.fileSystem().inputFile(context.fileSystem().predicates().hasPath(lintLocation.file));
        if (inputFile != null) {
            LOGGER.warn("Processing report File {} for Issue {}  line {}", lintLocation.file, lintIssue.id, lintLocation.line);
            try {
                final NewIssue newIssue = context.newIssue()
                        .forRule(rule.ruleKey());
                final NewIssueLocation newIssueLocation = newIssue
                        .newLocation()
                        .on(inputFile)
                        .message(lintIssue.message);
                if (lintLocation.line != null) {
                    newIssueLocation.at(inputFile.selectLine(lintLocation.line));
                }
                newIssue.at(newIssueLocation).save();
            } catch (Exception e) {
                LOGGER.error("Processing File error {} for Issue {}", lintLocation.file, lintIssue.id);
            }
            return;
        } else {
            //LOGGER.warn("Unable to find file {} to report issue", lintLocation.file);
        }
    }

    @Root(name = "location", strict = false)
    private static class LintLocation {
        @Attribute
        String file;
        @Attribute(required = false)
        Integer line;
        @Attribute(required = false)
        Integer column;
    }

    @Root(name = "issues", strict = false)
    private static class LintIssues {
        @ElementList(required = false, inline = true, empty = false)
        List<LintIssue> issues;
    }

    @Root(name = "issue", strict = false)
    private static class LintIssue {
        @Attribute
        String id;
        @Attribute
        String message;
        @ElementList(inline = true)
        List<LintLocation> locations;
    }

}
