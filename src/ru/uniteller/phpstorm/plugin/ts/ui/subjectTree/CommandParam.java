package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.psi.PsiElement;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocParamTag;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import com.jetbrains.php.lang.psi.elements.Parameter;
import org.jetbrains.annotations.Nullable;


public class CommandParam extends AbstractSubjectObject implements DescriptionProvider {
    private String paramName = "";
    private String paramDescription = "";


    CommandParam(SubjectCommand aParent, Parameter param) {
        super(aParent, param.getName(), null);
        initObject(param);
        updatePresentation();
    }

    private void initObject(Parameter param) {
        paramName = param.getName();
        if (null != param.getDocComment()) {
            @Nullable PhpDocParamTag paramTag = param.getDocComment().getParamTagByName(param.getName());


            if (null != paramTag) {
                PsiElement docMethodLastPart = paramTag.getLastChild();
                if (PhpPsiUtil.isOfType(docMethodLastPart, PhpDocElementTypes.DOC_METHOD_DESCRIPTION)) {
                    paramName = paramName + "(" + docMethodLastPart.getText() + ")";
                }
            }

        }




//        @NotNull PhpIndex index = PhpIndex.getInstance(Objects.requireNonNull(getProject()));
//        Optional<PhpClass> objOpt = index.getClassesByFQN(objClass).stream().findFirst();
//        if (!objOpt.isPresent()) {
//            return;
//        }
//        PhpClass objClass = objOpt.get();
//
//
//        if (null != objClass.getDocComment()) {
//            String phpDocComment = PhpDocUtil.getDescription(objClass.getDocComment());
//            int endFirstLine = phpDocComment.indexOf("\n");
//            if (-1 == endFirstLine) {
//                this.paramName = phpDocComment.replaceAll("<.*?>", "");
//            } else {
//                this.paramName =  phpDocComment.substring(0, endFirstLine).replaceAll("<.*?>", "");
//                this.paramDescription = phpDocComment.substring(endFirstLine + 1);
//            }
//        }
    }

    private void updatePresentation() {
        PresentationData presentation = getPresentation();
        presentation.clear();
        presentation.addText(paramName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        update(presentation);
    }

    @Override
    protected SimpleNode[] buildChildren() {
        return super.buildChildren();
    }

    @Override
    public String getDescriptionSource() {
        return paramDescription;
    }
}
