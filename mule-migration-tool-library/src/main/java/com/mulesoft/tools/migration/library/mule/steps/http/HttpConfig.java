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

import static com.google.common.collect.Lists.newArrayList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the http and https connector of the http transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpConfig extends AbstractApplicationModelMigrationStep {

  private static final String HTTP_NAMESPACE_PREFIX = "http";
  private static final String HTTP_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/http";
  private static final Namespace HTTP_NAMESPACE = Namespace.getNamespace(HTTP_NAMESPACE_PREFIX, HTTP_NAMESPACE_URI);
  private static final String HTTPS_NAMESPACE_PREFIX = "https";
  private static final String HTTPS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/https";
  private static final Namespace HTTPS_NAMESPACE = Namespace.getNamespace(HTTPS_NAMESPACE_PREFIX, HTTPS_NAMESPACE_URI);
  public static final String XPATH_SELECTOR =
      "/*/*[(namespace-uri() = '" + HTTP_NAMESPACE_URI + "' or namespace-uri() = '" + HTTPS_NAMESPACE_URI
          + "') and local-name() = 'connector']";

  @Override
  public String getDescription() {
    return "Update http and https connector config.";
  }

  public HttpConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(HTTP_NAMESPACE, HTTPS_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.detach();
  }
}
