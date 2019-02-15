package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.ChangeCommandParam;

import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.PhpParameterDocSource;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.uniteller.phpstorm.plugin.ts.service.Config;
import ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.ChangeDomainCommand;
import ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.ExpandTreeNodeProvider;
import ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.NamedNode;
import com.jetbrains.php.lang.psi.elements.Parameter;
import ru.uniteller.phpstorm.plugin.ts.util.PhpDocUtil;
import ru.uniteller.phpstorm.plugin.ts.util.PhpIndexUtil;
import ru.uniteller.phpstorm.plugin.ts.util.TestStandNavigationUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;

public class ChangeCommandParamFactory {

    class CommandParamInfo  {
        private PhpDocUtil.DocCommentInfo docCommentInfo;
        private ExpandTreeNodeProvider.TargetInfo targetInfo;
        private TypeParamInterface.TypeParam type;
        private String typeClassFqn;
        private String name;

        CommandParamInfo(PhpDocUtil.DocCommentInfo docCommentInfo,
                         @Nullable ExpandTreeNodeProvider.TargetInfo targetInfo,
                         @NotNull TypeParamInterface.TypeParam type,
                         @Nullable String typeClassFqn,
                         @NotNull String name
        ) {
            this.docCommentInfo = docCommentInfo;
            this.targetInfo = targetInfo;
            this.type = type;
            this.typeClassFqn = typeClassFqn;
            this.name = name;
        }

        PhpDocUtil.DocCommentInfo getDocCommentInfo() {
            return docCommentInfo;
        }

        @Nullable ExpandTreeNodeProvider.TargetInfo getTargetInfo() {
            return targetInfo;
        }

        @NotNull TypeParamInterface.TypeParam getType() {
            return type;
        }

        @Nullable String getTypeClassFqn() {
            return typeClassFqn;
        }

        @NotNull public String getName() {
            return name;
        }
    }


    private Config config;

    public ChangeCommandParamFactory( Config config) {
        this.config = config;
    }

    private void dispatchNameParam(
            PhpClass paramClass,
            @Nullable String paramExtName,
            HashSet<String> extNameParts,
            HashSet<String> extDescParts
    ) {
        if (null == paramClass.getDocComment()) {
            return;
        }

        PhpDocUtil.DocCommentInfo docCommentInfo = PhpDocUtil.buildDocCommentInfo(paramClass.getDocComment());
        if (null == paramExtName && !docCommentInfo.getName().equals("")) {
            extNameParts.add(docCommentInfo.getName());
            if (!docCommentInfo.getDescription().equals("")) {
                extDescParts.add(docCommentInfo.getDescription());
            }
        }
    }

