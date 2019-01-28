package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class SubjectObject extends NamedNode implements DescriptionProvider {
    private String objClass;
    private String objName = "";
    private String objDescription = "";

    SubjectObject(SubjectNode.SubjectObjects aParent, String name, String objClass) {
        super(aParent, name);
        this.objClass = objClass;
        initObject();
        updatePresentation();
    }

    private void initObject() {
        @NotNull PhpIndex index = PhpIndex.getInstance(Objects.requireNonNull(getProject()));
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

    @Override
    public String getDescriptionSource() {
        return objDescription;
    }
}
