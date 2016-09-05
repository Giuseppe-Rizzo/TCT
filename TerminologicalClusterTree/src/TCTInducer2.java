

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.util.Stack;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.Thing;

import KnowledgeBaseHandler.KnowledgeBase;



import utils.Couple;
import utils.Npla;


import refinementOperator.RefinementOperator;
import refinementOperator.RhoRefinementOperator;
//import classifiers.trees.models.AbstractTree;
//import classifiers.trees.models.DLTree;
import evaluation.Parameters;
import evaluation.metrics.separability.FeaturesDrivenDistance;

public class TCTInducer2 {


	KnowledgeBase kb;
	public TCTInducer2(KnowledgeBase k){

		kb=k;
		//	super(k);

	}



	public ClusterTree induceDLTree(ArrayList<Integer> posExs, ArrayList<Integer> negExs, ArrayList<Integer> undExs, 
			int dim, RefinementOperator op) {		
		System.out.printf("Learning problem\t p:%d\t n:%d\t u:%d\t prPos:%4f\t prNeg:%4f\n", 
				posExs.size(), negExs.size(), undExs.size(), 0.5, 0.5);
		//		ArrayList<Integer> truePos= posExs;
		//		ArrayList<Integer> trueNeg= negExs;
		
		 long startingTime= System.currentTimeMillis();

		Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double> examples = new Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>(posExs, negExs, undExs, dim, 0.5, 0.5);
		ClusterTree tree = new ClusterTree(); // new (sub)tree
		Stack<Couple<ClusterTree,Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>>> stack= new Stack<Couple<ClusterTree,Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>>>();
		Couple<ClusterTree,Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>> toInduce= new Couple<ClusterTree,Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>>();
		toInduce.setFirstElement(tree);
		toInduce.setSecondElement(examples);
		stack.push(toInduce);
		Stack<ClusterTree> lastTrees= new Stack<ClusterTree>();
		while(!stack.isEmpty()){
			System.out.printf("Stack: %d \n",stack.size());
			Couple<ClusterTree,Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>> current= stack.pop(); // extract the next element
			ClusterTree currentTree= current.getFirstElement();
			Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double> currentExamples= current.getSecondElement();
			// set of negative, positive and undefined example
			posExs=currentExamples.getFirst();
			negExs=currentExamples.getSecond();
			undExs=currentExamples.getThird();
			
			if (posExs.size() <=1) // no exs
				//	if (prPos >= prNeg) { // prior majority of positives
				currentTree.setRoot(null, posExs, null, null); // set positive leaf
			//				}
			//				else { // prior majority of negatives
			//					currentTree.setRoot(new Nothing()); // set negative leaf
			//				}

			//		double numPos = posExs.size() + undExs.size()*prPos;
			//		double numNeg = negExs.size() + undExs.size()*prNeg;
			else{

				long currentTime=System.currentTimeMillis();
				if (currentTime-startingTime>5000)
					currentTree.setRoot(null, posExs, null, null);
				else{
					//				prPos=perPos;
					//				prNeg=perNeg;

					ArrayList<Description> generateNewConcepts = null;
					//if (Parameters.refinementOperator.compareToIgnoreCase("classifiers.refinementOperator.RhoRefinementOperator")!=0)
					generateNewConcepts=op.generateNewConcepts(Parameters.beam, posExs, negExs); // genera i concetti sulla base degli esempi
					//else{
					//if (lastTrees.isEmpty()){
					//	generateNewConcepts=((RhoRefinementOperator) op).generateNewConcepts(Parameters.beam, posExs, negExs, thing);
					//}
					//else
					//generateNewConcepts=((RhoRefinementOperator) op).generateNewConcepts(Parameters.beam, posExs, negExs, lastTrees.pop().getRoot());
					//}
					Description[] cConcepts= new Description[0];
					//						ArrayList<Description> cConceptsL = op.generateNewConcepts(dim, posExs, negExs);
					//						//						cConceptsL= getRandomSelection(cConceptsL); // random selection of feature set
					//
					cConcepts = generateNewConcepts.toArray(cConcepts);

					//System.out.println("Size refinements "+ cConcepts.length);

					//if (cConcepts.length>1){

					// select node concept
					Description newRootConcept =  selectConceptWithMinOverlap(cConcepts, posExs) ; //(Parameters.CCP?(selectBestConceptCCP(cConcepts, posExs, negExs, undExs, prPos, prNeg, truePos, trueNeg)):(selectBestConcept(cConcepts, posExs, negExs, undExs, prPos, prNeg));

					ArrayList<Integer> posExsT = new ArrayList<Integer>();
					ArrayList<Integer> negExsT = new ArrayList<Integer>();
					ArrayList<Integer> undExsT = new ArrayList<Integer>();
					ArrayList<Integer> posExsF = new ArrayList<Integer>();
					ArrayList<Integer> negExsF = new ArrayList<Integer>();
					ArrayList<Integer> undExsF = new ArrayList<Integer>();

					splitInstanceCheck(newRootConcept, posExs, posExsT, negExsT, undExsT);
					Integer medoidP = getMedoid(posExsT);
					Integer medoidN = getMedoid(negExsT);
					split(newRootConcept, posExs,  posExsT, negExsT );



					// select node concept
					currentTree.setRoot(newRootConcept, posExs, medoidP, medoidN);		
					// build subtrees

					//		undExsT = union(undExsT,);
					ClusterTree posTree= new ClusterTree();
					ClusterTree negTree= new ClusterTree(); // recursive calls simulation
					currentTree.setPosTree(posTree);
					currentTree.setNegTree(negTree);
					Npla<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>, Integer, Double, Double> npla1 = new Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>(posExsT, posExsF, undExsT, dim, 0.0, 0.0);
					Npla<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>, Integer, Double, Double> npla2 = new Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>(negExsT, negExsF, undExsF, dim, 0.0, 0.0);
					Couple<ClusterTree,Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>> pos= new Couple<ClusterTree,Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>>();
					pos.setFirstElement(posTree);
					pos.setSecondElement(npla1);

					// negative branch
					Couple<ClusterTree,Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>> neg= new Couple<ClusterTree,Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>>();
					neg.setFirstElement(negTree);
					neg.setSecondElement(npla2);
					stack.push(neg);
					stack.push(pos);
					lastTrees.push(currentTree);
					//}
					//						else{
					//							if(cConcepts.length!=0){
					//								currentTree.setRoot(cConcepts[0]);
					//								if (currentTree!=null){
					//									System.out.println("sono qui");
					//									DLTree posTree= new DLTree();
					//									posTree.setRoot(new Thing());
					//									System.out.println(currentTree);
					//									currentTree.setPosTree(posTree);
					//
					//									// negative branch
					//									DLTree negTree= new DLTree();
					//									negTree.setRoot(new Nothing());
					//									currentTree.setNegTree(negTree);
					//								}	
					//							}else{
					//								if (prPos >= prNeg) { // prior majority of positives
					//									currentTree.setRoot(thing); // set positive leaf
					//								}
					//								else { // prior majority of negatives
					//									currentTree.setRoot(new Nothing()); // set negative leaf
					//								}
					//
					//
					//							}
					//						}
					//	}
				}
			}
		}
		return tree;

	}



