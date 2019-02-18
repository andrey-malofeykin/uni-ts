package ru.uniteller.phpstorm.plugin.ts.ui;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.ui.JBColor;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.MouseEventAdapter;
import com.intellij.util.ui.SwingHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.uniteller.phpstorm.plugin.ts.service.Config;
import ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestStandPanel extends JBTabbedPane implements DataProvider {
    public static final String ID = "TestStand";
    private final ActionManager actionManager = ActionManager.getInstance();
    private Project project;
    private Config config;

    private SubjectsTreeStructure subjectsTreeStructure;
    private JEditorPane subjectDocPanel;
    private SimpleTree subjectTree;

    private boolean isInitViewComponent() {
        return null != subjectsTreeStructure && null != subjectDocPanel && null != subjectTree;
    }

    public TestStandPanel(Project project, Config config) {
        super();
        this.project = project;
        this.config = config;

        this.add("Субъекты/Объекты/Команды", this.buildSubjectPanel());


        this.add("Тесты", buildTestPanel());
        Tree tree2 = new Tree();
        this.add("Библиотеки", tree2);
    }

    private JComponent buildTestPanel() {
        Tree tree = new Tree();
        new TestMapStructure(project, config, tree);
        subjectTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        return tree;
    }



    private JComponent buildSubjectPanel() {
        subjectDocPanel = SwingHelper.createHtmlViewer(true, null, JBColor.WHITE, JBColor.BLACK);

        subjectTree = new SimpleTree();
        subjectsTreeStructure = new SubjectsTreeStructure(project, config, subjectTree);

        //Меню пока не используется. На будущее.
        subjectTree.addMouseListener(new PopupHandler() {
            @Override
            public void invokePopup(final Component comp, final int x, final int y) {
                final String id = getMenuId(SubjectsTreeStructure.getSelectedNodes(subjectTree, NamedNode.class));
                if (id != null) {
                    final ActionGroup actionGroup = (ActionGroup) actionManager.getAction(id);
                    if (actionGroup != null) {
                        actionManager.createActionPopupMenu("TestStand Tool Window", actionGroup).getComponent().show(comp, x, y);
                    }
                }
            }

            @Nullable
            private String getMenuId(Collection<? extends NamedNode> nodes) {
                String id = null;
                for (NamedNode node : nodes) {
                    String menuId = node.getMenuId();
                    if (menuId == null) {
                        return null;
                    }
                    if (id == null) {
                        id = menuId;
                    }
                    else if (!id.equals(menuId)) {
                        return null;
                    }
                }
                return id;
            }
        });



        subjectTree.addMouseListener(new MouseEventAdapter<Void>(null) {
            public void mousePressed(MouseEvent e) {
                int selRow = subjectTree.getRowForLocation(e.getX(), e.getY());
                if(selRow != -1 && e.getClickCount() == 2) {
                    handleDoubleClickOrEnter(subjectTree);
                }
            }
        });
        subjectTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER
                        && !subjectTree.isSelectionEmpty()
                        && null != subjectTree.getSelectionPaths()
                        && subjectTree.getSelectionPaths().length == 1

                ) {
                    handleDoubleClickOrEnter(subjectTree);
                }
            }
        });




        subjectTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        subjectTree.addTreeSelectionListener(e -> {
            if (!(e.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode)) {
                return;
            }
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
            Object userObj = node.getUserObject();

            boolean needDefaultAction = true;
            if (userObj instanceof DescriptionProvider) {
                subjectDocPanel.setText(((DescriptionProvider) userObj).getDescriptionSource());
                needDefaultAction = false;
            }

            if (needDefaultAction) {
                subjectDocPanel.setText("");
            }
        });

        subjectTree.setRootVisible(true);

        return new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                ScrollPaneFactory.createScrollPane(subjectTree),
                ScrollPaneFactory.createScrollPane(subjectDocPanel)
        );
    }

    private void handleDoubleClickOrEnter(SimpleTree tree) {
        if (!isInitViewComponent()) {
            return;
        }

        List<NamedNode> nodes = SubjectsTreeStructure.getSelectedNodes(tree, NamedNode.class);
        if (1 != nodes.size()) {
            return;
        }
        NamedNode node = nodes.iterator().next();
        if (node instanceof ExpandTreeNodeProvider) {
            expandTreeNodeHandler((ExpandTreeNodeProvider)node, subjectsTreeStructure, subjectDocPanel);
        }
    }

    private void expandTreeNodeHandler(ExpandTreeNodeProvider userObj, SubjectsTreeStructure  treeStructure, JEditorPane docPanel) {
        ExpandTreeNodeProvider.TargetInfo objInfo = userObj.getTargetInfo();
        if (null == objInfo ) {
            return;
        }
        @NotNull RootNode root = (RootNode)treeStructure.getRootElement();
        SubjectNode subjectNode =  root.getSubject(objInfo.getSubjectName());

        docPanel.setText("name subject node" + objInfo.getSubjectName());
        if (null == subjectNode) {
           return;
        }

        @Nullable ObjectNode objNode = subjectNode.getSubjectObject(objInfo.getObjectName());
        if (null == objNode) {
            treeStructure.getTreeBuilder().expand(subjectNode, () -> treeStructure.getTreeBuilder().select(subjectNode));
        } else {
            treeStructure.getTreeBuilder().expand(objNode, () -> treeStructure.getTreeBuilder().select(objNode));
        }
    }

    @Override
    public @Nullable Object getData(@NotNull String dataId) {
        if (CommonDataKeys.NAVIGATABLE_ARRAY.is(dataId)) return extractNavigatables();
        return null;
    }

    private Object extractNavigatables() {
        final List<Navigatable> navigatables = new ArrayList<>();
        for (NamedNode each : getSelectedNodes(NamedNode.class)) {
            Navigatable navigatable = each.getNavigatable();
            if (navigatable != null) navigatables.add(navigatable);
        }
        return navigatables.isEmpty() ? null : navigatables.toArray(new Navigatable[0]);
    }

    private <T extends NamedNode> List<T> getSelectedNodes(Class<T> aClass) {
        if (null == subjectTree) {
            return (List<T>) new ArrayList<NamedNode>();
        }
        return SubjectsTreeStructure.getSelectedNodes(subjectTree, aClass);
    }
}
