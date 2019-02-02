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
        
        DetectorCollection<H1F> H1_a_Sevd = new DetectorCollection<H1F>();
        DetectorCollection<H1F> H1_t_Sevd = new DetectorCollection<H1F>();
        DetectorCollection<H2F> H2_a_Hist = new DetectorCollection<H2F>();
        DetectorCollection<H2F> H2_t_Hist = new DetectorCollection<H2F>();
        DetectorCollection<H2F> H2_a_Sevd = new DetectorCollection<H2F>();
        DetectorCollection<H2F> H2_t_Sevd = new DetectorCollection<H2F>();
        DetectorCollection<H1F> H1_o_Hist = new DetectorCollection<H1F>();
        
        //Histograms stored as:
        //	H2_a_hist.get(sector,l/r,idx) where idx specifies which 2D histogram is being saved here
        // 		sector: 1-6
        //		l/r: 1=left, 2=right, 0=gmean??
        //	Then of course there are 5 BANDPix objects, for each layer.
        
        
        
        for (int is=1; is<(bc.IS2-bc.IS1+1) ; is++) { // loop over sectors in a layer
        	double axis_scale = amax[id];
        	if (is == 3 || is == 4) {
        		axis_scale = ashortmax[id];
        	}
        	double nend = nstr[is-1]+1;	// find how many bars are in a sector
            int ill=0; iid="s"+Integer.toString(is)+"_l"+Integer.toString(ill)+"_c";
            
            // Geometric mean plot for L_ADC and R_ADC on a given paddle
            H2_a_Hist.add(is, 0, 0, new H2F("a_gmean_"+iid+0, 200,   0., axis_scale,nstr[is-1], 1., nend));	
            // L-R TDC plot of a given paddle for TDC time
            H2_t_Hist.add(is, 0, 0, new H2F("t_tdif_"+iid+0,  600, -20.,      20.,nstr[is-1], 1., nend));
            // L-R TDC plot of a given paddle for FADC time
            H2_a_Hist.add(is, 0, 1, new H2F("a_tdif_"+iid+0,  1000, -20.,      20.,nstr[is-1], 1., nend));
            
            // Add gm vs fadc time sum for each paddle
            H2_a_Hist.add(is, 0, 9, new H2F("a_tsum_"+iid+0,  5000, 0.,	   500., nstr[is-1], 1., nend));
            for( int ip = 1 ; ip<nstr[is-1]+1; ip++) {
            	H2_a_Hist.add(is, ip, 8, new H2F("a_tsum_gm_"+iid+ip+1, 200, 0., 0.5*axis_scale , 600, -15., 15.));
            }
          
            
            //for (int ip=1; ip<nstr[is-1]+1; ip++) { // loop over bars in sector
            //    H2_t_Hist.add(is, ip, 2, new H2F("c_tdif_"+iid+1+ip,   1000, -50., 50.,50,-0.2, 0.4));
            //}            
            
            for (int il=1 ; il<3 ; il++){ // loop for left side and then right side
                iid="s"+Integer.toString(is)+"_l"+Integer.toString(il)+"_c";
                H2_a_Hist.add(is, il, 0, new H2F("a_raw_"+iid+0,      400,   0., axis_scale,nstr[is-1], 1., nend));
                H2_a_Hist.add(is, il, 7, new H2F("a_raw_overflowInc_"+iid+0,      400,   0., axis_scale,nstr[is-1], 1., nend));
               
                H2_t_Hist.add(is, il, 0, new H2F("a_raw_"+iid+0,      500,   -tmax[id], tmax[id], nstr[is-1], 1., nend));
                
                H2_a_Hist.add(is, il, 1, new H2F("a_raw_"+iid+1,      300,   0., axis_scale,500, -tmax[id],tmax[id]));
                H2_a_Hist.add(is, il, 3, new H2F("a_ped_"+iid+3,      1000, -500.,  500., nstr[is-1], 1., nend)); 
                H2_a_Hist.add(is, il, 5, new H2F("a_fadc_"+iid+5,     1000,   0., 1000., nstr[is-1], 1., nend));			// this is used for mode1:sum 2D graph where x axis is samples(4ns/ch)
                																										// and y-axis is which PMT in the sector/layer for left and right side
                              
            
                H2_a_Hist.add(is, il, 6, new H2F("a_fadc_"+iid+6,     100, -20.,  20., nstr[is-1], 1., nend));
                H1_a_Sevd.add(is, il, 0, new H1F("a_sed_"+iid+0,                       nstr[is-1], 1., nend));
                H2_a_Sevd.add(is, il, 0, new H2F("a_sed_fadc_"+iid+0, 100,   0., 100., nstr[is-1], 1., nend));
                H2_a_Sevd.add(is, il, 1, new H2F("a_sed_fadc_"+iid+1, 100,   0., 100., nstr[is-1], 1., nend));
                H2_t_Sevd.add(is, il, 0, new H2F("a_sed_fadc_"+iid+0, 200,   0., 100., nstr[is-1], 1., nend));
                
                iid="s"+Integer.toString(is)+"_l"+Integer.toString(il);
                H1_o_Hist.add(is,il,0,	new H1F("overflow_"+iid,	nstr[is-1],1.,nend) );
                H1F d = null;
                
            }
        }       

        if(!hipoFile.equals(" ")){
            FCCalibrationData calib = new FCCalibrationData();
            calib.getFile(hipoFile);
            H2_a_Hist = calib.getCollection("H2_a_Hist");
            H2_t_Hist = calib.getCollection("H2_t_Hist");
        }         
        
        strips.addH1DMap("H1_a_Sevd",  H1_a_Sevd);
        strips.addH1DMap("H1_o_Hist",  H1_o_Hist);
        strips.addH1DMap("H1_t_Sevd",  H1_t_Sevd);
        strips.addH2DMap("H2_a_Hist",  H2_a_Hist);
        strips.addH2DMap("H2_t_Hist",  H2_t_Hist);
        strips.addH2DMap("H2_a_Sevd",  H2_a_Sevd);
        strips.addH2DMap("H2_t_Sevd",  H2_t_Sevd);
    } 
    
}
