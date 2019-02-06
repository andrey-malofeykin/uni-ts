package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.ui.treeStructure.CachingSimpleNode;
import com.intellij.openapi.project.Project;
import ru.uniteller.phpstorm.plugin.ts.service.Config;

abstract public class NamedNode extends CachingSimpleNode{
    final Config config;
    public NamedNode(NamedNode aParent, String name) {
        super(aParent);
        myName = name;
        this.config = aParent.config;
    }

    NamedNode(Project project, Config config, String name) {
        super(project, null);
        myName = name;
        this.config = config;
    }
}
