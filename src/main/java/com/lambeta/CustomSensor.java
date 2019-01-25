package com.lambeta;

import net.sourceforge.pmd.PMD;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CustomSensor implements Sensor {
    private static Logger LOG = Loggers.get(Sensor.class);

    private final CustomLanguage custom;

    public CustomSensor(CustomLanguage custom) {
        this.custom = custom;
    }

    public void describe(SensorDescriptor descriptor) {
        descriptor.name("CustomSensor")
                .onlyOnLanguage(custom.getKey());
    }

    public void execute(SensorContext context) {
        FileUtil.printLog("start run android lint analysis...");
        new AndroidLintProcessor().process(context, new File("E://lint-results.xml"));
        FileUtil.printLog("end run android lint analysis.");
        final File reportFile = new File(context.fileSystem().workDir(), "report.xml");

        LOG.info("start run pmd analysis...");
        runPMD(context, reportFile);
        LOG.info("end run pmd analysis.");

        try {
            LOG.debug("contents: \n{}", IOUtils.toString(new FileInputStream(reportFile)));
        } catch (IOException e) {
            LOG.error("fail to print content. error is {}", e);
        }
        LOG.info("start report...");
        convertToIssues(context, doc(reportFile));
        LOG.info("end report.");
    }

    private File getFile(SensorContext context, String path) {
        try {
            File file = new File(path);
            if (!file.isAbsolute()) {
                file = new File(context.fileSystem().baseDir(), path).getCanonicalFile();
            }
            return file;
        } catch (Exception e) {
            LOG.warn("Lint report not found, please set {} property to a correct value.", path);
            LOG.warn("Unable to resolve path : " + path, e);
        }
        return null;
    }

    private void convertToIssues(SensorContext context, Document doc) {
        final Element root = doc.getRootElement();
        final List<Element> files = root.elements("file");
        for (Element file : files) {

            final List<Element> violations = file.elements("violation");
            final String filePath = file.attributeValue("name");
            final FileSystem fs = context.fileSystem();
            final InputFile inputFile = fs.inputFile(fs.predicates().hasAbsolutePath(filePath));
            if (inputFile == null) {
                LOG.info("fs predicates that there is no {}", filePath);
                continue;
            }
            for (Element violation : violations) {
                final String rule = violation.attributeValue("rule");
                final int beginLine = Integer.parseInt(violation.attributeValue("beginline"));
                final int endLine = Integer.parseInt(violation.attributeValue("endline"));
                final int beginColumn = Integer.parseInt(violation.attributeValue("begincolumn"));
                final int endColumn = Integer.parseInt(violation.attributeValue("endcolumn"));
                final NewIssue newIssue = context.newIssue()
                        .forRule(RuleKey.of(CustomRulesDefinition.REPOSITORY_KEY, rule));
                final NewIssueLocation newIssueLocation = newIssue
                        .newLocation()
                        .on(inputFile)
                        .at(inputFile.newRange(beginLine, beginColumn, endLine, endColumn))
                        .message(violation.getText());
                newIssue.at(newIssueLocation).save();
            }
        }
    }

    private Document doc(File reportFile) {
        Document doc = null;
        try {
            doc = new SAXReader().read(reportFile);
        } catch (DocumentException e) {
            LOG.error("Cannot read report xml file: {}.", reportFile);
        }
        return doc;
    }

    private void runPMD(SensorContext context, File reportFile) {
        final String dir = context.settings().getString("sonar.sources");
        final File file = new File(dir);
        LOG.info("files listed here: {}", Arrays.toString(file.listFiles()));
        String[] pmdArgs = {
                "-f", "xml",
                "-R", "custom-pmd-rules.xml",
                "-d", dir,
                "-r", reportFile.getAbsolutePath(),
                "-e", context.settings().getString("sonar.sourceEncoding"),
                "-language", "xml",
                "-version", "1.0"
        };

        LOG.info("the pmd args are: {}.", Arrays.toString(pmdArgs));
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            PMD.run(pmdArgs);
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }
    }
}
