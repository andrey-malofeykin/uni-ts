package ru.uniteller.phpstorm.plugin.ts.ui;


import com.intellij.ide.util.treeView.IndexComparator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.treeStructure.*;
import org.jetbrains.annotations.NotNull;
import ru.uniteller.phpstorm.plugin.ts.service.Config;
import ru.uniteller.phpstorm.plugin.ts.ui.testMap.NamedNode;
import ru.uniteller.phpstorm.plugin.ts.ui.testMap.TestTreeBuilder;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class TestMapStructure extends SimpleTreeStructure {
    private final NamedNode myRoot;
    //private final SimpleTreeBuilder myTreeBuilder;

    TestMapStructure(Project project, Config config, JTree tree) {
        this.myRoot = new NamedNode(project, config, "Тесты") {
            @Override
            protected SimpleNode[] buildChildren() {
                return (new TestTreeBuilder(project, config, this)).build();
            }
        };
        SimpleTreeBuilder myTreeBuilder = new SimpleTreeBuilder(tree, (DefaultTreeModel) tree.getModel(), this, (Comparator<SimpleNode>) (o1, o2) -> {
            if (o1 instanceof NamedNode && o2 instanceof NamedNode)
                return o1.getName().compareTo(o2.getName());
            else
                return IndexComparator.INSTANCE.compare(o1, o2);
        });
        Disposer.register(project, myTreeBuilder);
        myTreeBuilder.initRoot();
        myTreeBuilder.expand(myRoot, null);

    }

    @NotNull
    @Override
    public Object getRootElement() {
        return myRoot;
    }

    static  List<SimpleNode> getSelectedNodes(SimpleTree tree) {
        final List<SimpleNode> filtered = new ArrayList<>();
        for (SimpleNode node : buildSelectedNodes(tree)) {
            if (!(node instanceof NamedNode)) {
                filtered.clear();
                break;
            }

            filtered.add(node);
        }
        return filtered;
    }


    private static List<SimpleNode> buildSelectedNodes(SimpleTree tree) {
        List<SimpleNode> nodes = new ArrayList<>();
        TreePath[] treePaths = tree.getSelectionPaths();
        if (treePaths != null) {
            for (TreePath treePath : treePaths) {
                nodes.add(tree.getNodeFor(treePath));
            }
        }
        return nodes;
    }
}
