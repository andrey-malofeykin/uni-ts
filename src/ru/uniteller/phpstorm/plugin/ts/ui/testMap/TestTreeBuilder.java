package ru.uniteller.phpstorm.plugin.ts.ui.testMap;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.uniteller.phpstorm.plugin.ts.service.Config;
import ru.uniteller.phpstorm.plugin.ts.ui.testMap.presentation.General;
import ru.uniteller.phpstorm.plugin.ts.util.PhpPsiUtil;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class TestTreeBuilder {
    public class Tab {
        private String name;

        Tab(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public class DevType {
        private String name;

        DevType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public class MetaTest {
        private String name;
        private String testLinkId;
        private String presentationName;

        MetaTest(String name, String testLinkId) {
            this.name = name;
            this.testLinkId = testLinkId;
            this.presentationName = null != testLinkId ? "(" + testLinkId + ")" + name : name;
        }

        public String getName() {
            return name;
        }

        public String getTestLinkId() {
            return testLinkId;
        }

        public String getPresentationName() {
            return presentationName;
        }
    }

    public class Column {
        private String name;

        Column(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }
    }

    public class TestTemplate {
        private MetaTest metaTest;
        private Column column;
        private String columnValue;
        private String presentationName;

        TestTemplate(MetaTest metaTest, Column column, String columnValue) {
            this.metaTest = metaTest;
            this.column = column;
            this.columnValue = columnValue;

            this.presentationName = column.getName();
            if (null != columnValue){
                this.presentationName = this.presentationName + "(" + columnValue + ")";
            }

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

        public String getPresentationName() {
            return  presentationName;
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
        metadata.getHashElements().forEach(metadataItem -> {
            @Nullable String devTypeStr = extractStr(metadataItem.getKey());
            if (null == devTypeStr) {
                return;
            }
            devTypeIndex.putIfAbsent(devTypeStr, new DevType(devTypeStr));
            @NotNull DevType devType = devTypeIndex.get(devTypeStr);
            generalIndex.putIfAbsent(devType, new HashMap<>());

            @Nullable PhpPsiElement devTests = metadataItem.getValue();
            if (!(devTests instanceof ArrayCreationExpression)) {
                return;
            }

            ((ArrayCreationExpression) devTests).getHashElements().forEach(tabTests->{
                @Nullable String tabName = extractStr(tabTests.getKey());
                if (null == tabName) {
                    return;
                }
                tabIndex.putIfAbsent(tabName, new Tab(tabName));
                @NotNull Tab tab = tabIndex.get(tabName);
                generalIndex.get(devType).putIfAbsent(tab, new HashMap<>());


                @Nullable PhpPsiElement metaTestCollectionNode = tabTests.getValue();
                if (!(metaTestCollectionNode instanceof ArrayCreationExpression)) {
                    return;
                }
                PhpPsiUtil.iterateArrayOfArray(metaTestCollectionNode, metaTestInfo->{
                    AtomicReference<String> metaTestDescription = new AtomicReference<>(null);
                    AtomicReference<String> metaTestTestLink = new AtomicReference<>(null);
                    AtomicReference<ArrayCreationExpression> metaTestColumnsNode = new AtomicReference<>(null);

                    metaTestInfo.getHashElements().forEach(metaTestElement->{
                        String metaTestElementKey = extractStr(metaTestElement.getKey());
                        if (null != metaTestElementKey && metaTestElementKey.equals("description")) {
                            String candidateMetaTestDescription = extractStr(metaTestElement.getValue());
                            if (null != candidateMetaTestDescription) {
                                metaTestDescription.set(candidateMetaTestDescription);
                            }
                        }
                        if (null != metaTestElementKey && metaTestElementKey.equals("testLink")) {
                            String candidateMetaTestTestLink = extractStr(metaTestElement.getValue());
                            if (null != candidateMetaTestTestLink) {
                                metaTestTestLink.set(candidateMetaTestTestLink);
                            }
                        }
                        if (null != metaTestElementKey && metaTestElementKey.equals("columns")) {
                            if (metaTestElement.getValue() instanceof ArrayCreationExpression) {
                                metaTestColumnsNode.set((ArrayCreationExpression) metaTestElement.getValue());
                            }
                        }
                    });

                    AtomicReference<MetaTest> metaTest = new AtomicReference<>();
                    if (null != metaTestDescription.get()) {
                        metaTestIndex.putIfAbsent(metaTestDescription.get(), new MetaTest(metaTestDescription.get(), metaTestTestLink.get()));
                        metaTest.set(metaTestIndex.get(metaTestDescription.get()));
                        generalIndex.get(devType).get(tab).putIfAbsent(metaTest.get(), new HashMap<>());
                    }
                    if (null != metaTestColumnsNode.get()) {

                        PhpPsiUtil.iterateArrayOfArray(metaTestColumnsNode.get(), columnNode-> {
                            AtomicReference<String> columnName = new AtomicReference<>(null);
                            AtomicReference<String> columnValue = new AtomicReference<>(null);

                            columnNode.getHashElements().forEach(column->{
                                String columnKey = extractStr(column.getKey());
                                if (null != columnKey && columnKey.equals("columnName")) {
                                    String candidateColumnName = extractStr(column.getValue());
                                    if (null != candidateColumnName) {
                                        columnName.set(candidateColumnName);
                                    }
                                }
                                if (null != columnKey && columnKey.equals("columnValue")) {
                                    String candidateColumnValue = extractStr(column.getValue());
                                    if (null != candidateColumnValue) {
                                        columnValue.set(candidateColumnValue);
                                    }
                                }
                            });
                            if (null != columnName.get()) {
                                columnIndex.putIfAbsent(columnName.get(), new Column(columnName.get()));
                                @NotNull Column column = columnIndex.get(columnName.get());
                                if (null != metaTest.get()) {
                                    TestTemplate testTemplate = new TestTemplate(metaTest.get(), column, columnValue.get());
                                    generalIndex.get(devType).get(tab).get(metaTest.get()).putIfAbsent(column, testTemplate);
                                }
                            }



                        });

                    }
                });
            });
        });

    }

    @Nullable
    private String extractStr(PsiElement element) {
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
        phpIndex.getInterfacesByFQN(config.getTestInterfaceFQN()).forEach(testInterface -> phpIndex.getAllSubclasses(testInterface.getFQN()).forEach(testClass -> {
            if (testClass.isAbstract() || testClass.isInterface()) {
                return;
            }
            @Nullable Field metadataField = testClass.findFieldByName("metadata", false);
            if (null == metadataField
                    || !metadataField.isValid()
                    || !metadataField.getModifier().isProtected()
                    || !metadataField.getModifier().isStatic()
                    || null == metadataField.getDefaultValue()
                    || null == metadataField.getDefaultValue()
            ) {
                return;
            }
            PsiElement metadataFieldValue = metadataField.getDefaultValue();
            if (!metadataFieldValue.isValid() || !(metadataFieldValue instanceof ArrayCreationExpression)) {
                return;
            }

            dispatchMetadata((ArrayCreationExpression) metadataFieldValue);

        }));
    }


    public NamedNode[] build() {
        buildIndex();


        return (new General(generalIndex, project, config)).build();
    }


}
