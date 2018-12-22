package org.clas.fcmon.tools;

import java.util.Arrays;

import java.util.List;

import org.jlab.detector.base.DetectorType;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.detector.decode.DetectorDataDgtz.ADCData;
import org.jlab.utils.groups.IndexedTable;

//lcs
import org.jlab.detector.decode.DetectorDataDgtz;
import org.jlab.detector.decode.BasicFADCFitter;
import org.jlab.detector.decode.ExtendedFADCFitter;
import org.jlab.detector.decode.MVTFitter;

/**
 *
 * @author gavalian
 */
public class DetectorEventDecoder {
    
    ConstantsManager  translationManager = new ConstantsManager();
    ConstantsManager  fitterManager      = new ConstantsManager();
    
    List<String>  tablesTrans            = null;
    List<String>  keysTrans              = null;
    
    List<String>  tablesFitter            = null;
    List<String>  keysFitter              = null;
    
    private  int  runNumber               = 10;
    
    private  BasicFADCFitter      basicFitter     = new BasicFADCFitter();
    private  ExtendedFADCFitter   extendedFitter  = new ExtendedFADCFitter();
    private  MVTFitter            mvtFitter       = new MVTFitter();
    
    private  Boolean          useExtendedFitter   = false;
    
    //lcs
    private  int                            tet  = 0;
    private  int                            nsa  = 0;
    private  int                            nsb  = 0;    
    
    public DetectorEventDecoder(boolean development){
            this.initDecoder();
    }
    
    public void setRunNumber(int run){
        this.runNumber = run;
    }
    
    //lcs
    public void setTET(int tet) {
    	  this.tet = tet;
    }
      
    public void setNSA(int nsa) {
    	  this.nsa = nsa;
    }
      
    public void setNSB(int nsb) {
    	  this.nsb = nsb;
    }
    
    public void setFitterParms(int tet, int nsa, int nsb) {
    	    this.tet = tet;
    	    this.nsa = nsa;
    	    this.nsb = nsb;
    }
      
    public DetectorEventDecoder(){
        this.initDecoder();
        
    }
    
    
    public final void initDecoder(){
    	
    	keysTrans = Arrays.asList(new String[]{
                "RF","BAND"
        });

        tablesTrans = Arrays.asList(new String[]{
           "/daq/tt/rf","/daq/tt/band"
        });

        translationManager.init(keysTrans,tablesTrans);

        keysFitter   = Arrays.asList(new String[]{"RF","BAND"});
        tablesFitter = Arrays.asList(new String[]{
            "/daq/fadc/rf","/daq/fadc/band"
        });
        fitterManager.init(keysFitter, tablesFitter);
    	  
//        keysTrans = Arrays.asList(new String[]{
//		"FTOF","RF","BAND"});
//        
//        tablesTrans = Arrays.asList(new String[]{
//            "daq/tt/ftof","/daq/tt/rf","/daq/tt/band" });
//        
//        translationManager.init(keysTrans,tablesTrans);
//        
//        tablesFitter = Arrays.asList(new String[]{
//            "daq/fadc/ftof","/daq/fadc/rf","/daq/fadc/band" });
//        fitterManager.init(keysFitter, tablesFitter);
    }
    /**
     * Set the flag to use extended fitter instead of basic fitter
     * which simply integrates over given bins inside of the given
     * windows for the pulse. The pulse parameters are provided by 
     * fitterManager (loaded from database).
     * @param flag 
     */
    public void setUseExtendedFitter(boolean flag){
        this.useExtendedFitter = flag;
    }
    /**
     * applies translation table to the digitized data to translate
     * crate,slot channel to sector layer component.
     * @param detectorData 
     */
    
    
    public void translate(List<DetectorDataDgtz>  detectorData){
        
        for(DetectorDataDgtz data : detectorData){
            
            int crate    = data.getDescriptor().getCrate();
            int slot     = data.getDescriptor().getSlot();
            int channel  = data.getDescriptor().getChannel();

            boolean hasBeenAssigned = false;
            
            for(String table : keysTrans){
                IndexedTable  tt = translationManager.getConstants(runNumber, table);
                DetectorType  type = DetectorType.getType(table);
                
                //FIX ME: temporarily add BAND channel 410_R without modification of ccdb F.H. 12/11/18
               if((crate==66 || crate==67) && slot==10 && channel==15) {
                	data.getDescriptor().setSectorLayerComponent(2, 4, 7);
                	data.getDescriptor().setOrder(1);
                	data.getDescriptor().setType(type);
                	for(int i = 0; i < data.getADCSize(); i++) {
                        data.getADCData(i).setOrder(1);
                    }   
                }

                if(tt.hasEntry(crate,slot,channel)==true){
                    int sector    = tt.getIntValue("sector", crate,slot,channel);
                    int layer     = tt.getIntValue("layer", crate,slot,channel);
                    int component = tt.getIntValue("component", crate,slot,channel);
                    int order     = tt.getIntValue("order", crate,slot,channel);
                    
                    data.getDescriptor().setSectorLayerComponent(sector, layer, component);
                    data.getDescriptor().setOrder(order);
                    data.getDescriptor().setType(type);
                    for(int i = 0; i < data.getADCSize(); i++) {
                        data.getADCData(i).setOrder(order);
                    }
                    for(int i = 0; i < data.getTDCSize(); i++) {
                        data.getTDCData(i).setOrder(order);
                    }
                }
            }
        }
    }
    

