package ru.uniteller.phpstorm.plugin.ts.util;

import com.intellij.pom.Navigatable;
import com.intellij.openapi.project.Project;
import com.intellij.pom.NavigatableAdapter;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;


public class TestStandNavigationUtil {
    private static abstract class NavigationSourceItem {
        private String value;

        NavigationSourceItem(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
    private static abstract class ClassMember extends NavigationSourceItem {
        private ClassFQN classFQN;
        ClassMember(@NotNull String value, @NotNull ClassFQN classFQN) {
            super(value);
            this.classFQN = classFQN;
        }

        @NotNull public ClassFQN getClassFQN() {
            return classFQN;
        }
    }

    final static public class ClassFQN extends NavigationSourceItem {
        public ClassFQN(String value) {
            super(value);
        }
    }

    final static public class ClassField extends ClassMember {
        public ClassField(@NotNull String value, @NotNull ClassFQN classFQN) {
            super(value, classFQN);
        }
    }

    final static public class ClassConst extends ClassMember {
        public ClassConst(@NotNull String value, @NotNull ClassFQN classFQN) {
            super(value, classFQN);
        }
    }

    final static public class ClassMethod extends ClassMember {
        public ClassMethod(@NotNull String value, @NotNull ClassFQN classFQN) {
            super(value, classFQN);
        }
    }

    final static public class MethodParam extends NavigationSourceItem {
        private ClassMethod classMethod;
        MethodParam(@NotNull String value, @NotNull ClassMethod classMethod) {
            super(value);
            this.classMethod = classMethod;
        }

        @NotNull public ClassMethod getClassMethod() {
            return classMethod;
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

    public static @Nullable Navigatable createNavigatable(@Nullable Project project, @Nullable TestStandNavigationUtil.ClassField classField) {
        if (null == project || null == classField) {
            return null;
        }
        return new NavigatableAdapter() {
            @Override
            public void navigate(boolean requestFocus) {
                @NotNull Collection<PhpClass> phpClassCollection = PhpIndex.getInstance(project).getAnyByFQN(classField.getClassFQN().toString());
                if (1 != phpClassCollection.size()) {
                    return;
                }
                PhpClass phpClass = phpClassCollection.iterator().next();

                @Nullable Field field = phpClass.findFieldByName(classField.toString(), false);
                if (null == field) {
                    return;
                }

                navigate(project, phpClass.getContainingFile().getVirtualFile(), field.getTextOffset() + field.getName().length() , requestFocus);
            }
        };
    }

    public static @Nullable Navigatable createNavigatable(@Nullable Project project, @Nullable TestStandNavigationUtil.ClassConst classField) {
        if (null == project || null == classField) {
            return null;
        }
        return new NavigatableAdapter() {
            @Override
            public void navigate(boolean requestFocus) {
                @NotNull Collection<PhpClass> phpClassCollection = PhpIndex.getInstance(project).getAnyByFQN(classField.getClassFQN().toString());
                if (1 != phpClassCollection.size()) {
                    return;
                }
                PhpClass phpClass = phpClassCollection.iterator().next();

                @Nullable Field field = phpClass.findFieldByName(classField.toString(), true);
                if (null == field) {
                    return;
                }

                navigate(project, phpClass.getContainingFile().getVirtualFile(), field.getTextOffset() + field.getName().length() , requestFocus);
            }
        };
    }


    public static @Nullable Navigatable createNavigatable(@Nullable Project project, @Nullable TestStandNavigationUtil.ClassMethod classMethod) {
        if (null == project || null == classMethod) {
            return null;
        }
        return new NavigatableAdapter() {
            @Override
            public void navigate(boolean requestFocus) {
                @NotNull Collection<PhpClass> phpClassCollection = PhpIndex.getInstance(project).getAnyByFQN(classMethod.getClassFQN().toString());
                if (1 != phpClassCollection.size()) {
                    return;
                }
                PhpClass phpClass = phpClassCollection.iterator().next();

                @Nullable Method method = phpClass.findMethodByName(classMethod.toString());
                if (null == method) {
                    return;
                }

                navigate(project, phpClass.getContainingFile().getVirtualFile(), method.getTextOffset() + method.getName().length() , requestFocus);
            }
        };
    }

    public static @Nullable Navigatable createNavigatable(@Nullable Project project, @Nullable TestStandNavigationUtil.MethodParam param) {
        if (null == project || null == param) {
            return null;
        }
        return new NavigatableAdapter() {
            @Override
            public void navigate(boolean requestFocus) {
                @NotNull ClassFQN classFQN = param.getClassMethod().getClassFQN();

                @NotNull Collection<PhpClass> phpClassCollection = PhpIndex.getInstance(project).getAnyByFQN(classFQN.toString());
                if (1 != phpClassCollection.size()) {
                    return;
                }
                PhpClass phpClass = phpClassCollection.iterator().next();

                @Nullable Method method = phpClass.findMethodByName( param.getClassMethod().toString());
                if (null == method) {
                    return;
                }

                Parameter sourceParam = null;
                for (Parameter currentParam: method.getParameters())  {
                    if (param.toString().equals(param.toString())) {
                        sourceParam = currentParam;
                        break;
                    }
                }
                if (null == sourceParam) {
                    return;
                }


                navigate(project, phpClass.getContainingFile().getVirtualFile(), sourceParam.getTextOffset() , true);
            }
        };
    }


    public static MethodParam createMethodParam(Parameter param) {
        Method method = (Method) param.getParent().getParent();
        return new MethodParam(
                param.getName(),
                new ClassMethod(
                        method.getName(),
                        new ClassFQN(Objects.requireNonNull(method.getContainingClass()).getFQN())
                )
        );
    }
}
