package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.pom.Navigatable;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.uniteller.phpstorm.plugin.ts.util.PhpDocUtil;
import ru.uniteller.phpstorm.plugin.ts.util.TestStandNavigationUtil;

import java.util.*;
import java.util.regex.Matcher;

public class ReadDomainCommand extends NamedNode implements DescriptionProvider {
    private String commandName = "";
    private String commandDescription = "";
    private String returnClassFqn;
    private boolean hasSearchCriteria = false;
    private TestStandNavigationUtil.ClassDocMethod classDocMethod;


    ReadDomainCommand(SubjectCommandCollection aParent,  SubjectCommandCollection.ReadDomainCommandData data) {
        super(aParent, data.getMethodName());
        classDocMethod = new TestStandNavigationUtil.ClassDocMethod(
                data.getPhpDocMethod().getName(),
                new TestStandNavigationUtil.ClassFQN(
                        Objects.requireNonNull(data.getPhpDocMethod().getContainingClass()).getFQN()
                )
        );
        if (data.getPhpDocMethod().getName().startsWith("find") && 1 == data.getPhpDocMethod().getParameters().length) {
            hasSearchCriteria = true;
        }

        if (null != data.getCommandName()) {
            commandName = data.getMethodName() + data.getCommandName();
            commandDescription = data.getCommandName();
        }
        returnClassFqn = data.getPhpDocMethod().getDeclaredType().toString().replaceAll("\\[ *] *$", "");
        updatePresentation();
    }

