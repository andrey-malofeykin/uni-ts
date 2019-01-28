package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import ru.uniteller.phpstorm.plugin.ts.util.PhpIndexUtil;

import java.util.*;
import java.util.stream.Stream;

public class SubjectNode extends NamedNode implements DescriptionProvider{
    /**
     * Имя субъекта
     */
    private String subjectName = "";
    /**
     * Описание субъекта
     */
    private String description = "";

    /**
     * Имя субъекта
     */
    private String subjectClassFQN;

    SubjectNode(NamedNode aParent, PhpClass subjectClass) {
        super(aParent, subjectClass.getName());
        this.subjectClassFQN = subjectClass.getFQN();
        initSubjectNode(subjectClass);
        myClosedIcon = AllIcons.General.BalloonInformation;
        updatePresentation();
    }


    private void initSubjectNode(PhpClass subjectClass) {
        @NotNull PhpIndex index = PhpIndex.getInstance(Objects.requireNonNull(getProject()));
        Optional<PhpClass> subjInterfaceOpt = index.getInterfacesByFQN(subjectClass.getFQN() + "Interface").stream().findFirst();
        if (!subjInterfaceOpt.isPresent()) {
            return;
        }
        PhpClass subjInterface  = subjInterfaceOpt.get();


        if (null != subjInterface.getDocComment()) {
            String phpDocComment = PhpDocUtil.getDescription(subjInterface.getDocComment());
            int endFirstLine = phpDocComment.indexOf("\n");
            if (-1 == endFirstLine) {
                this.subjectName = phpDocComment.replaceAll("<.*?>", "");
            } else {
                this.subjectName =  phpDocComment.substring(0, endFirstLine).replaceAll("<.*?>", "");
                description = phpDocComment.substring(endFirstLine + 1);
            }
        }
    }

    /**
     * Возвращает описание субъекта
     */
    public String getDescription() {
        return description;
    }

    private void updatePresentation() {
        PresentationData presentation = getPresentation();
        presentation.clear();
        presentation.addText(subjectName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        //presentation.addText(subjectName, new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.RED));
        //presentation.addText(" Level1Node", SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES);
        update(presentation);
    }


    @Override
    protected SimpleNode[] buildChildren() {
        return new SimpleNode[]{
                new SubjectObjects(this),
                new SubjectCommands(this)
        };
    }

    @Override
    public String getDescriptionSource() {
        return description;
    }


    class SubjectObjects extends NamedNode {
        SubjectObjects(SubjectNode aParent) {
            super(aParent, "objects");
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


    class SubjectCommands extends NamedNode {
        SubjectCommands(SubjectNode aParent) {
            super(aParent, "commands");
            updatePresentation();
        }

        private void updatePresentation() {
            PresentationData presentation = getPresentation();
            presentation.clear();
            presentation.addText("Команды", SimpleTextAttributes.REGULAR_ATTRIBUTES);
            update(presentation);
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return new SimpleNode[]{};
        }
    }
}
