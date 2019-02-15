package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.lang.documentation.PhpDocLinkResolver;
import com.jetbrains.php.lang.documentation.PhpDocMethodSource;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocMethod;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocMethodTag;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocRef;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpClassMember;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;


class SubjectCommandCollection extends NamedNode {

    private final String[] READ_COMMAND_PREFIX = new String[]{"getLast", "get", "find", "findOne"};
    /**
     * Имя субъекта
     */
    private String subjectClassFQN;
    SubjectCommandCollection(SubjectNode aParent) {
        super(aParent, "commands");
        subjectClassFQN = aParent.getSubjectClassFQN();
        updatePresentation();
    }


    @Override
    protected SimpleNode[] buildChildren() {

        ArrayList<NamedNode> namedNodes = new ArrayList<>();


        HashSet<String> publicCommandNames = new HashSet<>();
        SimpleNode[] publicCommands = buildListPublicCommand(publicCommandNames);

        if (publicCommands.length > 0) {
            namedNodes.add(
                    new NamedNode(this, "Публичные") {
                        @Override
                        protected SimpleNode[] buildChildren() {
                            return publicCommands;
                        }
                    }
            );
        }

        SimpleNode[] protectedCommands = buildListProtectedCommand(publicCommandNames);
        if (protectedCommands.length > 0) {
            namedNodes.add(
                    new NamedNode(this, "Внутренние") {
                        @Override
                        protected SimpleNode[] buildChildren() {
                            return protectedCommands;
                        }
                    }
            );
        }


        return namedNodes.toArray(new NamedNode[0]);
    }

    private SimpleNode[] buildListPublicCommand(HashSet<String> publicCommandNames) {
        String subjectInterfaceFqn = subjectClassFQN + "Interface";

        HashMap<String, ChangeDomainCommand> changeDomainCommands = new HashMap<>();
        HashMap<String, ReadDomainCommand> readDomainCommands = new HashMap<>();

        @NotNull Collection<PhpClass> subjectInterfaces = phpIndex.getInterfacesByFQN(subjectInterfaceFqn);

        subjectInterfaces.forEach(subjectInterface -> {
            @Nullable PhpDocComment docComment = subjectInterface.getDocComment();
            if (null == docComment) {
                return;
            }
            for (PhpDocMethod method: docComment.getMethods()) {
                publicCommandNames.add(method.getName());
                @Nullable AbstractCommandData commandData = buildCommandDataByMethodTag(method);
                if (commandData instanceof ReadDomainCommandData) {

                    readDomainCommands.put(commandData.getMethodName(), new ReadDomainCommand(this, (ReadDomainCommandData)commandData));
                }
                if (commandData instanceof ChangeDomainCommandData) {
                    changeDomainCommands.put(commandData.getMethodName(), new ChangeDomainCommand(this, (ChangeDomainCommandData)commandData));
                }
            }
        });


        ArrayList<NamedNode> namedNodes = new ArrayList<>();
        if (readDomainCommands.size() > 0) {
            namedNodes.add(new ReadDomainCollection((SubjectNode)getParent(), readDomainCommands.values().toArray(new ReadDomainCommand[0])));
        }
        if (changeDomainCommands.size() > 0) {
            namedNodes.add(new ChangeDomainCollection((SubjectNode)getParent(), changeDomainCommands.values().toArray(new ChangeDomainCommand[0])));
        }


        return namedNodes.toArray(new NamedNode[0]);
    }



    private SimpleNode[] buildListProtectedCommand(HashSet<String> publicCommandNames) {
        String subjectInterfaceFqn = subjectClassFQN;

        HashMap<String, ChangeDomainCommand> changeDomainCommands = new HashMap<>();
        HashMap<String, ReadDomainCommand> readDomainCommands = new HashMap<>();

        @NotNull Collection<PhpClass> subjectClasses = phpIndex.getClassesByFQN(subjectInterfaceFqn);

        subjectClasses.forEach(subjectClass -> {
            @Nullable PhpDocComment docComment = subjectClass.getDocComment();
            if (null == docComment) {
                return;
            }
            for (PhpDocMethod method: docComment.getMethods()) {
                if (publicCommandNames.contains(method.getName())) {
                    continue;
                }

                @Nullable AbstractCommandData commandData = buildCommandDataByMethodTag(method);
                if (commandData instanceof ReadDomainCommandData) {

                    readDomainCommands.put(commandData.getMethodName(), new ReadDomainCommand(this, (ReadDomainCommandData)commandData));
                }
                if (commandData instanceof ChangeDomainCommandData) {
                    changeDomainCommands.put(commandData.getMethodName(), new ChangeDomainCommand(this, (ChangeDomainCommandData)commandData));
                }
            }
        });


        ArrayList<NamedNode> namedNodes = new ArrayList<>();
        if (readDomainCommands.size() > 0) {
            namedNodes.add(new ReadDomainCollection((SubjectNode)getParent(), readDomainCommands.values().toArray(new ReadDomainCommand[0])));
        }
        if (changeDomainCommands.size() > 0) {
            namedNodes.add(new ChangeDomainCollection((SubjectNode)getParent(), changeDomainCommands.values().toArray(new ChangeDomainCommand[0])));
        }


        return namedNodes.toArray(new NamedNode[0]);
    }




