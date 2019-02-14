package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.ChangeCommandParam;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.DescriptionProvider;
import ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.ExpandTreeNodeProvider;
import ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.NamedNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class SmartParam extends NamedNode implements DescriptionProvider, ExpandTreeNodeProvider {
    private String paramName;
    private String paramDescription;
    private HashMap<String, ChangeCommandParamFactory.CommandParamInfo> commandParamInfoCollections;
    private @Nullable ExpandTreeNodeProvider.TargetInfo targetInfo;

    SmartParam(
            NamedNode aParent,
            @NotNull String paramName,
            @Nullable String paramDescription,
            @NotNull HashMap<String, ChangeCommandParamFactory.CommandParamInfo> commandParamInfoCollections) {
        super(aParent, paramName);
        this.paramName = paramName;
        this.paramDescription = paramDescription;
        this.commandParamInfoCollections = commandParamInfoCollections;
        if (1 == commandParamInfoCollections.values().size()) {
            ChangeCommandParamFactory.CommandParamInfo commandParamInfo = commandParamInfoCollections.values().iterator().next();
            if (null != commandParamInfo.getTargetInfo()) {
                this.targetInfo = commandParamInfo.getTargetInfo();
            }
        }

        updatePresentation();
    }

    @Override
    public @Nullable ExpandTreeNodeProvider.TargetInfo getTargetInfo() {
        return targetInfo;
    }

    private void updatePresentation() {
        PresentationData presentation = getPresentation();
        presentation.clear();

        presentation.addText("[", SimpleTextAttributes.REGULAR_ATTRIBUTES);
        HashSet<String> prefix = new HashSet<>();

        for (ChangeCommandParamFactory.CommandParamInfo commandParamInfo:  commandParamInfoCollections.values()) {
            TypeParamInterface.TypeParam  typeCommandParam = commandParamInfo.getType();
            if (typeCommandParam.equals(TypeParamInterface.TypeParam.OBJ ) && !prefix.contains("OBJ")) {
                presentation.addText("OBJ", new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.GREEN));
                prefix.add("OBJ");
            } else if (typeCommandParam.equals(TypeParamInterface.TypeParam.SBJ )&& !prefix.contains("SBJ") ) {
                presentation.addText("SBJ", new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.BLUE));
                prefix.add("SBJ");
            } else if (typeCommandParam.equals(TypeParamInterface.TypeParam.DATA )  && !prefix.contains("DTA") ) {
                presentation.addText("DTA", new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.GRAY));
                prefix.add("DTA");
            } else if (typeCommandParam.equals(TypeParamInterface.TypeParam.DEFAULT ) && !prefix.contains("SIMPLE")) {
                presentation.addText("SIMPLE", new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.GRAY));
                prefix.add("SIMPLE");
            }
        }
        presentation.addText("]", SimpleTextAttributes.REGULAR_ATTRIBUTES);



        SimpleTextAttributes attr = SimpleTextAttributes.REGULAR_ATTRIBUTES;
        if (1 == commandParamInfoCollections.size()) {
            TypeParamInterface.TypeParam commandParamType = commandParamInfoCollections.values().iterator().next().getType();
            if (commandParamType.equals(TypeParamInterface.TypeParam.OBJ) || commandParamType.equals(TypeParamInterface.TypeParam.SBJ)) {
                attr = SimpleTextAttributes.LINK_ATTRIBUTES;
            }
        }

        presentation.addText(paramName, attr);

        update(presentation);
    }

    @Override
    protected SimpleNode[] buildChildren() {
        if (commandParamInfoCollections.values().size() <= 1) {
            return new SimpleNode[0];
        }

        ArrayList<SmartParam> childParams = new ArrayList<>();

        for (String keyName: commandParamInfoCollections.keySet()) {

            ChangeCommandParamFactory.CommandParamInfo commandParam = commandParamInfoCollections.get(keyName);

            HashMap<String, ChangeCommandParamFactory.CommandParamInfo> commandParamInfoCollectionsLocal = new HashMap<>();
            commandParamInfoCollectionsLocal.put(keyName, commandParam);
            childParams.add(new SmartParam(
                    (NamedNode)getParent(),
                    commandParam.getDocCommentInfo().getName(),
                    commandParam.getDocCommentInfo().getDescription(),
                    commandParamInfoCollectionsLocal
                    ));
        }

        return childParams.toArray(new SimpleNode[0]);
    }

    @Override
    public String getDescriptionSource() {
        return paramDescription;
    }
}
