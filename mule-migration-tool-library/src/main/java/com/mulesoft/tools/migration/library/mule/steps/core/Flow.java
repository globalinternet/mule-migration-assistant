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
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.isErrorHanldingElement;
import static java.util.Collections.reverse;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Migrate flow definitions
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Flow extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "/*/mule:flow";

  @Override
  public String getDescription() {
    return "Migrate flow definitions";
  }

  public Flow() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    element.setAttribute("name", element.getAttributeValue("name")
        .replaceAll("/", "\\\\")
        .replaceAll("\\[|\\{", "(")
        .replaceAll("\\]|\\}", ")")
        .replaceAll("#", "_"));

    Attribute processingStrategy = element.getAttribute("processingStrategy");
    if (processingStrategy != null) {
      if ("synchronous".equals(processingStrategy.getValue())) {
        element.setAttribute("maxConcurrency", "1");
      } else {
        Element processingStrategyConfig = getApplicationModel().getNode("//*[@name = '" + processingStrategy.getValue() + "']");
        if (processingStrategyConfig != null) {
          processingStrategyConfig.detach();
          if (processingStrategyConfig.getAttribute("maxThreads") != null) {
            element.setAttribute("maxConcurrency", processingStrategyConfig.getAttribute("maxThreads").getValue());
          }
        }
      }

      element.removeAttribute(processingStrategy);
      report.report("flow.processingStrategy", element, element);
    }

    List<Element> responses = new ArrayList<>(element.getChildren("response", CORE_NAMESPACE));
    reverse(responses);

    for (Element response : responses) {
      Element wrappingTry = new Element("try", CORE_NAMESPACE);

      new ArrayList<>(element.getContent().subList(element.indexOf(response) + 1, element.getContentSize())).forEach(c -> {
        if (c instanceof Element && !isErrorHanldingElement((Element) c)) {
          c.detach();
          wrappingTry.addContent(c);
        }
      });

      addElementAfter(wrappingTry, response);

      List<Content> content = response.cloneContent();
      response.detach();
      element.addContent(content);
    }
  }

}
