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
import org.jlab.groot.fitter.DataFitter;

public class BANDCalib_HV extends FCApplication implements CalibrationConstantsListener,ChangeListener{

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

	public DetectorCollection<F1D> adcFitL = new DetectorCollection<F1D>();
	public DetectorCollection<F1D> adcFitR = new DetectorCollection<F1D>();
	
	double sigma_modifier = 2; // Multiplicative factor for increasing sigma value if fitting on overflow events
    double overflow_ratio = 0.8; // If ratio of normal histogram events to overflow events is smaller than this, fit overflow
    int runnumber = 0;
    double fitscale = 1;
    double x_fit_range = fitscale*BANDPixels.adcMax;//This sets the fitting range based off the x axis range


	public BANDCalib_HV(String name, BANDPixels[] bandPix) {
		super(name,bandPix);    
	}

	public void init(int is1, int is2) {

		System.out.println("\tInitializing HV Calibration");

		this.is1=is1;
		this.is2=is2;

		
		calib = new CalibrationConstants(3,
				"Left_Mean/F:Left_Sigma/F:Right_Mean/F:Right_Sigma/F");
		calib.setName("/calibration/band/gain_balance");
		calib.setPrecision(3);

		/* for (int i=0; i<3; i++) {
		   int layer = i+1;
		//calib.addConstraint(3, EXPECTED_MIP_CHANNEL[i]-ALLOWED_MIP_DIFF, 
		//                       EXPECTED_MIP_CHANNEL[i]+ALLOWED_MIP_DIFF, 1, layer);
		// calib.addConstraint(calibration column, min value, max value,
		// col to check if constraint should apply, value of col if constraint should be applied);
		// (omit last two if applying to all rows)
		//calib.addConstraint(4, EXPECTED_MIP_CHANNEL[i]-ALLOWED_MIP_DIFF, 
		//                       EXPECTED_MIP_CHANNEL[i]+ALLOWED_MIP_DIFF, 1, layer);
		}*/

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
			
			file = new File(String.format("../band_analysis/hvScan/calibOutput/run_%d-adcFit.txt",runno));
			// Try to open a text file, otherwise do not try to analyze
			try(PrintWriter output = new PrintWriter(file)) {
				output.println("#Sector\tLayer\tComponent\tOrder\tMean\tSigma\n");
	
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
	
							// Fit both sides of paddle
							for( int lr = 1 ; lr < 3 ; lr++) {
								//int x_fit_range = 7000;
								fit(layer, sector, paddle, lr,x_fit_range,output);//x_fit_range);
							}
							//System.out.println("Done with Layer "+ lidx + ", Sector "+ sector + " , Component " + pidx);
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



    public void fit(int layer, int sector, int paddle, int lr, double x_fit_max, PrintWriter FILE){ 
        
        runnumber++;
        int lidx = (layer+1);
        int pidx = (paddle+1);
        
        H1F h = null;
        double sigma_scaler = 1;
        H1F reg = bandPix[layer].strips.hmap2.get("H2_a_Hist").get(sector,0,1+lr).sliceY(paddle);
        
        H1F over = bandPix[layer].strips.hmap2.get("H2_a_Hist").get(sector,0,3+lr).sliceY(paddle);
        
        //System.out.println("Ratio for " + layer + " " + sector + " " + paddle + " is"+reg.getIntegral()/over.getIntegral());
        
        if (reg.getIntegral()/over.getIntegral() < overflow_ratio) {
     	   sigma_scaler = sigma_modifier;
     	   //System.out.println("A lot of overflow events in S.L.P.lr = " + sector + " " + layer + " " + paddle +  " " + lr+ ", fitting overflow" );
     	   h = over;
        }
        else {
     	   h = reg;
        }
        
        if( h.getIntegral() < 1000) {
     	   //System.out.println("Integral value is " + h.getIntegral() + " so setting " + layer + " " + sector + " " + paddle + " to null");
     	   // Layer runs from 0 to 5 (5 is veto layer) while sector runs from 1 - 5 (instead of 0 - 4)
     	   F1D f1 = null;
     	   if( lr == 1) adcFitL.add(layer, sector,paddle, f1);
     	   if( lr == 2) adcFitR.add(layer, sector,paddle, f1);
     	  FILE.println(String.format("%d\t%d\t%d\t%d\t%f\t%f",sector,layer+1,pidx,lr-1,-1.,-1.));
     	   return;
        	};
        
        //F1D  f1 = new F1D("f1","[amp]*gaus(x,[mean],[sigma])", 0,40000);

        	//***************************************************************************************************************
        	//NOTE: The parameters below are chosen essentially randomly. They seem to work but are not optimized. They should be optimized. 
         //*****************************************************************************************************************
        	//System.out.println(x_fit_max);
        	
        	double fit_amp = h.getMax()*1;
        	
        	if (reg.getIntegral()/over.getIntegral() < overflow_ratio) {fit_amp = h.getMax()*0.3;} //Overflow peak is usually smaller than global peak
        	
        	F1D f1 = new F1D("f1", "[amp]*landau(x,[mean],[sigma]) +[exp_amp]*exp([p]*x)", 500, x_fit_max);
         f1.setParameter(0, fit_amp);
         f1.setParameter(1, h.getMean() );
         f1.setParameter(2, h.getRMS()*0.5 );
         f1.setParameter(3, fit_amp*0.5 );
         f1.setParameter(4, -0.002);
         DataFitter.fit(f1, h, "REQ");
         
         double amp = h.getFunction().getParameter(0);
         double mean = h.getFunction().getParameter(1);
         double sigma = h.getFunction().getParameter(2)*sigma_scaler;
         double exp_amp = h.getFunction().getParameter(3);
         double exp_const = h.getFunction().getParameter(4);
         //if (reg.getIntegral()/over.getIntegral() < overflow_ratio) {h.getFunction().show();}
         
         if (sigma > 2*h.getRMS()) {
         	System.out.println("Large sigma value at S.L.P.lr = " + sector + " " + layer + " " + paddle +  " " + lr);
         	h.getFunction().show();
         	}
        	
         
        	/*
        	F1D  f1 = new F1D("f1","[amp]*landau(x,[mean],[sigma])+[const]",25000,x_fit_max);
        	f1.setParameter(0, h.getMax() );
        	f1.setParameter(1, h.getMean() );
        	f1.setParameter(2, h.getRMS()*0.5 );
        	f1.setParameter(3, 20 );
        	DataFitter.fit(f1, h, "REQ");
        	h.getFunction().show();
         */
         
         
         
         
 		//gmFunc.setParameter(0, maxCounts*0.8);
 		//gmFunc.setParLimits(0, maxCounts*0.5, maxCounts*1.2);
 		//gmFunc.setParameter(1, maxPos);
 		//gmFunc.setParameter(2, 200.0);
 		//gmFunc.setParLimits(2, 0.0,400.0);
 		//gmFunc.setParameter(3, maxCounts*0.5);
 		//gmFunc.setParameter(4, -0.001);
 		/*
        	F1D  f2 = new F1D("f2","[amp]*landau(x,[mean],[sigma])+[const]",500,x_fit_max);
        	f2.setParameter(0, h.getMax() );
        	f2.setParameter(1, h.getMean() );
        	f2.setParameter(2, 1000 );
        	f2.setParameter(3, 20 );
        	DataFitter.fit(f2, h, "REQ");
        	h.getFunction().show();
		   
			*/
		   

        
        if( amp < 0 || sigma < 0 ) {
     	   System.out.println("Fit failed at S.L.P.lr = " + sector + " " + layer + " " + paddle +  " " + lr);
     	   System.out.println("Failed fit params = " + amp + " " + mean + " " + sigma +  " " + exp_amp+  " " + exp_const);
     	   if( lr == 1) adcFitL.add(layer, sector,paddle, null);
     	   if( lr == 2) adcFitR.add(layer, sector,paddle, null);
     	  FILE.println(String.format("%d\t%d\t%d\t%d\t%f\t%f",sector,layer+1,pidx,lr-1,-1.,-1.));
     	   return; 
        }
        
       
        
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
        
        
        FILE.println(String.format("%d\t%d\t%d\t%d\t%f\t%f",sector,layer+1,pidx,lr-1,mean,sigma));
     }

 
 

 public void updateCanvas(DetectorDescriptor dd) {
     
     this.getDetIndices(dd); 
             
     int lr = dd.getOrder()+1;
     int sector = dd.getSector();
     int component = dd.getComponent();   
     int layer = ilmap;
     //int ilm = ilmap;
            
     int nstr = bandPix[layer].nstr[sector-1];
     int min=0, max=nstr;
     
     c.clear(); c.divide(1,2);
     c.setAxisFontSize(12);

//   canvas.setAxisTitleFontSize(12);
//   canvas.setTitleFontSize(14);
//   canvas.setStatBoxFontSize(10);
     
          
     
     H1F h;
     String alab;
     String otab[]={" L PMT "," R PMT "};
     String calTitles[]={" ADC"," TDC"};
     //String lab4[]={" ADC"," TDC"," OVERFLOW"}; 
     String tit = "SEC "+sector+" LAY "+(layer+1);
     //String tit = "SEC "+is+" LAY "+(ilm+1);
     int bothFired = 0;


     // We will loop here for all the calibration plots we want to make for
     // selected pmt
     /*for(int iip=min;iip<max;iip++) {
         
     	alab = tit+otab[lr-1]+(iip+1)+calTitles[0];
         */
     	alab = tit+otab[0]+(component+1)+calTitles[0];
         c.cd(0);          
         
         // Draw one including overflow samples
         h = bandPix[layer].strips.hmap2.get("H2_a_Hist").get(sector,0,4).sliceY(component);
         h.setOptStat(Integer.parseInt("1000100")); 
         h.setTitleX(alab); h.setTitle(""); h.setTitleY("Entries"); h.setFillColor(34); c.draw(h);
         
         // Draw one without overflow samples
         h = bandPix[layer].strips.hmap2.get("H2_a_Hist").get(sector,0,2).sliceY(component);
         h.setOptStat(Integer.parseInt("1000100"));
         h.setTitleX(alab); h.setTitle(""); h.setTitleY("Entries"); h.setFillColor(32); c.draw(h,"same");
          
										// 32 is the color red
     c.cd(1);
     alab = tit+otab[1]+(component+1)+calTitles[0];  
     //Plot right overflow
     h = bandPix[layer].strips.hmap2.get("H2_a_Hist").get(sector,0,5).sliceY(component);
     h.setOptStat(Integer.parseInt("1000100")); 
     h.setTitleX(alab); h.setTitle(""); h.setTitleY("Entries"); h.setFillColor(34); c.draw(h);
     //Plot right histogram													// 34 is the color light blue
     h = bandPix[layer].strips.hmap2.get("H2_a_Hist").get(sector,0,3).sliceY(component);
     h.setOptStat(Integer.parseInt("1000100")); 
     h.setTitleX(alab); h.setTitle(""); h.setTitleY("Entries"); h.setFillColor(32); c.draw(h,"same");
         																	// 32 is the color red
/*****************************************************************************
* Empty comment.  
******************************************************************************/
         
     c.cd(0);    
     F1D f1 = adcFitL.get(layer,is,component);
     F1D f2 = adcFitR.get(layer,is,component);
     	
         
     if( f1 != null && f2 != null) {
	        if( lr == 1) {
	           f1.setLineColor(2);
	           f2.setLineColor(3);	
	        }
	        if( lr == 2) {
	            f1.setLineColor(3);
	            f2.setLineColor(2);
	            }
	        f1.setLineWidth(2);
	        f2.setLineWidth(2);
         c.draw(f1,"same");
         c.draw(f2,"same");
     	bothFired = 1;
     }
     else if( lr == 1 && f1 != null) {
         f1.setLineColor(4);
         f1.setLineWidth(2);
         c.draw(f1,"same");
     }
     else if( lr == 2 && f2 != null) {
         f2.setLineColor(5);
         f2.setLineWidth(2);
        	c.draw(f2,"same");
     }

         c.cd(1);   
         F1D f3 = adcFitL.get(layer,is,component);
         F1D f4 = adcFitR.get(layer,is,component);
     	
         
         if( f3 != null && f4 != null) {
	            if( lr == 1) {
	            	f3.setLineColor(2);
	            	f4.setLineColor(3);	
	            }
	            if( lr == 2) {
	            	f3.setLineColor(3);
	            	f4.setLineColor(2);
	            }
	            f3.setLineWidth(2);
	            f4.setLineWidth(2);
         	c.draw(f3,"same");
         	c.draw(f4,"same");
     		bothFired = 1;

         }
         else if( lr == 1 && f3 != null) {
         	f1.setLineColor(2);
         	f1.setLineWidth(2);
         	c.draw(f3,"same");
         }
         else if( lr == 2 && f4 != null) {
         	f4.setLineColor(2);
         	f4.setLineWidth(2);
         	c.draw(f4,"same");
         }
            
         
         
     /*
     if( bothFired == 1) {
	        // For L-R time plot
	        canvas.cd(1);
	        H1F h2 = bandPix[layer].strips.hmap2.get("H2_t_Hist").get(is,0,0).sliceY(component);   
	        //H1F h2 = bandPix[layer].strips.hmap2.get("H2_t_Hist").get(is,component+1,2).projectionX();
		    h2.setTitleX(tit+" BAR "+(component+1)+" TL-TR (ns)"); h2.setTitleY("Entries"); h2.setFillColor(32);
		    canvas.draw(h2);            
	
		    // For L-R time plot with FADC time
		    canvas.cd(2);
		    h2 = bandPix[layer].strips.hmap2.get("H2_a_Hist").get(is,0,1).sliceY(component);   
		    h2.setTitleX(tit+" BAR "+(component+1)+" TL-TR (ns)"); h2.setTitleY("Entries"); h2.setFillColor(32);
		    canvas.draw(h2);  
		    
		    //  For L+R time plot iwht FADC time
		    canvas.cd(3);
		    H2F h3 = bandPix[layer].strips.hmap2.get("H2_a_Hist").get(sector, component, 8);
		    h3.setTitleX(tit+" BAR "+(component+1)+" TL+TR (ns) vs FADC Mean"); h3.setTitleY("TL+TR");
		    canvas.draw(h3); 
		    
		    // Add projection of above
		    canvas.cd(4);
		    h2 = h3.projectionY();
		    h2.setTitleX(tit+" BAR "+(component+1)+" TL+TR-TREF (ns)"); h2.setTitleY("Entries"); h2.setFillColor(32);
		    canvas.draw(h2);
     }
     */

         
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

