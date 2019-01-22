package ru.uniteller.phpstorm.plugin.ts.index;

import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;
import com.jetbrains.php.lang.PhpFileType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SubjectIndex extends FileBasedIndexExtension<String, SubjectIndex.Subject> {
    private static final ID<String, SubjectIndex.Subject> KEY = ID.create("ru.uniteller.phpstorm.plugin.ts.index.subject");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();
    final private static StringSetDataExternalizer EXTERNALIZER = new StringSetDataExternalizer();

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
            }
        }
    }
}
