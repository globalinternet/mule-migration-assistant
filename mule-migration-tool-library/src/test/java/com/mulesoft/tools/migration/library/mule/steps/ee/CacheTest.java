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
package com.mulesoft.tools.migration.library.mule.steps.ee;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
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
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Parameterized.class)
public class CacheTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path CACHE_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/ee");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "cache-01",
        "cache-02",
        "cache-03",
        "cache-04",
        "cache-05",
        "cache-06"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public CacheTest(String filePrefix) {
    configPath = CACHE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = CACHE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private CacheScope cacheScope;
  private CacheInvalidateKey cacheInvalidateKey;
  private CacheObjectStoreCachingStrategy cacheObjectStoreCachingStrategy;
  private CacheHttpCachingStrategy cacheHttpCachingStrategy;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), appModel);

    cacheScope = new CacheScope();
    cacheScope.setExpressionMigrator(expressionMigrator);
    cacheScope.setApplicationModel(appModel);

    cacheInvalidateKey = new CacheInvalidateKey();
    cacheInvalidateKey.setExpressionMigrator(expressionMigrator);
    cacheInvalidateKey.setApplicationModel(appModel);

    cacheObjectStoreCachingStrategy = new CacheObjectStoreCachingStrategy();
    cacheObjectStoreCachingStrategy.setExpressionMigrator(expressionMigrator);
    cacheObjectStoreCachingStrategy.setApplicationModel(appModel);

    cacheHttpCachingStrategy = new CacheHttpCachingStrategy();
    cacheHttpCachingStrategy.setExpressionMigrator(expressionMigrator);
    cacheHttpCachingStrategy.setApplicationModel(appModel);
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, cacheScope.getAppliedTo().getExpression())
        .forEach(node -> cacheScope.execute(node, report.getReport()));
    getElementsFromDocument(doc, cacheInvalidateKey.getAppliedTo().getExpression())
        .forEach(node -> cacheInvalidateKey.execute(node, report.getReport()));
    getElementsFromDocument(doc, cacheObjectStoreCachingStrategy.getAppliedTo().getExpression())
        .forEach(node -> cacheObjectStoreCachingStrategy.execute(node, report.getReport()));
    getElementsFromDocument(doc, cacheHttpCachingStrategy.getAppliedTo().getExpression())
        .forEach(node -> cacheHttpCachingStrategy.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
