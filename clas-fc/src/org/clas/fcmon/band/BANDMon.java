// Issue in extendedFADCFitter -- line 91, and & instead of &&?
 //                   if(bin>=tcross & pulse[bin]>pmax) {
// Issue in extendedFADCFitter -- in definition of halfmax, 
// because when pedestal is defined (i.e. not 0) then pmax does not have pedestal sub

package org.clas.fcmon.band;

import java.util.Arrays;
import java.util.TreeMap;

import org.clas.containers.FTHashCollection;
import org.clas.fcmon.detector.view.DetectorShape2D;
import org.clas.fcmon.tools.*;

//clas12rec
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.io.base.DataEvent;

public class BANDMon extends DetectorMonitor {
	
    static MonitorApp             app = new MonitorApp("BANDCalibration",1800,950);	
    
    BANDPixels              bandPix[] = new BANDPixels[6];
    ConstantsManager             ccdb = new ConstantsManager();
    FTHashCollection              rtt = null;      
    BANDDet                   bandDet = null;  
    
    BANDReconstructionApp   	bandRecon = null;
    BANDCalib_HV                bandCalib_hv = null; 
    BANDCalib_C					bandCalib_c = null;
    BANDCalib_Atten				bandCalib_atten = null;
    
    int 		analyzedBefore = 0;

    public int                 calRun = 12;
    int                         detID = 0;
    int                           is1 = 1;    //All sectors: is1=1 is2=7  Single sector: is1=s is2=s+1
    int                           is2 = 6; 
    int      nsa,nsb,tet,p1,p2,pedref = 0;
    double                 PCMon_zmin = 0;
    double                 PCMon_zmax = 0;
    boolean                firstevent = true;
    
    String mondet                     = "BAND";
    static String             appname = "BANDMON";
	
    TreeMap<String,Object> glob = new TreeMap<String,Object>();
	   
    public BANDMon(String det) {
        super(appname, "1.0", "lcsmith");
        mondet = det;
        bandPix[0] = new BANDPixels("LAYER1");
        bandPix[1] = new BANDPixels("LAYER2");
        bandPix[2] = new BANDPixels("LAYER3");
        bandPix[3] = new BANDPixels("LAYER4");
        bandPix[4] = new BANDPixels("LAYER5");
        bandPix[5] = new BANDPixels("VETO");
    }

    public static void main(String[] args){		
        String det = "BAND";
        BANDMon monitor = new BANDMon(det);	
        if (args.length != 0) {
            monitor.is1=Integer.parseInt(args[0]); 
            monitor.is2=Integer.parseInt(args[1]);    
         }
        app.setPluginClass(monitor);
        app.setAppName(appname);
        app.makeGUI();
        app.getEnv();
        monitor.initConstants();
        monitor.initCCDB(10);
        monitor.initGlob();
        monitor.makeApps();
        monitor.addCanvas();
        monitor.init();
        monitor.initDetector();
        app.init();
        app.getDetectorView().setFPS(10);
        app.setSelectedTab(2); 
        app.setTDCOffset(1200);
        monitor.bandDet.initButtons();
    }
    
    public void initConstants() {
        BANDConstants.setSectorRange(is1,is2);
    }   
    
    public void initCCDB(int runno) {
        System.out.println(appname+".initCCDB()"); 
        ccdb.init(Arrays.asList(new String[]{
                "/daq/fadc/band",
                "/daq/tt/band"
                 }));
        app.getReverseTT(ccdb,runno,"/daq/tt/band"); 
        app.mode7Emulation.init(ccdb,runno,"/daq/fadc/band", 66,3,1);        
    } 
    
    public void initDetector() {
        System.out.println(appname=".initDetector()"); 
        bandDet = new BANDDet("BANDDet",bandPix);
        bandDet.setMonitoringClass(this);
        bandDet.setApplicationClass(app);
        bandDet.init();
    }
	
    public void makeApps() {
        System.out.println(appname+".makeApps()"); 
        bandRecon = new BANDReconstructionApp("BANDREC",bandPix);        
        bandRecon.setMonitoringClass(this);
        bandRecon.setApplicationClass(app);	    
        
        bandCalib_hv = new BANDCalib_HV("HV",bandPix);        
        bandCalib_hv.setMonitoringClass(this);
        bandCalib_hv.setApplicationClass(app); 
        bandCalib_hv.init(is1,is2);
        
        bandCalib_c = new BANDCalib_C("Speed of Light",bandPix);        
        bandCalib_c.setMonitoringClass(this);
        bandCalib_c.setApplicationClass(app); 
        bandCalib_c.init(is1,is2);
        
        bandCalib_atten = new BANDCalib_Atten("Attenuation",bandPix);        
        bandCalib_atten.setMonitoringClass(this);
        bandCalib_atten.setApplicationClass(app); 
        bandCalib_atten.init(is1,is2);

        
        if(app.xMsgHost=="localhost") app.startEpics();
    }
	
    public void addCanvas() {
        System.out.println(appname+".addCanvas()"); 
        //app.addFrame(bandMode1.getName(),          bandMode1.getPanel());
        //app.addCanvas(bandAdc.getName(),             bandAdc.getCanvas());          
        //app.addCanvas(bandTdc.getName(),             bandTdc.getCanvas());          
        //app.addCanvas(bandPedestal.getName(),   bandPedestal.getCanvas());
        
        
        app.addCanvas(bandCalib_hv.getName(),             		bandCalib_hv.getCanvas());
        app.addCanvas(bandCalib_c.getName(),             		bandCalib_c.getCanvas());
        app.addCanvas(bandCalib_atten.getName(),             	bandCalib_atten.getCanvas());
        

        
        
        //app.addFrame(bandScalers.getName(),      bandScalers.getPanel());
    }
    
