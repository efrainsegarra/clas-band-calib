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
	
	static double adcMax = 30000;
    double band_xpix[][][] = new double[4][14][6];
    double band_ypix[][][] = new double[4][14][6];
    
    public double 		 amax[] = {50000.,50000.,50000.,50000.,50000.,50000.};
    public double        tmax[] = {10000.,10000.,10000.,10000.,10000.,10000.};
        
    
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
    }
    
    // Gets the max / min events to know how to color scale the bars
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
    
    public void initHistograms(String hipoFile, double adcRange) {
    	
        adcMax = adcRange;
        amax[0] = adcMax; amax[1] = adcMax; amax[2] = adcMax;
        amax[3] = adcMax; amax[4] = adcMax; amax[5] = adcMax;
        
        System.out.println("BANDPixels.initHistograms(): "+this.detName);  
        
        String iid;
        
        DetectorCollection<H1F> H1_a_Sevd = new DetectorCollection<H1F>();
        DetectorCollection<H1F> H1_t_Sevd = new DetectorCollection<H1F>();
        DetectorCollection<H2F> H2_a_Hist = new DetectorCollection<H2F>();
        DetectorCollection<H2F> H2_t_Hist = new DetectorCollection<H2F>();
        DetectorCollection<H2F> H2_a_Sevd = new DetectorCollection<H2F>();
        DetectorCollection<H2F> H2_t_Sevd = new DetectorCollection<H2F>();
        DetectorCollection<H2F> H2_at_Hist = new DetectorCollection<H2F>();
        DetectorCollection<H1F> H1_a_Hist = new DetectorCollection<H1F>();
        
        // README:
        /*	Storage of histograms: we have 2D and 1D histograms for ADC/TDC information, 
         *  and also specifies FULL FILE read vs single event:
         *  	H1_a_Sevd: stores all 1D histograms for single event reading, all FADC info
         *  	H2_t_Hist: stores all 2D histograms for FULL FILE reading, all TDC info
         * 
         *  Accessing:
         *  	H2_a_Hist.get(sector,paddle,idx)
         *  		sector: identifies which sector in the layer (0,1,2,3,4)
         *  		paddle: IF THIS IS 0, it means the histogram is a 2D histogram that stores info for ALL pmts
         *  					in a sector. So typically the y-axis identifies which bar in the sector
         *  				ELSE: this ID's bar in the sector ( 1 - # bars in sector)
         *  		idx: 	we could have more than 1 histogram for the same sector,paddle, like
         *  					a histogram that stores ADC that doesn't include overflow (idx=0) and 
         *  					one that does include overflow (idx=1)
         *  
         *   Want to add more?:
         *   	Need to initialize it here, and then fill correct values in BANDReconstruction.
         *   	If you need to have information from both L&R pmts on a bar, you need to fill it
         *   	in the ProcessCalib() function of BANDReconstruction
         */					
        
        
        // Loop over sectors in a layer (remember there are 6 BANDPix objects for each layer)
        for (int is=1; is<(bc.IS2-bc.IS1+1) ; is++) {
        	// Grab how many bars are in the current sector (id is the current layer idx (0-5))
        	double nend = nstr[is-1]+1;
        	
        	// ill = 0 means histogram has info from both l+r pmts
            int ill=0; 
            // Create string name
            iid = Integer.toString(is)+"_S_"+Integer.toString(id)+"_L_"+"_all_C_"+"_0_LR";
            
            // First, let's create histograms that use information from BOTH left and right
            // and histograms that will store information for ALL PMTs in a sector 
            
            	// Geometric mean plot: x-axis is GM of sqrt(L*R) ADC and y-axis identifies which
            	// bar in the sector
            H2_a_Hist.add(is, 0, 0,	new H2F("fadc_gm_"+iid,		400,0.,8000.,	nstr[is-1],1.,nend));	
            
            H2_a_Hist.add(is, 0, 14,new H2F("fadc_diff_"+iid,	100,-500.,500.,	nstr[is-1],1.,nend));	
            
            
	        	// L-R FADC plot: x-axis is TDC time difference for L-R given from our FADC digital signal
	        	// y-axis identifies which bar in the sector
            H2_a_Hist.add(is, 0, 1, new H2F("fadc_tdif_"+iid,  400, -20.,20.,		nstr[is-1], 1., nend));
            
            	// L-R TDC plot: x-axis is TDC time difference for L-R given from our TDC modules
            	// y-axis identifies which bar in the sector
            H2_t_Hist.add(is, 0, 0, new H2F("tdc_tdif_"+iid,	400,-20.,20.,		nstr[is-1],1.,nend));
            
            
            // Loop over all bars in a sector
            for( int ip = 1 ; ip<nstr[is-1]+1; ip++) {
            		//  ToF - Ref vs GM: x-axis is GM of sqrt(L*R) ADC and y-axis is (L+R)/2. - RF time using 
            		//	times from FADC
            	iid = Integer.toString(is)+"_S_"+Integer.toString(id)+"_L_"+Integer.toString(ip)+"_C_"+"_0_LR";
            	if( (is == 3 || is == 4) && id!=4 ) {
            		H2_a_Hist.add(is, ip, 0, new H2F("fadc_gm_tof_"+iid,		100,0.,30000,	200,7.,27.));
	            	//  ToF - Ref vs GM: x-axis is GM of sqrt(L*R) ADC and y-axis is (L+R)/2. - RF time using 
	        		//	times from TDC
            		H2_t_Hist.add(is, ip, 0, new H2F("tdc_gm_tof_"+iid,		100,0.,30000,	200,7.,27.));
            	}
            	else {
	            	H2_a_Hist.add(is, ip, 0, new H2F("fadc_gm_tof_"+iid,		100,0.,30000,	200,-5.,15.));
		            	//  ToF - Ref vs GM: x-axis is GM of sqrt(L*R) ADC and y-axis is (L+R)/2. - RF time using 
		        		//	times from TDC
	            	H2_t_Hist.add(is, ip, 0, new H2F("tdc_gm_tof_"+iid,		100,0.,30000,	200,-5.,15.));
            	}
            	
            	
            }
          
            // Loop over L and R side of a bar, because I no longer
            // need to use information from both L & R side
            for (int lr=1 ; lr<3 ; lr++){
            	iid = Integer.toString(is)+"_S_"+Integer.toString(id)+"_L_"+"_all_C_"+Integer.toString(lr)+"_LR";

	            	// ADC plot that throws out overflow: x-axis is ADC spectrum, not including any overflow events
            		// and y-axis identifies which bar in the sector -- only L side and only R side separately
                H2_a_Hist.add(is, 0, 1+lr, new H2F("fadc_adc_"+iid,			200,0.,amax[id],		nstr[is-1], 1., nend));
             	
                	// ADC plot that throws out overflow: x-axis is ADC spectrum, not including any overflow events
        			// and y-axis identifies which bar in the sector -- only L side and only R side separately
                H2_a_Hist.add(is, 0, 3+lr, new H2F("fadc_adcOver_"+iid,		200,0.,amax[id],		nstr[is-1], 1., nend));
                
                // Loop over all the bars for the L side and R side individually:	
                for( int ip = 1 ; ip<nstr[is-1]+1; ip++) {
                		// FADC-TDC diff plot: x-axis is the ADC of the PMT and y-axis is the FADC-TDC time difference 

                	H2_at_Hist.add(is, ip, lr-1, new H2F("fadc_tdc_diff_"+iid,	100,0.,30000,	400,-190,-30));
                	H1_a_Hist.add(is, 0, ip   , new H1F("a_ln_R",100,-1,1));

                }
                
                // Histograms not used by calib suite, but for monitoring software
                	// First two are raw ADC,TDC for coloring in the bandMon
                H2_t_Hist.add(is, 0, lr, new H2F("t_raw_"+iid+0,      100,   0, tmax[id], nstr[is-1], 1., nend));
                H2_a_Hist.add(is, 0, 5+lr, new H2F("a_raw_"+iid+1,      100,   0., amax[id], nstr[is-1], 1., nend));
                H2_a_Hist.add(is, 0, 7+lr, new H2F("a_ped_"+iid+3,      1000, -500.,  500., nstr[is-1], 1., nend)); 
                H2_a_Hist.add(is, 0, 9+lr, new H2F("a_fadc_"+iid+5,     1000,   0., 1000., nstr[is-1], 1., nend));
                H2_a_Hist.add(is, 0, 11+lr, new H2F("a_fadc_"+iid+6,     100, -20.,  20., nstr[is-1], 1., nend));
                H1_a_Sevd.add(is, lr, 0, new H1F("a_sed_"+iid+0,                       nstr[is-1], 1., nend));
                H2_a_Sevd.add(is, lr, 0, new H2F("a_sed_fadc_"+iid+0, 100,   0., 100., nstr[is-1], 1., nend));
                H2_a_Sevd.add(is, lr, 1, new H2F("a_sed_fadc_"+iid+1, 100,   0., 100., nstr[is-1], 1., nend));
                H2_t_Sevd.add(is, lr, 0, new H2F("a_sed_fadc_"+iid+0, 200,   0., 100., nstr[is-1], 1., nend));
                iid="s"+Integer.toString(is)+"_l"+Integer.toString(lr);
                
            }
        }       

        if(!hipoFile.equals(" ")){
            FCCalibrationData calib = new FCCalibrationData();
            calib.getFile(hipoFile);
            H2_a_Hist = calib.getCollection("H2_a_Hist");
            H2_t_Hist = calib.getCollection("H2_t_Hist");
            H2_t_Hist = calib.getCollection("H2_x_Hist");          
        }         
        
        strips.addH1DMap("H1_a_Sevd",  H1_a_Sevd);
        strips.addH1DMap("H1_a_Hist",  H1_a_Hist);
        strips.addH1DMap("H1_t_Sevd",  H1_t_Sevd);
        strips.addH2DMap("H2_a_Hist",  H2_a_Hist);
        strips.addH2DMap("H2_t_Hist",  H2_t_Hist);
        strips.addH2DMap("H2_a_Sevd",  H2_a_Sevd);
        strips.addH2DMap("H2_t_Sevd",  H2_t_Sevd);
        strips.addH2DMap("H2_at_Hist",  H2_at_Hist);
        
    } 
    
}
