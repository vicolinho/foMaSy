package de.uni_leipzig.dbs.formRepository.modelGeneration;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.FormRepositoryImpl;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;
import de.uni_leipzig.dbs.formRepository.exception.VersionNotExistsException;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.deepLearning.generation.Word2VecGenerator;

import java.io.File;
import java.util.*;

/**
 * Created by christen on 21.11.2016.
 */
public class Word2VecModel {

  public static void main(String[] args){
    FormRepository rep = new FormRepositoryImpl();
    try {
      if (args.length >0) {
        rep.initialize(args[0]);
      }
      Set<String> formTypes = new HashSet<>();
      if (args.length>1){

        for (String t: Arrays.asList(args[1].split(","))){
          formTypes.add(t.replaceAll("_"," "));
        }
      }
      String file = "word2VecForm";
      if (args.length>2){
        file = args[2];
      }

      Set<EntityStructureVersion> esvs = rep.getFormManager().getStructureVersionsByType(formTypes);
      Collection<GenericProperty> properties = esvs.iterator().next().getAvailableProperties();
      Iterator<GenericProperty> iter = properties.iterator();
      while(iter.hasNext()){
        GenericProperty gp = iter.next();
        if (!(gp.getName().equals("question")&&gp.getLanguage().equals("EN"))){
          iter.remove();
        }
      }
      Word2VecGenerator generator = new Word2VecGenerator();
      generator = generator.minWordFrequency(5)
              .windowSize(5)
              .iterations(10)
              .layerSize(100)
              .seed(42);
      Set <String> types = new HashSet<>();
      types.add("item");
      generator.generateModel(esvs, types, properties, new File(file));


    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (InitializationException e) {
      e.printStackTrace();
    } catch (StructureBuildException e) {
      e.printStackTrace();
    } catch (VersionNotExistsException e) {
      e.printStackTrace();
    }
  }
}