    public void init( ) {       
        System.out.println(appname+".init()");   
        app.setInProcess(0);
        initApps();
        for (int i=0; i<bandPix.length; i++) bandPix[i].initHistograms(" "); 
    }

    public void initApps() {
        System.out.println(appname+".initApps()");
        firstevent = true;
        for (int i=0; i<bandPix.length; i++)   bandPix[i].init();
        bandRecon.init();
    }
    
    public void initGlob() {
        System.out.println(appname+".initGlob()");
        putGlob("detID", detID);
        putGlob("nsa", nsa);
        putGlob("nsb", nsb);
        putGlob("tet", tet);        
        putGlob("ccdb", ccdb);
        putGlob("zmin", PCMon_zmin);
        putGlob("zmax", PCMon_zmax);
        putGlob("mondet",mondet);
        putGlob("is1",BANDConstants.IS1);
        putGlob("is2",BANDConstants.IS2);
    }
    
    @Override
    public TreeMap<String,Object> getGlob(){
        return this.glob;
    }	
    
    @Override
    public void putGlob(String name, Object obj){
        glob.put(name,obj);
    }  
    
    @Override
    public void reset() {
        bandRecon.clearHistograms();
    }
	
    @Override
    public void dataEventAction(DataEvent de) {
        if (firstevent&&app.getEventNumber()>2) {
    	        System.out.println(appname+".dataEventAction: First Event");
   	        initCCDB(app.run);
   	        firstevent=false;
         }
        bandRecon.addEvent(de);	
    }

    @Override
    public void analyze() {
    	
        switch (app.getInProcess()) {
        case 1: 
            for (int idet=0; idet<bandPix.length; idet++) bandRecon.makeMaps(idet); 
            break;
        case 2:
            for (int idet=0; idet<bandPix.length; idet++) bandRecon.makeMaps(idet); 
            System.out.println("End of run");      
            if( analyzedBefore == 0) {
	            app.addFrame(bandCalib_hv.getName(),             bandCalib_hv.getCalibPane()); 
	            app.addFrame(bandCalib_c.getName(),             bandCalib_c.getCalibPane()); 
	            app.addFrame(bandCalib_atten.getName(),             bandCalib_atten.getCalibPane()); 
            }
            
            bandCalib_hv.analyze();
            bandCalib_c.analyze();
            bandCalib_atten.analyze();
            
            //bandCalib.engines[0].analyze();
            analyzedBefore = 1;
            app.setInProcess(3);
        }
    }
    
    @Override
    public void update(DetectorShape2D shape) {
        //From DetectorView2D.DetectorViewLayer2D.drawLayer: Update color map of shape
        bandDet.update(shape);
//        if (app.getSelectedTabName().equals("Scalers")) bandScalers.updateDetectorView(shape);
//        if (app.getSelectedTabName().equals("HV"))           bandHv.updateDetectorView(shape);
 }
        
    @Override
    public void processShape(DetectorShape2D shape) { 
        // From updateGUI timer or mouseover : process entering a new detector shape and repaint
        DetectorDescriptor dd = shape.getDescriptor();
        app.updateStatusString(dd); // For strip/pixel ID and reverse translation table
        this.analyze();  // Refresh color maps      
        switch (app.getSelectedTabName()) {
        

        case "HV":                            bandCalib_hv.updateCanvas(dd); break; 
        case "Speed of Light":                bandCalib_c.updateCanvas(dd); break; 
        case "Attenuation":                   bandCalib_atten.updateCanvas(dd); break; 
       

       } 
    }
    
    public void loadHV(int is1, int is2, int il1, int il2) {
    //    bandHv.loadHV(is1,is2,il1,il2);
    }  
    
    @Override
    public void resetEventListener() {
    }

    @Override
    public void timerUpdate() {
    }
    
    @Override
    public void readHipoFile() {        
        System.out.println(appname+".readHipoFile()");
        for (int idet=0; idet<3; idet++) {
          String hipoFileName = app.hipoPath+mondet+idet+"_"+app.runNumber+".hipo";
          System.out.println("Reading Histograms from "+hipoFileName);
          bandPix[idet].initHistograms(hipoFileName);
        }
        app.setInProcess(2);          
    }
    
    @Override
    public void writeHipoFile() {
        System.out.println(appname+".writeHipoFile()");
        for (int idet=0; idet<3; idet++) {
          String hipoFileName = app.hipoPath+mondet+idet+"_"+app.runNumber+".hipo";
          System.out.println("Writing Histograms to "+hipoFileName);
          HipoFile histofile = new HipoFile(hipoFileName);
          histofile.addToMap("H2_a_Hist",bandPix[idet].strips.hmap2.get("H2_a_Hist"));
          histofile.addToMap("H2_t_Hist",bandPix[idet].strips.hmap2.get("H2_t_Hist"));
          histofile.writeHipoFile(hipoFileName);
        }
    }
    
    @Override
    public void close() {
        System.out.println(appname+".close()");
        app.displayControl.setFPS(1);
    }
    
    @Override
    public void pause() {
        app.displayControl.setFPS(1);
        
    }

    @Override
    public void go() {
        app.displayControl.setFPS(10);
        
    }

    @Override
    public void initEngine() {
        // TODO Auto-generated method stub
        
    }

    
    @Override
    public void initEpics(Boolean doEpics) {
        // TODO Auto-generated method stub
    }
    
}
