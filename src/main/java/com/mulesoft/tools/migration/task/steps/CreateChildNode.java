package com.mulesoft.tools.migration.task.steps;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;

public class CreateChildNode extends MigrationStep {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CreateChildNode(String name) {
        setName(name);
    }

    public CreateChildNode(){}

    public void execute() throws Exception {
        try {
            if(!StringUtils.isBlank(name)) {
                for (Element node : getNodes()) {
                    Element child = new Element(getName());
                    child.setNamespace(node.getNamespace());
                    node.addContent(child);
                }
            }
        }catch (Exception ex) {
            throw new MigrationStepException("Create child node exception. " + ex.getMessage());
        }
    }

}