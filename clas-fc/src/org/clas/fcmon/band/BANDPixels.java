package org.clas.fcmon.band;

import java.util.Arrays;
import java.util.TreeMap;

import org.clas.fcmon.tools.FCCalibrationData;
import org.clas.fcmon.tools.Strips;
import org.jlab.detector.base.DetectorCollection;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.utils.groups.IndexedList;
import org.jlab.utils.groups.IndexedTable;

public class BANDPixels {
	
	// Object to hold all of our histograms
	public Strips          strips = new Strips();
	DatabaseConstantProvider ccdb = new DatabaseConstantProvider(1,"default");
	BANDConstants              bc = new BANDConstants();  

	double band_xpix[][][] = new double[4][14][6];
	double band_ypix[][][] = new double[4][14][6];


	static double BANDPixels_x_axis_max = 20000;
	static double short_bar_scaler = 2.5;
	static double shortscalemax = BANDPixels_x_axis_max*short_bar_scaler;
	// Note: To have all bars have the same x-axis range, set short_bar_scaler to 1 

	//int[]  array = new int[5];
	//amax = Arrays.fill(array, 1);//*x_axis_max;
	//public double        amax[]= {5000.,5000.,5000.,5000.,5000.};
	public double 		 amax[] = {1.,1.,1.,1.,1.,1.}; 
	public double		 ashortmax[] = {1.,1.,1.,1.,1.,1.}; 
	public double        tmax[] = {10000.,10000.,10000.,10000.,10000.,10000.};


	public void Rescale(double array[],double scaler){
		for (int i=0; i<array.length; i++) {
			array[i] = array[i] * scaler;
		}
		//return array;
	}





	int        nha[][] = new    int[6][2];
	int        nht[][] = new    int[6][2];
	int    strra[][][] = new    int[6][2][7]; 
	int    strrt[][][] = new    int[6][2][7]; 
	int     adcr[][][] = new    int[6][2][7];      
	float   tdcr[][][] = new  float[6][2][7]; 
	float     tf[][][] = new  float[6][2][7]; 
	float     ph[][][] = new  float[6][2][7]; 

	public DetectorCollection<TreeMap<Integer,Object>> Lmap_a = new DetectorCollection<TreeMap<Integer,Object>>();
	public DetectorCollection<TreeMap<Integer,Object>> Lmap_t = new DetectorCollection<TreeMap<Integer,Object>>();
	public IndexedList<double[]>                     Lmap_a_z = new IndexedList<double[]>(2);
	public IndexedList<double[]>                     Lmap_t_z = new IndexedList<double[]>(2);

	public int id;
	public int[] nstr = new int[bc.bandlen.length];
	public String detName = null;

	public BANDPixels(String det) {
		bc.setSectorRange(1, 6);
		if (det.equals("LAYER1")) id=0;
		if (det.equals("LAYER2")) id=1;
		if (det.equals("LAYER3")) id=2;
		if (det.equals("LAYER4")) id=3;
		if (det.equals("LAYER5")) id=4;
		if (det.equals("VETO")) id=5;
		//System.out.println(id);
		for (int is=0; is<(bc.IS2-bc.IS1); is++) {
			nstr[is]=bc.bandlay[id][is];
		}
		detName = det;
		pixdef();
		Rescale(amax,BANDPixels_x_axis_max);
		Rescale(ashortmax,shortscalemax);
	}

	public void getLmapMinMax(int is1, int is2, int il, int opt){
		TreeMap<Integer,Object> map = null;
		double min,max,avg,aavg=0,tavg=0;
		double[] a = {1000,0,0};
		double[] t = {1000,0,0};
		for (int is=is1 ; is<is2; is++) {
			map = Lmap_a.get(is, il, opt);
			min = (double) map.get(2); max = (double) map.get(3); avg = (double) map.get(4);
			if (min<a[0]) a[0]=min; if (max>a[1]) a[1]=max; aavg+=avg;
			map = Lmap_t.get(is, il, opt);
			min = (double) map.get(2); max = (double) map.get(3); avg = (double) map.get(4);
			if (min<t[0]) t[0]=min; if (max>t[1]) t[1]=max; tavg+=avg;
		}

		a[2]=Math.min(500000,aavg/(is2-is1));
		t[2]=Math.min(500000,tavg/(is2-is1));

		Lmap_a_z.add(a,il,opt);
		Lmap_t_z.add(t,il,opt);        
	}	

