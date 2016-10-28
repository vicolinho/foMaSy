package de.uni_leipzig.dbs.formRepository.evals;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.PropertyConfigurator;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.FormRepositoryImpl;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.operation.SetAnnotationOperator;
import de.uni_leipzig.dbs.formRepository.evals.calculation.EvaluationResult;
import de.uni_leipzig.dbs.formRepository.evals.calculation.MappingEvaluation;
import de.uni_leipzig.dbs.formRepository.evals.io.EvaluationResultWriter;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;
import de.uni_leipzig.dbs.formRepository.exception.VersionNotExistsException;
import de.uni_leipzig.dbs.formRepository.manager.MatchManager;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;
import de.uni_leipzig.dbs.formRepository.matching.execution.RegisteredMatcher;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.ExecutionTree;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.MatchGroup;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.MatchOperator;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.SetOperator;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessingSteps;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorConfig;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorExecutor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.TokenWeighting.TFIDFTokenWeightGenerator;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.encoding.EncodingManager;
import de.uni_leipzig.dbs.formRepository.matching.lookup.TokenSimilarityLookup;
import de.uni_leipzig.dbs.formRepository.matching.selection.GroupSelection;
import de.uni_leipzig.dbs.formRepository.matching.selection.Selection;
import de.uni_leipzig.dbs.formRepository.matching.token.SoftTFIDFMatcher;
import de.uni_leipzig.dbs.formRepository.matching.token.TFIDFMatcher;
import de.uni_leipzig.dbs.formRepository.matching.token.TFIDFWindowMatcher;
import de.uni_leipzig.dbs.formRepository.util.CantorDecoder;

public class UMLS_SimpleFormMatching {
	static boolean filterGeneralConcepts =true; 
	static String[] generalConcepts = new String[]{"Qualitative Concept","Quantitative Concept",
		"Functional Concept","Temporal Concept","Conceptual Entity"};
	
	
	public static void main (String args[]){
		FormRepository rep = new FormRepositoryImpl();
		PropertyConfigurator.configure("log4j.properties");
		String date = "2014-01-01";
		String name="umls2014AB";
		String type ="ontology";
		
		String formName = "NCT00556270";
		String dateForm = "2012-05-25";
		String formType ="eligibility form";
		
		Set<String> semTypes = new HashSet<String> ();
		for (String sem: generalConcepts){
			semTypes.add(sem);
		}
//		int[] selectedForms = new int[]{1,2,7,8,9,
//				10,11,12,13,14,
//				15,19,20,75,76,
//				77,78,79,80,81,
//				82,83,84,85,86};

		int[] selectedForms = new int[]{461,455,456,457,458,459,464,466,
			467,468,465,463,462,452,453,454,469,470,460,473,475,476,439,440};
		
		Set <Integer> selForms = new HashSet<Integer>();
		for(int i : selectedForms)selForms.add(i);

		
		try {
			rep.initialize("fms.ini");
			EntityStructureVersion umls = rep.getFormManager().getStructureVersion(name, type, date);
			PreprocessorConfig config = new PreprocessorConfig();
			PreprocessProperty[] properties = new PreprocessProperty[]{new PreprocessProperty("name", null, null),
					new PreprocessProperty("question","EN",null)};
			config.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW, properties);
			config.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION,properties);
			Set<String> formTypes = new HashSet<String>();
			formTypes.add("quality assurance");
			Set <EntityStructureVersion>forms = rep.getFormManager().getStructureVersionsByType(formTypes);
			Set<EncodedEntityStructure> encodedStructures= new HashSet<EncodedEntityStructure>();
			
			PreprocessorExecutor preExec = new PreprocessorExecutor();
			Set<String> entTypes = new HashSet<String>();
			entTypes.add("item");
			Set<GenericProperty> propsSrc = null;
			Map <Integer,EntityStructureVersion> metaMap = new HashMap<Integer,EntityStructureVersion>();
			
			
			int size =0;
			for (EntityStructureVersion esv: forms){
				if (selForms.contains(esv.getStructureId())){
					esv = preExec.preprocess(esv, config);
					EncodedEntityStructure encForm =  EncodingManager.getInstance().encoding(esv,entTypes, true);
					encodedStructures.add(encForm);
					propsSrc = esv.getAvailableProperties("question","EN",null);
					//propsSrc.addAll(esv.getAvailableProperties("name", null, null));
					size +=encForm.getObjIds().size();
					TFIDFTokenWeightGenerator.getInstance().initializeGlobalCount(encForm.getStructureId(), encForm.getPropertyValueIds(),
							encForm.getPropertyPosition(), propsSrc.toArray(new GenericProperty[]{}));
					metaMap.put(esv.getStructureId(), esv);
				}
			}
			
			Set <Integer> generalConceptIds = new HashSet<Integer>();
			if (filterGeneralConcepts){
				for (GenericEntity ge: umls.getEntities()){
					List <String> semType = ge.getPropertyValues("sem_type", null, null);
					for (String st :semType){
						if (semTypes.contains(st)){
							generalConceptIds.add(ge.getId());
							break;
						}
					}
				}
				for(int id : generalConceptIds){
					umls.removeEntity(id);
				}
			}
			
			PreprocessProperty[] propertiesUmls = new PreprocessProperty[]{new PreprocessProperty("name", "EN", null),
					new PreprocessProperty("synonym","EN",null)};
			PreprocessorConfig configUmls = new PreprocessorConfig();
			
