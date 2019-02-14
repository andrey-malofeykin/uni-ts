package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;
import ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.ChangeCommandParam.ChangeCommandParamFactory;
import ru.uniteller.phpstorm.plugin.ts.util.PhpDocUtil;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class ChangeDomainCommand extends NamedNode implements DescriptionProvider {
    private String commandName = "";
    private String commandDescription = "";
    private String commandClassFqn;
    private String commandMethod;

    ChangeDomainCommand(SubjectCommandCollection aParent, SubjectCommandCollection.ChangeDomainCommandData data) {
        super(aParent, data.getMethodName());
        this.commandClassFqn = Objects.requireNonNull(data.getClassMethod().getContainingClass()).getFQN();
        this.commandMethod = data.getClassMethod().getName();
        if (null != data.getCommandName()) {
            this.commandName = data.getMethodName() + ":" + data.getCommandName();
        }
        initObject();
        updatePresentation();
    }

    private void initObject() {
        if (null == commandClassFqn || null == commandMethod) {
            return;
        }
        initCommandName();
    }


    private void initCommandName() {
        if (null != commandName) {
            return;
        }

        Optional<PhpClass> commandOpt = phpIndex.getClassesByFQN(commandClassFqn).stream().findFirst();
        if (!commandOpt.isPresent()) {
            return;
        }
        PhpClass objClass = commandOpt.get();

        @Nullable Method method = objClass.findMethodByName(commandMethod);
        if (null == method) {
            return;
        }


        this.commandName = commandClassFqn + ":" + commandMethod;

        if (null != method.getDocComment()) {
            PhpDocUtil.DocCommentInfo docCommentInfo = PhpDocUtil.buildDocCommentInfo(method.getDocComment());
            commandName = docCommentInfo.getName();
            commandDescription = docCommentInfo.getName();
        }

    }

    private void updatePresentation() {
        PresentationData presentation = getPresentation();
        presentation.clear();
        presentation.addText( commandName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        update(presentation);
    }


    @Override
    protected SimpleNode[] buildChildren() {
        ArrayList<NamedNode> changeDomainCommandParams = new ArrayList<>();

        ChangeCommandParamFactory paramNodeFactory = new ChangeCommandParamFactory(config);

        phpIndex.getClassesByFQN(commandClassFqn).forEach(commandClass -> {
            @Nullable Method method = commandClass.findMethodByName(commandMethod);
            if (null == method) {
                return;
            }
            int paramIndex = 0;
            for (Parameter param: method.getParameters()) {
                paramIndex++;

                if (1 == paramIndex ) {
                    if (!isDomainParam(param)) {
                        break;
                    }
                    continue;
                }

                changeDomainCommandParams.add(paramNodeFactory.paramFactory(this, param));
            }



        });

        return changeDomainCommandParams.toArray(new NamedNode[0]);

    }

    private boolean isDomainParam(Parameter param) {
        return param.getType().toString().equals(config.getDomainInterface());
    }

    @Override
    public String getDescriptionSource() {
        return commandDescription;
    }

}
