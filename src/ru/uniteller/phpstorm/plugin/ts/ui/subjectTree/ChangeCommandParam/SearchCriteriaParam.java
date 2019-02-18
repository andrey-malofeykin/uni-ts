package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.ChangeCommandParam;

import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.lang.documentation.PhpClassMemberDocSource;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.DescriptionProvider;
import ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.NamedNode;
import ru.uniteller.phpstorm.plugin.ts.util.TestStandNavigationUtil;

import java.util.ArrayList;
import java.util.HashSet;


class SearchCriteriaParam extends NamedNode  {

    private class SearchCriteriaNode extends NamedNode implements DescriptionProvider {
        private String description;
        private TestStandNavigationUtil.ClassConst navigatable;
        SearchCriteriaNode(NamedNode aParent, String name, String description, TestStandNavigationUtil.ClassConst navigatable) {
            super(aParent, name);
            this.description = description;
            this.navigatable = navigatable;
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return new SimpleNode[0];
        }

        @Override
        public String getDescriptionSource() {
            return description;
        }

        @Nullable
        public Navigatable getNavigatable() {
            return TestStandNavigationUtil.createNavigatable(getProject(), navigatable);
        }

    }


    private TestStandNavigationUtil.MethodParam methodParamNavigation;
    private HashSet<String> searchCriteriaSource;

    SearchCriteriaParam(
            NamedNode aParent,
            TestStandNavigationUtil.MethodParam methodParamNavigation,
            HashSet<String> searchCriteriaSource
    ) {
        super(aParent, "Критерии поиска");
        this.methodParamNavigation = methodParamNavigation;
        this.searchCriteriaSource = searchCriteriaSource;
    }


    @Override
    protected SimpleNode[] buildChildren() {
        ArrayList<SearchCriteriaNode> nodes = new ArrayList<>();
        for (String source: searchCriteriaSource) {
            for (PhpClass sourcePhpClass: phpIndex.getInterfacesByFQN(source)) {
                for (Field field: sourcePhpClass.getFields()) {
                    @Nullable PsiElement defaultValue = field.getDefaultValue();
                    if (null == defaultValue || null == defaultValue.getText()) {
                        continue;
                    }
                    String criteriaName = defaultValue.getText();
                    @NotNull String description = (new PhpClassMemberDocSource(field)).getDescription();
                    TestStandNavigationUtil.ClassConst navigatable = new TestStandNavigationUtil.ClassConst(
                            field.getName(),
                            new TestStandNavigationUtil.ClassFQN(sourcePhpClass.getFQN())
                    );

                    nodes.add(new SearchCriteriaNode(this, criteriaName, description, navigatable));

                }

            }

        }
        return nodes.toArray(new SearchCriteriaNode[0]);
    }

    @Override
    public @Nullable Navigatable getNavigatable() {
        return TestStandNavigationUtil.createNavigatable(getProject(), methodParamNavigation);
    }
}
