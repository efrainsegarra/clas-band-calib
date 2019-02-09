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
import org.jlab.utils.groups.IndexedList;
import org.jlab.groot.data.H1F;
import org.jlab.groot.fitter.DataFitter;

public class BANDCalib_Atten extends FCApplication implements CalibrationConstantsListener,ChangeListener {


	EmbeddedCanvas c = this.getCanvas(this.getName()); 
	CalibrationConstantsView      ccview = new CalibrationConstantsView();
	ArrayList<CalibrationConstants> list = new ArrayList<CalibrationConstants>();
	CalibrationConstants calib;
	JPanel                    engineView = new JPanel();
	int runno;

	int is1,is2;

	String[] names = {"/calibration/band/attenuation"};
	String selectedDir = names[0];
	int selectedSector = 1;
	int selectedLayer = 1;
	int selectedPaddle = 1;

	File file = null;


	public BANDCalib_Atten(String name, BANDPixels[] bandPix) {
		super(name,bandPix);    
	}

	public void init(int is1, int is2) {

		System.out.println("\tInitializing Speed of Light Calibration");

		this.is1=is1;
		this.is2=is2;

		calib = new CalibrationConstants(3,
				"sigma/F:mu[m]/F");
		calib.setName("/calibration/band/attenuation");
		calib.setPrecision(3);

		list.add(calib);         
	}   
	public List<CalibrationConstants>  getCalibrationConstants(){
		return list;
	} 
	public JPanel getCalibPane() {        
		engineView.setLayout(new BorderLayout());
		JSplitPane enginePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT); 
		ccview.getTabbedPane().addChangeListener(this);
		ccview.addConstants(this.getCalibrationConstants().get(0),this);

		enginePane.setTopComponent(c);
		enginePane.setBottomComponent(ccview);       
		enginePane.setResizeWeight(0.8);
		engineView.add(enginePane);
		return engineView;       
	}  

	public void analyze() {


		if( app.cosmicData == true){

			
			file = new File(String.format("../band_analysis/hvScan/calibOutput/run_%d-attenFit.txt",runno));
			//file = new File(String.format("/work/band/calibOutput/run_%d-adcFit.txt",runno));
			// Try to open a text file, otherwise do not try to analyze
			try(PrintWriter output = new PrintWriter(file)) {
				output.println("#Sector\tLayer\tComponent\tAttenuation");

				for( int layer = 0 ; layer<bandPix.length ; layer++) {							// loop over layer

					for (int sector=is1 ; sector<is2 ; sector++) {								// loop over sector in layer
						for(int paddle=0; paddle<bandPix[layer].nstr[sector-1] ; paddle++) {	// loop over paddle in sector

							// Add entry for the unique paddle
							int lidx = (layer+1);
							int pidx = (paddle+1);
							calib.addEntry(sector,lidx,pidx);

							fit(layer,sector,paddle,output);

							//System.out.println("Completed Layer "+ lidx + ", Sector "+ sector + " , Component " + pidx);
						} 
					}        		
				}
			}
			catch(FileNotFoundException e){
				e.printStackTrace();
			}

		}

		calib.fireTableDataChanged();
	}

	public void fit(int layer,int sector,int paddle, PrintWriter FILE){ 

		H1F h1 = bandPix[layer].strips.hmap1.get("H1_a_Hist").get(sector,0,paddle+1);
		
		double sigma;
		if( sector == 3 || sector == 4){
				// Gaus works well for short bars
			F1D f1 = new F1D("f1", "[amp]*gaus(x,[mean],[sigma])", -1, 1);
			f1.setParameter(0,h1.getMax());
			f1.setParameter(1,h1.getMean());
			f1.setParameter(2,h1.getRMS());
			DataFitter.fit(f1, h1, "REQ");
			sigma = Math.abs(h1.getFunction().getParameter(2));
		}
		else{
			sigma = h1.getRMS();
		}

		int lidx = (layer+1);
		int pidx = (paddle+1);
		calib.setDoubleValue(sigma, "sigma", sector, lidx, pidx);

		double bar_length = 0;
		if     (sector==1)            bar_length = 164.0; //cm
		else if(sector==2||sector==5) bar_length = 202.0; //cm
		else if(sector==3||sector==4) bar_length =  51.0; //cm
		double mu = 2*bar_length/1000./sigma;
		calib.setDoubleValue(mu, "mu[m]", sector, lidx, pidx);

		FILE.println(String.format( "%d\t%d\t%d\t%f",sector,layer+1,pidx,mu  ) );
	}





	public void updateCanvas(DetectorDescriptor dd) {

		IndexedList<List<Float>>          fadc_int = new IndexedList<List<Float>>(4);

		this.getDetIndices(dd); 

		int lr = dd.getOrder()+1;
		int sector = dd.getSector();
		int component = dd.getComponent();   
		int layer = ilmap;

		int nstr = bandPix[layer].nstr[is-1];
		int min=0, max=nstr;

		c.clear(); c.divide(2, 4);
		c.setAxisFontSize(12);

		H1F h1;

		for( int ip = 1 ; ip<nstr+1; ip++) {
			c.cd(ip-1);
			h1 = bandPix[layer].strips.hmap1.get("H1_a_Hist").get(sector,0,ip);
			h1.setTitle("SEC:"+sector+", BAR:"+ip);
			h1.setTitleX("ln(R_i)");
			c.draw(h1);
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
