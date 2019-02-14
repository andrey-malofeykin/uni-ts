package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.lang.documentation.PhpClassDocSource;
import com.jetbrains.php.lang.documentation.PhpParameterDocSource;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.uniteller.phpstorm.plugin.ts.util.PhpIndexUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;





public class ChangeDomainCommandParam extends AbstractObjectNode implements DescriptionProvider, ExpandTreeNodeProvider {
    private String paramName;
    private String paramExtName;
    private String paramDescription = "";
    private @Nullable ExpandTreeNodeProvider.TargetInfo targetInfo;
    private Pattern pattern = Pattern.compile("\\\\TestSrv\\\\Subject\\\\(?<SUBJECT>.+)\\\\Object\\\\(?<OBJ>.+)");
    private HashMap<String, CommandParamInfo> objInfoCollections;
    public enum TypeParam {OBJ, SBJ, DATA, DEFAULT}
    private TypeParam typeCommandParam = TypeParam.DEFAULT;



    private ChangeDomainCommandParam(ChangeDomainCommandParam aParent, @NotNull CommandParamInfo objInfo) {
        super(aParent, objInfo.getClassDocInfo().getName(),  objInfo.getClassDocInfo().getSourceFQN());
        this.paramName = objInfo.getClassDocInfo().getName();
        this.paramDescription = objInfo.getClassDocInfo().getDescription();
        this.targetInfo = objInfo.getObjInfo();
        updatePresentation();
    }


    ChangeDomainCommandParam(ChangeDomainCommand aParent, Parameter param) {
        super(aParent, param.getName(), null);
        paramName = param.getName();
        initExtNameByParaDoc(param);
        initObject(param);
        if (null != paramExtName) {
            paramName = paramName + "(" + paramExtName + ")";
        }


        updatePresentation();
    }

    private void initExtNameByParaDoc(Parameter param) {

        PhpParameterDocSource parameterDocSource = new PhpParameterDocSource(param);
        @NotNull String paramDsc = parameterDocSource.getDescription();

        if (!paramDsc.trim().equals("")) {
            paramExtName = paramDsc;
        }
    }

    private class ClassDocInfo {
        private String name;
        private String description;
        private @Nullable String sourceFQN;

        ClassDocInfo(String name, String description, @Nullable String sourceFQN) {

            this.name = name;
            this.description = description;
            this.sourceFQN = sourceFQN;
        }

        String getName() {
            return name;
        }

        String getDescription() {
            return description;
        }

        @Nullable String getSourceFQN() {
            return sourceFQN;
        }
    }

    private class CommandParamInfo  {
        private ClassDocInfo classDocInfo;
        TargetInfo objInfo;

        CommandParamInfo(ClassDocInfo classDocInfo, @Nullable ExpandTreeNodeProvider.TargetInfo objInfo) {
            this.classDocInfo = classDocInfo;
            this.objInfo = objInfo;
        }

        ClassDocInfo getClassDocInfo() {
            return classDocInfo;
        }

        @Nullable ExpandTreeNodeProvider.TargetInfo getObjInfo() {
            return objInfo;
        }
    }

    private ClassDocInfo buildDocInfo(PhpClass phpClass, boolean isSource) {
        @NotNull String phpClassDesc = (new PhpClassDocSource(phpClass)).getDescription();

        int endFirstLine = phpClassDesc.indexOf("\n");
        String firstLine;
        String desc = "";
        if (-1 != endFirstLine) {
            firstLine = phpClassDesc.substring(0, endFirstLine).replaceAll("<.*?>", "").trim();
            desc = phpClassDesc.trim();
        } else {
            firstLine = phpClassDesc.replaceAll("<.*?>", "").trim();

        }


        return new ClassDocInfo(firstLine, desc, isSource ? phpClass.getFQN() : null);
    }

    private String buildParamNamePresentation(String paramName, String comment) {
        String presentation;
        if (!comment.equals("")) {
            presentation = paramName + "(" + comment + ")";
        } else {
            presentation = paramName;
        }
        return presentation;
    }

