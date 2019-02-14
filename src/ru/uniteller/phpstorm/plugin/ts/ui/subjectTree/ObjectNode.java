package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.pom.Navigatable;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;
import ru.uniteller.phpstorm.plugin.ts.util.TestStandNavigationUtil;

public class ObjectNode extends AbstractObjectNode implements DescriptionProvider {
    private String objName = "";
    private String objDescription = "";

    private String objectShortName;
    private TestStandNavigationUtil.ClassFQN classFQN;

    ObjectNode(SubjectObjectCollection aParent, PhpClass objClass) {
        super(aParent, objClass.getName(), objClass.getFQN());
        this.classFQN = new TestStandNavigationUtil.ClassFQN(objClass.getFQN());

        initObject(objClass);

        updatePresentation();


    }

    String getObjectShortName() {
        return objectShortName;
    }

    private void initObject(PhpClass objClass) {
        objectShortName = objClass.getName();


        if (null != objClass.getDocComment()) {
            String phpDocComment = PhpDocUtil.getDescription(objClass.getDocComment());
            int endFirstLine = phpDocComment.indexOf("\n");
            if (-1 == endFirstLine) {
                objName = phpDocComment.replaceAll("<.*?>", "");
            } else {
                objName =  phpDocComment.substring(0, endFirstLine).replaceAll("<.*?>", "");
                objDescription = phpDocComment.substring(endFirstLine + 1);
            }
        }
        objDescription = "<h1>" + objClass.getFQN() + "</h1>" + objDescription;
    }

    private void updatePresentation() {
        PresentationData presentation = getPresentation();
        presentation.clear();
        presentation.addText(objName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        update(presentation);
    }

    @Override
    protected SimpleNode[] buildChildren() {
        return super.buildChildren();
    }

    @Override
    public String getDescriptionSource() {
        return objDescription;
    }


    @Override
    public @Nullable Navigatable getNavigatable() {
        return TestStandNavigationUtil.createNavigatable(getProject(), classFQN);
    }
}
