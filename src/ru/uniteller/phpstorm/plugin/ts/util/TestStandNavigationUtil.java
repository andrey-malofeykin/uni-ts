package ru.uniteller.phpstorm.plugin.ts.util;

import com.intellij.pom.Navigatable;
import com.intellij.openapi.project.Project;
import com.intellij.pom.NavigatableAdapter;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;


public class TestStandNavigationUtil {
    final static public class ClassFQN {
        private String value;

        public ClassFQN(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    final static public class ClassMethod {
        private String value;

        public ClassMethod(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    final static public class MethodParam {
        private String value;

        public MethodParam(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }


    private TestStandNavigationUtil() { }

    public static @Nullable Navigatable createNavigatable(@Nullable Project project, @Nullable ClassFQN classFQN) {
        if (null == project || null == classFQN) {
            return null;
        }
        return new NavigatableAdapter() {
            @Override
            public void navigate(boolean requestFocus) {
                @NotNull Collection<PhpClass> phpClassCollection = PhpIndex.getInstance(project).getAnyByFQN(classFQN.toString());
                if (1 != phpClassCollection.size()) {
                    return;
                }
                PhpClass phpClass = phpClassCollection.iterator().next();


                navigate(project, phpClass.getContainingFile().getVirtualFile(), phpClass.getTextOffset() + phpClass.getName().length() , requestFocus);
            }
        };
    }
}
