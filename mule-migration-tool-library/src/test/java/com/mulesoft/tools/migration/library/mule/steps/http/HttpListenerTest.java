/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationAttributes;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Parameterized.class)
public class HttpListenerTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path HTTP_LISTENER_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/http");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "http-listener-01",
        "http-listener-04",
        "http-listener-05",
        "http-listener-09",
        "http-listener-10",
        "http-listener-11",
        // TODO Multiheader support
        // "http-listener-12",
        "http-listener-13",
        "http-listener-14",
        // TODO Multiheader support
        // "http-listener-15",
        "http-listener-16",
        "http-listener-17",
        "http-listener-18",
        "http-listener-19",
        "http-listener-20"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public HttpListenerTest(String filePrefix) {
    configPath = HTTP_LISTENER_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = HTTP_LISTENER_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private HttpConnectorListenerConfig httpListenerConfig;
  private HttpConnectorListener httpListener;
  private HttpConnectorHeaders httpHeaders;
  private HttpGlobalBuilders httpGlobalBuilders;
  private RemoveSyntheticMigrationAttributes removeSyntheticMigrationAttributes;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    httpListenerConfig = new HttpConnectorListenerConfig();

    httpListener = new HttpConnectorListener();
    httpListener.setApplicationModel(appModel);
    httpListener.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));

    httpHeaders = new HttpConnectorHeaders();
    httpHeaders.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));

    httpGlobalBuilders = new HttpGlobalBuilders();
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Ignore
  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    httpListenerConfig.execute(null, report.getReport());
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, httpListenerConfig.getAppliedTo().getExpression())
        .forEach(node -> httpListenerConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, httpListener.getAppliedTo().getExpression())
        .forEach(node -> httpListener.execute(node, report.getReport()));
    getElementsFromDocument(doc, httpHeaders.getAppliedTo().getExpression())
        .forEach(node -> httpHeaders.execute(node, report.getReport()));
    getElementsFromDocument(doc, httpGlobalBuilders.getAppliedTo().getExpression())
        .forEach(node -> httpGlobalBuilders.execute(node, report.getReport()));
    getElementsFromDocument(doc, removeSyntheticMigrationAttributes.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationAttributes.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
