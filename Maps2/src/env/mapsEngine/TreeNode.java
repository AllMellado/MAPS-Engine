package mapsEngine;

import java.util.List;

public class TreeNode {
	private Boolean isLeaf;
	private String Label;
	private float threshold;
	private List<TreeNode> children;
	
	public TreeNode(Boolean isLeaf, String label, float threshold) {
		setIsLeaf(isLeaf);
		setLabel(label);
		setThreshold(threshold);
	}
	
	public Boolean getIsLeaf() {
		return isLeaf;
	}

	public void setIsLeaf(Boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	public String getLabel() {
		return Label;
	}

	public void setLabel(String label) {
		Label = label;
	}

	public float getThreshold() {
		return threshold;
	}

	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}

	public List<TreeNode> getChildren() {
		return children;
	}

	public void setChildren(List<TreeNode> children) {
		this.children = children;
	}
}
