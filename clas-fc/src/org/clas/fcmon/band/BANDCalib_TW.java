package org.clas.fcmon.band;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.clas.fcmon.tools.FCApplication;
import org.jlab.detector.base.DetectorCollection;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.CalibrationConstantsListener;
import org.jlab.detector.calib.utils.CalibrationConstantsView;
//import org.root.basic.EmbeddedCanvas;
//import org.root.func.F1D;
//import org.root.histogram.H1D;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.math.F1D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.fitter.DataFitter;

public class BANDCalib_TW extends FCApplication implements CalibrationConstantsListener,ChangeListener {

 
   
	EmbeddedCanvas c = this.getCanvas(this.getName()); 
	CalibrationConstantsView      ccview = new CalibrationConstantsView();
	ArrayList<CalibrationConstants> list = new ArrayList<CalibrationConstants>();
	CalibrationConstants calib;
	JPanel                    engineView = new JPanel();
	int runno;

	int is1,is2;

	String[] names = {"/calibration/band/NAMEME"};
	String selectedDir = names[0];
	int selectedSector = 1;
	int selectedLayer = 1;
	int selectedPaddle = 1;

	File file = null;
    
    public BANDCalib_TW(String name, BANDPixels[] bandPix) {
        super(name,bandPix);    
     }

    public void init(int is1, int is2) {
    	
    	System.out.println("\tInitializing Speed of Light Calibration");
    	
        this.is1=is1;
        this.is2=is2;
        
        calib = new CalibrationConstants(3,
                "nameMe/F:nameMeToo/F");
        calib.setName("/calibration/band/nameMeThree");
        calib.setPrecision(3);
        
        list.add(calib);         



    }   
    public List<CalibrationConstants>  getCalibrationConstants(){
        return list;
    } 
    public JPanel getCalibPane() {        
		engineView.setLayout(new BorderLayout());
		JSplitPane enginePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT); 
		ccview.getTabbedPane().addChangeListener(this);
		ccview.addConstants(this.getCalibrationConstants().get(0),this);

		enginePane.setTopComponent(c);
		enginePane.setBottomComponent(ccview);       
		enginePane.setResizeWeight(0.8);
		engineView.add(enginePane);
		return engineView;       
	}   

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
        				
    		        	fit();
        			}
    		        //System.out.println("Finished with Layer "+ lidx + ", Sector "+ sector + " , Component " + pidx);
            	} 
    		}        		
        }   	
    	
        calib.fireTableDataChanged();
    }
    
    public void fit(){ 
        
     }

    
    
    

    public void updateCanvas(DetectorDescriptor dd) {
        
        this.getDetIndices(dd); 
        
        int lr = dd.getOrder()+1;
        int sector = dd.getSector();
        int component = dd.getComponent();   
        int layer = ilmap;
               
        int nstr = bandPix[layer].nstr[is-1];
        int min=0, max=nstr;
        
        H2F h2;
        
        c.clear(); c.divide(2, 2);
        c.setAxisFontSize(12);

        c.cd(0);
        h2 = bandPix[layer].strips.hmap2.get("H2_at_Hist").get(sector,component+1,0); 
        h2.setTitleX("Left PMT: ADC vs FADC t - TDC t");   
        c.draw(h2);
        
        c.cd(1);
        h2 = bandPix[layer].strips.hmap2.get("H2_at_Hist").get(sector,component+1,1); 
        h2.setTitleX("Right PMT: ADC vs FADC t - TDC t");   
        c.draw(h2);
        
        c.cd(2);
        h2 = bandPix[layer].strips.hmap2.get("H2_a_Hist").get(sector,component+1,0); 
        h2.setTitleX("Sqrt(ADC_L*ADC_R) vs (ToF-Ref) from FADC");   
        c.draw(h2);
        
        c.cd(3);
        h2 = bandPix[layer].strips.hmap2.get("H2_t_Hist").get(sector,component+1,0); 
        h2.setTitleX("Sqrt(ADC_L*ADC_R) vs (ToF-Ref) from TDC");   
        c.draw(h2);
       
        
        c.repaint();

        
    }
    
    
    
    
    public void stateChanged(ChangeEvent e) {
		int i = ccview.getTabbedPane().getSelectedIndex();
		String tabTitle = ccview.getTabbedPane().getTitleAt(i);
		if (tabTitle != selectedDir) {
			selectedDir = tabTitle;
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
}