	private Description selectConceptWithMinOverlap(Description[] cConcepts,
			ArrayList<Integer> posExs) {
		// TODO Auto-generated method stub

		Double maxDiff= 0.0d;
		Description bestConcept= cConcepts[0];
		int idx=0;

		for (int i =0; i< cConcepts.length;i++){

			ArrayList<Integer> trueExs= new ArrayList<Integer>();
			ArrayList<Integer> falseExs= new ArrayList<Integer>();
			ArrayList<Integer> undExs= new ArrayList<Integer>();
			splitInstanceCheck(cConcepts[i], posExs, trueExs, falseExs, undExs);
			System.out.println("Size: "+trueExs.size());
			System.out.println("Size: "+falseExs.size());
			Integer medoidP=  trueExs.isEmpty()?getMedoid(posExs): getMedoid(trueExs); // compute the overlap between individuals 
			Integer  medoidN= falseExs.isEmpty()? getMedoid(posExs): getMedoid(falseExs);

			double simpleEntropyDistance = FeaturesDrivenDistance.simpleDistance(medoidP, medoidN);
			if (simpleEntropyDistance>= maxDiff){
				maxDiff= simpleEntropyDistance;
				bestConcept= cConcepts[i];
				idx= i;

			}




		}

		return cConcepts[idx]; // the concept with the minimum risk of overlap
	}


	private Integer getMedoid(ArrayList<Integer> trueExs) {
		// TODO Auto-generated method stub

		if (trueExs.isEmpty() )
			return null;
		else{
			Double maxDist= Double.MIN_VALUE; // the maximum value is 1
			Integer currentMedoid= trueExs.get(0); // the first element
			for (Integer integer : trueExs) {
				double sumDistance= 0.0f;
				for (Integer integer2 : trueExs) {
					sumDistance+=FeaturesDrivenDistance.simpleDistance(integer, integer2);

				}

				if (sumDistance> maxDist){
					currentMedoid = integer;
					maxDist= sumDistance;
				}

			}

			return currentMedoid;
		}


	}