    private boolean isReadDomainCommand(String methodName) {
        boolean isReadDomainCommand = false;
        for (String prefix: READ_COMMAND_PREFIX) {
            if (methodName.startsWith(prefix)) {
                isReadDomainCommand = true;
                break;
            }

        }
        return isReadDomainCommand;
    }


    private void updatePresentation() {
        PresentationData presentation = getPresentation();
        presentation.clear();
        presentation.addText("Команды", SimpleTextAttributes.REGULAR_ATTRIBUTES);
        update(presentation);
    }

    private @Nullable AbstractCommandData buildCommandDataByMethodTag(PhpDocMethod method) {

        PsiElement methodTag = method.getParent();
        if (!(methodTag instanceof PhpDocMethodTag)) {
            return null;
        }
        @Nullable PsiElement nameIdentifier = method.getNameIdentifier();
        if (null == nameIdentifier) {
            return null;
        }
        String methodName = nameIdentifier.getText();

        PhpDocMethodSource phpDocMethodSource = new PhpDocMethodSource(method);
        String commandName = phpDocMethodSource.getDescription();


        if (isReadDomainCommand(methodName)) {
            return new ReadDomainCommandData(commandName, methodName, method);
        } else {
            @Nullable PhpPsiElement seeTag = ((PhpDocMethodTag) methodTag).getNextPsiSibling();
            if (null == seeTag) {
                return null;
            }
            @Nullable PhpDocRef phpDocRef = PsiTreeUtil.getStubChildOfType(seeTag, PhpDocRef.class);
            if (null == phpDocRef) {
                return null;
            }
            @NotNull Collection<PhpDocLinkResolver.Result> resolvers = PhpDocLinkResolver.resolve(phpDocRef.getText(), seeTag);
            for (PhpDocLinkResolver.Result resolver: resolvers) {
                @Nullable PhpClassMember member = resolver.getMember();
                if (member instanceof Method) {
                    return new ChangeDomainCommandData(commandName, methodName, method, (Method)member);
                }
            }
        }

        return null;
    }

    abstract class AbstractCommandData {
        String commandName;
        String methodName;
        PhpDocMethod phpDocMethod;

        AbstractCommandData(String commandName, String methodName, PhpDocMethod phpDocMethod) {
            this.commandName = commandName;
            this.methodName = methodName;
            this.phpDocMethod = phpDocMethod;
        }

        String getCommandName() {
            return commandName;
        }

        String getMethodName() {
            return methodName;
        }

        PhpDocMethod getPhpDocMethod() {
            return phpDocMethod;
        }
    }

    class ReadDomainCommandData extends AbstractCommandData {
        ReadDomainCommandData(String commandName, String methodName, PhpDocMethod phpDocMethod) {
            super(commandName, methodName, phpDocMethod);
        }
    }

    class ChangeDomainCommandData extends AbstractCommandData {
        Method classMethod;

        ChangeDomainCommandData(String commandName, String methodName, PhpDocMethod phpDocMethod, Method classMethod) {
            super(commandName, methodName, phpDocMethod);
            this.classMethod = classMethod;
        }

        Method getClassMethod() {
            return classMethod;
        }
    }





    class ReadDomainCollection extends NamedNode {
        ReadDomainCommand[] readDomainCommandCollection;

        ReadDomainCollection(SubjectNode aParent, ReadDomainCommand[] readDomainCommandCollection) {
            super(aParent, "readDomain");
            this.readDomainCommandCollection = readDomainCommandCollection;
            updatePresentation();
        }


        @Override
        protected SimpleNode[] buildChildren() {
            return readDomainCommandCollection;
        }

        private void updatePresentation() {
            PresentationData presentation = getPresentation();
            presentation.clear();
            presentation.addText("Поиск в домене", SimpleTextAttributes.REGULAR_ATTRIBUTES);
            update(presentation);
        }

    }


    class ChangeDomainCollection extends NamedNode {
        ChangeDomainCommand[] changeDomainCommand;

        ChangeDomainCollection(SubjectNode aParent, ChangeDomainCommand[] changeDomainCommand) {
            super(aParent, "readDomain");
            this.changeDomainCommand = changeDomainCommand;
            updatePresentation();
        }


        @Override
        protected SimpleNode[] buildChildren() {
            return changeDomainCommand;
        }

        private void updatePresentation() {
            PresentationData presentation = getPresentation();
            presentation.clear();
            presentation.addText("Изменение состояния домена", SimpleTextAttributes.REGULAR_ATTRIBUTES);
            update(presentation);
        }

    }




}
