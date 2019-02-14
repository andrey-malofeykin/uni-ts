package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.uniteller.phpstorm.plugin.ts.service.Config;

import java.util.Collection;
import java.util.HashMap;

public class RootNode extends NamedNode {
    private HashMap<String, SubjectNode> hashMapSubjects = new HashMap<>();

    public RootNode(Project project, Config config) {

        super(project, config, "Субъекты");
        updatePresentation();
    }

    private void updatePresentation() {
        PresentationData presentation = getPresentation();
        presentation.clear();
        presentation.addText("Субъекты", SimpleTextAttributes.REGULAR_ATTRIBUTES);
        update(presentation);
    }

    @Override
    final protected SimpleNode[] buildChildren() {
        hashMapSubjects.clear();

        Collection<PhpClass> subjInterfaces = phpIndex.getInterfacesByFQN(config.getSubjectInterfaceFQN());

        subjInterfaces.forEach(subjInterface -> phpIndex.getAllSubclasses(subjInterface.getFQN()).forEach(subjectClass -> {
            if (isSubjectClass(subjectClass)) {
                SubjectNode subjectNode = new  SubjectNode(this, subjectClass);
                hashMapSubjects.put(subjectNode.getSubjectShortName(), subjectNode);
            }
        }));

        return hashMapSubjects.values().toArray(new SimpleNode[0]);
    }

    public @Nullable SubjectNode getSubject(@NotNull String name) {
        return hashMapSubjects.get(name);
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