			configUmls.addPreprocessingStepForProperties(PreprocessingSteps.TO_LOW, propertiesUmls);
			configUmls.addPreprocessingStepForProperties(PreprocessingSteps.STOPWORD_EXTRACTION, propertiesUmls);
			umls = preExec.preprocess(umls, configUmls);
			EncodedEntityStructure eesTarget = EncodingManager.getInstance().encoding(umls, true);
			size+=eesTarget.getObjIds().size();
			Set<GenericProperty> propsTarget = umls.getAvailableProperties("name", "EN", null);
			propsTarget.addAll(umls.getAvailableProperties("synonym", "EN",null));
			
			TokenSimilarityLookup.getInstance().computeTrigramLookup(forms, umls, rep);
			TFIDFTokenWeightGenerator.getInstance().initializeGlobalCount(eesTarget.getStructureId(), eesTarget.getPropertyValueIds(),
					eesTarget.getPropertyPosition(), propsTarget.toArray(new GenericProperty[]{}));
			Int2FloatMap idfMap = TFIDFTokenWeightGenerator.getInstance().generateIDFValuesForAllSources(size);
			MatchManager mm  = rep.getMatchManager();
			Map <String,Object> globalObjects = new HashMap<String,Object>();
			globalObjects.put(SoftTFIDFMatcher.LOOKUP, TokenSimilarityLookup.getInstance().getLookup());
			long startTime = System.currentTimeMillis();
			
			
			AnnotationMapping overallCalculatedMapping= new AnnotationMapping();
			AnnotationMapping overallReferenceMapping = new AnnotationMapping();
			for (EncodedEntityStructure ees:encodedStructures){
				if (selForms.contains(ees.getStructureId())){
					
				    MatchOperator mop3 = new MatchOperator (RegisteredMatcher.SOFT_TFIDF_WND_MATCHER, AggregationFunction.MAX, propsSrc, propsTarget, 0.7f);
					MatchGroup group = new MatchGroup();
//					group.addMatcher(mop);
//					group.addMatcher(mop2);
					group.addMatcher(mop3);
					SetOperator sop = new SetOperator(AggregationFunction.MAX, SetOperator.UNION);
					group.setOperator(sop);
					globalObjects.put(TFIDFMatcher.IDF_MAP_SOURCE, idfMap);
					globalObjects.put(TFIDFMatcher.IDF_MAP_TARGET, idfMap);
					globalObjects.put(TFIDFMatcher.TFIDF_SOURCE_SEPARATED, false);
					globalObjects.put(TFIDFWindowMatcher.WND_SIZE, 5);
					mop3.setGlobalObjects(globalObjects);
					ExecutionTree tree = new ExecutionTree();
					tree.addOperator(group);
					//umls.clear();
					forms.clear();
					AnnotationMapping am = mm.match(ees, eesTarget, tree, null);
					Selection selection = new GroupSelection ();
					System.out.println("before selection: "+am.getNumberOfAnnotations());
					am = selection.select(am, ees, eesTarget, propsSrc, propsTarget, 0.35f, 0,1, rep);
					
					VersionMetadata vm = metaMap.get(ees.getStructureId()).getMetadata();
					String mappingName= vm.getName()+"["+vm.getTopic()+"]-"
							+umls.getMetadata().getName()+"["+umls.getMetadata().getTopic()+"]_odm";
								AnnotationMapping am1 = rep.getMappingManager().getAnnotationMapping(vm,
										umls.getMetadata(), mappingName);
					System.out.println("after selection:"+ am.getNumberOfAnnotations());		
								
				
					for (int id : ees.getObjIds().keySet()){
						Set<Integer> targetIds = am.getCorrespondingTargetIds(id);
						if (am1.getCorrespondingTargetIds(id).isEmpty()&&!targetIds.isEmpty()){ 
							for (int tid : targetIds){
								am.removeAnnotation(id, tid);
							}
						}
					}
					overallCalculatedMapping = SetAnnotationOperator.union(AggregationFunction.MAX, overallCalculatedMapping, am);
					Set<Long> removeAnnotations = new HashSet<Long>();
					if (filterGeneralConcepts){
						for (EntityAnnotation ea: am1.getAnnotations()){
							if (generalConceptIds.contains(ea.getTargetId())){
								removeAnnotations.add(ea.getId());
							}
						}
						for(long id : removeAnnotations){
							int srcId= (int) CantorDecoder.decode_a(id);
							int targetId=(int) CantorDecoder.decode_b(id);	
							am1.removeAnnotation(srcId, targetId);
						}
					}
					
					overallReferenceMapping = SetAnnotationOperator.union(AggregationFunction.MAX, overallReferenceMapping, am1);
//	
				}
			}
			MappingEvaluation eval = new MappingEvaluation();
			EvaluationResult er = eval.getResult(overallCalculatedMapping, overallReferenceMapping, "eligibility forms", umls.getMetadata().getName());
			
			
			System.out.println(er.getMeasures().get("precision"));
			System.out.println(er.getMeasures().get("recall"));			
			System.out.println(er.getMeasures().get("fmeasure"));	
			long elapsedTime = System.currentTimeMillis()-startTime;	
			System.out.println("match time:"+elapsedTime);
			
			
			
						
		} catch (VersionNotExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StructureBuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MatchingExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