	public void init() {
		System.out.println("BANDPixels.init():");
		Lmap_a.clear();
		Lmap_t.clear();
	}

	public void pixdef() {

		System.out.println("BANDPixels.pixdef(): "+this.detName); 	

		double   k;
		double   x_inc=0;

		for (int is=0; is<bc.bandlen.length; is++) {
			int    nnstr = nstr[is];
			double  xoff = bc.bandxoff[is];
			double  yoff = bc.bandyoff[is];
			double y_inc = bc.bandwid[is];

			// Draw all the "pmts" except for the veto layer
			if(id<5) {
				for(int i=0 ; i<nnstr ; i++){
					x_inc = 0.5*bc.bandlen[is];        	
					band_xpix[0][nnstr+i][is]=xoff-x_inc;
					band_xpix[1][nnstr+i][is]=xoff+0;
					band_xpix[2][nnstr+i][is]=xoff+0.;
					band_xpix[3][nnstr+i][is]=xoff-x_inc;
					k = -i*y_inc+yoff*y_inc;	    	   
					band_ypix[0][nnstr+i][is]=k;
					band_ypix[1][nnstr+i][is]=k;
					band_ypix[2][nnstr+i][is]=k-y_inc;
					band_ypix[3][nnstr+i][is]=k-y_inc;
				}
				for(int i=0 ; i<nnstr ; i++){
					x_inc = 0.5*bc.bandlen[is];        	
					band_xpix[0][i][is]=xoff+0;
					band_xpix[1][i][is]=xoff+x_inc;
					band_xpix[2][i][is]=xoff+x_inc;
					band_xpix[3][i][is]=xoff+0.;
					k = -i*y_inc+yoff*y_inc;	    	   
					band_ypix[0][i][is]=k;
					band_ypix[1][i][is]=k;
					band_ypix[2][i][is]=k-y_inc;
					band_ypix[3][i][is]=k-y_inc;
				}
			}
			// Draw all the "pmts" for the veto layer
			else {
				for(int i=0 ; i<nnstr ; i++){
					if(is!=3){
						x_inc = 0.5*bc.bandlen[is];        	
						band_xpix[0][i][is]=xoff+0;
						band_xpix[1][i][is]=xoff+x_inc;
						band_xpix[2][i][is]=xoff+x_inc;
						band_xpix[3][i][is]=xoff+0.;
						k = -i*y_inc+yoff*y_inc;	    	   
						band_ypix[0][i][is]=k;
						band_ypix[1][i][is]=k;
						band_ypix[2][i][is]=k-y_inc;
						band_ypix[3][i][is]=k-y_inc;
					}
					else {
						x_inc = 0.5*bc.bandlen[is];        	
						band_xpix[0][nnstr+i][is]=xoff-x_inc;
						band_xpix[1][nnstr+i][is]=xoff+0;
						band_xpix[2][nnstr+i][is]=xoff+0.;
						band_xpix[3][nnstr+i][is]=xoff-x_inc;
						k = -i*y_inc+yoff*y_inc;	    	   
						band_ypix[0][nnstr+i][is]=k;
						band_ypix[1][nnstr+i][is]=k;
						band_ypix[2][nnstr+i][is]=k-y_inc;
						band_ypix[3][nnstr+i][is]=k-y_inc;
					}
				}
			}
		}
	}

