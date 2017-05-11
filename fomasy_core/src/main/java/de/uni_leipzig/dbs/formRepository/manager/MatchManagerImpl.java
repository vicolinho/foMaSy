package de.uni_leipzig.dbs.formRepository.manager;

import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.LongIterator;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_leipzig.dbs.formRepository.api.APIFactory;
import de.uni_leipzig.dbs.formRepository.api.annotation.AnnotationAPI;
import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EncodedAnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.dataModel.encoding.EncodedEntityStructure;
import de.uni_leipzig.dbs.formRepository.exception.EntityAPIException;
import de.uni_leipzig.dbs.formRepository.exception.MatchingExecutionException;
import de.uni_leipzig.dbs.formRepository.matching.execution.MatcherWorkflowExecuter;
import de.uni_leipzig.dbs.formRepository.matching.execution.data.ExecutionTree;
import de.uni_leipzig.dbs.formRepository.matching.metaMap.MetaMapWrapper;
import de.uni_leipzig.dbs.formRepository.matching.pruning.Pruning;
import de.uni_leipzig.dbs.formRepository.util.CantorDecoder;

public class MatchManagerImpl implements MatchManager {

  
  Logger log = Logger.getLogger(getClass());
  public AnnotationMapping match(EntityStructureVersion srcVersion, EncodedEntityStructure encodedSrc,
      EncodedEntityStructure encodedtarget, EntityStructureVersion targetVersion, ExecutionTree tree,
       Pruning pruning) throws MatchingExecutionException{
    MatcherWorkflowExecuter exec = new   MatcherWorkflowExecuter();
    Long2FloatMap encodedMapping = exec.match(encodedSrc, encodedtarget, tree, pruning);
    AnnotationMapping am = new AnnotationMapping();
    am.setEvidenceMap(exec.getEvidenceMap());
    Set<VersionMetadata> involvedStructures = new HashSet<VersionMetadata>();
    involvedStructures.add(srcVersion.getMetadata());
    involvedStructures.add(targetVersion.getMetadata());
    for (Entry<Long,Float> cor : encodedMapping.entrySet()){
      int srcId = (int) CantorDecoder.decode_a(cor.getKey());
      int targetId= (int) CantorDecoder.decode_b(cor.getKey());
      EntityAnnotation ea = new EntityAnnotation (srcId, targetId,
          srcVersion.getEntity(srcId).getAccession(), targetVersion.getEntity(targetId).getAccession(),
          cor.getValue(), false);
      am.addAnnotation(ea);
    }
    return am;
    
  }

  public AnnotationMapping match(EncodedEntityStructure encodedSrc,
      EncodedEntityStructure encodedtarget, ExecutionTree tree,
      Pruning pruning) throws MatchingExecutionException {
    MatcherWorkflowExecuter exec = new   MatcherWorkflowExecuter();
    Long2FloatMap encodedMapping = exec.match(encodedSrc, encodedtarget, tree, pruning);
    AnnotationAPI api = APIFactory.getInstance().getAnnotationAPI();
    AnnotationMapping am = api.getAnnotationMapping(encodedMapping, encodedSrc.getStructureId(), encodedtarget.getStructureId());
    am.setEvidenceMap(exec.getEvidenceMap());
    return am;
  }

  public EncodedAnnotationMapping matchEncoded(EncodedEntityStructure encodedSrc,
      EncodedEntityStructure encodedtarget, ExecutionTree tree,
      Pruning pruning) throws MatchingExecutionException {
    MatcherWorkflowExecuter exec = new   MatcherWorkflowExecuter();
    Long2FloatMap encodedMapping = exec.match(encodedSrc, encodedtarget, tree, pruning);
    EncodedAnnotationMapping am = new EncodedAnnotationMapping();
    for (Entry<Long,Float> cor : encodedMapping.entrySet()){
      EntityAnnotation ea= new EntityAnnotation(cor.getKey(), null, null, cor.getValue(), false);
      am.addAnnotation(ea);
    }
    am.setEvidenceMap(exec.getEvidenceMap());
    return am;
  }

  public AnnotationMapping match(Set<EntityStructureVersion> srcVersions,
      EntityStructureVersion ontology, ExecutionTree tree, Pruning pruning) {
    // TODO Auto-generated method stub
    return null;
  }

  public AnnotationMapping match(EntityStructureVersion srcVersion,
      EncodedEntityStructure encodedSrc,
      EncodedEntityStructure encodedtarget,
      EntityStructureVersion targetVersion)
      throws MatchingExecutionException {
    // TODO Auto-generated method stub
    return null;
  }

  public AnnotationMapping matchByMetaMap(EntityStructureVersion src,
      Set<String> entityTypes, Set<GenericProperty> props,
      Properties metaMapProperties) throws EntityAPIException {
    MetaMapWrapper wrapper = new MetaMapWrapper ();
    
    return wrapper.match(src, entityTypes, props, metaMapProperties);
  }
}
