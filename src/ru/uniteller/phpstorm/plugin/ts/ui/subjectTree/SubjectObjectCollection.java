package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import ru.uniteller.phpstorm.plugin.ts.util.PhpIndexUtil;

import java.util.*;
import java.util.stream.Stream;


class SubjectObjectCollection extends NamedNode {
    /**
     * Имя субъекта
     */
    private String subjectClassFQN;

    private HashMap<String, ObjectNode> subjectObjectStorage;


    SubjectObjectCollection(SubjectNode aParent, HashMap<String, ObjectNode> subjectObjectStorage) {
        super(aParent, "Объекты");
        subjectClassFQN = aParent.getSubjectClassFQN();
        this.subjectObjectStorage = subjectObjectStorage;
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
        subjectObjectStorage.clear();

        @NotNull Collection<PhpClass> objClasses = PhpIndexUtil.getPhpClassInsideNamespace(Objects.requireNonNull(getProject()), objNamespace);

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

            ObjectNode subjectObject = new ObjectNode(this, objClass);
            subjectObjectStorage.put(subjectObject.getObjectShortName(), subjectObject);

        });


        return subjectObjectStorage.values().toArray(new ObjectNode[0]);
    }
}