	public void initHistograms(String hipoFile) {

		System.out.println("BANDPixels.initHistograms(): "+this.detName);  

		String iid;

		// Histograms for all events -- 1D and 2D
		DetectorCollection<H2F> H2_a_Hist = new DetectorCollection<H2F>();
		DetectorCollection<H2F> H2_t_Hist = new DetectorCollection<H2F>();

		/*Histograms stored as:
		   H2_a_hist.get(sector,paddle,idx) where idx specifies which 2D histogram is being saved here
		 	sector: 1-6
			paddle: 1-# paddles in the sector -- this means it's a 2D histogram of just that paddle
				0		          -- this means it is a 2D histogram containing info for all paddles in the sector
			idx:    specifies if we want more histograms for this sector/paddle
			
			Then of course there are 6 BANDPix objects, for each layer, including veto
		*/

		// Loop over every sector in a layer (remember, there are 6 BANDPix objects!
		for (int is=1; is<(bc.IS2-bc.IS1+1) ; is++) {
			
			// For this sector, grab how many bars are in the sector
			double nend = nstr[is-1]+1;
			
			// Scale adcs differently for short bars and long bars for the moment
			double axis_scale = amax[id];
			if (is == 3 || is == 4) {
				axis_scale = ashortmax[id];
			}

			iid = Integer.toString(is);

			// <----------------------------------------------------------------------------------------------------------------------
			// 2D FADC histograms
				// Geometric mean plot for L_ADC and R_ADC for all paddles in sector
				// x-axis is ADC spectra and y-axis identifies which paddle in the sector
			H2_a_Hist.add(is, 0, 0, new H2F("fadc_gmeanLR_"+iid,	200,0.,axis_scale,	nstr[is-1],1.,nend));	
				// FADC L-R time difference for all paddles in a sector
				// x-axis is FADC time difference of L-R and y-axis identifies which paddle in the sector
			H2_a_Hist.add(is, 0, 1, new H2F("fadc_tdiff_"+iid,  	200,-20.,20.,		nstr[is-1],1.,nend));

			// For each paddle in a sector,
			for( int ip = 1 ; ip<nstr[is-1]+1; ip++) {
				// Geometric mean ToF plot for a single bar in a sector
				// x-axis is FADC geometric mean of 2 PMTs and y-axis is average FADC time minus reference time for that bar
				H2_a_Hist.add(is, ip, 0, new H2F("fadc_gmeanLR_fadc_tSum_"+iid, 	200,0.,0.5*axis_scale, 		300, -15., 15.));
			}
			// ------------------------------------------->


			// <----------------------------------------------------------------------------------------------------------------------
			// 2D TDC histograms
				// TDC L-R time difference for all paddles in a sector
				// x-axis is TDC time difference of L-R and y-axis identifies which paddle in the sector
			H2_t_Hist.add(is, 0, 0, new H2F("tdc_tdiff_"+iid,  	200,-20.,20.,		nstr[is-1], 1., nend));
	
			// For each paddle in a sector,
			for( int ip = 1 ; ip<nstr[is-1]+1; ip++) {
				// Geometric mean ToF plot for a single bar in a sector
				// x-axis is FADC geometric mean of 2 PMTs and y-axis is average TDC time minus reference time for that bar
				H2_t_Hist.add(is, ip, 0, new H2F("fadc_gmeanLR_tdc_tSum_"+iid, 		200,0.,0.5*axis_scale, 		300, -15., 15.));
			}
			// ------------------------------------------->


			// Create 2D histograms for TDC and FADC that will contain each have all the bars in a sector, but only
			// left PMTs, right PMTs separately stored
				// loop over left/right side
			for (int il=1 ; il<3 ; il++){ // loop for left side and then right side
				iid = Integer.toString(is)+"_"+Integer.toString(il);
				// FADC histogram for all L or R pmts in a sector (depending on index)
				// x-axis is ADC spectrum and y-axis is identifies which paddle in the sector
				// 	idx (is,0,2) = L pmts
				// 	idx (is,0,3) = R pmts
				H2_a_Hist.add(is, 0, 1+il, new H2F("fadc_adc_"+iid,		400,0.,axis_scale,	nstr[is-1],1.,nend));
				// FADC histogram for all L or R pmts in a sector which INCLUDES overflow events (part of waveform > 4095)
				// x-axis is ADC spectrum and y-axis is identifies which paddle in the sector
				// 	idx (is,0,4) = L pmts
				// 	idx (is,0,5) = R pmts
				H2_a_Hist.add(is, 0, 3+il, new H2F("fadc_ad_overflowInc_"+iid+0,      400,   0., axis_scale,nstr[is-1], 1., nend));

			}
		}       

		if(!hipoFile.equals(" ")){
			FCCalibrationData calib = new FCCalibrationData();
			calib.getFile(hipoFile);
			H2_a_Hist = calib.getCollection("H2_a_Hist");
			H2_t_Hist = calib.getCollection("H2_t_Hist");
		}         

		strips.addH2DMap("H2_a_Hist",  H2_a_Hist);
		strips.addH2DMap("H2_t_Hist",  H2_t_Hist);
	} 

}
