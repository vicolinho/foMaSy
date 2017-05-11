package de.uni_leipzig.dbs.formRepository.matching.pruning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorConfig;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessorExecutor;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.exception.PreprocessingException;

public class LDAInitialization {

  
  InstanceList instanceList;
  
  
  public LDAInitialization (){
    
  }
  
  
  public InstanceList buildInstanceListFromStructure(EntityStructureVersion ontology, PreprocessorConfig config,
      Set<GenericProperty> consideredProperties, GenericProperty topicAttribute){
    
    List<Pipe> pipeList = new ArrayList<Pipe>();
    pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
    pipeList.add( new TokenSequence2FeatureSequence() );
    
    
    PreprocessorExecutor pe = new PreprocessorExecutor();
    try {
      pe.preprocess(ontology, config);
    } catch (PreprocessingException e) {
      e.printStackTrace();
    }
    this.instanceList = new InstanceList(new SerialPipes(pipeList));
    Map<String, StringBuffer> semTypeDocuments = new HashMap<String,StringBuffer>(); 
    for (GenericEntity ge : ontology.getEntities()){
      StringBuffer sbSub = new StringBuffer();
      for (GenericProperty gp : consideredProperties){
        for (String s : ge.getPropertyValues(gp)){
          sbSub.append(s);
        }
      }
      
      List<String> topics = ge.getPropertyValues(topicAttribute);
      for(String topic:topics){
        StringBuffer sb= semTypeDocuments.get(topic);
        if (sb == null){
          sb = new StringBuffer();
          semTypeDocuments.put(topic, sb);
        }
        sb.append("\\s"+sbSub.toString());
      }
    }
    
    for (Entry <String,StringBuffer> e : semTypeDocuments.entrySet()){
      Instance i = new Instance(e.getValue().toString(), e.getKey(), e.getKey(), ontology.getMetadata().getName());
      instanceList.addThruPipe(i);
    }
    return instanceList;
  }
}
