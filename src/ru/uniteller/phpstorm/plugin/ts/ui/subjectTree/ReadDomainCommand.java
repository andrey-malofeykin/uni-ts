package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;

public class ReadDomainCommand extends NamedNode implements DescriptionProvider {
    private String commandName = "";
    private String commandDescription = "";
    private String commandClassFqn;
    private String commandMethod;

    ReadDomainCommand(SubjectCommandCollection aParent,  SubjectCommandCollection.ReadDomainCommandData data) {
        super(aParent, data.getMethodName());
        if (null != data.getCommandName()) {
            this.commandName = data.getCommandName();
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
            String phpDocComment = PhpDocUtil.getDescription(method.getDocComment());
            int endFirstLine = phpDocComment.indexOf("\n");
            if (-1 == endFirstLine) {
                this.commandName = phpDocComment.replaceAll("<.*?>", "");
            } else {
                this.commandName =  phpDocComment.substring(0, endFirstLine).replaceAll("<.*?>", "");
                this.commandDescription = phpDocComment.substring(endFirstLine + 1);
            }

        }
    }

    private void updatePresentation() {
        PresentationData presentation = getPresentation();
        presentation.clear();
        presentation.addText(commandName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        update(presentation);
    }


    @Override
    protected SimpleNode[] buildChildren() {

        ArrayList<ChangeDomainCommandParam> changeDomainCommandParams = new ArrayList<>();


        phpIndex.getClassesByFQN(commandClassFqn).forEach(commandClass -> {
            @Nullable Method method = commandClass.findMethodByName(commandMethod);
            if (null == method) {
                return;
            }
            int paramIndex = 0;
            for (Parameter param: method.getParameters()) {
                paramIndex++;

                if (1 == paramIndex && !isDomainParam(param)) {
                    break;
                }

                //changeDomainCommandParams.add(new ChangeDomainCommandParam(this, param));
            }



        });

        return changeDomainCommandParams.toArray(new ChangeDomainCommandParam[0]);

    }

    private boolean isDomainParam(Parameter param) {
        return param.getType().toString().equals(config.getDomainInterface());
    }

    @Override
    public String getDescriptionSource() {
        return commandDescription;
    }

    static class CommandData {
        private @Nullable String  commandName;
        private String commandClassFqn;
        private String commandMethod;

        CommandData(@Nullable String commandName, String commandClassFqn, String commandMethod) {
            this.commandName = commandName;
            this.commandClassFqn = commandClassFqn;
            this.commandMethod = commandMethod;
        }

        @Nullable String getCommandName() {
            return commandName;
        }

        String getCommandClassFqn() {
            return commandClassFqn;
        }

        String getCommandMethod() {
            return commandMethod;
        }
    }
}
