package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.pom.Navigatable;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.Field;
import org.jetbrains.annotations.Nullable;
import ru.uniteller.phpstorm.plugin.ts.util.TestStandNavigationUtil;

class ObjectConstant extends NamedNode implements DescriptionProvider{
    private String constantName;
    private String constantDescription = "";

    private TestStandNavigationUtil.ClassConst classField;

    ObjectConstant(AbstractObjectNode aParent, Field property) {
        super(aParent, property.getName());
        initProperty(property);
        updatePresentation();

        if (null != property.getContainingClass()) {
            TestStandNavigationUtil.ClassFQN classFQN = new TestStandNavigationUtil.ClassFQN(property.getContainingClass().getFQN());
            classField = new TestStandNavigationUtil.ClassConst(property.getName(), classFQN);
        }
    }


    private void initProperty(Field property) {
        @Nullable PhpDocComment docComment = property.getDocComment();
        constantName = property.getName();

        if (null != docComment) {
            String phpDocComment = PhpDocUtil.getDescription(docComment);
            int endFirstLine = phpDocComment.indexOf("\n");

            String defaultValue = property.getDefaultValuePresentation();

            if (null != defaultValue) {
                constantDescription = "<h1>Значение</h1><p>" + defaultValue + "</p>";
            }

            if (-1 == endFirstLine) {
                constantName = constantName + ". " + phpDocComment.replaceAll("<.*?>", "");
            } else {
                constantName =  constantName + ". " + phpDocComment.substring(0, endFirstLine).replaceAll("<.*?>", "");
                constantDescription = constantDescription + phpDocComment.substring(endFirstLine + 1);
            }
        }
    }

    private void updatePresentation() {
        PresentationData presentation = getPresentation();
        presentation.clear();
        presentation.addText(constantName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        update(presentation);
    }

    @Override
    protected SimpleNode[] buildChildren() {
        return new SimpleNode[0];
    }

    @Override
    public String getDescriptionSource() {
        return constantDescription;
    }

    @Override
    public @Nullable Navigatable getNavigatable() {
        return TestStandNavigationUtil.createNavigatable(getProject(), classField);
    }
}