        public void fitPulses(List<DetectorDataDgtz>  detectorData){
        	
        for(DetectorDataDgtz data : detectorData){            
            int crate    = data.getDescriptor().getCrate();
            int slot     = data.getDescriptor().getSlot();
            int channel  = data.getDescriptor().getChannel();
            for(String table : keysFitter){
                //custom MM fitter
                    IndexedTable  daq = fitterManager.getConstants(runNumber, table);
                    DetectorType  type = DetectorType.getType(table);
                    if(daq.hasEntry(crate,slot,channel)==true){                    
            	           int tet = (this.tet>0) ? this.tet:daq.getIntValue("tet", crate,slot,channel); //lcs
            	           int nsa = (this.nsa>0) ? this.nsa:daq.getIntValue("nsa", crate,slot,channel); //lcs
            	           int nsb = (this.nsb>0) ? this.nsb:daq.getIntValue("nsb", crate,slot,channel); //lcs
            	           int ped = ped = daq.getIntValue("pedestal", crate,slot,channel);
                       if(table.equals("RF")&&data.getDescriptor().getType().getName().equals("RF")) ped = daq.getIntValue("pedestal", crate,slot,channel);
        	           //System.out.println(">>>>> tet : " + tet + " nsa : " + nsa + 
        	        	//	   				" nsb : " + nsb + " ped: " + ped + " crate: " + crate + 
        	        	//	   				" slot: " + slot + " channel: " + channel );
        	           //ped = 0;
                       if(data.getADCSize()>0){
                            for(int i = 0; i < data.getADCSize(); i++){
                                ADCData adc = data.getADCData(i);
                                if(adc.getPulseSize()>0){
                                    try {
                                        extendedFitter.fit(nsa, nsb, tet, ped, adc.getPulseArray());
                                        
                                    } catch (Exception e) {
                                        System.out.println(">>>> error : fitting pulse "
                                                            +  crate + " / " + slot + " / " + channel);
                                    }
                                    int adc_corrected = extendedFitter.adc + extendedFitter.ped*(nsa+nsb);
                                    adc.setHeight((short) this.extendedFitter.pulsePeakValue);
                                    adc.setIntegral(adc_corrected);
                                    adc.setTimeWord(this.extendedFitter.t0);
                                    adc.setPedestal((short) this.extendedFitter.ped);    
                                    //System.out.println(">>>>>>>>> ped: " + this.extendedFitter.ped + 
                                    //		" adc: " + this.extendedFitter.adc);
                               }
                            }
                        }
                        if(data.getADCSize()>0){
                            for(int i = 0; i < data.getADCSize(); i++){
                                data.getADCData(i).setADC(nsa, nsb);
                            }
                        }
                }
            }
        }
    }
}