    private void initObject(Parameter param) {

        HashSet<String> extNameParts = new HashSet<>();
        HashSet<String> extDescParts = new HashSet<>();

        HashMap<String, CommandParamInfo> objInfoCollections = new HashMap<>();

        for (String paramType: param.getType().getTypes()) {
            @NotNull Collection<PhpClass> collectionClass = phpIndex.getAnyByFQN(paramType);
            for (PhpClass paramClass: collectionClass) {

                ClassDocInfo docInfo = buildDocInfo(paramClass, false);
                if (null == paramExtName && !docInfo.getName().equals("")) {
                    extNameParts.add(docInfo.getName());
                    if (!docInfo.getDescription().equals("")) {
                        extDescParts.add(docInfo.getDescription());
                    }

                }

                if (PhpIndexUtil.classIsImplement(paramClass, config.getObjInterface())) {
                    for (PhpClass objClass : PhpIndexUtil.getEndClasses(paramClass)) {
                        ClassDocInfo objDocInfo = buildDocInfo(objClass, false);
                        Matcher mathRes = pattern.matcher(objClass.getFQN());
                        if (mathRes.find()) {
                            objInfoCollections.put(objClass.getFQN(), new CommandParamInfo(
                                    new ClassDocInfo(
                                            buildParamNamePresentation(paramName, objDocInfo.getName()),
                                            objDocInfo.getDescription(),
                                            objDocInfo.getSourceFQN()
                                    ),

                                    new TargetInfo(
                                            mathRes.group("SUBJECT"),
                                            mathRes.group("OBJ")
                                    )));
                        }
                    }
                    typeCommandParam = TypeParam.OBJ;
                    continue;
                }

                if (PhpIndexUtil.classIsImplement(paramClass, config.getSubjectInterfaceFQN())) {
                    for (PhpClass subjClass : PhpIndexUtil.getEndClasses(paramClass)) {
                        ClassDocInfo subjDocInfo = buildDocInfo(subjClass, false);
                        objInfoCollections.put(subjClass.getFQN(), new CommandParamInfo(
                                new ClassDocInfo(
                                        buildParamNamePresentation(paramName, subjDocInfo.getName()),
                                        subjDocInfo.getDescription(),
                                        subjDocInfo.getSourceFQN()
                                ),
                                new TargetInfo(
                                    subjClass.getName(),
                                null
                        )));
                    }
                    typeCommandParam = TypeParam.SBJ;
                    continue;
                }

                ClassDocInfo paramDocInfo = buildDocInfo(paramClass, true);
                objInfoCollections.put(paramClass.getFQN(), new CommandParamInfo(
                        new ClassDocInfo(
                                buildParamNamePresentation(paramName, paramDocInfo.getName()),
                                paramDocInfo.getDescription(),
                                paramDocInfo.getSourceFQN()
                        ),null));
                typeCommandParam = TypeParam.DATA;

            }
        }

        if (null == paramExtName && extNameParts.size() > 0) {
            paramExtName = String.join("|", extNameParts);
        }

        if (extDescParts.size() > 0) {
            paramDescription = String.join("\n", extDescParts);
        }

        this.objInfoCollections = objInfoCollections;
        if (1 == objInfoCollections.size()) {
           targetInfo = objInfoCollections.values().iterator().next().getObjInfo();
        }
    }


    @Override
    public @Nullable ExpandTreeNodeProvider.TargetInfo getTargetInfo() {
        return targetInfo;
    }

    private void updatePresentation() {
        PresentationData presentation = getPresentation();
        presentation.clear();
        if (TypeParam.OBJ == typeCommandParam) {
            presentation.addText("[OBJ]", new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.GREEN));
        }
        if (TypeParam.SBJ == typeCommandParam) {
            presentation.addText("[SBJ]", new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.BLUE));
        }
        if (TypeParam.DATA == typeCommandParam) {
            presentation.addText("[DTA]", new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.GRAY));
        }

        if (null == targetInfo) {
            presentation.addText(paramName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        } else {
            presentation.addText(paramName, SimpleTextAttributes.LINK_ATTRIBUTES);
        }

        update(presentation);
    }

    @Override
    protected SimpleNode[] buildChildren() {
        if (null == objInfoCollections || 0 == objInfoCollections.size()) {
            return super.buildChildren();
        }
        if (1 == objInfoCollections.size()) {
            CommandParamInfo commandParamInfo = objInfoCollections.values().iterator().next();
            @Nullable String sourceFqn = commandParamInfo.getClassDocInfo().getSourceFQN();
            if (null != sourceFqn) {
                setObjClass(sourceFqn);
                return  super.buildChildren();
            }
            if (null != commandParamInfo.getObjInfo()) {
                return new SimpleNode[0];
            }
        }


        ArrayList<ChangeDomainCommandParam> childParams = new ArrayList<>();

        for (String fqnClass: objInfoCollections.keySet()) {
            childParams.add(new ChangeDomainCommandParam(this, objInfoCollections.get(fqnClass)));
        }

        return childParams.toArray(new SimpleNode[0]);
    }

    @Override
    public String getDescriptionSource() {
        return paramDescription;
    }
}
