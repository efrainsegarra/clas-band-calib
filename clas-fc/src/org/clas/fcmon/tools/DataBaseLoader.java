package org.clas.fcmon.tools;

 
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;

/**
 *
 * @author gavalian
 */
public class DataBaseLoader {
    
   
    public static ConstantProvider getCalorimeterConstants(){
        DatabaseConstantProvider provider = new DatabaseConstantProvider("mysql://clas12reader@clasdb.jlab.org/clas12");
        provider.loadTable("/geometry/pcal/pcal");
        provider.loadTable("/geometry/pcal/Uview");
        provider.loadTable("/geometry/pcal/Vview");
        provider.loadTable("/geometry/pcal/Wview");
        provider.loadTable("/geometry/ec/ec");
        provider.loadTable("/geometry/ec/uview");
        provider.loadTable("/geometry/ec/vview");
        provider.loadTable("/geometry/ec/wview");
        return provider;
    }
    
    
    public static ConstantProvider  getGeometryConstants(DetectorType type){
        return DataBaseLoader.getGeometryConstants(type, 10, "default");
    }
    
    public static ConstantProvider  getGeometryConstants(DetectorType type, int run){
        return DataBaseLoader.getGeometryConstants(type, run, "default");
    }
    
    public static ConstantProvider getCalibrationConstants(DetectorType type, int run){
        return DataBaseLoader.getCalibrationConstants(type, run, "default");
    }
    
    public static ConstantProvider getDetectorConstants(String type){
        return DataBaseLoader.getDetectorConstants(DetectorType.getType(type));
    }
    /**
     * This section returns for all detectors the known geometry constants
     * @param type
     * @return 
     */
    public static ConstantProvider getDetectorConstants(DetectorType type){
        int run = 10;
        String variation = "default";
        
        return null;
    }
    
    
    public static ConstantProvider getCalibrationConstants(DetectorType type, int run, String variation){
 
    	//FIX ME: Implement calibration data bases in CCDB
//    	if(type==DetectorType.BAND){
//            DatabaseConstantProvider provider = new DatabaseConstantProvider(run,variation);
//            provider.loadTable("/calibration/band/attenuation");
//            provider.loadTable("/calibration/band/effective_velocity");
//            provider.loadTable("/calibration/band/gain_balance");
//            provider.loadTable("/calibration/band/timing_offset");
//            provider.disconnect();
//            return provider;
//        }
//    	
       	
        return null;
    }
    
    
    public static ConstantProvider  getGeometryConstants(DetectorType type, int run, String variation){

        
        if(type==DetectorType.ECAL){
            DatabaseConstantProvider provider = new DatabaseConstantProvider(run,variation);
            provider.loadTable("/geometry/pcal/pcal");
            provider.loadTable("/geometry/pcal/Uview");
            provider.loadTable("/geometry/pcal/Vview");
            provider.loadTable("/geometry/pcal/Wview");
            provider.loadTable("/geometry/ec/ec");
            provider.loadTable("/geometry/ec/uview");
            provider.loadTable("/geometry/ec/vview");
            provider.loadTable("/geometry/ec/wview");
            provider.disconnect();
            return provider;
        }
        
        
        return null;
    }
    
    
  public static ConstantProvider getConstantsEC(){
        DatabaseConstantProvider provider = new DatabaseConstantProvider("mysql://clas12reader@clasdb.jlab.org/clas12");
        provider.loadTable("/geometry/pcal/pcal");
        provider.loadTable("/geometry/pcal/UView");
        provider.loadTable("/geometry/pcal/VView");
        provider.loadTable("/geometry/pcal/WView");
        provider.loadTable("/geometry/ec/ec");
        provider.loadTable("/geometry/ec/uview");
        provider.loadTable("/geometry/ec/vview");
        provider.loadTable("/geometry/ec/wview");
        return provider;
    }
    
}