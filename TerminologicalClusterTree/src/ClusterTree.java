


import java.util.ArrayList;

import org.dllearner.core.owl.Description;

public class ClusterTree {

	public ClusterTree getPos() {
		return root.pos;
	}

	public ClusterTree getNeg() {
		return root.neg;
	}

	private class DLNode {

		Description concept;		// node concept
	    ArrayList<Integer> cluster;
	    Integer posMedoid;
	    Integer  negMedoid;
		ClusterTree pos; 			// positive decision subtree
		
		ClusterTree neg; 			// negative decision subtree

		public DLNode(Description c, ArrayList<Integer>cluster, Integer p, Integer n) {
			concept = c;
			this.cluster=cluster;
			this.posMedoid= p;
			this.negMedoid=n;
			this.pos = this.neg = null; // node has no children
		}

		//		public DLNode() {
		//			concept = null;
		////			this.pos = this.neg = null; // node has no children
		//		}


		public String toString() {
			return this.concept.toString();
		}

	}

	DLNode root;
	
	public ClusterTree() {

	}

	public ClusterTree (Description c, ArrayList<Integer> inds, Integer p, Integer n) {		
		this.root = new DLNode(c,inds, p, n);
	}

	/**
	 * @param root the root to set
	 */
	public void setRoot(Description concept, ArrayList<Integer>cluster, Integer p, Integer  n) {
		this.root = new DLNode(concept, cluster, p, n);
		//		this.root.concept = concept;
	}

	/**
	 * @return the root
	 */
	public Description getRoot() {
		return root.concept;
	}


	public void setPosTree(ClusterTree subTree) {
		this.root.pos = subTree;

	}

	public void setNegTree(ClusterTree subTree) {

		this.root.neg = subTree;

	}

	
	//public String toString(){
		
		public String toString() {
			if (root.concept==null)
				return "{"+root.cluster.size()+"}";
			if (root.pos == null && root.neg == null)
				return root.toString();
			else
				return root.concept.toString() +"("+root.cluster.size() +") ["+root.pos.toString()+" "+root.neg.toString()+"]";
		}
				
		
//	}
	
}
