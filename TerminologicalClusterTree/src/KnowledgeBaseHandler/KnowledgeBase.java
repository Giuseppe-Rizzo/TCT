package KnowledgeBaseHandler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.owl.Constant;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;

//import org.semanticweb.owl.model.OWLImportsDeclaration;
//import org.semanticweb.owl.util.SimpleURIMapper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import evaluation.Parameters;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLImportsDeclarationImpl;


/**
 *  una classe per rappresentare una Knowledge base
 */
public class KnowledgeBase  {
	static final double d = 0.3;
	//private String urlOwlFile = "file:///C:/Users/Giuseppe/Desktop//mod-biopax-example-ecocyc-glycolysis.owl";
	private String urlOwlFile = "file:///C:/Users/Giusepp/Desktop/Ontologie/GeoSkills.owl";
	private  OWLAPIOntology ontology;
	private  OWLOntologyManager manager;
	private  NamedClass[] allConcepts;
	private  ObjectProperty[] allRoles;
	private  OWLDataFactory dataFactory;
	public OWLOntologyManager getManager() {
		return manager;
	}


	public void setManager(OWLOntologyManager manager) {
		this.manager = manager;
	}

	private  Individual[] allExamples;
	/* Data property: proprietà, valori e domini*/
	private AbstractReasonerComponent reasoner;
	private  DatatypeProperty[] properties;
	private  Individual[][] domini;
	private int[][] classifications;
	public static  Random generator = new Random(2);;
	private  Random sceltaDataP= new Random(1);
	private  Random sceltaObjectP= new Random(1);
	private Constant[][] dataPropertiesValue;
	public KnowledgeBase(String url) {
		urlOwlFile=url;
		ontology=initKB();

		// object property  Attribut-3AForschungsgruppe



	}

	
	public   OWLAPIOntology initKB() {

		manager = OWLManager.createOWLOntologyManager();        
		 OWLDataFactoryImpl owlDataFactoryImpl = new OWLDataFactoryImpl();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager(owlDataFactoryImpl);
        OWLOntology ontology= null;
		try {
			//SimpleIRIMapper mapper = new SimpleIRIMapper(IRI.create("http://semantic-mediawiki.org/swivt/1.0"),IRI.create("file:///C:/Users/Utente/Documents/Dottorato/Dataset/Dottorato/10.owl"));
			//			manager.addURIMapper();
			//manager.addIRIMapper(mapper);

			//ontology = manager.loadOntologyFromPhysicalURI(fileURI);
			//org.semanticweb.owlapi.model.OWLImportsDeclaration importDeclaraton = owlDataFactoryImpl.getOWLImportsDeclaration(IRI.create("file:///C:/Users/Utente/Documents/Dottorato/Dataset/Dottorato/10.owl"));
			//manager.makeLoadImportRequest(importDeclaraton);
			ontology = manager.loadOntologyFromOntologyDocument(new FileInputStream(urlOwlFile));
		} catch (OWLOntologyCreationException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        IRI ontologyIRI = manager.getOntologyDocumentIRI(ontology);
        OWLAPIOntology wrapper= new OWLAPIOntology(ontology);
        
        FastInstanceChecker fi= new FastInstanceChecker(wrapper);
        //OWLAPIReasoner fi=  new OWLAPIReasoner(wrapper);
        reasoner =fi;
        //fi.setDefaultNegation(false);
        try {
			reasoner.init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        

//		reasoner.getKB().realize();
		System.out.println("\nClasses\n-------");
		List<NamedClass> classList = fi.getAtomicConceptsList();
		allConcepts = new NamedClass[classList.size()];
		int c=0;
		for(NamedClass cls : classList) {
//			if (!fi. && !cls.isAnonymous()) {
				allConcepts[c++] = cls;
				System.out.println(c +" - "+cls);
//			}	        		
		}
		System.out.println("---------------------------- "+c);

		System.out.println("\nProperties\n-------");
		List<ObjectProperty> propList = fi.getAtomicRolesList();
		allRoles = new ObjectProperty[propList.size()];
		int op=0;
		for(ObjectProperty prop : propList) {
			
				allRoles[op++] = prop;
				System.out.println(op+"-"+prop);     		
		}
		System.out.println("---------------------------- "+op);

		System.out.println("\nIndividuals\n-----------");
		Set<Individual> indList = fi.getIndividuals();
		allExamples = new Individual[indList.size()];
		int i=0;
		for(Individual ind : indList) {
			
				allExamples[i++] = ind;
				//				System.out.println(ind);
			       		
		}
		System.out.println("---------------------------- "+i);

		System.out.println("\nKB loaded. \n");	
		return this.ontology;	

	}

	public int[][] getClassMembershipResult(Description[] testConcepts, Description[] negTestConcepts, Individual[] esempi){
		System.out.println("\nClassifying all examples ------ ");
		classifications = new int[testConcepts.length][esempi.length];
		System.out.print("Processed concepts ("+testConcepts.length+"): \n");

		for (int c=0; c<testConcepts.length; ++c) { 
			int p=0;
			int n=0;
			System.out.printf("[%d] ",c);
			for (int e=0; e<esempi.length; ++e) {			
				classifications[c][e] = 0;
				if (reasoner.hasType(testConcepts[c], esempi[e])) {
					classifications[c][e] = +1;
					p++;

				}
				else{ 
					//if (!Parameters.BINARYCLASSIFICATION){
						if (reasoner.hasType(negTestConcepts[c],esempi[e])) 
							classifications[c][e] = -1;
					}
					//else
						//classifications[c][e]=-1;
					
					n++;

			//	}
			}
		//	System.out.printf(": %d  %d \n",p,n);


		}
		return classifications;

	}

	public void setClassMembershipResult(int[][] classifications){
		
		this.classifications=classifications;
		
	}
//	
	public int[][] getClassMembershipResult(){
		
		return classifications;
	}
	
	
	
//	/* (non-Javadoc)
//	 * @see it.uniba.di.lacam.fanizzi.IKnowledgeBase#getRoleMembershipResult(org.semanticweb.owl.model.OWLObjectProperty[], org.semanticweb.owl.model.OWLIndividual[])
//	 */
//	@Override
	public int[][][] getRoleMembershipResult(ObjectProperty[] ruoli, Individual[]esempi){
		System.out.println("\nVerifyng all individuals' relationship ------ ");
		int[][][] correlati= new int [ruoli.length][esempi.length][esempi.length];
		// per ogni regola 
		for (int i=0;i<ruoli.length;i++){
			//per ogni esempio a

			for(int j=0;j<esempi.length;j++){

				//per ogni esempio b
				for(int k=0;k<esempi.length;k++){
					// verifico che l'esempio j è correlato all'esempio k rispetto alla regola i
					//System.out.println(regole[i]+" vs "+dataFactory.getOWLNegativeObjectPropertyAssertionAxiom(esempi[j], regole[i], esempi[k]).getProperty());
					correlati[i][j][k]=0;
					if( reasoner.getRelatedIndividuals(esempi[j], ruoli[i]).contains(esempi[k]))
					{correlati[i][j][k]=1;
					//System.out.println(" Regola "+i+":   "+regole[i]+" Individui: "+i+" "+esempi[j]+" "+k+" "+esempi[k]+" "+correlati[i][j][k]);

					}
					else{
						correlati[i][j][k]=-1;
						//						System.out.println(" Regola "+regole[i]+" Individui: "+i+" "+esempi[j]+" "+k+" "+esempi[k]+" "+correlati[i][j][k]);
					}

				}
			}


		}
		return correlati;
	}
//
	public  void loadFunctionalDataProperties(){
		System.out.println("Data Properties--------------");

		Set<DatatypeProperty> propertiesSet = reasoner.getDatatypeProperties();

		Iterator<DatatypeProperty> iterator=propertiesSet.iterator();
		List<DatatypeProperty> lista= new ArrayList<DatatypeProperty>();
		while(iterator.hasNext()){
			DatatypeProperty corrente=iterator.next();
//			System.out.println(corrente+"-"+corrente.isFunctional(ontology));
			// elimino le proprietà non funzionali

//			if(reasoner.){
				lista.add(corrente);
//				System.out.println(corrente+"-"+corrente.isFunctional(ontology));
//			}
		}
//
//
//
//
//		properties=new OWLDataProperty[lista.size()];
//		if(lista.isEmpty())
//			throw  new RuntimeException("Non ci sono proprietà funzionali");
//		lista.toArray(properties);
//		//		System.out.println("\n Verifica cardinalità del dominio....");
//
//
//		domini=new OWLIndividual[properties.length][];
//		dataPropertiesValue= new OWLConstant[properties.length][];
//		// per ogni proprietà...
//		for(int i=0;i<properties.length;i++){
//
//			domini[i]=new OWLIndividual[0];
//			Map<OWLIndividual, Set<OWLConstant>> prodottoCartesiano=creazioneProdottoCartesianoDominioXValore(properties[i]);
//			Set<OWLIndividual> chiavi=prodottoCartesiano.keySet();
//			//			System.out.println("Dominio proprietà: "+chiavi);
//			domini[i]=chiavi.toArray(domini[i]);// ottenimento individui facenti parte del dominio
//			//			System.out.println("Cardinalità: "+domini[i].length);
//			//			System.out.println(properties[i]+"-"+ domini[i].length);
//			dataPropertiesValue[i]= new OWLConstant[domini[i].length];
//
//			//... e  per l'elemento del dominio corrente...
//
//			for(int j=0;j<domini[i].length;j++){
//
//				//... determino il valore per una proprietà funzionale
//
//				Set<OWLConstant> valori=prodottoCartesiano.get(domini[i][j]);
//				//				System.out.println(properties[i]+":    "+ i+" "+j+domini[i][j]+"----"+valori);
//				OWLConstant[] valoriArray=new OWLConstant[0];
//				valoriArray=valori.toArray(valoriArray);
//				dataPropertiesValue[i][j]=valoriArray[0]; // la lunghezza è pari ad 1 perchè il valore possibile per 1 elemento è uno solo
//				//				System.out.println(dataPropertiesValue[i][j]);
//
//			}
//
//
//		}
//
//
//
	}


	//********************METODI DI ACCESSO  ALLE COMPONENTI DELL'ONTOLOGIA*******************************//
	/* (non-Javadoc)
	 * @see it.uniba.di.lacam.fanizzi.IKnowledgeBase#getRuoli()
	 */

	public ObjectProperty[] getRoles(){
		return allRoles;
	}

	/* (non-Javadoc)
	 * @see it.uniba.di.lacam.fanizzi.IKnowledgeBase#getClasses()
	 */

	public NamedClass[] getClasses(){
		return allConcepts;
	}

	/* (non-Javadoc)
	 * @see it.uniba.di.lacam.fanizzi.IKnowledgeBase#getIndividui()
	 */
	
	public Individual[] getIndividuals(){

		return allExamples;
	}

	/* (non-Javadoc)
	 * @see it.uniba.di.lacam.fanizzi.IKnowledgeBase#getDataProperties()
	 */
	
	public DatatypeProperty[] getDataProperties(){
		return properties;
	}

	/* (non-Javadoc)
	 * @see it.uniba.di.lacam.fanizzi.IKnowledgeBase#getDomini()
	 */

	public Individual[][] getDomains(){
		return domini;
	}
	/* (non-Javadoc)
	 * @see it.uniba.di.lacam.fanizzi.IKnowledgeBase#getDataPropertiesValue()
	 */

	public Constant[][] getDataPropertiesValue(){
		return dataPropertiesValue;

	}
	/* (non-Javadoc)
	 * @see it.uniba.di.lacam.fanizzi.IKnowledgeBase#getURL()
	 */
	public String getURL(){
		return urlOwlFile;
	}





	/* (non-Javadoc)
	 * @see it.uniba.di.lacam.fanizzi.IKnowledgeBase#getRandomProperty(int)
	 */

	public int[] getRandomProperty(int numQueryProperty){

		int[] queryProperty= new int[numQueryProperty];
		int dataTypeProperty=0;
		while(dataTypeProperty<numQueryProperty ){

			int query=sceltaDataP.nextInt(properties.length);
			if (domini[query].length>1){
				queryProperty[dataTypeProperty]=query ;	// creazione delle dataProperty usate per il test
				dataTypeProperty++;

			}

		}
		return queryProperty;
	}
	/* (non-Javadoc)
	 * @see it.uniba.di.lacam.fanizzi.IKnowledgeBase#getRandomRoles(int)
	 */
	
	public int[] getRandomRoles(int numRegole){
		int[] regoleTest= new int[numRegole];
		// 1-genero casualmente un certo numero di regole sulla base delle
		//quali fare la classificazione
		for(int i=0;i<numRegole;i++)
			regoleTest[i]=sceltaObjectP.nextInt(numRegole);
		return regoleTest;

	}



	public AbstractReasonerComponent getReasoner(){

		return reasoner;
	}

	public OWLDataFactory getDataFactory() {
		// TODO Auto-generated method sstub
		return dataFactory;
	}



	


	public OWLAPIOntology getOntology(){
		return ontology;
		
	}
	
	
	public void updateExamples(Individual[] individuals){

		allExamples=individuals;


	}
	
	/**
	 * Sceglie casualmente un concetto tra quelli generati
	 * @return il concetto scelto
	 */
	public Description getRandomConcept() {
		// sceglie casualmente uno tra i concetti presenti 
		Description newConcept = null;

		
		//if (!Parameters.BINARYCLASSIFICATION){
			
			// case A:  ALC and more expressive ontologies
			do {
				newConcept = allConcepts[KnowledgeBase.generator.nextInt(allConcepts.length)];
				if (KnowledgeBase.generator.nextDouble() < d) {
					Description newConceptBase = getRandomConcept();
					if (KnowledgeBase.generator.nextDouble() < d) {
						
						if (KnowledgeBase.generator.nextDouble() <d) { // new role restriction
							ObjectProperty role = allRoles[KnowledgeBase.generator.nextInt(allRoles.length)];
							//					OWLDescription roleRange = (OWLDescription) role.getRange;

							if (KnowledgeBase.generator.nextDouble() < d)
								newConcept = new ObjectAllRestriction(role, newConceptBase);//(dataFactory.getOWLObjectAllRestriction(role, newConceptBase));
							else
								newConcept = new ObjectSomeRestriction(role, newConceptBase);
						}
						else					
							newConcept =  new Negation(newConceptBase); //dataFactory.getOWLObjectComplementOf(newConceptBase);
					}
				} // else ext
				//				System.out.printf("-->\t %s\n",newConcept);
				//			} while (newConcept==null || !(reasoner.getIndividuals(newConcept,false).size() > 0));
			} while ((reasoner.getIndividuals(newConcept).size()<=0));
//		}else{
//			// for less expressive ontologies ALE and so on (complemento solo per concetti atomici)
//			do {
//				newConcept = allConcepts[KnowledgeBase.generator.nextInt(allConcepts.length)];
//				if (KnowledgeBase.generator.nextDouble() < d) {
//					Description newConceptBase = getRandomConcept();
//					if (KnowledgeBase.generator.nextDouble() < d)
//						if (KnowledgeBase.generator.nextDouble() < 0.1) { // new role restriction
//							ObjectProperty role = allRoles[KnowledgeBase.generator.nextInt(allRoles.length)];
//							//					OWLDescription roleRange = (OWLDescription) role.getRange;
//
//							if (KnowledgeBase.generator.nextDouble() < d)
//								newConcept = new ObjectAllRestriction(role, newConceptBase);
//							else
//								newConcept = new ObjectSomeRestriction(role, newConceptBase);
//						}
//				} // else ext
//				else{ //if (KnowledgeBase.generator.nextDouble() > 0.8) {					
//					newConcept = new Negation(newConcept);
//				}
//				//				System.out.printf("-->\t %s\n",newConcept);
//				//			} while (newConcept==null || !(reasoner.getIndividuals(newConcept,false).size() > 0));
//			} while ((reasoner.getIndividuals(newConcept).size()<=0));
			
			
			
//		}

		return newConcept;				
	}


}
