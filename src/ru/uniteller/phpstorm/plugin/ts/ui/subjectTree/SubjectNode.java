package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.Optional;

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
     * Возвращает имя класса субъекта
     */
    String getSubjectClassFQN() {
        return subjectClassFQN;
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
                new SubjectObjectCollection(this),
                new SubjectCommandCollection(this)
        };
    }

    @Override
    public String getDescriptionSource() {
        return description;
    }




}