    private void updatePresentation() {
        PresentationData presentation = getPresentation();
        presentation.clear();
        presentation.addText(commandName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        update(presentation);
    }


    @Override
    protected SimpleNode[] buildChildren() {
        @NotNull Collection<PhpClass> returnClasses = phpIndex.getAnyByFQN(returnClassFqn);
        if (0 == returnClasses.size()) {
            return new NamedNode[0];
        }
        PhpClass returnClass = returnClasses.iterator().next();


        ArrayList<NamedNode> childNodes = new ArrayList<>();
        String returnObjName = returnClass.getName();

//        if (null != returnClass.getDocComment()) {
//            PhpDocUtil.DocCommentInfo docCommentInfo = PhpDocUtil.buildDocCommentInfo(returnClass.getDocComment());
//            returnObjName += " " + docCommentInfo.getName();
//        }

        childNodes.add(new ReturnNode(this, "Возвращает: " + returnObjName, returnClass.getFQN()));
        if (hasSearchCriteria) {
            childNodes.add(new SearchCriteria(this, returnClass.getFQN()));
        } else {
            childNodes.add(new CommandParams(this, returnClass.getFQN()));
        }


        return childNodes.toArray(new NamedNode[0]);

    }

    private class CommandParams extends NamedNode {

        CommandParams(NamedNode aParent, String name) {
            super(aParent, "Параметры");
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return new SimpleNode[0];
        }
    }



    private class CriteriaInfo {
        private TestStandNavigationUtil.ClassMethod navigate;
        private String description;

        CriteriaInfo(@NotNull TestStandNavigationUtil.ClassMethod navigate, String description) {
            this.navigate = navigate;
            this.description = description;
        }

        TestStandNavigationUtil.ClassMethod getNavigate() {
            return navigate;
        }

        String getDescription() {
            return description;
        }
    }


    private class CriteriaNode extends NamedNode implements DescriptionProvider {
        private CriteriaInfo info;
        CriteriaNode(NamedNode aParent, String name, @NotNull CriteriaInfo info) {
            super(aParent, name);
            this.info = info;
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return new SimpleNode[0];
        }

        @Override
        public @Nullable Navigatable getNavigatable() {
            return TestStandNavigationUtil.createNavigatable(getProject(), info.getNavigate());
        }

        @Override
        public String getDescriptionSource() {
            return info.getDescription();
        }
    }


    private class SearchCriteria extends NamedNode {
        private String objClassFQN;
        SearchCriteria(NamedNode aParent, String objClassFQN) {
            super(aParent, "Критерии поиска");
            this.objClassFQN = objClassFQN;
        }

        @Override
        protected CriteriaNode[] buildChildren() {
            @NotNull Collection<PhpClass> returnClasses = phpIndex.getAnyByFQN(objClassFQN);
            if (0 == returnClasses.size()) {
                return new CriteriaNode[0];
            }
            PhpClass returnClass = returnClasses.iterator().next();
            HashMap<String, CriteriaInfo> gettersCollection = new HashMap<>();
            buildData(returnClass, gettersCollection);

            ArrayList<CriteriaNode> child = new ArrayList<>();

            for (String criteriaName: gettersCollection.keySet()) {
                CriteriaInfo navigation = gettersCollection.get(criteriaName);
                child.add(new CriteriaNode(this, criteriaName, navigation));
            }

            return child.toArray(new CriteriaNode[0]);
        }


        private void buildData(PhpClass phpClass, HashMap<String, CriteriaInfo> gettersCollection) {
            phpClass.getMethods().forEach(methodObject -> {
                String methodName = methodObject.getName();

                if (null != gettersCollection.get(methodName)) {
                    return;
                }
                if (methodObject.getParameters().length > 0) {
                    return;
                }
                if (!methodName.startsWith("get") || methodName.length() <= 3) {
                    return;
                }
                String criteriaName = methodName.substring(3);
                criteriaName = criteriaName.substring(0, 1).toLowerCase() + criteriaName.substring(1);

                TestStandNavigationUtil.ClassMethod navigation = new TestStandNavigationUtil.ClassMethod(
                        methodName,
                        new TestStandNavigationUtil.ClassFQN(phpClass.getFQN())
                );

                CriteriaInfo criteriaInfo = new CriteriaInfo(navigation, buildCriteriaDescription(methodObject));

                gettersCollection.put(criteriaName, criteriaInfo);
            });

            @Nullable PhpClass superClass = phpClass.getSuperClass();
            if (null != superClass) {
                buildData(superClass, gettersCollection);
            }

            for (PhpClass implementedInterface: phpClass.getImplementedInterfaces()) {
                buildData(implementedInterface, gettersCollection);
            }

            for (PhpClass trait: phpClass.getTraits()) {
                buildData(trait, gettersCollection);
            }
        }



    }

    private String buildCriteriaDescription(Method method) {
        String description = "";
        PhpDocComment docComment = null;

        if (null != method.getContainingClass()) {
            String methodName = method.getName();
            String propertyName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
            @Nullable PhpClass ownClass = method.getContainingClass();
            if (null != ownClass) {
                @Nullable Field property = ownClass.findFieldByName(propertyName, false);
                if (null != property && null != property.getDocComment()) {
                    docComment = property.getDocComment();
                }
            }
        }

        if (null == docComment) {
            docComment = method.getDocComment();
        }

        if (null != docComment) {
            PhpDocUtil.DocCommentInfo docCommentInfo = PhpDocUtil.buildDocCommentInfo(docComment);
            description += "<h1>" + docCommentInfo.getName() + "</h1>";
            if (!docCommentInfo.getDescription().equals("")) {
                description += "<h1>Описание</h1><p>" +  docCommentInfo.getDescription() + "</p>";
            }
        }

        @NotNull Set<String> types = method.getType().getTypes();
        if (types.size() > 0) {
            description += "<h1>Тип данных</h1><p>" + String.join("|", types) + "</p>";
        }



        return description;
    }



    private class ReturnNode extends NamedNode implements ExpandTreeNodeProvider {
        private TargetInfo expandTarget = null;
        private TestStandNavigationUtil.ClassFQN returnClassFQN;
        ReturnNode(NamedNode aParent, String name, String returnClassFQN) {
            super(aParent, name);
            this.returnClassFQN = new  TestStandNavigationUtil.ClassFQN(returnClassFQN);
            Matcher mathRes = config.getPatternObjInfo().matcher(returnClassFQN);
            if (mathRes.find()) {
                expandTarget = new TargetInfo(mathRes.group("SUBJECT"), mathRes.group("OBJ"));

            }
            updatePresentation();
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return new NamedNode[0];
        }

        private void updatePresentation() {
            PresentationData presentation = getPresentation();
            presentation.clear();

            SimpleTextAttributes attr;
            if (null != expandTarget) {
                attr = SimpleTextAttributes.LINK_ATTRIBUTES;
            } else {
                attr = SimpleTextAttributes.REGULAR_ATTRIBUTES;
            }
            presentation.addText(getName(), attr);

            update(presentation);
        }

        @Override
        public @Nullable ExpandTreeNodeProvider.TargetInfo getTargetInfo() {
            return expandTarget;
        }


        @Override
        public @Nullable Navigatable getNavigatable() {
            return TestStandNavigationUtil.createNavigatable(getProject(), returnClassFQN);
        }
    }



    @Override
    public String getDescriptionSource() {
        return commandDescription;
    }

    @Override
    public @Nullable Navigatable getNavigatable() {
        return TestStandNavigationUtil.createNavigatable(getProject(), classDocMethod);
    }
}
