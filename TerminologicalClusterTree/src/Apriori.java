import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.Negation;
import java.util.*;

public class Apriori {

	AbstractReasonerComponent r;
	Description[] concept;
	Description[] negConcept;
	
	Individual[] individual;
	Description[][] axiom;
	
	public Apriori(AbstractReasonerComponent r,  Description[] classes, Individual[] individuals){
		 this.r= r;
		 this.concept= new  Description[(classes.length*2)]; //classes;
		 Description[] d= new Description[classes.length];
		 for (int i= 0; i< classes.length;i++){
			 concept[i]=classes[i];
		 d[i]= new Negation(concept[i]);
		 }
		 for (int i= classes.length; i<(2*classes.length);i++)
			 concept[i]= d[(i-classes.length)];
		 
		 
		 this.individual= individuals;
		 axiom=new Description[classes.length][2];
	}
	
	
	
	
	public ArrayList<HashSet<Description>>  generateCandidate(ArrayList<HashSet<Description>> candidates, int itemsetSize, int length){
		System.out.println("Candidates: "+ candidates);
		if( itemsetSize==1){
			for (Description  c : concept) {
				System.out.println("Concept"+c);
				final int size = r.getIndividuals(c).size();
				if (size >10){
					 HashSet<Description> arrayList = new HashSet<Description>();
					 arrayList.add(c);
					candidates.add( arrayList);
				}
			}
			 return generateCandidate(candidates, itemsetSize+1, length);
			
		}else{
			//System.out.println("Sono qui: "+ itemsetSize);
			if (itemsetSize<=length){
			ArrayList<HashSet<Description>>  newCandidates= new ArrayList<HashSet<Description>>();
			 // definire passo ricorsivo
			for (HashSet<Description> c: candidates){
				 for (HashSet<Description> d : candidates) { // make the join
					  if (!c.equals(d)){
						    HashSet<Description>  newElem=  new HashSet<>(c);
						    newElem.addAll(d);
						    //System.out.println("newElem: "+ newElem);
						     int size= r.getIndividuals(new Intersection(new ArrayList<Description>(newElem))).size();
						     if (size>10){
						    	 newCandidates.add(newElem);
						     }			  
					  }
				}
				 
			}
			candidates.removeAll(candidates);
			candidates.addAll(newCandidates);
			return generateCandidate(newCandidates, itemsetSize+1, length);	
			}		
			
		}
			
	return candidates;
		
		
	}
}
