package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjectProperty extends AbstractSubjectObject implements DescriptionProvider{
    private String propertyName;
    private String propertyDescription = "";

    ObjectProperty(AbstractSubjectObject aParent, Field property) {
        super(aParent, property);
        initProperty(property);
        updatePresentation();
    }


    private void initProperty(Field property) {
        @Nullable PhpDocComment docComment = property.getDocComment();
        propertyName = property.getName();

        if (null != docComment) {
            String phpDocComment = PhpDocUtil.getDescription(docComment);
            int endFirstLine = phpDocComment.indexOf("\n");
            if (-1 == endFirstLine) {
                propertyName = propertyName + ". " + phpDocComment.replaceAll("<.*?>", "");
            } else {
                propertyName =  propertyName + ". " + phpDocComment.substring(0, endFirstLine).replaceAll("<.*?>", "");
                propertyDescription = phpDocComment.substring(endFirstLine + 1);
            }
        }
    }

    private void updatePresentation() {
        PresentationData presentation = getPresentation();
        presentation.clear();
        presentation.addText(propertyName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        update(presentation);
    }

    @Override
    protected SimpleNode[] buildChildren() {
        return super.buildChildren();
    }


    static boolean isPropertyObject(@NotNull Field property) {
        @Nullable PhpClass containingClass = property.getContainingClass();
        if (null == containingClass) {
            return false;
        }
        String propertyName = property.getName();
        String getter = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);

        if (containingClass.getMethods().stream().noneMatch(method->method.getName().equals(getter))) {
            return false;
        }


        return true;
    }

    @Override
    public String getDescriptionSource() {
        return propertyDescription;
    }
}
