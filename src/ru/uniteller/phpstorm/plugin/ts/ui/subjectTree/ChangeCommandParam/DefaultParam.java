package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.ChangeCommandParam;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.AbstractObjectNode;
import ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.ChangeDomainCommand;
import ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.DescriptionProvider;


public class DefaultParam extends AbstractObjectNode implements DescriptionProvider  {
    private String paramName;
    private String paramDescription;

    DefaultParam(ChangeDomainCommand aParent, @NotNull String paramName, @Nullable String paramDescription, @Nullable String classFqn) {
        super(aParent, paramName,  classFqn);
        this.paramName = paramName;
        this.paramDescription = paramDescription;
        updatePresentation();
    }

    private void updatePresentation() {
        PresentationData presentation = getPresentation();
        presentation.clear();
        presentation.addText("[DTA]", new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.GRAY));
        presentation.addText(paramName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        update(presentation);
    }

    @Override
    protected SimpleNode[] buildChildren() {
        return  super.buildChildren();
    }

    @Override
    public String getDescriptionSource() {
        return paramDescription;
    }
}
