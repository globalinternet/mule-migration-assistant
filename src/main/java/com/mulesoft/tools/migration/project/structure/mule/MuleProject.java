/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.structure.mule;

import java.nio.file.Path;
import com.mulesoft.tools.migration.project.structure.JavaProject;
import com.mulesoft.tools.migration.project.structure.MavenProject;

/**
 * Represents a mule project structure
 *
 * @author Mulesoft Inc.
 */
public abstract class MuleProject extends MavenProject {


  public MuleProject(Path baseFolder) {
    super(baseFolder);
  }

  public abstract Path srcMainConfiguration();

  public abstract Path srcTestConfiguration();
}