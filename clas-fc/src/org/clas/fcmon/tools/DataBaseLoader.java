package org.clas.fcmon.tools;

 
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;

/**
 *
 * @author gavalian
 */
public class DataBaseLoader {
     
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
        return null;
    }
    
    
    public static ConstantProvider  getGeometryConstants(DetectorType type, int run, String variation){

        //Implement BAND if necessary like 
//        if(type==DetectorType.BAND){
//            DatabaseConstantProvider provider = new DatabaseConstantProvider(run,variation);
//            provider.loadTable("/geometry/band/band");
//            provider.disconnect();
//            return provider;
//        }   
        return null;
    }
   
    
}