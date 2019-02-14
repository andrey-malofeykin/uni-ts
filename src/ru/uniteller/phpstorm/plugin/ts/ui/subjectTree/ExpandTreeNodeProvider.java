package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import org.jetbrains.annotations.Nullable;

public interface ExpandTreeNodeProvider {
    @Nullable ExpandTreeNodeProvider.TargetInfo getTargetInfo();


    class TargetInfo {
        private String subjectName;
        private String objectName;

        public TargetInfo(String subjectName, @Nullable String objectName) {
            this.subjectName = subjectName;
            this.objectName = objectName;
        }

        public String getSubjectName() {
            return subjectName;
        }

        @Nullable public String getObjectName() {
            return objectName;
        }
    }
}
