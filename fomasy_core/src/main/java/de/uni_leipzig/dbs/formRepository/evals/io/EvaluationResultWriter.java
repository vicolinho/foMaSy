package de.uni_leipzig.dbs.formRepository.evals.io;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationCluster;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityAnnotation;
import de.uni_leipzig.dbs.formRepository.dataModel.EntitySet;
import de.uni_leipzig.dbs.formRepository.dataModel.EntityStructureVersion;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericEntity;
import de.uni_leipzig.dbs.formRepository.dataModel.GenericProperty;
import de.uni_leipzig.dbs.formRepository.evals.calculation.EvaluationResult;
import de.uni_leipzig.dbs.formRepository.matching.preprocessing.PreprocessProperty;

public class EvaluationResultWriter {

  public EvaluationResultWriter() {
    // TODO Auto-generated constructor stub
  }

  
  public void writeEvaluationDetail(String file,EntitySet<GenericEntity> eset, EntityStructureVersion target, GenericProperty srcProp, GenericProperty targetProp, EvaluationResult er) throws IOException{
    FileWriter fw = new FileWriter(file);
    fw.append("false positives");
    fw.append("source Id  target Id   src property   target property"+System.getProperty("line.separator"));
    for (EntityAnnotation ea: er.getFalsePositives().getAnnotations()){
      fw.append(ea.getSrcAccession()+"  "+ea.getTargetAccession()+"  ");
      List<String> srcProps = eset.getEntity(ea.getSrcId()).getPropertyValues(srcProp);
      List<String> targetProps = target.getEntity(ea.getTargetId()).getPropertyValues(targetProp);
      String pt = "";
      if (!targetProps.isEmpty()){
        for (String t:targetProps){
          pt+=t+",";
        }
      }
      fw.append(srcProps.get(0)+"  "+pt+System.getProperty("line.separator"));
    }
    fw.close();
  }
  
  public void writeEvaluationDetail(String file,EntityStructureVersion eset, EntityStructureVersion target, GenericProperty srcProp, GenericProperty targetProp, EvaluationResult er) throws IOException{
    FileWriter fw = new FileWriter(file+"falsePositive.csv");
    
    fw.append("source Id  target Id   src property   target property  sim"+System.getProperty("line.separator"));
    for (EntityAnnotation ea: er.getFalsePositives().getAnnotations()){
      fw.append(ea.getSrcAccession()+"  "+ea.getTargetAccession()+"  ");
      List<String> srcProps = eset.getEntity(ea.getSrcId()).getPropertyValues(srcProp);
      List<String> targetProps = target.getEntity(ea.getTargetId()).getPropertyValues(targetProp);
      String pt = "";
      if (!targetProps.isEmpty()){
        for (String t:targetProps){
          pt+=t+",";
        }
      }
      fw.append(srcProps.get(0)+"  "+pt+"  "+ea.getSim()+System.getProperty("line.separator"));
    }
    fw.close();
    fw = new FileWriter(file+"falseNegative.csv");
    fw.append("source Id  target Id   src property   target property  sim"+System.getProperty("line.separator"));
    for (EntityAnnotation ea: er.getFalseNegative().getAnnotations()){
      fw.append(ea.getSrcAccession()+"  "+ea.getTargetAccession()+"  ");
      List<String> srcProps = eset.getEntity(ea.getSrcId()).getPropertyValues(srcProp);
      List<String> targetProps = target.getEntity(ea.getTargetId()).getPropertyValues(targetProp);
      String pt = "";
      if (!targetProps.isEmpty()){
        for (String t:targetProps){
          pt+=t+",";
        }
      }
      String ps ="";
      if (!srcProps.isEmpty()){
        ps = srcProps.get(0);
      }
      fw.append(ps+"  "+pt+"  "+ea.getSim()+System.getProperty("line.separator"));
    }
    fw.close();
    
    fw = new FileWriter(file+"truePositive.csv");
    fw.append("source Id  target Id   src property   target property  sim"+System.getProperty("line.separator"));
    for (EntityAnnotation ea: er.getTruePositive().getAnnotations()){
      fw.append(ea.getSrcAccession()+"  "+ea.getTargetAccession()+"  ");
      List<String> srcProps = eset.getEntity(ea.getSrcId()).getPropertyValues(srcProp);
      List<String> targetProps = target.getEntity(ea.getTargetId()).getPropertyValues(targetProp);
      String pt = "";
      if (!targetProps.isEmpty()){
        for (String t:targetProps){
          pt+=t+",";
        }
      }
      String st ="";
      if (!srcProps.isEmpty()){
        st =srcProps.get(0);
      }
      fw.append(st+"  "+pt+"  "+ea.getSim()+System.getProperty("line.separator"));
    }
    fw.close();
  }

