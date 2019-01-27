package ru.uniteller.phpstorm.plugin.ts.ui;


import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.IndexComparator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.CachingSimpleNode;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeBuilder;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import org.jetbrains.annotations.NotNull;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import java.awt.Color;
import java.awt.event.InputEvent;
import java.util.*;
import java.util.stream.Stream;

import com.jetbrains.php.PhpIndex;
import ru.uniteller.phpstorm.plugin.ts.service.Config;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import ru.uniteller.phpstorm.plugin.ts.util.PhpIndexUtil;


public class SubjectsTreeStructure extends SimpleTreeStructure {
    private final RootNode myRoot;
    private final SimpleTreeBuilder myTreeBuilder;
    private Project project;
    private Config config;

    SubjectsTreeStructure(Project project, Config config, JTree tree) {
        this.project = project;
        this.config = config;
        this.myRoot = new RootNode();
        myTreeBuilder = new SimpleTreeBuilder(tree, (DefaultTreeModel) tree.getModel(), this, (Comparator<SimpleNode>) (o1, o2) -> {
            if(o1 instanceof NamedNode && o2 instanceof NamedNode)
                return o1.getName().compareTo(o2.getName());
            else
                return IndexComparator.INSTANCE.compare(o1, o2);
        });
        Disposer.register(project, myTreeBuilder);
        myTreeBuilder.initRoot();
        myTreeBuilder.expand(myRoot, null);
    }

    public void updateFromRoot(){
        myTreeBuilder.addSubtreeToUpdateByElement(myRoot);
    }

    @NotNull
    @Override
    public Object getRootElement() {
        return myRoot;
    }

    abstract class NamedNode extends CachingSimpleNode {
        NamedNode(SimpleNode aParent, String name) {
            super(aParent);
            myName = name;
        }
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


    public class RootNode extends NamedNode {
        RootNode() {
            super(null,"Субъекты");
        }

        @Override
        protected SimpleNode[] buildChildren() {
            PhpIndex phpIndex = PhpIndex.getInstance(project);
            Collection<PhpClass> subjInterfaces = phpIndex.getInterfacesByFQN(config.getSubjectInterfaceFQN());

            ArrayList<SubjectNode> subjectNodes = new ArrayList<>();
            subjInterfaces.forEach(subjInterface -> phpIndex.getAllSubclasses(subjInterface.getFQN()).forEach(subjectClass -> {
                if (isSubjectClass(subjectClass)) {
                    subjectNodes.add(new SubjectNode(this, subjectClass));
                }
            }));

            return subjectNodes.toArray(new SimpleNode[0]);
        }
    }

    public class SubjectNode extends NamedNode {
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

        /**
         * Имя класса субъекта
         */
        public String getSubjectClassFQN() {
            return subjectClassFQN;
        }

        SubjectNode(SimpleNode aParent, PhpClass subjectClass) {
            super(aParent, subjectClass.getName());
            this.subjectClassFQN = subjectClass.getFQN();
            initSubjectNode(subjectClass);
            myClosedIcon = AllIcons.General.BalloonInformation;
            updatePresentation();
        }

        private void initSubjectNode(PhpClass subjectClass) {
            @NotNull PhpIndex index = PhpIndex.getInstance(project);
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
        String getDescription() {
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
            return new SimpleNode[]{new SubjectObjects(this), new SubjectCommands(this)};
        }

    }

    public class SubjectCommands extends NamedNode {
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

    public class SubjectObjects extends NamedNode {
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
            SubjectNode subjectNode = (SubjectNode) getParent();
            String objNamespace = subjectNode.getSubjectClassFQN() + "\\Object";

            @NotNull Collection<PhpClass> objClasses = PhpIndexUtil.getPhpClassInsideNamespace(project, objNamespace);

            ArrayList<SubjectObject> objectNodes = new ArrayList<>();

            objClasses.forEach(objClass -> {
                if (objClass.isAbstract() || objClass.isInterface() || objClass.isTrait()) {
                    return;
                }
                if (objClass.getNamespaceName().equals(objNamespace)) {
                    return;
                }
                Stream<PhpClass> classImplObjInterface = Arrays.stream(objClass.getImplementedInterfaces()).filter(phpClass -> {
                    return phpClass.getFQN().equals(config.getObjInterface());
                });


                if (0 == classImplObjInterface.count()) {
                    return;
                }

                objectNodes.add(new SubjectObject(this, objClass.getName(), objClass.getFQN()));

            });


            return objectNodes.toArray(new SubjectObject[0]);
        }

        public class SubjectObject extends NamedNode {
            private String objClass;
            private String objName = "";
            private String objDescription = "";

            SubjectObject(SubjectObjects aParent, String name, String objClass) {
                super(aParent, name);
                this.objClass = objClass;
                initObject();
                updatePresentation();
            }

            private void initObject() {
                @NotNull PhpIndex index = PhpIndex.getInstance(project);
                Optional<PhpClass> objOpt = index.getClassesByFQN(this.objClass).stream().findFirst();
                if (!objOpt.isPresent()) {
                    return;
                }
                PhpClass objClass = objOpt.get();


                if (null != objClass.getDocComment()) {
                    String phpDocComment = PhpDocUtil.getDescription(objClass.getDocComment());
                    int endFirstLine = phpDocComment.indexOf("\n");
                    if (-1 == endFirstLine) {
                        this.objName = phpDocComment.replaceAll("<.*?>", "");
                    } else {
                        this.objName =  phpDocComment.substring(0, endFirstLine).replaceAll("<.*?>", "");
                        this.objDescription = phpDocComment.substring(endFirstLine + 1);
                    }
                }
            }

            private void updatePresentation() {
                PresentationData presentation = getPresentation();
                presentation.clear();
                presentation.addText(objName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
                update(presentation);
            }

            @Override
            protected SimpleNode[] buildChildren() {
                return new SimpleNode[0];
            }
        }
    }






    public class Level2Node extends NamedNode {
        private Color myColor = JBColor.RED;

        Level2Node(SimpleNode aParent, String name) {
            super(aParent, name);
            myClosedIcon = AllIcons.General.BalloonWarning;
            updatePresentation();
        }



        private void updatePresentation() {
            PresentationData presentation = getPresentation();
            presentation.clear();
            presentation.addText(myName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            presentation.addText(" Red", new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, myColor));
            presentation.addText(" Level2Node", SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES);
        }

        @Override
        public void handleDoubleClickOrEnter(SimpleTree tree, InputEvent inputEvent) {
            if(JBColor.RED.equals(myColor)){
                myColor = JBColor.BLUE;
            } else {
                myColor = JBColor.RED;
            }
            updatePresentation();
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return NO_CHILDREN;
        }
    }
}
