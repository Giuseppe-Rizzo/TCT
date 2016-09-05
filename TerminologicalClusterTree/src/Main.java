

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.Reasoner;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;

import refinementOperator.RefinementOperator;



//import samplers.DatasetAcquisition;
//import samplers.DatasetGenerator;

import evaluation.Evaluation;
import evaluation.Parameters;
import evaluation.metrics.separability.FeaturesDrivenDistance;
//import evaluation.task.AffiliationPrediction;
//import evaluation.task.BiblicWomanPrediction;
//import evaluation.task.GeoSkillsGenerator;
//import evaluation.task.GeoSkillsPrediction;
//import evaluation.task.MutagenicoPrediction;
//import evaluation.task.ClassMembershipPrediction;
//import evaluation.task.PoliticianGenerator;
//import evaluation.task.PoliticianPrediction;
import evaluation.task.Tasks;




public class Main {
	
static KnowledgeBaseHandler.KnowledgeBase kb;
//	static int[][] classification;
 public  PrintStream console = System.out;
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Parameters.loadParameters(); //loading from property file
		System.out.println(Parameters.urlOwlFile);
		kb = new KnowledgeBaseHandler.KnowledgeBase(Parameters.urlOwlFile);
		
	//	Reasoner r=;
		final Individual[] individuals = kb.getIndividuals();
		final AbstractReasonerComponent reasoner = kb.getReasoner();
		final NamedClass[] classes = kb.getClasses();
		FeaturesDrivenDistance.preLoadPi(reasoner, classes, individuals);
		FeaturesDrivenDistance.computeFeatureEntropies(reasoner, classes);
		
		if (args[0].equalsIgnoreCase("apriori")){
			Apriori apriori= new Apriori(reasoner, classes, individuals);
			ArrayList<HashSet<Description>> arrayList = new ArrayList<HashSet<Description>>();
			ArrayList<HashSet<Description>> arrayList2 = new ArrayList<HashSet<Description>>();
			apriori.generateCandidate(arrayList, 1, 2);
			for (HashSet<Description> hashSet : arrayList) {
				boolean disjoint= false;
				for (Description description : hashSet) {
					if (description instanceof Negation){
						disjoint = true;
					}
				}
				if (disjoint)
					arrayList2.add(hashSet);

			} 		
			System.out.println("Axioms (a priori): "+ arrayList2.size());
		}
		else if (args[0].equalsIgnoreCase("tct")){	
			//TODO da decommentare	
			TCTInducer2 t = new TCTInducer2(kb);
			RefinementOperator op = new RefinementOperator(kb);
			ArrayList<Integer> list= new ArrayList<Integer>();
			for (int i = 0; i<individuals.length;i++)
				list.add(i);
			ClusterTree induceDLTree = t.induceDLTree(list, new ArrayList<Integer>(),  new ArrayList<Integer>(), 4, op);
			//System.out.println(induceDLTree);	 
			final ArrayList<Description> extractDisjointnessAxiom = t.extractDisjointnessAxiom(induceDLTree);
			System.out.println("Number of axioms: "+ extractDisjointnessAxiom.size());
	}
		else  if ( (args[0].equalsIgnoreCase("corr"))){
			Correlations corr= new Correlations(reasoner, classes, individuals);
			corr.computeCorrelation();
		}
		else{
			System.out.println("Please, insert one of the following parameters:");
			System.out.println( "'apriori' - for running the association rule mining");
			System.out.println("'tct- for running the terminological cluster tree algorithm");
			System.out.println("corr- for running the negative correlation algorithm ");
			
		}
		
		
//	
////
////		
	
////	
		
	
	 
	
	//Correlations corr= new Correlations(reasoner, classes, individuals);
		//corr.computeCorrelation();
//		
	
			
System.out.println("\n\nEnding: "+Parameters.urlOwlFile);

	} // main
	
	


} // class DLTreeInducer