  public void writeEvaluationDetail(String file,EntityStructureVersion eset, Map<Integer,AnnotationCluster> target, GenericProperty srcProp, GenericProperty targetProp, EvaluationResult er) throws IOException{
    FileWriter fw = new FileWriter(file+"falsePositive.csv");
    
    fw.append("source Id  target Id   src property   target property  sim"+System.getProperty("line.separator"));
    for (EntityAnnotation ea: er.getFalsePositives().getAnnotations()){
      fw.append(ea.getSrcAccession()+"  "+ea.getTargetAccession()+"  ");
      List<String> srcProps = eset.getEntity(ea.getSrcId()).getPropertyValues(srcProp);
      List<String> targetProps = target.get(ea.getTargetId()).getPropertyValues(targetProp);
      String pt = "";
      String ps ="";
      if (!srcProps.isEmpty()){
        ps = srcProps.get(0);
      }
      if (!targetProps.isEmpty()){
        for (String t:targetProps){
          pt+=t+",";
        }
      }
      fw.append(ps+"  "+pt+"  "+ea.getSim()+System.getProperty("line.separator"));
    }
    fw.close();
    fw = new FileWriter(file+"falseNegative.csv");
    fw.append("source Id  target Id   src property   target property  sim"+System.getProperty("line.separator"));
    for (EntityAnnotation ea: er.getFalseNegative().getAnnotations()){
      fw.append(ea.getSrcAccession()+"  "+ea.getTargetAccession()+"  ");
      List<String> srcProps = eset.getEntity(ea.getSrcId()).getPropertyValues(srcProp);
      String pt = "";
      try{
      List<String> targetProps = target.get(ea.getTargetId()).getPropertyValues(targetProp);
      if (!targetProps.isEmpty()){
        for (String t:targetProps){
          pt+=t+",";
        }
      }
      
      }catch (NullPointerException e){}
      String ps ="";
      if (!srcProps.isEmpty()){
        ps = srcProps.get(0);
      }
      
      fw.append(ps+"  "+pt+"  "+ea.getSim()+System.getProperty("line.separator"));
    }
    fw.close();
    
    fw = new FileWriter(file+"truePositive.csv");
    fw.append("source Id  target Id   src property   target property  sim"+System.getProperty("line.separator"));
    for (EntityAnnotation ea: er.getTruePositive().getAnnotations()){
      fw.append(ea.getSrcAccession()+"  "+ea.getTargetAccession()+"  ");
      List<String> srcProps = eset.getEntity(ea.getSrcId()).getPropertyValues(srcProp);
      List<String> targetProps = target.get(ea.getTargetId()).getPropertyValues(targetProp);
      String pt = "";
      if (!targetProps.isEmpty()){
        for (String t:targetProps){
          pt+=t+",";
        }
      }
      String ps ="";
      if (!srcProps.isEmpty()){
        ps = srcProps.get(0);
      }
      fw.append(ps+"  "+pt+"  "+ea.getSim()+System.getProperty("line.separator"));
    }
    fw.close();
  }

  
}