    private @Nullable String buildExtName(Parameter param) {
        PhpParameterDocSource parameterDocSource = new PhpParameterDocSource(param);
        @NotNull String paramDsc = parameterDocSource.getDescription();
        return  !paramDsc.trim().equals("") ? paramDsc : null;
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

    private void dispatchObjectParam(PhpClass paramObjClass, Parameter param, HashMap<String, CommandParamInfo> objInfoCollections) {
        for (PhpClass objClass : PhpIndexUtil.getEndClasses(paramObjClass)) {
            if (null != objClass.getDocComment()) {
                PhpDocUtil.DocCommentInfo docCommentInfo = PhpDocUtil.buildDocCommentInfo(objClass.getDocComment());
                Matcher mathRes = config.getPatternObjInfo().matcher(objClass.getFQN());

                if (mathRes.find()) {
                    objInfoCollections.put(objClass.getFQN(), new CommandParamInfo(
                            new PhpDocUtil.DocCommentInfo(
                                    buildParamNamePresentation(param.getName(), docCommentInfo.getName()),
                                    docCommentInfo.getDescription()
                            ),
                            new ExpandTreeNodeProvider.TargetInfo(mathRes.group("SUBJECT"), mathRes.group("OBJ")),
                            TypeParamInterface.TypeParam.OBJ,
                            objClass.getFQN(),
                            param.getName()

                    ));
                }
            }
        }
    }

    private void dispatchSubjectParam(PhpClass paramSubjClass, Parameter param, HashMap<String, CommandParamInfo> objInfoCollections) {
        for (PhpClass subjClass : PhpIndexUtil.getEndClasses(paramSubjClass)) {
            if (null != subjClass.getDocComment()) {
                PhpDocUtil.DocCommentInfo docCommentInfo = PhpDocUtil.buildDocCommentInfo(subjClass.getDocComment());
                objInfoCollections.put(subjClass.getFQN(), new CommandParamInfo(
                        new PhpDocUtil.DocCommentInfo(
                                buildParamNamePresentation(param.getName(), docCommentInfo.getName()),
                                docCommentInfo.getDescription()
                        ),
                        new ExpandTreeNodeProvider.TargetInfo(
                                subjClass.getName(),
                                null
                        ),
                        TypeParamInterface.TypeParam.SBJ,
                        subjClass.getFQN(),
                        param.getName()

                ));
            }

        }
    }

    private void dispatchDefaultParam(PhpClass paramClass, Parameter param, HashMap<String, CommandParamInfo> objInfoCollections) {
        if (null != paramClass.getDocComment()) {
            PhpDocUtil.DocCommentInfo docCommentInfo = PhpDocUtil.buildDocCommentInfo(paramClass.getDocComment());

            objInfoCollections.put(paramClass.getFQN(), new CommandParamInfo(
                    new PhpDocUtil.DocCommentInfo(
                            buildParamNamePresentation(param.getName(), docCommentInfo.getName()),
                            docCommentInfo.getDescription()
                    ),
                    null,
                    TypeParamInterface.TypeParam.DATA,
                    paramClass.getFQN(),
                    param.getName()
                )
            );
        }
    }


    public NamedNode paramFactory(ChangeDomainCommand pNode, Parameter param) {
        @Nullable String paramExtName = buildExtName(param);
        @Nullable String paramDescription = null;


        HashSet<String> extNameParts = new HashSet<>();
        HashSet<String> extDescParts = new HashSet<>();


        HashMap<String, CommandParamInfo> commandParamInfoCollections = new HashMap<>();

        @NotNull PhpIndex phpIndex = PhpIndex.getInstance(param.getProject());
        for (String paramType: param.getType().getTypes()) {
            @NotNull Collection<PhpClass> collectionClass = phpIndex.getAnyByFQN(paramType);
            for (PhpClass paramClass: collectionClass) {
                dispatchNameParam(paramClass, paramExtName, extNameParts, extDescParts);

                if (PhpIndexUtil.classIsImplement(paramClass, config.getObjInterface())) {
                    dispatchObjectParam(paramClass, param, commandParamInfoCollections);
                    continue;
                }

                if (PhpIndexUtil.classIsImplement(paramClass, config.getSubjectInterfaceFQN())) {
                    dispatchSubjectParam(paramClass, param, commandParamInfoCollections);
                    continue;
                }

                dispatchDefaultParam(paramClass, param, commandParamInfoCollections);
            }
        }

        if (null == paramExtName && extNameParts.size() > 0) {
            paramExtName = String.join("|", extNameParts);
        }

        if (extDescParts.size() > 0) {
            paramDescription = String.join("\n", extDescParts);
        }

        String paramName = param.getName();
        if (null != paramExtName) {
            paramName = paramName + "(" + paramExtName + ")";
        }


        TestStandNavigationUtil.MethodParam methodParam = TestStandNavigationUtil.createMethodParam(param);
        if (1 == commandParamInfoCollections.values().size()) {
            CommandParamInfo commandParamInfo = commandParamInfoCollections.values().iterator().next();
            if (commandParamInfo.getType().equals(TypeParamInterface.TypeParam.DATA) && null != commandParamInfo.getTypeClassFqn()) {
                return new DefaultParam(
                        pNode,
                        paramName,
                        paramDescription,
                        commandParamInfo.getTypeClassFqn(),
                        methodParam
                );
            }
        }

        return new SmartParam(pNode, paramName, paramDescription, commandParamInfoCollections, methodParam);

    }
}
