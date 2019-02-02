package org.clas.fcmon.band;

import java.awt.BorderLayout;
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

import edu.umd.cs.findbugs.detect.StaticCalendarDetector;

import org.jlab.groot.data.H1F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.data.GraphErrors;

public class BANDCalib_C extends FCApplication implements CalibrationConstantsListener,ChangeListener {

    
    EmbeddedCanvas c = this.getCanvas(this.getName()); 
    CalibrationConstantsView      ccview = new CalibrationConstantsView();
    ArrayList<CalibrationConstants> list = new ArrayList<CalibrationConstants>();
    CalibrationConstants calib;
    JPanel                    engineView = new JPanel();

    int is1,is2;

    public DetectorCollection<GraphErrors> col_gr_l1 = new DetectorCollection<GraphErrors>();
    public DetectorCollection<GraphErrors> col_gr_l2 = new DetectorCollection<GraphErrors>();
    
    String[] names = {"/calibration/band/NAMEME"};
    String selectedDir = names[0];
    int selectedSector = 1;
    int selectedLayer = 1;
    int selectedPaddle = 1;
    
    public BANDCalib_C(String name, BANDPixels[] bandPix) {
        super(name,bandPix);    
     }

    public void init(int is1, int is2) {
    	
    	System.out.println("\tInitializing Speed of Light Calibration");
    	
        this.is1=is1;
        this.is2=is2;
        
        calib = new CalibrationConstants(3,
                "low_edge/F:high_edge/F:offset [ns]/F:c[cm_ns]/F");
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
    				
    				H1F h1c = bandPix[ilmap].strips.hmap2.get("H2_t_Hist").get(sector,0,0).sliceY(paddle); h1c.setTitleX(" BAR "+(paddle+1)+" TDIF");
    				double tenPerMax = 0.10*h1c.getMax();
    		        double temp1, temp2, minX, maxX;
    		        minX=0;
    		        maxX=0;
    		        for(int i = 1 ; i <= h1c.getXaxis().getNBins();i++ ) {
    		        	temp1 = h1c.getBinContent(i);
    		        	temp2 = h1c.getxAxis().getBinCenter(i);
    		        	if(minX==0&&temp1>tenPerMax) minX=temp2;
    		        }
    		        for(int i = h1c.getXaxis().getNBins() ; i >= 1;i-- ) {
    		        	temp1 = h1c.getBinContent(i);
    		        	temp2 = h1c.getxAxis().getBinCenter(i);
    		        	if(maxX==0&&temp1>tenPerMax) maxX=temp2;
    		        }

    		        double[] xl1 = { minX , minX };
    		        double[] xl2 = { maxX , maxX };
    		        double[] yl  = { 0,h1c.getMax()};
    		        GraphErrors gr_l1 = new GraphErrors("gr_l1",xl1,yl);
    		        GraphErrors gr_l2 = new GraphErrors("gr_l2",xl2,yl);
    		        
    		        col_gr_l1.add(layer, sector,paddle,gr_l1);
    		        col_gr_l2.add(layer, sector,paddle,gr_l2);
    		        
    		        calib.setDoubleValue(minX, "low_edge" , sector, lidx, pidx);
    		        calib.setDoubleValue(maxX, "high_edge", sector, lidx, pidx);
    		        
    		        double offset = (maxX+minX)/2.0;
    		        calib.setDoubleValue(offset,"offset [ns]" , sector, lidx, pidx);
    		        
    		        double bar_length = 0;
    		        if     (sector==1)            bar_length = 164.0; //cm
    		        else if(sector==2||sector==5) bar_length = 202.0; //cm
    		        else if(sector==3||sector==4) bar_length =  51.0; //cm
    		        double c_cm_ns = 2*bar_length/(maxX-minX);
    		        
    		        calib.setDoubleValue(c_cm_ns, "c[cm_ns]" , sector, lidx, pidx);
    	
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
        
        c.clear(); //c.divide(2, 2);
        c.setAxisFontSize(12);

        H1F h1c;
        h1c = bandPix[ilmap].strips.hmap2.get("H2_t_Hist").get(is,0,0).sliceY(ic); h1c.setTitleX(" BAR "+(ic+1)+" TDIF");   h1c.setFillColor(2);
        c.draw(h1c);
        
        GraphErrors gr_l1 = col_gr_l1.get(layer,sector,component);
        GraphErrors gr_l2 = col_gr_l2.get(layer,sector,component);
        
        if( gr_l1 != null && gr_l2 != null) {
        	c.draw(gr_l1,"same");
        	c.draw(gr_l2,"same");
        }
        
        c.repaint();
        //End of plotting
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
