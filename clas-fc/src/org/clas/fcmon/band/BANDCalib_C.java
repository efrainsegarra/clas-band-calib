package org.clas.fcmon.band;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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
	int runno;

	double MAXX,MINX;

	public DetectorCollection<GraphErrors> fa_col_gr_l1 = new DetectorCollection<GraphErrors>();
	public DetectorCollection<GraphErrors> fa_col_gr_l2 = new DetectorCollection<GraphErrors>();
	public DetectorCollection<GraphErrors> td_col_gr_l1 = new DetectorCollection<GraphErrors>();
	public DetectorCollection<GraphErrors> td_col_gr_l2 = new DetectorCollection<GraphErrors>();

	String[] names = {"/calibration/band/TsC"};
	String selectedDir = names[0];
	int selectedSector = 1;
	int selectedLayer = 1;
	int selectedPaddle = 1;
	File file = null;

	public BANDCalib_C(String name, BANDPixels[] bandPix) {
		super(name,bandPix);    
	}

	public void init(int is1, int is2) {

		System.out.println("\tInitializing Speed of Light Calibration");

		this.is1=is1;
		this.is2=is2;

		calib = new CalibrationConstants(3,
				"offset_TDC [ns]/F:c_TDC [cm_ns]/F:offset_FADC [ns]/F:c_FADC [cm_ns]/F");
		calib.setName("/calibration/band/TsC");
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

		if( app.cosmicData  == true) {

			file = new File(String.format("../band_analysis/hvScan/calibOutput/run_%d-tdcFit.txt",runno));
			//file = new File(String.format("/work/band/calibOutput/run_%d-tdcFit.txt",runno));
			// Try to open a text file, otherwise do not try to analyze
			try(PrintWriter output = new PrintWriter(file)) {

				output.println("#Sector\tLayer\tComponent\ttdc_TShift\ttdc_c\tfadc_TShift\tfadc_c");

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

							fit(sector, layer, paddle,output);//x_fit_range);



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

	public void fit(int sector, int layer, int paddle,PrintWriter FILE){ 
		int lidx = (layer+1);
		int pidx = (paddle+1);

		// TDC offset
		H1F h1c = bandPix[layer].strips.hmap2.get("H2_t_Hist").get(sector,0,0).sliceY(paddle);
		boxHist(h1c,sector,layer,paddle,false);

		double offset_T = (MAXX+MINX)/2.0;
		calib.setDoubleValue(offset_T,"offset_TDC [ns]" , sector, lidx, pidx);
		double bar_length = 0;
		if     (sector==1)            bar_length = 164.0; //cm
		else if(sector==2||sector==5) bar_length = 202.0; //cm
		else if(sector==3||sector==4) bar_length =  51.0; //cm
		double c_cm_ns_T = 2*bar_length/(MAXX-MINX);
		calib.setDoubleValue(c_cm_ns_T, "c_TDC [cm_ns]" , sector, lidx, pidx);

		// FADC offset
		h1c = bandPix[layer].strips.hmap2.get("H2_a_Hist").get(sector,0,1).sliceY(paddle);
		boxHist(h1c,sector,layer,paddle,true);

		double offset_A = (MAXX+MINX)/2.0;
		calib.setDoubleValue(offset_A,"offset_FADC [ns]" , sector, lidx, pidx);
		bar_length = 0;
		if     (sector==1)            bar_length = 164.0; //cm
		else if(sector==2||sector==5) bar_length = 202.0; //cm
		else if(sector==3||sector==4) bar_length =  51.0; //cm
		double c_cm_ns_A = 2*bar_length/(MAXX-MINX);
		calib.setDoubleValue(c_cm_ns_A, "c_FADC [cm_ns]" , sector, lidx, pidx);

		FILE.println(String.format("%d\t%d\t%d\t%f\t%f\t%f\t%f",sector,lidx,pidx,offset_T,c_cm_ns_T,offset_A,c_cm_ns_A));        
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

		c.divide(2,2);

		H1F h1a;
		c.cd(0);
		h1a = bandPix[ilmap].strips.hmap2.get("H2_t_Hist").get(sector,0,1).sliceY(component);
		h1a.setTitle("Raw TDC L");
		c.draw(h1a);
		
		c.cd(1);
		h1a = bandPix[ilmap].strips.hmap2.get("H2_t_Hist").get(sector,0,2).sliceY(component);
		h1a.setTitle("Raw TDC R");
		c.draw(h1a);
		
		c.cd(2);
		H1F h1c;
		h1c = bandPix[ilmap].strips.hmap2.get("H2_t_Hist").get(sector,0,0).sliceY(component); 
		h1c.setOptStat(Integer.parseInt("1000100")); 
		h1c.setTitle("TDC L-R Time");
		h1c.setFillColor(2);
		c.draw(h1c);


		GraphErrors gr_l1 = td_col_gr_l1.get(layer,sector,component);
		GraphErrors gr_l2 = td_col_gr_l2.get(layer,sector,component);

		if( gr_l1 != null && gr_l2 != null) {
			c.draw(gr_l1,"same");
			c.draw(gr_l2,"same");
		}

		c.cd(3);
		h1c = bandPix[ilmap].strips.hmap2.get("H2_a_Hist").get(is,0,1).sliceY(ic); 
		h1c.setFillColor(2);
		h1c.setTitle("FADC L-R Time");
		h1c.setOptStat(Integer.parseInt("1000100")); 
		c.draw(h1c);

		gr_l1 = fa_col_gr_l1.get(layer,sector,component);
		gr_l2 = fa_col_gr_l2.get(layer,sector,component);

		if( gr_l1 != null && gr_l2 != null) {
			c.draw(gr_l1,"same");
			c.draw(gr_l2,"same");
		}


		c.repaint();
		//End of plotting
	}

	public void boxHist(H1F h1c, int sector,int layer,int paddle,boolean FADC) {

		int bin_mean = h1c.getXaxis().getBin( h1c.getMean() );
		int bin_sample = bin_mean - h1c.getXaxis().getBin( h1c.getMean() - h1c.getRMS() );
		double AvH = h1c.integral( bin_mean - bin_sample , bin_mean + bin_sample ) / ( (double)bin_sample * 2.);
		AvH *= 0.3; // ask for 30% crossing threshold

		double temp1, temp2, minX, maxX;
		minX=0;
		maxX=0;
		for(int i = 1 ; i <= h1c.getXaxis().getNBins();i++ ) {
			temp1 = h1c.getBinContent(i);
			temp2 = h1c.getxAxis().getBinCenter(i);
				// Find first bin above AvH
			if(minX==0&&temp1>AvH){
				minX=temp2;
				break;
			}
		}
		for(int i = h1c.getXaxis().getNBins() ; i >= 1;i-- ) {
			temp1 = h1c.getBinContent(i);
			temp2 = h1c.getxAxis().getBinCenter(i);
				// Find first bin above AvH
			if(maxX==0&&temp1>AvH){
				maxX=temp2;
				break;
			}
		}
		double[] xl1 = { minX , minX };
		double[] xl2 = { maxX , maxX };
		double[] yl  = { 0,h1c.getMax()};
		GraphErrors gr_l1 = new GraphErrors("gr_l1",xl1,yl);
		GraphErrors gr_l2 = new GraphErrors("gr_l2",xl2,yl);

		if( FADC == true ) {
			fa_col_gr_l1.add(layer, sector,paddle,gr_l1);
			fa_col_gr_l2.add(layer, sector,paddle,gr_l2);
		}
		else {
			td_col_gr_l1.add(layer, sector,paddle,gr_l1);
			td_col_gr_l2.add(layer, sector,paddle,gr_l2);
		}

		MAXX=maxX;
		MINX=minX;
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
