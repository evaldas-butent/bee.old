package com.butent.bee.egg.shared.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class GenericTree<T> {
  private static final int indent = 2;

  public static <T> Collection<T> getSuccessors(T of,
      Collection<GenericTree<T>> in) {
    for (GenericTree<T> tree : in) {
      if (tree.locate.containsKey(of)) {
        return tree.getSuccessors(of);
      }
    }
    return new ArrayList<T>();
  }

  private T root;

  private ArrayList<GenericTree<T>> leafs = new ArrayList<GenericTree<T>>();

  private GenericTree<T> parent = null;

  private HashMap<T, GenericTree<T>> locate = new HashMap<T, GenericTree<T>>();

  public GenericTree(T root) {
    this.root = root;
    locate.put(root, this);
  }

  public GenericTree<T> addLeaf(T leaf) {
    GenericTree<T> t = new GenericTree<T>(leaf);
    leafs.add(t);
    t.parent = this;
    t.locate = this.locate;
    locate.put(leaf, t);
    return t;
  }

  public void addLeaf(T parent, T leaf) {
    if (locate.containsKey(parent)) {
      locate.get(parent).addLeaf(leaf);
    } else {
      addLeaf(parent).addLeaf(leaf);
    }
  }

  public GenericTree<T> getParent() {
    return parent;
  }

  public T getRoot() {
    return root;
  }

  public Collection<GenericTree<T>> getSubTrees() {
    return leafs;
  }

  public Collection<T> getSuccessors(T root) {
    Collection<T> successors = new ArrayList<T>();
    GenericTree<T> tree = getTree(root);
    if (null != tree) {
      for (GenericTree<T> leaf : tree.leafs) {
        successors.add(leaf.root);
      }
    }
    return successors;
  }

  public GenericTree<T> getTree(T element) {
    return locate.get(element);
  }

  public GenericTree<T> setAsParent(T parentRoot) {
    GenericTree<T> t = new GenericTree<T>(parentRoot);
    t.leafs.add(this);
    this.parent = t;
    t.locate = this.locate;
    t.locate.put(root, this);
    t.locate.put(parentRoot, t);
    return t;
  }

  @Override
  public String toString() {
    return printTree(0);
  }

  private String printTree(int increment) {
    String s = "";
    String inc = "";
    for (int i = 0; i < increment; ++i) {
      inc = inc + " ";
    }
    s = inc + root;
    for (GenericTree<T> child : leafs) {
      s += "\n" + child.printTree(increment + indent);
    }
    return s;
  }
}
