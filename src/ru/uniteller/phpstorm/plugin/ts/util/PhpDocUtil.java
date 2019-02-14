package ru.uniteller.phpstorm.plugin.ts.util;


import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class PhpDocUtil {

    public static class DocCommentInfo {
        private String name;
        private String description;

        public DocCommentInfo(@NotNull String name, @Nullable String description) {
            this.name = name;
            this.description = null == description ? "" : description;
        }

        @NotNull public String getName() {
            return name;
        }

        @NotNull public String getDescription() {
            return description;
        }
    }

    static public DocCommentInfo buildDocCommentInfo(@NotNull PhpDocComment phpDocComment) {
        String phpDocCommentStr = com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil.getDescription(phpDocComment);
        int endFirstLine = phpDocCommentStr.indexOf("\n");
        String name;
        String description;
        if (-1 == endFirstLine) {
            name = phpDocCommentStr.replaceAll("<.*?>", "");
            description = "";
        } else {
            name =  phpDocCommentStr.substring(0, endFirstLine).replaceAll("<.*?>", "");
            description = phpDocCommentStr.substring(endFirstLine + 1);
        }
        return new DocCommentInfo(name, description);
    }
}
