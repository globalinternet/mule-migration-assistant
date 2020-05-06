/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.transformers;

import com.mulesoft.tools.migration.library.mule.steps.core.ByteArrayToStringTransformer;
import com.mulesoft.tools.migration.tck.ReportVerification;
import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

@RunWith(Parameterized.class)
public class ByteArrayToStringTransformerTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path XML_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/transformers");

  @Parameterized.Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "byte-array-to-string"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public ByteArrayToStringTransformerTest(String xmlPrefix) {
    configPath = XML_CONFIG_EXAMPLES_PATH.resolve(xmlPrefix + "-original.xml");
    targetPath = XML_CONFIG_EXAMPLES_PATH.resolve(xmlPrefix + ".xml");
  }

  private ByteArrayToStringTransformer byteArrayToStringTransformer;

  @Before
  public void setUp() throws Exception {
    byteArrayToStringTransformer = new ByteArrayToStringTransformer();
  }

  @Test
  public void execute() throws Exception {
    Document doc =
        getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    getElementsFromDocument(doc, byteArrayToStringTransformer.getAppliedTo().getExpression())
        .forEach(node -> byteArrayToStringTransformer.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
    report.expectReportEntry("expressionTransformer.deprecated");
  }
}