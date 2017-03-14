package de.uni_leipzig.dbs.formRepository.evals;

import de.uni_leipzig.dbs.formRepository.FormRepository;
import de.uni_leipzig.dbs.formRepository.FormRepositoryImpl;
import de.uni_leipzig.dbs.formRepository.api.util.InitializationException;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.exception.StructureBuildException;
import de.uni_leipzig.dbs.formRepository.exception.VersionNotExistsException;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.extraction.POSBasedExtractingPreprocessor;
import org.apache.log4j.PropertyConfigurator;

import java.util.*;

/**
 * Created by christen on 20.02.2017.
 */
public class PropertyCheck {

  public static void main (String[] args) {
    FormRepository rep = new FormRepositoryImpl();
    PropertyConfigurator.configure("log4j.properties");
    String date = "2014-01-01";
    String name = "umls2014AB";
    String type = "ontology";

    String formName = "NCT00556270";
    String dateForm = "2012-05-25";
    String formType = "eligibility form";

    Set<String> semTypes = new HashSet<String>();

    //	int[] selectedForms = new int[]{76};

    //	int[] selectedForms = new int[]{461,455,456,457,458,459,464,466,
    //		467,468,465,463,462,452,453,454,469,470,460,473,475,476,439,440};

    Map<String, Object> extMap = new HashMap<>();


    try {
      rep.initialize("fms.ini");
      GenericProperty ugp = new GenericProperty(1,"name", "PN", "EN");
      GenericProperty ugp2 = new GenericProperty(2,"synonym", "SY", "EN");
      GenericProperty ugp3 = new GenericProperty(3,"synonym", "PT", "EN");
      GenericProperty ugp4 = new GenericProperty(4,"synonym", "MH", "EN");
      GenericProperty ugp5 = new GenericProperty(6,"synonym", "SCN", "EN");
      Set<GenericProperty> usedProperties = new HashSet<>();
      usedProperties.addAll(Arrays.asList(ugp,ugp2,ugp3,ugp4,ugp5));
      EntityStructureVersion umls = rep.getFormManager().getStructureVersion(name, type, date,usedProperties,null);
      System.out.println(umls.getNumberOfEntities());
      Set<GenericProperty> propsTarget = umls.getAvailableProperties("name", "EN", null);
      propsTarget.addAll(umls.getAvailableProperties("synonym", "EN",null));
      Set <String> pvs = new HashSet<>();
      int count =0;
      System.out.println("read finished");

      for (GenericEntity ge: umls.getEntities()){
        for (GenericProperty gp : propsTarget) {
          List<String> pv = ge.getPropertyValues(gp);
          count+= pv.size();
          pvs.addAll(pv);
        }
      }
      System.out.println(pvs.size()+"---"+count);
      System.out.println("deduplication start");
      umls.deduplicateProperties(propsTarget);
      System.out.println(pvs.size()+"---"+count);
      count =0;
      pvs.clear();
      for (GenericEntity ge: umls.getEntities()){
        for (GenericProperty gp : propsTarget) {
          List<String> pv = ge.getPropertyValues(gp);
          count+= pv.size();
          pvs.addAll(pv);
        }
      }
      System.out.println(pvs.size()+"---"+count);
    } catch (InitializationException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (StructureBuildException e) {
      e.printStackTrace();
    } catch (VersionNotExistsException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

  }
}
