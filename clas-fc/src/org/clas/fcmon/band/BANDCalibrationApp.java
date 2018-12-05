package org.clas.fcmon.band;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.clas.fcmon.detector.view.DetectorShape2D;
import org.clas.fcmon.tools.FCApplication;
import org.jlab.detector.base.DetectorCollection;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.CalibrationConstantsListener;
import org.jlab.detector.calib.utils.CalibrationConstantsView;
import org.jlab.groot.data.H1F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.math.F1D;

public class BANDCalibrationApp extends FCApplication implements CalibrationConstantsListener,ChangeListener {
    
    JPanel                    engineView = new JPanel();
    EmbeddedCanvas                canvas = new EmbeddedCanvas();
    CalibrationConstantsView      ccview = new CalibrationConstantsView();
    ArrayList<CalibrationConstants> list = new ArrayList<CalibrationConstants>();
    
    
    public DetectorCollection<F1D> adcFitL = new DetectorCollection<F1D>();
    public DetectorCollection<F1D> adcFitR = new DetectorCollection<F1D>();

    public BANDCalibrationEngine[] engines = {
            new BANDHVEventListener(),
            new BANDStatusEventListener()
    };

    public final int     HV  = 0;
    public final int STATUS  = 1;
    
    String[] names = {"/calibration/band/gain_balance,/calibration/band/status"};
    
    String selectedDir = names[HV];
       
    int selectedSector = 1;
    int selectedLayer = 1;
    int selectedPaddle = 1;
    
    public void init(int is1, int is2) {
    	System.out.println("Running init for BANDCalibrationEngines");
        engines[0].init(is1,is2);
        engines[1].init(is1,is2);   
    }
    
    public BANDCalibrationApp(String name , BANDPixels[] bandPix) {
        super(name, bandPix);       
        System.out.println("BANDCalibrationApp created");
     } 
    
    public JPanel getCalibPane() {        
        engineView.setLayout(new BorderLayout());
        JSplitPane enginePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT); 
        ccview.getTabbedPane().addChangeListener(this);
        for (int i=0; i < engines.length; i++) {
            ccview.addConstants(engines[i].getCalibrationConstants().get(0),this);
        }   

