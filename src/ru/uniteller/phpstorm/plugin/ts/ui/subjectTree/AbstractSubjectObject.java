package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

abstract class AbstractSubjectObject extends NamedNode implements DescriptionProvider {
    String objClass;

    AbstractSubjectObject(NamedNode aParent, String name, @Nullable String objClass) {
        super(aParent, name);
        this.objClass = objClass;
    }

    AbstractSubjectObject(AbstractSubjectObject aParent, Field property) {
        this(aParent, property.getName(), null);


        if (property.getName().equals("lines")) {
            String hhh = "d";
        }

        @NotNull PhpType docType = property.getDocType();
        if (docType.isNotExtendablePrimitiveType()) {
            return;
        }

        PhpIndex index = Objects.requireNonNull(getProject()).getComponent(PhpIndex.class);

        @NotNull Set<String> types = docType.getTypes();

        Optional<String> typeOpt = docType.getTypes().stream().findFirst();
        if (!typeOpt.isPresent()) {
            return;
        }
        String typeProperty = typeOpt.get();


        typeProperty = typeProperty.replaceAll("\\[ *] *$", "");


        if (!index.getAnyByFQN(typeProperty).stream().findFirst().isPresent()) {
            return;
        }
        this.objClass = typeProperty;
    }



    @Override
    protected SimpleNode[] buildChildren() {
        if (null == objClass) {
            return new SimpleNode[]{};
        }

        @NotNull PhpIndex index = PhpIndex.getInstance(Objects.requireNonNull(getProject()));
        Optional<PhpClass> objOpt = index.getClassesByFQN(objClass).stream().findFirst();
        if (!objOpt.isPresent()) {
            return new ObjectProperty[0];
        }
        PhpClass objClass = objOpt.get();

        HashMap<String, ObjectProperty> objectProperties = new HashMap<>();
        HashMap<String, ObjectConstant> objectConstants = new HashMap<>();

        buildData(objClass, objectProperties, objectConstants);

        return new SimpleNode[]{
                new Constants(this, objectConstants.values().toArray(new ObjectConstant[0])),
                new Properties(this, objectProperties.values().toArray(new ObjectProperty[0])),
        };
    }

    private void buildData(PhpClass phpClass, HashMap<String, ObjectProperty> objectProperties, HashMap<String, ObjectConstant> objectConstants) {
        phpClass.getFields().forEach(propertyObject -> {
            String propertyName = propertyObject.getName();

            if (null != objectProperties.get(propertyName) || null != objectConstants.get(propertyName)) {
                return;
            }

            if (propertyObject.isConstant()) {
                objectConstants.put(propertyName, new ObjectConstant(this, propertyObject));
            } else if (ObjectProperty.isPropertyObject(propertyObject)) {
                objectProperties.put(propertyName, new ObjectProperty(this, propertyObject));
            }
        });

        @Nullable PhpClass superClass = phpClass.getSuperClass();
        if (null != superClass) {
            buildData(superClass, objectProperties, objectConstants);
        }

        for (PhpClass implementedInterface: phpClass.getImplementedInterfaces()) {
            buildData(implementedInterface, objectProperties, objectConstants);
        }

        for (PhpClass trait: phpClass.getTraits()) {
            buildData(trait, objectProperties, objectConstants);
        }
    }

    class Properties extends NamedNode {
        ObjectProperty[] objectProperties;

        Properties(NamedNode aParent, ObjectProperty[] objectProperties) {
            super(aParent, "properties");
            this.objectProperties = objectProperties;
            updatePresentation();
        }

        private void updatePresentation() {
            PresentationData presentation = getPresentation();
            presentation.clear();
            presentation.addText("Свойства", SimpleTextAttributes.REGULAR_ATTRIBUTES);
            update(presentation);
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return objectProperties;
        }
    }
    class Constants extends NamedNode {
        ObjectConstant[] objectConstants;


        Constants(NamedNode aParent, ObjectConstant[] objectConstants) {
            super(aParent, "constants");
            this.objectConstants = objectConstants;
            updatePresentation();
        }

        private void updatePresentation() {
            PresentationData presentation = getPresentation();
            presentation.clear();
            presentation.addText("Константы", SimpleTextAttributes.REGULAR_ATTRIBUTES);
            update(presentation);
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return objectConstants;
        }
    }

}
