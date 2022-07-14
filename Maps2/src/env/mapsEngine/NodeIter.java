package mapsEngine;

import java.util.Iterator;

public class NodeIter<T,ST> implements Iterator<Node<T,ST>>{
	
	enum ProcessStages {
		ProcessParent, ProcessChildCurNode, ProcessChildSubNode
	}

	private Node<T,ST> Node;

	public NodeIter(Node<T,ST> Node) {
		this.Node = Node;
		this.doNext = ProcessStages.ProcessParent;
		this.childrenCurNodeIter = Node.children.iterator();
	}

	private ProcessStages doNext;
	private Node<T,ST> next;
	private Iterator<Node<T,ST>> childrenCurNodeIter;
	private Iterator<Node<T,ST>> childrenSubNodeIter;

	@Override
	public boolean hasNext() {

		if (this.doNext == ProcessStages.ProcessParent) {
			this.next = this.Node;
			this.doNext = ProcessStages.ProcessChildCurNode;
			return true;
		}

		if (this.doNext == ProcessStages.ProcessChildCurNode) {
			if (childrenCurNodeIter.hasNext()) {
				Node<T,ST> childDirect = childrenCurNodeIter.next();
				childrenSubNodeIter = childDirect.iterator();
				this.doNext = ProcessStages.ProcessChildSubNode;
				return hasNext();
			}

			else {
				this.doNext = null;
				return false;
			}
		}
		
		if (this.doNext == ProcessStages.ProcessChildSubNode) {
			if (childrenSubNodeIter.hasNext()) {
				this.next = childrenSubNodeIter.next();
				return true;
			}
			else {
				this.next = null;
				this.doNext = ProcessStages.ProcessChildCurNode;
				return hasNext();
			}
		}

		return false;
	}

	@Override
	public Node<T,ST> next() {
		return this.next;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
