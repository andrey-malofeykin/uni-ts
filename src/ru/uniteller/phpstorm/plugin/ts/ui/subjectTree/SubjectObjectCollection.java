package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import ru.uniteller.phpstorm.plugin.ts.util.PhpIndexUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;


class SubjectObjectCollection extends NamedNode {
    /**
     * Имя субъекта
     */
    private String subjectClassFQN;

    SubjectObjectCollection(SubjectNode aParent) {
        super(aParent, "objects");
        subjectClassFQN = aParent.getSubjectClassFQN();
        updatePresentation();
    }

    private void updatePresentation() {
        PresentationData presentation = getPresentation();
        presentation.clear();
        presentation.addText("Объекты", SimpleTextAttributes.REGULAR_ATTRIBUTES);
        update(presentation);
    }

    @Override
    protected SimpleNode[] buildChildren() {
        String objNamespace = subjectClassFQN + "\\Object";

        @NotNull Collection<PhpClass> objClasses = PhpIndexUtil.getPhpClassInsideNamespace(Objects.requireNonNull(getProject()), objNamespace);

        ArrayList<SubjectObject> objectNodes = new ArrayList<>();

        objClasses.forEach(objClass -> {
            if (objClass.isAbstract() || objClass.isInterface() || objClass.isTrait()) {
                return;
            }
            if (objClass.getNamespaceName().equals(objNamespace)) {
                return;
            }
            Stream<PhpClass> classImplObjInterface = Arrays.stream(objClass.getImplementedInterfaces()).filter(
                    phpClass -> phpClass.getFQN().equals(config.getObjInterface())
            );


            if (0 == classImplObjInterface.count()) {
                return;
            }


            objectNodes.add(new SubjectObject(this, objClass.getName(), objClass.getFQN()));

        });


        return objectNodes.toArray(new SubjectObject[0]);
    }
}
