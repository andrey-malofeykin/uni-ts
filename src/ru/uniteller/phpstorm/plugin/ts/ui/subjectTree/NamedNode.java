package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.ui.treeStructure.CachingSimpleNode;
import com.jetbrains.php.PhpIndex;
import org.jetbrains.annotations.Nullable;
import ru.uniteller.phpstorm.plugin.ts.service.Config;
import ru.uniteller.phpstorm.plugin.ts.util.TestStandNavigationUtil;

import java.util.Objects;

abstract public class NamedNode extends CachingSimpleNode{
    final Config config;
    final PhpIndex phpIndex;
    public NamedNode(NamedNode aParent, String name) {
        super(aParent);
        myName = name;
        this.config = aParent.config;
        phpIndex = PhpIndex.getInstance(Objects.requireNonNull(aParent.getProject()));

    }

    NamedNode(Project project, Config config, String name) {
        super(project, null);
        phpIndex = PhpIndex.getInstance(project);
        myName = name;
        this.config = config;
    }

    @Nullable
    public String getMenuId() {
        return null;
    }

    @Nullable
    public Navigatable getNavigatable() {
        return null;
    }
}
