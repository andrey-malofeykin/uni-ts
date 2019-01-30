package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class SubjectCommand extends NamedNode implements DescriptionProvider {
    private String commandName = "";
    private String commandDescription = "";
    private String commandClassFqn;
    private String commandMethod;

    SubjectCommand(SubjectNode.SubjectCommands aParent, String name, CommandData data) {
        super(aParent, name);
        this.commandClassFqn = data.getCommandClassFqn();
        this.commandMethod = data.getCommandMethod();
        this.commandName = data.getCommandName();
        initObject();
        updatePresentation();
    }

    private void initObject() {
        if (null == commandClassFqn || null == commandMethod) {
            return;
        }

        @NotNull PhpIndex index = PhpIndex.getInstance(Objects.requireNonNull(getProject()));
        Optional<PhpClass> commandOpt = index.getClassesByFQN(commandClassFqn).stream().findFirst();
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
        return new SimpleNode[0];
    }

    @Override
    public String getDescriptionSource() {
        return commandDescription;
    }

    public static class CommandData {
        private String commandName = "";
        private String commandClassFqn;
        private String commandMethod;

        public CommandData(String commandName, String commandClassFqn, String commandMethod) {
            this.commandName = commandName;
            this.commandClassFqn = commandClassFqn;
            this.commandMethod = commandMethod;
        }

        public String getCommandName() {
            return commandName;
        }

        public String getCommandClassFqn() {
            return commandClassFqn;
        }

        public String getCommandMethod() {
            return commandMethod;
        }
    }
}
