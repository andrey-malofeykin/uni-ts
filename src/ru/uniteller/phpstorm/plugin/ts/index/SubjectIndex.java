package ru.uniteller.phpstorm.plugin.ts.index;

import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;
import com.jetbrains.php.lang.PhpFileType;
import java.io.DataOutput;
import java.io.DataInput;
import java.io.IOException;

import java.util.Map;

public class SubjectIndex extends FileBasedIndexExtension<String, SubjectIndex.Subject> {
    private static final ID<String, SubjectIndex.Subject> KEY = ID.create("ru.uniteller.phpstorm.plugin.ts.index.subject");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();
    final private static SubjectExternalizer EXTERNALIZER = new SubjectExternalizer();

    @Override
    public @NotNull ID<String, SubjectIndex.Subject> getName() {
        return KEY;
    }

    @Override
    public FileBasedIndex.@NotNull InputFilter getInputFilter() {
        return virtualFile -> virtualFile.getFileType() == PhpFileType.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public @NotNull DataIndexer<String, SubjectIndex.Subject, FileContent> getIndexer() {
        return null;
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return this.myKeyDescriptor;
    }

    @Override
    public @NotNull DataExternalizer<SubjectIndex.Subject> getValueExternalizer() {
        return EXTERNALIZER;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    private static class SubjectExternalizer implements DataExternalizer<Subject> {
        public synchronized void save(@NotNull DataOutput out, Subject value) throws IOException {
            out.writeUTF(value.getName());
            out.writeUTF(value.getClassName());
            out.writeUTF(value.getDescription());
            out.writeInt(value.getCommands().size());

            for (String commandName : value.getCommands().keySet()) {
                Subject.Command command = value.getCommands().get(commandName);

                out.writeUTF(command.getName());
                out.writeUTF(command.getDescription());
                out.writeInt(command.getArgs().size());


                for (String argName : command.getArgs().keySet()) {
                    Subject.Command.Arg arg = command.getArgs().get(argName);

                    out.writeUTF(arg.getName());
                    out.writeUTF(arg.getDescription());
                    out.writeInt(arg.getIndex());
                    out.writeUTF(arg.getClassName());
                }


            }
        }

        public synchronized SubjectIndex.Subject read(@NotNull DataInput in) throws IOException {
            return new Subject();
        }
    }

    /**
     * Структура данных описывающая информацию о субъекте
     */
    static class Subject {
        /**
         * Имя субъекта
         */
        private String name;

        /**
         * Имя класса субъекта
         */
        private String className;

        /**
         * Описание субъекта
         */
        private String description;

        /**
         * Команды субъекта
         */
        private Map<String, Subject.Command> commands;
        /**
         * Имя субъекта
         */
        String getName() {
            return name;
        }
        /**
         * Имя класса субъекта
         */
        String getClassName() {
            return className;
        }
        /**
         * Описание субъекта
         */
        String getDescription() {
            return description;
        }
        /**
         * Команды субъекта
         */
        Map<String, Command> getCommands() {
            return commands;
        }

        /**
         * Комманды субъекта
         */
        static class  Command {
            /**
             * Имя команды
             */
            private String name;

            /**
             * Описание команды субъекта
             */
            private String description;

            /**
             * Аргументы команды субъекта
             */
            private Map<String, Command.Arg> args;
            /**
             * Имя команды
             */
            String getName() {
                return name;
            }
            /**
             * Описание команды субъекта
             */
            String getDescription() {
                return description;
            }
            /**
             * Аргументы команды субъекта
             */

            Map<String, Arg> getArgs() {
                return args;
            }

            /**
             * Аргументы команды
             */
            static class Arg {
                /**
                 * Имя аргумента команды
                 */
                private String name;

                /**
                 * Описание аргумента
                 */
                private String description;

                /**
                 * Порядковый номер аргумента
                 */
                private Integer index;

                /**
                 * Тип аргумента
                 */
                private ArgType argType;

                /**
                 * Имя класса объекта
                 */
                private String className;

                /**
                 * Допустимые типа аргумента
                 */
                enum ArgType{
                    boolType, intType, floatType, strType, arrayType, objType,  resourceType, nullType
                }

                /**
                 * Имя аргумента команды
                 */
                String getName() {
                    return name;
                }

                /**
                 * Описание аргумента
                 */
                String getDescription() {
                    return description;
                }
                /**
                 * Порядковый номер аргумента
                 */
                Integer getIndex() {
                    return index;
                }

                /**
                 * Имя класса объекта
                 */
                String getClassName() {
                    return className;
                }
            }
        }
    }
}
