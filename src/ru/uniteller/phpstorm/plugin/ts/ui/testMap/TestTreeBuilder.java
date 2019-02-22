package ru.uniteller.phpstorm.plugin.ts.ui.testMap;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.uniteller.phpstorm.plugin.ts.service.Config;

import java.util.HashMap;

public class TestTreeBuilder {
    private class Tab {
        private String name;
        Tab(String name) {
            this.name = name;
        }
        String getName() {
            return name;
        }
    }
    private class DevType {
        private String name;
        DevType(String name) {
            this.name = name;
        }
        String getName() {
            return name;
        }
    }
    private class MetaTest {
        private String name;
        private String testLinkId;
        MetaTest(String name, String testLinkId) {
            this.name = name;
            this.testLinkId = testLinkId;
        }
        String getName() {
            return name;
        }

        String getTestLinkId() {
            return testLinkId;
        }
    }
    private class Column {
        private String name;
        Column(String name) {
            this.name = name;
        }
        String getName() {
            return name;
        }
    }
    private class TestTemplate {
        private MetaTest metaTest;
        private Column column;
        private String columnValue;
        TestTemplate(MetaTest metaTest, Column column, String columnValue) {
            this.metaTest = metaTest;
            this.column = column;
            this.columnValue = columnValue;
        }
        MetaTest getMetaTest() {
            return metaTest;
        }
        Column getColumn() {
            return column;
        }
        String getColumnValue() {
            return columnValue;
        }
    }

    private HashMap<DevType, HashMap<Tab, HashMap<MetaTest, HashMap<Column, TestTemplate>>>> generalIndex = new HashMap<>();
    private HashMap<String, DevType> devTypeIndex = new HashMap<>();
    private HashMap<String, Tab> tabIndex = new HashMap<>();
    private HashMap<String, MetaTest> metaTestIndex = new HashMap<>();
    private HashMap<String, Column> columnIndex = new HashMap<>();



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

    private void dispatchMetadata(@NotNull ArrayCreationExpression metadata) {
        for (ArrayHashElement hashElement: metadata.getHashElements()) {
            @Nullable String devTypeStr = extractStr(hashElement.getKey());
            if (null == devTypeStr) {
                continue;
            }
            devTypeIndex.putIfAbsent(devTypeStr, new DevType(devTypeStr));
            @NotNull DevType devType = devTypeIndex.get(devTypeStr);
            generalIndex.putIfAbsent(devType, new HashMap<>());



            @Nullable PhpPsiElement devTests = hashElement.getValue();
            if (!(devTests instanceof ArrayCreationExpression)) {
                continue;
            }

            for (ArrayHashElement tabTests: ((ArrayCreationExpression) devTests).getHashElements()) {
                @Nullable String tabName = extractStr(tabTests.getKey());
                if (null == tabName) {
                    continue;
                }
                tabIndex.putIfAbsent(tabName,  new Tab(tabName));
                @NotNull Tab tab = tabIndex.get(tabName);
                generalIndex.get(devType).putIfAbsent(tab, new HashMap<>());


                @Nullable PhpPsiElement metaTestCollection = tabTests.getValue();
                if (!(metaTestCollection instanceof ArrayCreationExpression)) {
                    continue;
                }
                for (ArrayHashElement metaTestConfig: ((ArrayCreationExpression) metaTestCollection).getHashElements()) {
                    if (!(metaTestConfig instanceof ArrayCreationExpression)) {
                        continue;
                    }
                    String metaTestDescription = null;
                    String metaTestTestLink = null;
                    ArrayCreationExpression metaTestColumns = null;
                    for (ArrayHashElement metaTestElement: ((ArrayCreationExpression) metaTestConfig).getHashElements()) {
                        String metaTestElementKey = extractStr(metaTestElement.getKey());
                        if (null != metaTestElementKey && metaTestElementKey.equals("description")) {
                            String candidateMetaTestDescription = extractStr(metaTestElement.getValue());
                            if (null != candidateMetaTestDescription) {
                                metaTestDescription = candidateMetaTestDescription;
                            }
                        }
                        if (null != metaTestElementKey && metaTestElementKey.equals("testLink")) {
                            String candidateMetaTestTestLink = extractStr(metaTestElement.getValue());
                            if (null != candidateMetaTestTestLink) {
                                metaTestTestLink = candidateMetaTestTestLink;
                            }
                        }
                        if (null != metaTestElementKey && metaTestElementKey.equals("columns")) {
                            if (metaTestElement.getValue() instanceof ArrayCreationExpression) {
                                metaTestColumns = (ArrayCreationExpression)metaTestElement.getValue();
                            }
                        }
                    }
                    MetaTest metaTest = null;
                    if (null != metaTestDescription) {
                        metaTestIndex.putIfAbsent(metaTestDescription,  new MetaTest(metaTestDescription, metaTestTestLink));
                        metaTest = metaTestIndex.get(metaTestDescription);
                        generalIndex.get(devType).get(tab).putIfAbsent(metaTest, new HashMap<>());
                    }
                    if (null != metaTestColumns) {
                        String columnName = null;
                        String columnValue = null;

                        for (ArrayHashElement column: metaTestColumns.getHashElements()) {
                            String columnKey = extractStr(column.getKey());
                            if (null != columnKey && columnKey.equals("columnName")) {
                                String candidateColumnName = extractStr(column.getValue());
                                if (null != candidateColumnName) {
                                    columnName = candidateColumnName;
                                }
                            }
                            if (null != columnKey && columnKey.equals("columnValue")) {
                                String candidateColumnValue = extractStr(column.getValue());
                                if (null != candidateColumnValue) {
                                    columnValue = candidateColumnValue;
                                }
                            }

                        }

                        if (null != columnName) {
                            columnIndex.putIfAbsent(columnName, new Column(columnName));
                            @NotNull Column column = columnIndex.get(columnName);
                            if (null != metaTest) {
                                TestTemplate testTemplate = new TestTemplate(metaTest, column, columnValue);
                                generalIndex.get(devType).get(tab).get(metaTest).putIfAbsent(column,  testTemplate);
                            }
                        }
                    }
                }
            }

        }
    }

    @Nullable private String extractStr(PsiElement element) {
        if (element instanceof Field) {
            @Nullable PsiElement value = ((Field) element).getDefaultValue();
            return extractStr(value);
        } else if (element instanceof StringLiteralExpression) {
            String value = element.getText();
            if (value.startsWith("'") && value.endsWith("'")) {
                return value.substring(1, value.length() - 1);
            }
            if (value.startsWith("\"") && value.endsWith("\"")) {
                return value.substring(1, value.length() - 1);
            }

            return element.getText();
        }
        return null;
    }


    private void buildIndex() {
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

                dispatchMetadata((ArrayCreationExpression)metadataFieldValue);

            });
        });
    }



    public NamedNode[] build() {
        buildIndex();




        return new NamedNode[0];
    }


}