        enginePane.setTopComponent(canvas);
        enginePane.setBottomComponent(ccview);       
        enginePane.setResizeWeight(0.8);
        engineView.add(enginePane);
        return engineView;       
    }  
    
    public BANDCalibrationEngine getSelectedEngine() {
        
        BANDCalibrationEngine engine = engines[HV];

        if (selectedDir == names[HV]) {
            engine = engines[HV];
        } else if (selectedDir == names[STATUS]) {
            engine = engines[STATUS];
        } 
        return engine;
    }

    
    public class BANDHVEventListener extends BANDCalibrationEngine {
    	
        
        public final int[]      EXPECTED_MIP_CHANNEL = {12000, 12000, 12000};
        public final int        ALLOWED_MIP_DIFF = 12000;    
        
        
        
        int is1,is2;
        
        BANDHVEventListener(){};
        
        public void init(int is1, int is2) {
            
        	System.out.println("BANDHVEventListener.init");
        	
            this.is1=is1;
            this.is2=is2;
            
            calib = new CalibrationConstants(3,
                    "Left_Mean/F:Left_Sigma/F:Right_Mean/F:Right_Sigma/F");
            calib.setName("/calibration/band/gain_balance");
            calib.setPrecision(3);

            for (int i=0; i<3; i++) {
                
                int layer = i+1;
                //calib.addConstraint(3, EXPECTED_MIP_CHANNEL[i]-ALLOWED_MIP_DIFF, 
                //                       EXPECTED_MIP_CHANNEL[i]+ALLOWED_MIP_DIFF, 1, layer);
                // calib.addConstraint(calibration column, min value, max value,
                // col to check if constraint should apply, value of col if constraint should be applied);
                // (omit last two if applying to all rows)
                //calib.addConstraint(4, EXPECTED_MIP_CHANNEL[i]-ALLOWED_MIP_DIFF, 
                //                       EXPECTED_MIP_CHANNEL[i]+ALLOWED_MIP_DIFF, 1, layer);
            }
            
            list.add(calib);         
        }
     
        public List<CalibrationConstants>  getCalibrationConstants(){
            return list;
        }  
        
        @Override
        public void analyze() {
        	
        	for( int layer = 0 ; layer<bandPix.length ; layer++) {							// loop over layer
        		
        		for (int sector=is1 ; sector<is2 ; sector++) {								// loop over sector in layer
        			for(int paddle=0; paddle<bandPix[layer].nstr[sector-1] ; paddle++) {	// loop over paddle in sector
        					
        					// Add entry for the unique paddle
        				int lidx = (layer+1);
        		        int pidx = (paddle+1);
        		        calib.addEntry(sector,lidx,pidx);
	        			
        		        	// Fit both sides of paddle
        		        for( int lr = 1 ; lr < 3 ; lr++) {									// loop over left/right PMT in paddle
	        				fit(layer, sector, paddle, lr, 0., 0.);
	        			}
        			}
        		}
        	}
    
        	
        	
        	
            calib.fireTableDataChanged();
        }
        
        
        @Override
        public void fit(int layer, int sector, int paddle, int lr, double minRange, double maxRange){ 

           H1F h = bandPix[layer].strips.hmap2.get("H2_a_Hist").get(sector,lr,0).sliceY(paddle);
           if( h.getIntegral() < 5000) {
        	   F1D f1 = null;
        	   if( lr == 1) adcFitL.add(layer, sector,paddle, f1);
        	   if( lr == 2) adcFitR.add(layer, sector,paddle, f1);
        	   return;
           };
           
           //F1D  f1 = new F1D("f1","[amp]*gaus(x,[mean],[sigma])", 0,40000);
           F1D  f1 = new F1D("f1","[amp]*landau(x,[mean],[sigma])+[const]",0,40000);
           f1.setParameter(0, h.getMax() );
           f1.setParameter(1, h.getMean() );
           f1.setParameter(2, 1000 );
           f1.setParameter(3, 20 );
           DataFitter.fit(f1, h, "REQ");
           h.getFunction().show();

           double amp = h.getFunction().getParameter(0);
           double mean = h.getFunction().getParameter(1);
           double sigma = h.getFunction().getParameter(2);
           double offset = h.getFunction().getParameter(3);
           
           if( amp < 0 || sigma < 0 ) {
        	   if( lr == 1) adcFitL.add(layer, sector,paddle, null);
        	   if( lr == 2) adcFitR.add(layer, sector,paddle, null);
        	   return; 
           }
           
           int lidx = (layer+1);
           int pidx = (paddle+1);
           
           if( lr == 1) {
        	   adcFitL.add(layer, sector,paddle, f1);
        	   calib.setDoubleValue(mean, "Left_Mean", sector, lidx, pidx);
        	   calib.setDoubleValue(sigma, "Left_Sigma", sector, lidx, pidx);
           }
           if( lr == 2) {
        	   adcFitR.add(layer, sector,paddle, f1);
        	   calib.setDoubleValue(mean, "Right_Mean", sector, lidx, pidx);
        	   calib.setDoubleValue(sigma, "Right_Sigma", sector, lidx, pidx);

           }
        }
        
        /*
        public double getMipChannel(int sector, int layer, int paddle) {
            return calib.getDoubleValue("mipa_left", sector, layer, paddle);
        }
        */
        
        @Override
        public boolean isGoodPaddle(int sector, int layer, int paddle) {
        	return true;
            //return (getMipChannel(sector,layer,paddle) >= EXPECTED_MIP_CHANNEL[layer-1]-ALLOWED_MIP_DIFF  &&
            //        getMipChannel(sector,layer,paddle) <= EXPECTED_MIP_CHANNEL[layer-1]+ALLOWED_MIP_DIFF);

        }        

    }
    
    private class BANDStatusEventListener extends BANDCalibrationEngine {
        
        public final int[]    EXPECTED_STATUS = {0,0,0};
        public final int  ALLOWED_STATUS_DIFF = 1;
        
        BANDStatusEventListener(){};
        
        public void init(int is1, int is2){
        	
        	System.out.println("BANDStatusEventListener.init");
        	
            calib = new CalibrationConstants(3,"stat_left/I:stat_right/I");
            calib.setName("/calibration/band/LR_tdiff");
            calib.setPrecision(1);
            
            //for (int i=0 ; i<3; i++) {
            //    calib.addConstraint(3, EXPECTED_STATUS[i]-ALLOWED_STATUS_DIFF,
            //                           EXPECTED_STATUS[i]+ALLOWED_STATUS_DIFF);
            //}
/*            
            for(int is=is1; is<is2; is++) {                
                for(int il=1; il<3; il++) {
                    for(int ip = 1; ip < 19; ip++) {
                        calib.addEntry(is,il,ip);
                        calib.setIntValue(0,"status",is,il,ip);
                    }
                }
            }
            */
            list.add(calib);
        }
    }
           
    public void updateDetectorView(DetectorShape2D shape) {
        BANDCalibrationEngine engine = getSelectedEngine();
        DetectorDescriptor dd = shape.getDescriptor();
        this.getDetIndices(dd);
        layer = lay;
        if (app.omap==3) {
           if(engine.isGoodPaddle(is, layer-1, ic)) {
               shape.setColor(101, 200, 59);
           } else {
               shape.setColor(225, 75, 60);
           }
        }
    }     
    
    public void constantsEvent(CalibrationConstants cc, int col, int row) {

        String str_sector    = (String) cc.getValueAt(row, 0);
        String str_layer     = (String) cc.getValueAt(row, 1);
        String str_component = (String) cc.getValueAt(row, 2);
            
        if (cc.getName() != selectedDir) {
            selectedDir = cc.getName();
        }
            
        selectedSector = Integer.parseInt(str_sector);
        selectedLayer  = Integer.parseInt(str_layer);
        selectedPaddle = Integer.parseInt(str_component);

    }

