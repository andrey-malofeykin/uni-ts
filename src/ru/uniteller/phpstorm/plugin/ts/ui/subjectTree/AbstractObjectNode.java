package ru.uniteller.phpstorm.plugin.ts.ui.subjectTree;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public abstract class AbstractObjectNode extends NamedNode implements DescriptionProvider {
    private String objClass;

    public AbstractObjectNode(NamedNode aParent, String name, @Nullable String objClass) {
        super(aParent, name);
        this.objClass = objClass;
    }


    void setObjClass(String objClass) {
        this.objClass = objClass;
    }


    AbstractObjectNode(AbstractObjectNode aParent, Field property) {
        this(aParent, property.getName(), null);

        @NotNull PhpType docType = property.getDocType();
        if (docType.isNotExtendablePrimitiveType()) {
            return;
        }

        Optional<String> typeOpt = docType.getTypes().stream().findFirst();
        if (!typeOpt.isPresent()) {
            return;
        }
        String typeProperty = typeOpt.get();


        typeProperty = typeProperty.replaceAll("\\[ *] *$", "");


        if (!phpIndex.getAnyByFQN(typeProperty).stream().findFirst().isPresent()) {
            return;
        }
        this.objClass = typeProperty;
    }



    @Override
    protected SimpleNode[] buildChildren() {
        if (null == objClass) {
            return new SimpleNode[]{};
        }

        Optional<PhpClass> objOpt = phpIndex.getClassesByFQN(objClass).stream().findFirst();
        if (!objOpt.isPresent()) {
            return  new SimpleNode[]{};
        }
        PhpClass objClass = objOpt.get();

        HashMap<String, ObjectNodeProperty> objectProperties = new HashMap<>();
        HashMap<String, ObjectConstant> objectConstants = new HashMap<>();

        buildData(objClass, objectProperties, objectConstants);

        ArrayList<NamedNode>  children = new ArrayList<>();

        if (objectConstants.size() > 0) {
            children.add(new Constants(this, objectConstants.values().toArray(new ObjectConstant[0])));
        }

        if (objectProperties.size() > 0) {
            children.add(new Properties(this, objectProperties.values().toArray(new ObjectNodeProperty[0])));
        }

        return children.toArray(new NamedNode[0]);

    }

    private void buildData(PhpClass phpClass, HashMap<String, ObjectNodeProperty> objectProperties, HashMap<String, ObjectConstant> objectConstants) {
        phpClass.getFields().forEach(propertyObject -> {
            String propertyName = propertyObject.getName();

            if (null != objectProperties.get(propertyName) || null != objectConstants.get(propertyName)) {
                return;
            }

            if (propertyObject.isConstant()) {
                objectConstants.put(propertyName, new ObjectConstant(this, propertyObject));
            } else if (ObjectNodeProperty.isPropertyObject(propertyObject)) {
                objectProperties.put(propertyName, new ObjectNodeProperty(this, propertyObject));
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
        ObjectNodeProperty[] objectProperties;

        Properties(NamedNode aParent, ObjectNodeProperty[] objectProperties) {
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
