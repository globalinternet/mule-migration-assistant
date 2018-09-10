/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateExpression;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

/**
 * Migrate flow-ref components
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FlowRef extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "//mule:flow-ref";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Migrate flow-ref components";
  }

  public FlowRef() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    if (getExpressionMigrator().isWrapped(element.getAttributeValue("name"))) {
      // replace chars in DW
      migrateExpression(element.getAttribute("name"), expressionMigrator);

      String unwrappedExpression = expressionMigrator.unwrap(element.getAttributeValue("name"));
      if (!unwrappedExpression.startsWith("mel:")) {
        element.getAttribute("name").setValue(expressionMigrator.wrap("(" + unwrappedExpression
            + ") replace '/' with '\\\\' replace /\\[|\\{/ with '(' replace /\\]|\\}/ with ')' replace '#' with '_'"));
      }

      report.report(WARN, element, element,
                    "Make sure the expression used in the flow-ref already has the correct flow name and remove the replacements from this expression.");
    } else {
      element.setAttribute("name", element.getAttributeValue("name")
          .replaceAll("/", "\\\\")
          .replaceAll("\\[|\\{", "(")
          .replaceAll("\\]|\\}", ")")
          .replaceAll("#", "_"));
    }
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

}