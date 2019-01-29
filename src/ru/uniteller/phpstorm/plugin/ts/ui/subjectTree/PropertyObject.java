package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocParamTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import com.jetbrains.php.lang.psi.elements.Field;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class PropertyObject extends NamedNode implements DescriptionProvider{
    private String propertyName;
    private String propertyDescription = "";

    PropertyObject(SubjectObject aParent, Field property) {
        super(aParent, property.getName());
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
        return new SimpleNode[0];
    }



    static boolean isPropertyObject(@NotNull Field property) {

        return true;
    }

    @Override
    public String getDescriptionSource() {
        return propertyDescription;
    }
}
