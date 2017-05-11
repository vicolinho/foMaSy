package de.uni_leipzig.dbs.formRepository.matching.holistic.clustering;

public enum ThresholdFunctions implements ThresholdFunction{
  
  CONSTANT{
    private float threshold;
    
    @Override
    public float getThreshold() {
      // TODO Auto-generated method stub
      return threshold;
    }

    @Override
    public void setThreshold(float thres) {
      this.threshold= thres;
      
    }
    
  }


}
