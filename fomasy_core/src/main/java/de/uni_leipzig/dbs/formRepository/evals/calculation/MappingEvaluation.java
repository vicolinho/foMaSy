package de.uni_leipzig.dbs.formRepository.evals.calculation;

import java.util.Set;

import de.uni_leipzig.dbs.formRepository.dataModel.AnnotationMapping;
import de.uni_leipzig.dbs.formRepository.dataModel.VersionMetadata;
import de.uni_leipzig.dbs.formRepository.operation.SetAnnotationOperator;
import de.uni_leipzig.dbs.formRepository.matching.aggregation.AggregationFunction;

public class MappingEvaluation {

  
  
  public EvaluationResult getResult (AnnotationMapping result, AnnotationMapping referenceMapping,
      VersionMetadata src, VersionMetadata target){
    
    EvaluationResult er = new EvaluationResult(src.getName(), target.getName());
    AnnotationMapping correct = SetAnnotationOperator.intersect(AggregationFunction.MIN, result, referenceMapping);
    AnnotationMapping falsePositives = SetAnnotationOperator.diff( result, referenceMapping);
    AnnotationMapping falseNegatives = SetAnnotationOperator.diff( referenceMapping, result);
    er.setFalseNegative(falseNegatives, correct.getNumberOfAnnotations());
    er.setFalsePositives(falsePositives, correct.getNumberOfAnnotations());
    return er;
    
  }
  
  public EvaluationResult getResult (AnnotationMapping result, AnnotationMapping referenceMapping,
      String src, String target){
    
    EvaluationResult er = new EvaluationResult(src, target);
    AnnotationMapping correct = SetAnnotationOperator.intersect(AggregationFunction.MIN, result, referenceMapping);
    AnnotationMapping falsePositives = SetAnnotationOperator.diff( result, referenceMapping);
    AnnotationMapping falseNegatives = SetAnnotationOperator.diff( referenceMapping, result);
    er.setFalseNegative(falseNegatives, correct.getNumberOfAnnotations());
    er.setFalsePositives(falsePositives, correct.getNumberOfAnnotations());
    er.setTruePositive(correct);
    er.calculateFmeasure();
    return er;
    
  }
  
  public EvaluationResult getResult (AnnotationMapping result, AnnotationMapping referenceMapping,
      String src, String target,Set<Integer>questionIds){
    
    EvaluationResult er = new EvaluationResult(src, target);
    AnnotationMapping correct = SetAnnotationOperator.intersect(AggregationFunction.MIN, result, referenceMapping);
    AnnotationMapping falsePositives = SetAnnotationOperator.diff( result, referenceMapping);
    AnnotationMapping falseNegatives = SetAnnotationOperator.diff( referenceMapping, result);
    er.setMinimalCoveragePerQuestion(correct, questionIds);
    er.setFalseNegative(falseNegatives, correct.getNumberOfAnnotations());
    er.setFalsePositives(falsePositives, correct.getNumberOfAnnotations());
    er.setTruePositive(correct);
    er.calculateFmeasure();
    return er;
    
  }
  
}