public void updateCanvas(DetectorDescriptor dd) {
        
        this.getDetIndices(dd); 
        
        int lr = dd.getOrder()+1;
        int sector = dd.getSector();
        int component = dd.getComponent();   
        int layer = ilmap;
        
        canvas.clear(); canvas.divide(2, 2);
        canvas.setAxisFontSize(12);
        
//      canvas.setAxisTitleFontSize(12);
//      canvas.setTitleFontSize(14);
//      canvas.setStatBoxFontSize(10);
        
        H1F h;
        String alab;
        String otab[]={" L PMT "," R PMT "};
        String calTitles[]={" ADC"," TDC"};      
        String tit = "SEC "+sector+" LAY "+(layer+1);
       
        // We will loop here for all the calibration plots we want to make for
        // selected pmt
        	alab = tit+otab[lr-1]+(component+1)+calTitles[0];
            canvas.cd(0);          
            // Pull the ADC histogram for 1st canvas plot
            h = bandPix[layer].strips.hmap2.get("H2_a_Hist").get(is,lr,0).sliceY(component);
            h.setOptStat(Integer.parseInt("1000100")); 
            h.setTitleX(alab); h.setTitle(""); h.setTitleY("Entries"); h.setFillColor(32); canvas.draw(h);
            
            F1D f1 = adcFitL.get(layer,is,component);
            F1D f2 = adcFitR.get(layer,is,component);
        	
            if( f1 != null && f2 != null) {
	            if( lr == 1) {
	            	f1.setLineColor(2);
	            	f2.setLineColor(1);	
	            }
	            if( lr == 2) {
	            	f1.setLineColor(1);
	            	f2.setLineColor(2);
	            }
	            f1.setLineWidth(2);
	            f2.setLineWidth(2);
            	canvas.draw(f1,"same");
            	canvas.draw(f2,"same");
 
            }
            else if( lr == 1 && f1 != null) {
            	f1.setLineColor(2);
            	f1.setLineWidth(2);
            	canvas.draw(f1,"same");
            }
            else if( lr == 2 && f2 != null) {
            	f2.setLineColor(2);
            	f2.setLineWidth(2);
            	canvas.draw(f2,"same");
            }
            
        // For L-R time plot
        canvas.cd(1);
        H1F h2 = bandPix[layer].strips.hmap2.get("H2_t_Hist").get(is,component+1,2).projectionX();
	    h2.setTitleX(tit+" BAR "+(component+1)+" TL-TR (ns)"); h2.setTitleY("Entries"); h2.setFillColor(32);
	    canvas.draw(h2);            

            
            
        canvas.repaint();

    }

    public void stateChanged(ChangeEvent e) {
        int i = ccview.getTabbedPane().getSelectedIndex();
        String tabTitle = ccview.getTabbedPane().getTitleAt(i);
        if (tabTitle != selectedDir) {
            selectedDir = tabTitle;
        }
    }
 
}
