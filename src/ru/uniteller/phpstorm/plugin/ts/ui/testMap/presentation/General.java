package ru.uniteller.phpstorm.plugin.ts.ui.testMap.presentation;

import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleNode;
import ru.uniteller.phpstorm.plugin.ts.service.Config;
import ru.uniteller.phpstorm.plugin.ts.ui.testMap.NamedNode;
import ru.uniteller.phpstorm.plugin.ts.ui.testMap.TestTreeBuilder;

import java.util.ArrayList;
import java.util.HashMap;

public class General {
    private class TestTemplateNode extends NamedNode {
        TestTemplateNode(
                NamedNode aParent,
                TestTreeBuilder.TestTemplate testTemplate
        ) {
            super(aParent,  testTemplate.getPresentationName());
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return new SimpleNode[0];
        }
    }


    private class MetaTestNode extends NamedNode {
        private HashMap<TestTreeBuilder.Column, TestTreeBuilder.TestTemplate> child;
        MetaTestNode(
                NamedNode aParent,
                TestTreeBuilder.MetaTest metaTest,
                HashMap<TestTreeBuilder.Column, TestTreeBuilder.TestTemplate> child
        ) {
            super(aParent,  metaTest.getPresentationName());
            this.child = child;
        }

        @Override
        protected SimpleNode[] buildChildren() {
            ArrayList<TestTemplateNode> nodes  = new ArrayList<>();

            NamedNode aParent = this;
            child.forEach((column, testTemplate) -> nodes.add(new TestTemplateNode(aParent, testTemplate)));


            return nodes.toArray(new NamedNode[0]);
        }
    }



    private class TabNode extends NamedNode {
        private HashMap<TestTreeBuilder.MetaTest, HashMap<TestTreeBuilder.Column, TestTreeBuilder.TestTemplate>> child;
        TabNode(
                NamedNode aParent,
                TestTreeBuilder.Tab tab,
                HashMap<TestTreeBuilder.MetaTest, HashMap<TestTreeBuilder.Column, TestTreeBuilder.TestTemplate>> child
        ) {
            super(aParent,  tab.getName());
            this.child = child;
        }

        @Override
        protected SimpleNode[] buildChildren() {
            ArrayList<MetaTestNode> nodes  = new ArrayList<>();

            NamedNode aParent = this;
            child.forEach((metaTest, hashMap) -> nodes.add(new MetaTestNode(aParent, metaTest, hashMap)));


            return nodes.toArray(new NamedNode[0]);
        }
    }


    private class DevTypeNode extends NamedNode {
        private HashMap<TestTreeBuilder.Tab, HashMap<TestTreeBuilder.MetaTest, HashMap<TestTreeBuilder.Column, TestTreeBuilder.TestTemplate>>> child;
        DevTypeNode(
                TestTreeBuilder.DevType devType,
                HashMap<TestTreeBuilder.Tab, HashMap<TestTreeBuilder.MetaTest, HashMap<TestTreeBuilder.Column, TestTreeBuilder.TestTemplate>>> child
        ) {
            super(General.this.project,General.this.config,  devType.getName());
            this.child = child;
        }

        @Override
        protected SimpleNode[] buildChildren() {
            ArrayList<TabNode> nodes  = new ArrayList<>();

            NamedNode aParent = this;
            child.forEach((tab, metaTestHashMapHashMap) -> nodes.add(new TabNode(aParent, tab, metaTestHashMapHashMap)));


            return nodes.toArray(new NamedNode[0]);
        }
    }



    private HashMap<TestTreeBuilder.DevType, HashMap<TestTreeBuilder.Tab, HashMap<TestTreeBuilder.MetaTest, HashMap<TestTreeBuilder.Column, TestTreeBuilder.TestTemplate>>>> generalIndex;
    private Config config;
    private Project project;

    public General(
            HashMap<TestTreeBuilder.DevType, HashMap<TestTreeBuilder.Tab, HashMap<TestTreeBuilder.MetaTest, HashMap<TestTreeBuilder.Column, TestTreeBuilder.TestTemplate>>>> generalIndex,
            Project project,
            Config config
    ) {
        this.generalIndex = generalIndex;
        this.config = config;
        this.project = project;
    }

    public NamedNode[] build() {

        ArrayList<DevTypeNode> nodes  = new ArrayList<>();

        generalIndex.forEach((devType, tabHashMapHashMap) -> nodes.add(new DevTypeNode(devType, tabHashMapHashMap)));


        return nodes.toArray(new NamedNode[0]);
    }
}
