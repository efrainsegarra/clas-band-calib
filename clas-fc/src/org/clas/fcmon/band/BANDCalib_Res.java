package org.clas.fcmon.band;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

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
import org.jlab.groot.fitter.ParallelSliceFitter;

public class BANDCalib_Res extends FCApplication implements CalibrationConstantsListener,ChangeListener{

	EmbeddedCanvas c = this.getCanvas(this.getName()); 
	CalibrationConstantsView      ccview = new CalibrationConstantsView();
	ArrayList<CalibrationConstants> list = new ArrayList<CalibrationConstants>();
	CalibrationConstants calib;
	JPanel                    engineView = new JPanel();
	int runno;

	int is1,is2;
	
	public DetectorCollection<ArrayList<H1F>> projections = new DetectorCollection<ArrayList<H1F>>();
	
	

	String[] names = {"/calibration/band/resolution"};
	String selectedDir = names[0];
	int selectedSector = 1;
	int selectedLayer = 1;
	int selectedPaddle = 1;
	
	//File file = null;

	//public DetectorCollection<F1D> adcFitL = new DetectorCollection<F1D>();
	//public DetectorCollection<F1D> adcFitR = new DetectorCollection<F1D>();
	

	public BANDCalib_Res(String name, BANDPixels[] bandPix) {
		super(name,bandPix);    
	}

	public void init(int is1, int is2) {

		System.out.println("\tInitializing HV Calibration");

		this.is1=is1;
		this.is2=is2;
		
		calib = new CalibrationConstants(3,
				"Mean ADC/F:Bin Width [ADC]/F:Resolution/F");
		calib.setName("/calibration/band/resolution");
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
					
		// Loop over all layers
		for( int layer = 0 ; layer<bandPix.length ; layer++) {

			// Loop over all sectors in a layer
			for (int sector=is1 ; sector<is2 ; sector++) {

				// Loop over all paddles in a sector
				for(int paddle=0; paddle<bandPix[layer].nstr[sector-1] ; paddle++) {

					// Add entry for the unique paddle
					int lidx = (layer+1);
					int pidx = (paddle+1);
					calib.addEntry(sector,lidx,pidx);
							// layer 0:5, sector 1:5, paddle 0:whatever
					fit(layer, sector, paddle);//x_fit_range);
					//System.out.println("Done with Layer "+ lidx + ", Sector "+ sector + " , Component " + pidx);
				} 
			}        		
		}   	

		calib.fireTableDataChanged();
	}



    public void fit(int layer, int sector, int paddle){ 
        
        int lidx = (layer+1);
        int pidx = (paddle+1);
        
        H2F h = bandPix[layer].strips.hmap2.get("H2_a_Hist").get(sector,pidx,0);

        
        	// Grab projections based on number of events
        ArrayList<H1F> slices = h.getSlicesX();
        ArrayList<H1F> saved = new ArrayList<H1F>();
        int scale = (50000/100);
        int startBin = 5; //2500 ADC channel = (50000/200)*10
        int stopBin = 70; //35000 ADC channel = (50000/200)*140
        
        H1F temp = slices.get(startBin);
        if( slices.size() < stopBin ) stopBin=slices.size();
        int minBin; int maxBin;
        minBin = startBin;
        for (int i = startBin+1; i < stopBin; i++) {
        	H1F slice = slices.get(i);
        	temp.add(slice);
        	if( temp.integral() > 1500) {
        		maxBin = i;
        		H1F copy = temp.histClone("Proj around ADC: "+((maxBin+minBin)/2*scale));
        		copy.setTitle("Proj around ADC: "+((maxBin+minBin)/2*scale));
        		saved.add(copy);
        		temp.reset();
        		slice.reset();
        	}
        	slice.reset();
        }
        temp.reset();
        slices.clear();
        
        	// For all the saved projections, fit them
  	    projections.add(sector,lidx,pidx, saved);
  	    
  	    
        for( int i = 0; i < saved.size(); i++) {
        	F1D f1 = new F1D("f1", "[amp]*gaus(x,[mean],[sigma])",
        			saved.get(i).getxAxis().min(),saved.get(i).getxAxis().max() );
        	
            f1.setParameter(0, saved.get(i).getMax() );
            f1.setParameter(1, saved.get(i).getMean() );
            f1.setParameter(2, saved.get(i).getRMS() );
            f1.setOptStat(1111);

            DataFitter.fit(f1, saved.get(i), "REQ");
        }
        
        
       
        
        
    }

 
 

 public void updateCanvas(DetectorDescriptor dd) {
     
     this.getDetIndices(dd); 
             
     int lr = dd.getOrder()+1;
     int sector = dd.getSector();
     int component = dd.getComponent();   
     int layer = dd.getLayer();
     //int ilm = ilmap;
     c.clear();
    
     ArrayList<H1F> saved = projections.get(sector,layer,component+1);
     if( saved!= null) {
	     int divide = saved.size();
	     c.divide(1, divide);
	     
	     for( int i = 0 ; i<saved.size(); i++) {
	    	 c.cd(i);
	    	 H1F h = saved.get(i);
	    	 h.setTitle(h.getTitle());
	    	 c.draw(h);
	    	 
	     }
	     
	     
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

