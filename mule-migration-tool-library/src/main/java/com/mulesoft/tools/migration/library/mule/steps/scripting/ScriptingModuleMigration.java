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
package com.mulesoft.tools.migration.library.mule.steps.scripting;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Update scripting module.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ScriptingModuleMigration extends AbstractApplicationModelMigrationStep {

  protected static final String SCRIPT_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/scripting";
  private static final String SCRIPT_NAMESPACE_PREFIX = "scripting";
  protected static final Namespace SCRIPT_NAMESPACE = Namespace.getNamespace(SCRIPT_NAMESPACE_PREFIX, SCRIPT_NAMESPACE_URI);

  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + SCRIPT_NAMESPACE_URI + "' and local-name()='component']";

  public ScriptingModuleMigration() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(SCRIPT_NAMESPACE));
  }

  @Override
  public String getDescription() {
    return "Update scripting module.";
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    changeNodeName("scripting", "execute").apply(element);

    Element scriptNode = element.getChildren("script", element.getNamespace()).size() > 0
        ? element.getChildren("script", element.getNamespace()).get(0)
        : null;
    if (scriptNode != null) {
      changeNodeName("scripting", "code").apply(scriptNode);
      Attribute attribute = scriptNode.getAttribute("engine");
      if (attribute != null) {
        attribute.setValue(updateEngineValue(attribute.getValue()).toLowerCase());
        scriptNode.removeAttribute(attribute);
        element.setAttribute(attribute);
      } else {
        element.setAttribute("engine", "groovy");
      }
      attribute = scriptNode.getAttribute("file");
      if (attribute != null) {
        scriptNode.addContent("${file::" + attribute.getValue() + "}");
        scriptNode.removeAttribute(attribute);
      } else {
        handleCode(scriptNode);
      }
      movePropertiesToMap(scriptNode);
    }

    element.removeAttribute("name");

    report.report("scripting.messageFormat", element, element);
  }

  protected void handleCode(Element scriptNode) {
    // Nothing to do
  }

  private String updateEngineValue(String engine) {
    if (engine.equalsIgnoreCase("jruby")) {
      return "ruby";
    } else if (engine.equalsIgnoreCase("javascript")) {
      return "nashorn";
    } else {
      return engine;
    }
  }

  private void movePropertiesToMap(Element scriptNode) {
    Map<String, String> scriptParameters = new HashMap<>();
    List<Element> childsToRemove = new ArrayList<>();
    if (scriptNode.getChildren().size() > 0) {
      scriptNode.getChildren().forEach(p -> {
        scriptParameters.put(p.getAttributeValue("key"), p.getAttributeValue("value"));
        childsToRemove.add(p);
      });
      childsToRemove.forEach(s -> s.getParent().removeContent(s));

      Element parametersElement = new Element("parameters", scriptNode.getNamespace());

      Gson gson = new GsonBuilder().create();
      String jsonMap = gson.toJson(scriptParameters);

      parametersElement.addContent("#[" + jsonMap + "]");
      scriptNode.getParentElement().addContent(parametersElement);
    }
  }
}
