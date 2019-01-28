package ru.uniteller.phpstorm.plugin.ts.ui;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.SwingHelper;
import org.jetbrains.annotations.NotNull;
import ru.uniteller.phpstorm.plugin.ts.service.Config;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.ScrollPaneFactory;
import ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.DescriptionProvider;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

public class TestStandPanel extends JBTabbedPane {
    public static final String ID = "TestStand";
    private Project project;
    private Config config;

    public TestStandPanel(Project project, Config config) {
        super();
        this.project = project;
        this.config = config;

        this.add("Субъекты/Объекты/Команды", this.buildSubjectPanel());

        Tree tree1 = new Tree();
        this.add("Тесты", tree1);
        Tree tree2 = new Tree();
        this.add("Библиотеки", tree2);
    }

    private JComponent buildSubjectPanel() {
        @NotNull JEditorPane docPanel = SwingHelper.createHtmlViewer(true, null, JBColor.WHITE, JBColor.BLACK);

        SimpleTree myTree = new SimpleTree();
        myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        myTree.addTreeSelectionListener(e -> {
            if (!(e.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode)) {
                return;
            }
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
            Object userObj = node.getUserObject();
            if (userObj instanceof DescriptionProvider) {
                docPanel.setText(((DescriptionProvider) userObj).getDescriptionSource());
            } else {
                docPanel.setText("");
            }

        });

        myTree.setRootVisible(true);
        new SubjectsTreeStructure(project, config, myTree);




        return new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                ScrollPaneFactory.createScrollPane(myTree),
                ScrollPaneFactory.createScrollPane(docPanel)
        );
    }
}