	private void splitInstanceCheck(Description concept, ArrayList<Integer> posExs, ArrayList<Integer> posExsT, ArrayList<Integer> negExsT, ArrayList<Integer> undExsT){
		Description negConcept = new Negation(concept);
		for (int e=0; e<posExs.size(); e++) {
			int exIndex = posExs.get(e);
			if (kb.getReasoner().hasType(concept,kb.getIndividuals()[exIndex]))
				posExsT.add(exIndex);
			else if (kb.getReasoner().hasType(negConcept, kb.getIndividuals()[exIndex]))
				negExsT.add(exIndex);
			else
				undExsT.add(exIndex);		
		}			
	}


	private void split(Description newRootConcept, ArrayList<Integer> posExs,
			ArrayList<Integer> posExsT,
			ArrayList<Integer> negExsT) {
		// TODO Auto-generated method stub


	}


	//	@Override
	//	public void prune(Integer[] pruningSet, AbstractTree tree,
	//			AbstractTree subtree) {
	//
	//
	//
	//		DLTree treeDL= (DLTree) tree;
	//
	//		Stack<DLTree> stack= new Stack<DLTree>();
	//		stack.add(treeDL);
	//		// array list come pila
	//		double nodes= treeDL.getComplexityMeasure();
	//		if(nodes>1){
	//			while(!stack.isEmpty()){
	//				DLTree current= stack.pop(); // leggo l'albero corrente
	//
	//				DLTree pos= current.getPosSubTree();
	//				DLTree neg= current.getNegSubTree();
	//				System.out.println("Current: "+pos+" ----- "+neg+"visited? "+current.isVisited());
	//
	//				if(current.isVisited()){
	//					System.out.println("Valutazione");
	//					int comissionRoot=current.getCommission();
	//					int comissionPosTree= pos.getCommission();
	//					int comissionNegTree= neg.getCommission();
	//
	//					int gainC=comissionRoot-(comissionPosTree+comissionNegTree);
	//
	//					if(gainC<0){
	//
	//						int posExs=current.getPos();
	//						int negExs= current.getNeg();
	//						// rimpiazzo rispetto alla classe di maggioranza
	//						if(posExs<=negExs){
	//
	//							current.setRoot(new Nothing());
	//						}
	//						else{
	//
	//							current.setRoot(new Thing());
	//						}
	//
	//						current.setNegTree(null);
	//						current.setPosTree(null);	
	//
	//
	//
	//					}
	//				}
	//				else{
	//					current.setAsVisited();
	//					stack.push(current); // rimetto in  pila  e procedo alle chiamate ricorsive
	//					if(pos!=null){
	//						if((pos.getNegSubTree()!=null)||(pos.getPosSubTree()!=null))
	//							stack.push(pos);
	//
	//					}
	//					if(neg!=null){
	//						if((neg.getNegSubTree()!=null)||(neg.getPosSubTree()!=null))
	//							stack.push(neg);
	//
	//					}
	//				}
	//
	//			}				
	//		}
	//
	//	}
	//
	//	public void prunePEP(Integer[] pruningSet, AbstractTree tree,
	//			AbstractTree subtree) {
	//
	//
	//
	//		DLTree treeDL= (DLTree) tree;
	//
	//		Stack<DLTree> stack= new Stack<DLTree>();
	//		stack.add(treeDL);
	//		// array list come pila
	//
	//		while(!stack.isEmpty()){
	//			System.out.println("Print");
	//			DLTree current= stack.pop(); // leggo l'albero corrente
	//
	//			List<DLTree> leaves= current.getFoglie();
	//			System.out.println("Print 2");
	//
	//			int commissionRoot= current.getCommission();
	//
	//			int nExsForLeaves=0;
	//			int commissions=0;
	//
	//
	//			for (Iterator iterator = leaves.iterator(); iterator
	//					.hasNext();) {
	//				System.out.println("Print");
	//				DLTree dlTree = (DLTree) iterator.next();
	//				commissions+=dlTree.getCommission();
	//				nExsForLeaves=nExsForLeaves+current.getPos()+current.getNeg();
	//
	//
	//			} 
	//			nExsForLeaves+=2; // laplace correction
	//			commissions+=1;
	//			int gainC=commissionRoot-commissions;
	//
	//			if(gainC<0){
	//
	//				int posExs=current.getPos();
	//				int negExs= current.getNeg();
	//				// rimpiazzo rispetto alla classe di maggioranza
	//				if(posExs<=negExs){
	//
	//					current.setRoot(new Nothing());
	//				}
	//				else{
	//
	//					current.setRoot(new Thing());
	//				}
	//
	//				current.setNegTree(null);
	//				current.setPosTree(null);	
	//
	//
	//
	//			}
	//			else{
	//
	//				DLTree pos=current.getPosSubTree();
	//				DLTree neg= current.getNegSubTree();
	//				if(pos!=null){
	//
	//					stack.push(pos);
	//
	//				}
	//				if(neg!=null){
	//
	//					stack.push(neg);
	//
	//				}
	//			}
	//
	//		}				
	//
	//
	//	}
	//
	//
	//
	//
	//
	//
	//	/**
	//	 * Implementation of a REP-pruning algorithm for TDT
	//	 * @param pruningset
	//	 * @param tree
	//	 * @param results2
	//	 * @return
	//	 */
	//	public int[] doREPPruning(Integer[] pruningset, DLTree tree, int[] results2){
	//		// step 1: classification
	//		System.out.println("Number of Nodes  Before pruning"+ tree.getComplexityMeasure());
	//		int[] results= new int[pruningset.length];
	//		//for each element of the pruning set
	//		for (int element=0; element< pruningset.length; element++){
	//			//  per ogni elemento del pruning set
	//			// versione modificata per supportare il pruning
	//			classifyExampleforPruning(pruningset[element], tree,results2); // classificazione top down
	//
	//		}
	//
	//		prune(pruningset, tree, tree);
	//		System.out.println("Number of Nodes  After pruning"+ tree.getComplexityMeasure());
	//
	//		return results;
	//	}
	//
	//
	//	public int[] doPEPPruning(Integer[] pruningset, DLTree tree, int[] results2){
	//		// step 1: classification
	//		System.out.println("Number of Nodes  Before pruning"+ tree.getComplexityMeasure());
	//		int[] results= new int[pruningset.length];
	//		//for each element of the pruning set
	//		for (int element=0; element< pruningset.length; element++){
	//			//  per ogni elemento del pruning set
	//			// versione modificata per supportare il pruning
	//			classifyExampleforPruning(pruningset[element], tree,results2); // classificazione top down
	//
	//		}
	//		System.out.println("Classification for pruning");
	//		prunePEP(pruningset, tree, tree);
	//		System.out.println("Number of Nodes  After pruning"+ tree.getComplexityMeasure());
	//
	//		return results;
	//	}
	//
	//
	//
	//
	//	/**
	//	 * Ad-hoc implementation for evaluation step in REP-pruning. the method count positive, negative and uncertain instances 
	//	 * @param indTestEx
	//	 * @param tree
	//	 * @param results2
	//	 * @return
	//	 */
	//	public int classifyExampleforPruning(int indTestEx, DLTree tree,int[] results2) {
	//		Stack<DLTree> stack= new Stack<DLTree>();
	//
	//		stack.add(tree);
	//		int result=0;
	//		boolean stop=false;
	//
	//
	//		if (!Parameters.BINARYCLASSIFICATION){
	//			while(!stack.isEmpty() && !stop){
	//				DLTree currentTree= stack.pop();
	//
	//				Description rootClass = currentTree.getRoot();
	//				//			System.out.println("Root class: "+ rootClass);
	//				if (rootClass.equals(new Thing())){
	//					if (results2[indTestEx]==+1){
	//						currentTree.setMatch(0);
	//						currentTree.setPos();
	//					}
	//					else if (results2[indTestEx]==-1){
	//						currentTree.setCommission(0);
	//						currentTree.setNeg(0);
	//					}else{
	//						currentTree.setInduction(0);
	//						currentTree.setUnd();
	//					}
	//					stop=true;
	//					result=+1;
	//
	//				}
	//				else if (rootClass.equals(new Nothing())){
	//
	//					if(results2[indTestEx]==+1){
	//
	//						currentTree.setPos();
	//						currentTree.setCommission(0);
	//					}
	//					else if (results2[indTestEx]==-1){
	//						currentTree.setNeg(0);
	//						currentTree.setMatch(0);
	//					}
	//					else{
	//						currentTree.setUnd();
	//						currentTree.setInduction(0);
	//					}
	//					stop=true;
	//					result=-1;
	//
	//				}else if (kb.getReasoner().hasType( rootClass, kb.getIndividuals()[indTestEx])){
	//					if(results2[indTestEx]==+1){
	//						currentTree.setMatch(0);
	//						currentTree.setPos();
	//					}else if (results2[indTestEx]==-1){
	//						currentTree.setCommission(0);
	//						currentTree.setNeg(0);
	//					}else{
	//						currentTree.setUnd();
	//						currentTree.setInduction(0);
	//					}
	//					stack.push(currentTree.getPosSubTree());
	//
	//				}
	//				else if (kb.getReasoner().hasType( new Negation(rootClass), kb.getIndividuals()[indTestEx])){
	//
	//					if(results2[indTestEx]==+1){
	//						currentTree.setPos();
	//						currentTree.setCommission(0);
	//					}else if(results2[indTestEx]==-1){
	//						currentTree.setNeg(0);
	//						currentTree.setMatch(0);
	//					}else{
	//						currentTree.setUnd();
	//						currentTree.setInduction(0);
	//					}
	//					stack.push(currentTree.getNegSubTree());
	//
	//				}
	//				else {
	//					if(results2[indTestEx]==+1){
	//						currentTree.setPos();
	//						currentTree.setInduction(0);
	//					}else if(results2[indTestEx]==-1){
	//						currentTree.setNeg(0);
	//						currentTree.setInduction(0);
	//					}else{
	//						currentTree.setUnd();
	//						currentTree.setMatch(0);
	//					}
	//					stop=true;
	//					result=0; 
	//
	//				}
	//			};
	//		}else{
	//
	//			while(!stack.isEmpty() && !stop){
	//				DLTree currentTree= stack.pop();
	//
	//				Description rootClass = currentTree.getRoot();
	//				//			System.out.println("Root class: "+ rootClass);
	//				if (rootClass.equals(new Thing())){
	//					if(results2[indTestEx]==+1){
	//						currentTree.setMatch(0);
	//						currentTree.setPos();
	//					}
	//					else{
	//						currentTree.setCommission(0);
	//						currentTree.setNeg(0);
	//					}
	//					stop=true;
	//					result=+1;
	//
	//				}
	//				else if (rootClass.equals(new Nothing())){
	//
	//					if(results2[indTestEx]==+1){
	//
	//						currentTree.setPos();
	//						currentTree.setCommission(0);
	//					}
	//					else {
	//						currentTree.setNeg(0);
	//						currentTree.setMatch(0);
	//					}
	//
	//					stop=true;
	//					result=-1;
	//
	//				}else if (kb.getReasoner().hasType( rootClass, kb.getIndividuals()[indTestEx])){
	//					if(results2[indTestEx]==+1){
	//						currentTree.setMatch(0);
	//						currentTree.setPos();
	//					}else{
	//						currentTree.setCommission(0);
	//						currentTree.setNeg(0);
	//					}
	//					stack.push(currentTree.getPosSubTree());
	//
	//				}
	//				else {
	//
	//					if(results2[indTestEx]==+1){
	//						currentTree.setPos();
	//						currentTree.setCommission(0);
	//					}else{
	//						currentTree.setNeg(0);
	//						currentTree.setMatch(0);
	//					}
	//					stack.push(currentTree.getNegSubTree());
	//
	//				}
	//
	//			};
	//
	//
	//		}
	//
	//		return result;
	//
	//	}
	
