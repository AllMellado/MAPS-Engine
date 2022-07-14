package mapsEngine;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Node<T,ST> implements Iterable<Node<T,ST>> {
	
	T key;
	ST value;
	Node<T,ST> parent;
	List<Node<T,ST>> children;
	
	public boolean isRoot() {
		return parent == null;
	}

	public boolean isLeaf() {
		return children.size() == 0;
	}

	private List<Node<T,ST>> elementsIndex;
	
	public Node(T key, ST value) {
		this.key = key;
		this.value = value;
		this.children = new LinkedList<Node<T,ST>>();
		this.elementsIndex = new LinkedList<Node<T,ST>>();
		this.elementsIndex.add(this);
	}
	
	public Node<T,ST> addChild(Node<T,ST> child){
		Node<T,ST> childNode = child;
		childNode.parent = this;
		this.children.add(childNode);
		return childNode;
	}
	
	public int getLevel() {
		if (this.isRoot())
			return 0;
		else
			return parent.getLevel() + 1;
	}

	private void registerChildForSearch(Node<T,ST> node) {
		elementsIndex.add(node);
		if (parent != null)
			parent.registerChildForSearch(node);
	}

	public Node<T,ST> findTreeNode(Comparable<T> cmp) {
		for (Node<T,ST> element : this.elementsIndex) {
			T elData = element.key;
			if (cmp.compareTo(elData) == 0)
				return element;
		}

		return null;
	}

	@Override
	public String toString() {
		return key != null ? key.toString() : "[data null]";
	}

	@Override
	public Iterator<Node<T,ST>> iterator() {
		NodeIter<T,ST> iter = new NodeIter<T,ST>(this);
		return iter;
	}
	
	
}
