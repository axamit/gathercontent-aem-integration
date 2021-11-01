/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.pojo.helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>TreeNode</code> tree structure wrapper of parent-child structure.
 *
 * @param <T> Type of data wrapped in tree.
 * @author Axamit, gc.support@axamit.com
 */
public class TreeNode<T> {
    private List<T> data = new ArrayList<>();
    private List<TreeNode<T>> children = new ArrayList<>();
    private TreeNode parent;
    private Integer id;
    private Integer parentId;

    /**
     * Tree node constructor.
     *
     * @param data     Wrapped data.
     * @param id       Current data item id.
     * @param parentId Parent id of data item.
     */
    public TreeNode(T data, Integer id, Integer parentId) {
        this.data.add(data);
        this.id = id;
        this.parentId = parentId;
    }

    public boolean isRoot() {
        return parent == null;
    }

    /**
     * Add child to tree node.
     *
     * @param child Child to add.
     */
    public void addChild(TreeNode<T> child) {
        children.add(child);
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    /**
     * Add data to tree node.
     *
     * @param dataToAdd Data to add.
     */
    public void addData(T dataToAdd) {
        this.data.add(dataToAdd);
    }

    public List<TreeNode<T>> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode<T>> children) {
        this.children = children;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    /**
     * Recursive traverse of tree and adding items to list.
     *
     * @param list List to add tree items.
     * @param node Tree root to traverse.
     * @param <T>  Type of data wrapped in tree.
     */
    public static <T> void treeToList(List<T> list, TreeNode<T> node) {
        list.addAll(node.getData());
        for (TreeNode<T> child : node.getChildren()) {
            treeToList(list, child);
        }
    }

    @Override
    public String toString() {
        return "TreeNode{"
            + "data=" + data
            + ", children=" + children
            + ", parent=" + parent
            + ", id='" + id + '\''
            + ", parentId='" + parentId + '\''
            + '}';
    }
}
