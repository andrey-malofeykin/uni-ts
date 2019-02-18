package ru.uniteller.phpstorm.plugin.ts.ui.testMap;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpCodeUtil;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClassMember;
import com.jetbrains.php.lang.psi.elements.PhpModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.uniteller.phpstorm.plugin.ts.service.Config;

public class TestTreeBuilder {

    private class DevType {
        private String name;
        DevType(String name) {
            this.name = name;
        }
        String getName() {
            return name;
        }

    }



    private Project project;
    private Config config;
    private NamedNode root;
    private PhpIndex phpIndex;




    public TestTreeBuilder(Project project, Config config, NamedNode root) {
        this.project = project;
        this.config = config;
        this.root = root;
        this.phpIndex = project.getComponent(PhpIndex.class);
    }


    public NamedNode[] build() {
        phpIndex.getInterfacesByFQN(config.getTestInterfaceFQN()).forEach(testInterface -> {
            phpIndex.getAllSubclasses(testInterface.getFQN()).forEach(testClass -> {
                if (testClass.isAbstract() || testClass.isInterface()) {
                    return;
                }
                @Nullable Field metadataField = testClass.findFieldByName("metadata", false);
                if (null == metadataField
                        || !metadataField.isValid()
                        ||  !metadataField.getModifier().isProtected()
                        ||  !metadataField.getModifier().isStatic()
                        || null == metadataField.getDefaultValue()
                        || null == metadataField.getDefaultValue()
                ) {
                    return;
                }
                PsiElement metadataFieldValue = metadataField.getDefaultValue();
                if (!metadataFieldValue.isValid() || !(metadataFieldValue instanceof ArrayCreationExpression)) {
                    return;
                }





                return;
            });
        });




        return new NamedNode[0];
    }
}
