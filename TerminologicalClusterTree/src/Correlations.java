import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.Negation;


public class Correlations {
	
	AbstractReasonerComponent r;
	Description[] concept;
	Individual[] individual;
	Description[][] axiom;
	
	public Correlations(AbstractReasonerComponent r,  Description[] classes, Individual[] individuals){
		 this.r= r;
		 this.concept= classes;
		 this.individual= individuals;
		 axiom=new Description[classes.length][2];
	} 
	
	
	public double computeCorrelation(){
		int a=0;
		int co=0;
		for (int i=0; i<concept.length-1;i++) {
			Description c= concept[i];
			for (int j=i+1; j<concept.length;j++){
				Description d= concept[j];
				Description DAndC= new Intersection(d,c);
				double DANDCInds= r.getIndividuals(DAndC).size(); // both in c and D
				double DAndNotC= r.getIndividuals(new Intersection(c, new Negation(d))).size();
				double NotDAndC= r.getIndividuals(new Intersection(new Negation(c), d)).size();
				double NotDAndNotC= r.getIndividuals(new Intersection(new Negation(c),new Negation(d))).size();
				
				double  dInds=  r.getIndividuals(d).size();
				double cInds= r.getIndividuals(c).size();
				double negD= r.getIndividuals(new Negation(d)).size();
				double negC= r.getIndividuals(new Negation(c)).size();
				
				double den= Math.sqrt(negC*negD*dInds*cInds);
				double  num= (DANDCInds*NotDAndNotC)-(DAndNotC*NotDAndC);
				double coeff= num/den;
				co++;
				if (coeff<0.5){
					//System.out.println(d+"disjoint with "+ c);
					
					a++;
				}
			}
		}
		
		System.out.println( "Number of axioms: "+a);
		System.out.println( "Number of corrlations: "+(co));
		
		return 0.0d;
	}

}
