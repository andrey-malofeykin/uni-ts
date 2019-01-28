package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import ru.uniteller.phpstorm.plugin.ts.service.Config;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import com.intellij.openapi.project.Project;

public class RootNode extends NamedNode {


    public RootNode(Project project, Config config) {
        super(project, config, "Root");
    }

    @Override
    final protected SimpleNode[] buildChildren() {
        Project project = getProject();
        PhpIndex phpIndex = PhpIndex.getInstance(Objects.requireNonNull(project));

        Collection<PhpClass> subjInterfaces = phpIndex.getInterfacesByFQN(config.getSubjectInterfaceFQN());

        ArrayList<SubjectNode> subjectNodes = new ArrayList<>();
        subjInterfaces.forEach(subjInterface -> phpIndex.getAllSubclasses(subjInterface.getFQN()).forEach(subjectClass -> {
            if (isSubjectClass(subjectClass)) {
                subjectNodes.add(new SubjectNode(this, subjectClass));
            }
        }));

        return subjectNodes.toArray(new SimpleNode[0]);
    }

    /**
     * Проверят что класс является субъектом
     * @param subjectClass - кандидат на то что бы быть классом субъекта
     */
    private boolean isSubjectClass(PhpClass subjectClass) {
        if (subjectClass.isInterface() || subjectClass.isAbstract()) {
            return false;
        }

        if (!subjectClass.getNamespaceName().equals(config.getSubjectNamespace())) {
            return false;
        }

        return 1 == subjectClass.getProject().getComponent(PhpIndex.class).getInterfacesByFQN(subjectClass.getFQN() + "Interface").size();
    }
}