	public ArrayList<Description> extractDisjointnessAxiom (Description  fatherDescription, ClusterTree  tree){
		
		ArrayList<Description> concepts= new ArrayList<Description>();
		Description root = tree.getRoot();
		if (root ==null){
			concepts.add(fatherDescription);
			return   concepts;
		}
		else {
			
			  Description currentDescriptionLeft= //fatherDescription !=null? new Intersection(fatherDescription, root): 
				  root;
			  Description currentDescriptionRight= //fatherDescription !=null? new Intersection( new Negation(fatherDescription),new Negation(root)): 
				  new Negation(root);
			   
			  ArrayList<Description> toAdd= new ArrayList<Description>();
			  ArrayList<Description> toAdd2= new ArrayList<Description>();
			  toAdd.addAll(extractDisjointnessAxiom(currentDescriptionLeft, tree.getPos()));
			  toAdd2.addAll(extractDisjointnessAxiom(currentDescriptionRight, tree.getNeg()));
			  concepts.addAll(toAdd);
			  concepts.addAll(toAdd2);
;			return concepts;
		}
		
	}
	
	public ArrayList<Description>  extractDisjointnessAxiom(ClusterTree t){
		Description fatherNode= null;
		ArrayList<Description> concepts=  extractDisjointnessAxiom(fatherNode, t);
		
		for  (int i=0; i<concepts.size();i++){
			Description  c= concepts.get(i);
			
			for (int j=0; j<concepts.size();j++){
				Description  d= concepts.get(j);
				if (!(c.equals(d)))
					System.out.println(c +"disjoint With"+d);
				
			} 
		}
		
		return concepts;
	}

}



