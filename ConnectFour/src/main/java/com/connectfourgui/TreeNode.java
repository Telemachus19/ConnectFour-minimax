package com.connectfourgui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TreeNode {

    String name;
    List<TreeNode> children;

    public TreeNode(String name, List<TreeNode> children) {
        this.name = name;
        this.children = children;
    }
    public TreeNode(String name){
        this.name = name;
        this.children = new ArrayList<>();
    }
    public TreeNode(){
        this.name= "";
        this.children = new ArrayList<>();
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(50);
        print(buffer, "", "");
        return buffer.toString();
    }

    public String getName() {
        return name;
    }

    public List<TreeNode> getChildren() {
        return children;
    }
    public void addChild(TreeNode child){
        this.children.add(child);
    }

    private void print(StringBuilder buffer, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        buffer.append(name);
        buffer.append('\n');
        for (Iterator<TreeNode> it = children.iterator(); it.hasNext();) {
            TreeNode next = it.next();
            if (it.hasNext()) {
                next.print(buffer, childrenPrefix + "|---  ", childrenPrefix + "|  ");
            } else {
                next.print(buffer, childrenPrefix + "|---  ", childrenPrefix + "    ");
            }
        }
    }
}
