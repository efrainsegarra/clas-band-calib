package org.clas.fcmon.band;

import static java.lang.System.out;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.clas.fcmon.tools.FADCFitter;
import org.clas.fcmon.tools.FCApplication;

import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;

import org.jlab.detector.base.DetectorCollection;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.decode.DetectorDataDgtz;
import org.jlab.utils.groups.IndexedList;
import org.jlab.utils.groups.IndexedList.IndexGenerator;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
//import org.jlab.rec.tof.banks.BaseHit;
//import org.jlab.rec.tof.banks.BaseHitReader;
//import org.jlab.rec.tof.banks.BaseHitReader.DetectorLocation;


public class BANDReconstructionApp extends FCApplication {

	FADCFitter     fitter  = new FADCFitter(1,15);
	String          mondet = null;

	String        BankType = null;
	int              detID = 0;
	

	Boolean stop = true;

	List<DetectorDataDgtz>        dataList = new ArrayList<DetectorDataDgtz>();
	
	IndexedList<List<Float>>          tdc_time = new IndexedList<List<Float>>(4);
	IndexedList<List<Float>>          fadc_int = new IndexedList<List<Float>>(4);
	IndexedList<List<Double>>          fadc_time = new IndexedList<List<Double>>(4);
	IndexedList<List<Float>>          overflow = new IndexedList<List<Float>>(4);
	IndexedList<List<Float>>          fadc_height = new IndexedList<List<Float>>(4);
	
	IndexedList<List<Integer>>       lapmt = new IndexedList<List<Integer>>(3); 
	IndexedList<List<Integer>>       ltpmt = new IndexedList<List<Integer>>(3); 

	BANDConstants                   bandcc = new BANDConstants();  

	double[]                sed7=null,sed8=null;
	TreeMap<Integer,Object> map7=null,map8=null; 

	int nsa,nsb,tet,pedref;     
	int     thrcc = 20;
	short[] pulse = new short[100]; 
	public int[] nstr = new int[bandcc.bandlen.length];


	public BANDReconstructionApp(String name, BANDPixels[] bandPix) {
		super(name, bandPix);
	}

	public void init() {
		System.out.println("BANDReconstruction.init()");
		mondet = (String) mon.getGlob().get("mondet");
		is1 = BANDConstants.IS1;
		is2 = BANDConstants.IS2;
		iis1 = BANDConstants.IS1-1;
		iis2 = BANDConstants.IS2-1;
	} 

	public void clearHistograms() {

		for (int idet=0; idet<bandPix.length; idet++) {
			for (int is=is1 ; is<is2 ; is++) {
				
				bandPix[idet].strips.hmap2.get("H2_a_Hist").get(is,0,0).reset();
				bandPix[idet].strips.hmap2.get("H2_a_Hist").get(is,0,1).reset();
				bandPix[idet].strips.hmap2.get("H2_a_Hist").get(is,0,14).reset();
				
	        	nstr[is]=bandcc.bandlay[idet][is];
	            for( int ip = 1 ; ip<nstr[is-1]+1; ip++) {
	            	bandPix[idet].strips.hmap2.get("H2_a_Hist").get(is,ip,0).reset();
	            	bandPix[idet].strips.hmap2.get("H2_t_Hist").get(is,ip,0).reset();
	            	
	            	bandPix[idet].strips.hmap2.get("H2_at_Hist").get(is,ip,0).reset();
	            	bandPix[idet].strips.hmap2.get("H2_at_Hist").get(is,ip,1).reset();
	            }
	            
				bandPix[idet].strips.hmap2.get("H2_t_Hist").get(is,0,0).reset();
				bandPix[idet].strips.hmap2.get("H2_t_Hist").get(is,0,1).reset();
				bandPix[idet].strips.hmap2.get("H2_t_Hist").get(is,0,2).reset();
				for (int lr=1 ; lr<3 ; lr++) {
					bandPix[idet].strips.hmap2.get("H2_a_Hist").get(is,0,1+lr).reset();
					bandPix[idet].strips.hmap2.get("H2_a_Hist").get(is,0,3+lr).reset();
					bandPix[idet].strips.hmap2.get("H2_a_Hist").get(is,0,5+lr).reset();
					bandPix[idet].strips.hmap2.get("H2_a_Hist").get(is,0,7+lr).reset();
					bandPix[idet].strips.hmap2.get("H2_a_Hist").get(is,0,9+lr).reset();
					bandPix[idet].strips.hmap2.get("H2_a_Hist").get(is,0,11+lr).reset();	
				}
			}       
		} 
	}

	public void getMode7(int cr, int sl, int ch) {    
		app.mode7Emulation.configMode7(cr,sl,ch);
		this.nsa    = app.mode7Emulation.nsa;
		this.nsb    = app.mode7Emulation.nsb;
		this.tet    = app.mode7Emulation.tet;
		this.pedref = app.mode7Emulation.pedref;
	}

	public void getHits(DataEvent event) {

		//      // BaseHitReader hitReader = new BaseHitReader();
		//       
		//    //   Map<DetectorLocation, ArrayList<BaseHit>> hitMap = hitReader.get_Hits(event, "BAND");
		//       
		//       System.out.println(" ");
		//       System.out.println("New Event Size "+hitMap.size());
		//       
		//       if (hitMap != null) {
		//
		//           Set entrySet = hitMap.entrySet();
		//           Iterator it = entrySet.iterator();
		//
		//           while (it.hasNext()) {
		//               Map.Entry me = (Map.Entry) it.next();
		//               ArrayList<BaseHit> hitList = (ArrayList<BaseHit>) me.getValue();
		//               
		//               List<ArrayList<BaseHit>> hitlists = new ArrayList<ArrayList<BaseHit>>();  
		//               Collections.sort(hitList);
		//              
		//   			  for(BaseHit h : hitList)
		//   				System.out.println("Sector "+h.get_Sector()+
		//   						           " Layer "+h.get_Layer()+
		//   						             " PMT "+h.get_Component()+
		//   						            " ADC1 "+h.ADC1+
		//   						            " ADC2 "+h.ADC2+
		//   						            " TDC1 "+h.TDC1+
		//   						            " TDC2 "+h.TDC2+
		//   						           " ADCi1 "+h.ADCbankHitIdx1+
		//   						           " ADCi2 "+h.ADCbankHitIdx2+
		//   						           " TDCi1 "+h.TDCbankHitIdx1+
		//   						           " TDCi2 "+h.TDCbankHitIdx2);
		//   						           
		//               for (int i = 0; i < hitList.size(); i++) {
		//                   hitlists.add(new ArrayList<BaseHit>());
		//               }
		//
		//           }
		//       }	   	   
	}

	public void updateHipoData(DataEvent event) {

		float      tps =  (float) 0.02345;
		float     tdcd = 0;

		clear(0); clear(1); clear(2); clear(3); clear(4); clear(5); ltpmt.clear() ; lapmt.clear();
		tdc_time.clear();
		fadc_int.clear();
		fadc_height.clear();
		fadc_time.clear();
		overflow.clear();


		if(event.hasBank("BAND::tdc")){
			DataBank  bank = event.getBank("BAND::tdc");
			int rows = bank.rows();           
			for(int i = 0; i < rows; i++){
				int  is = bank.getByte("sector",i);
				int  il = bank.getByte("layer",i);
				int  lr = bank.getByte("order",i);                       
				int  ip = bank.getShort("component",i);
				tdcd = bank.getInt("TDC",i)*tps-app.tdcOffset;  

				
				if(isGoodSector(is)&&tdcd>0) {
					if(!tdc_time.hasItem(is,il,lr,ip)) tdc_time.add(new ArrayList<Float>(),is,il,lr,ip);
					tdc_time.getItem(is,il,lr,ip).add(tdcd); 
					if (!ltpmt.hasItem(is,il,ip)) {
						ltpmt.add(new ArrayList<Integer>(),is,il,ip);
						ltpmt.getItem(is,il,ip).add(ip);
					} 
				}
			}
		}

		if(event.hasBank("BAND::adc")){
			DataBank  bank = event.getBank("BAND::adc");
			int rows = bank.rows();
			for(int i = 0; i < rows; i++){
				int  is = bank.getByte("sector",i);
				int  il = bank.getByte("layer",i);
				int  lr = bank.getByte("order",i);
				int  ip = bank.getShort("component",i);
				int adc = bank.getInt("ADC",i);
				double t = bank.getFloat("time",i);               
				int ped = bank.getShort("ped", i);

				if(isGoodSector(is)) {

					
					if(adc>0) {
						if(!fadc_int.hasItem(is,il,lr,ip)) fadc_int.add(new ArrayList<Float>(),is,il,lr,ip);
						fadc_int.getItem(is,il,lr,ip).add((float) adc); 
						if(!fadc_time.hasItem(is,il,lr,ip)) fadc_time.add(new ArrayList<Double>(),is,il,lr,ip);
						fadc_time.getItem(is,il,lr,ip).add((double) t); 
						//if(!fadc_height.hasItem(is,il,lr,ip)) fadc_height.add(new ArrayList<Float>(),is,il,lr,ip);
						//fadc_height.getItem(is,il,lr,ip).add((float) ap); 
						if (!lapmt.hasItem(is,il,ip)) {
							lapmt.add(new ArrayList<Integer>(),is,il,ip);
							lapmt.getItem(is,il,ip).add(ip);              
						}
					}

					Float[] tdcc; float[] tdc;



					
					if (tdc_time.hasItem(is,il,lr,ip)) {
						List<Float> list = new ArrayList<Float>();
						list = tdc_time.getItem(is,il,lr,ip); tdcc=new Float[list.size()]; list.toArray(tdcc);
						tdc = new float[list.size()];
						for (int ii=0; ii<tdcc.length; ii++) {
							tdc[ii] = tdcc[ii]-app.phaseCorrection*4;  
							double tdif = (double) tdc[ii] -BANDConstants.TOFFSET[lr]-t;
							//bandPix[il-1].strips.hmap2.get("H2_a_Hist").get(is,lr+1,6).fill(tdif,ip);
						}
					} else {
						tdc = new float[1];
					}

					for (int ii=0 ; ii< 100 ; ii++) {
						float wgt = (ii==(int)(t/4)) ? adc:0;
						//bandPix[il-1].strips.hmap2.get("H2_a_Hist").get(is,lr+1,5).fill(ii,ip,wgt);
						if (app.isSingleEvent()) {
							//bandPix[il-1].strips.hmap2.get("H2_a_Sevd").get(is,lr+1,0).fill(ii,ip,wgt);
						}
					}

					if (app.rtt.hasItem(is,il,ip,lr)) {
						int[] dum = (int[]) app.rtt.getItem(is,il,ip,lr);
						getMode7(dum[0],dum[1],dum[2]);
					}

					//if (ped>0) bandPix[il-1].strips.hmap2.get("H2_a_Hist").get(is,lr+1,3).fill(this.pedref-ped, ip);  

					//fill(il-1, is, lr+1, ip, adc, tdc, t, (float) adc);    

				} //isGoodSector?
			}
		}
		if (app.isHipoFileOpen) app.writer.writeEvent(event);       

	}  
	
	//****************************** EVIO DATA UPDATING *********************************************

	public void updateEvioData(DataEvent event) {

		//float      tps =  (float) 0.025;//0.02345;
		//float	   tps =  (float) 0.02345;
		final float tps = 0.02345f;
		float     tdcd = 0;

		tdc_time.clear();
		fadc_int.clear();
		fadc_height.clear();
		fadc_time.clear();
		overflow.clear();
		
		clear(0); clear(1); clear(2); clear(3); clear(4); clear(5);
		ltpmt.clear() ; lapmt.clear();

		List<DetectorDataDgtz> adcDGTZ = app.decoder.getEntriesADC(DetectorType.BAND);
		List<DetectorDataDgtz> tdcDGTZ = app.decoder.getEntriesTDC(DetectorType.BAND);

		// For all the TDC entries in our event
		for (int i=0; i < tdcDGTZ.size(); i++) {
			DetectorDataDgtz ddd=tdcDGTZ.get(i);
			int is = ddd.getDescriptor().getSector(); // Sector goes from 1-5
			int il = ddd.getDescriptor().getLayer();  // Layer goes from 1-6
			int lr = ddd.getDescriptor().getOrder();  // LR for TDC is 2,3
			int ip = ddd.getDescriptor().getComponent();   // Paddle starts at 1
			
			// Take TDC channel, convert to nanosecond, and subtract offset.
			// This offset is arb. defined by looking at data, because our 
			// 1190 TDC offset is far too large, and our real data starts ~1.2microseconds
			// after our hardware offset.
			tdcd   = ddd.getTDCData(0).getTime()*tps;
			System.out.println("TDC Info: "+tdcd+" SLCO: "+is+" "+il+" "+ip+" "+lr);
			
			// Make sure that tdc time is > 0 -- < 0 is non-physical
			// since app.tdcOffset is less than our real TDC offset
			if(tdcd>0) {

					// Search in an array if we already have a TDC entry for a certain
					// sector,layer,component,LR, and if we don't, create a new array
					// and either way, add the current TDC time to the array.
				if(!tdc_time.hasItem(is,il,lr-2,ip)) tdc_time.add(new ArrayList<Float>(),is,il,lr-2,ip);
				tdc_time.getItem(is,il,lr-2,ip).add(tdcd);    
				System.out.println("\tTDC info saved: "+tdc_time.getItem(is,il,lr-2,ip).get(0));

					// Add unique paddles that fired TDC
				if (!ltpmt.hasItem(is,il,ip)) {
					ltpmt.add(new ArrayList<Integer>(),is,il,ip);
					ltpmt.getItem(is,il,ip).add(ip);
				}

			}           
		}

		for (int i=0; i < adcDGTZ.size(); i++) {
			DetectorDataDgtz ddd=adcDGTZ.get(i);
			int is = ddd.getDescriptor().getSector(); // Sector goes from 1-5
			if (isGoodSector(is)) {
				int cr = ddd.getDescriptor().getCrate();
				int sl = ddd.getDescriptor().getSlot();
				int ch = ddd.getDescriptor().getChannel();
				int il = ddd.getDescriptor().getLayer(); // Layer goes from 1-6
				int lr = ddd.getDescriptor().getOrder(); // ADC LR goes from 0,1
				int ip = ddd.getDescriptor().getComponent(); // Component starts from 1
				int ad = ddd.getADCData(0).getADC();
				int pd = ddd.getADCData(0).getPedestal();
				int t0 = ddd.getADCData(0).getTimeCourse();
				double tf = (double) ddd.getADCData(0).getTime();
				float ph = (float) ddd.getADCData(0).getHeight()-pd;
				short[]    pulse = ddd.getADCData(0).getPulseArray();

				System.out.println("ADC Info: "+ad+"/"+tf+" SLCO: "+is+" "+il+" "+ip+" "+lr);
				
					// Check for overflow of PMT digitized pulse, which
					// caps at 4095. If there is overflow, fill a different
					// histogram
				  int isOver = 0;
				  for (int ii=0 ; ii< pulse.length ; ii++) {
					  if( pulse[ii] >= 4095) {
						  isOver = 1;
						  break;
					  }
				  }
				  
				  // Keep track whether or not this ADC value was in overflow for this PMT
				  if (!overflow.hasItem(is,il,lr,ip))overflow.add(new ArrayList<Float>(),is,il,lr,ip);
				  overflow.getItem(is,il,lr,ip).add((float)isOver); 
				  

				// Add ADCs based on unique ID
				if (!fadc_int.hasItem(is,il,lr,ip))fadc_int.add(new ArrayList<Float>(),is,il,lr,ip);
				fadc_int.getItem(is,il,lr,ip).add((float)ad); 
				System.out.println("\tFADC info saved: "+fadc_int.getItem(is,il,lr,ip).get(0));
				
				if (!fadc_height.hasItem(is,il,lr,ip))fadc_height.add(new ArrayList<Float>(),is,il,lr,ip);
				fadc_height.getItem(is,il,lr,ip).add((float)ph); 

				// Add FADC time based on unique ID
				if (!fadc_time.hasItem(is,il,lr,ip))fadc_time.add(new ArrayList<Double>(),is,il,lr,ip);
				fadc_time.getItem(is,il,lr,ip).add((double)tf);
				System.out.println("\tFADC info saved: "+fadc_time.getItem(is,il,lr,ip).get(0));

				// Add unique paddles that fired ADC
				if (!lapmt.hasItem(is,il,ip)) {
					lapmt.add(new ArrayList<Integer>(),is,il,ip);
					lapmt.getItem(is,il,ip).add(ip);
				}

				
				Float[] tdcc; float[] tdc;
				// If we have a corresponding entry in TDC list:
				if (tdc_time.hasItem(is,il,lr,ip)) {
					List<Float> list = new ArrayList<Float>();
					list = tdc_time.getItem(is,il,lr,ip); 	// For each PMT, get multi TDC hits
					tdcc=new Float[list.size()]; 
					list.toArray(tdcc);
					tdc  = new float[list.size()];
					for (int ii=0; ii<tdcc.length; ii++) {
						// Correct each TDC by phase correction to compare to FADC time 
						tdc[ii] = tdcc[ii]-app.phaseCorrection*4;  
						double tdif = ( (double) tdc[ii] ) -BANDConstants.TOFFSET[lr]-tf;
						// FOR EACH TDC HIT, fill fadc-tdc time.
						// This means if there are multiple ADCs, we do the 
						// filling twice... But, quoting Florian, we shouldn't have
						// this happening...
						//bandPix[il-1].strips.hmap2.get("H2_a_Hist").get(is,0,11+lr+1).fill(tdif,ip);
					}
				} else {
					tdc = new float[1];
				}
				

				// If we manually set the tet, pedestal, nsa, etc..
				getMode7(cr,sl,ch);            
				int ped = app.mode7Emulation.User_pedref==1 ? this.pedref:pd;

				// Loop through the waveform and fill a waveform histogram and
				// fill single event histogram
				for (int ii=0 ; ii< pulse.length ; ii++) {
						// We can fill this if there are multiple waveforms per PMT
					bandPix[il-1].strips.hmap2.get("H2_a_Hist").get(is,0,9+lr+1).fill(ii,ip,pulse[ii]-ped);
					if (app.isSingleEvent()) {
						bandPix[il-1].strips.hmap2.get("H2_a_Sevd").get(is,lr+1,0).fill(ii,ip,pulse[ii]-ped);
						int w1 = t0-this.nsb;
						int w2 = t0+this.nsa;
						if (ad>0&&ii>=w1&&ii<=w2) bandPix[il-1].strips.hmap2.get("H2_a_Sevd").get(is,lr+1,1).fill(ii,ip,pulse[ii]-ped);                     
					}
				}
				// Fill a pedestal histogram  -- we can fill this if there are mulitple waveforms per PMT
				if (pd>0) bandPix[il-1].strips.hmap2.get("H2_a_Hist").get(is,0,7+lr+1).fill(this.pedref-pd, ip);

				// Fill a bunch of histograms
				fill(il-1, is, lr+1, ip, ad, tdc, (float) tf, ph);
					//  layer is by definition 1-6, so need to subtract 1 for indexing
					//  lr is right now 0, and I expect it to be 1,2, which is why I add 1 here
					//  ip is bar number
					//  ad is the FADC integral
				    //  tdc is array of all tdc's in the event
					//  tf is the FADC time
				    //  ph is the FADC peak h

			}       
		}

		if (app.isHipoFileOpen) writeHipoOutput();

	}

	public void writeHipoOutput() {

		DataEvent  decodedEvent = app.decoder.getDataEvent();
		DataBank   header = app.decoder.createHeaderBank(decodedEvent,0,0,0,0);
		decodedEvent.appendBanks(header);
		app.writer.writeEvent(decodedEvent);

	} 

	public void updateSimulatedData(DataEvent event) {

		float tdcmax=100000;
		int adc, tdcc, fac;
		float mc_t=0,tdcf=0;
		float[] tdc = new float[1];

		String det[] = {"BAND"}; // FTOF.xml banknames

		clear(0); clear(1); clear(2);

		for (int idet=0; idet<det.length ; idet++) {

			if(event.hasBank(det[idet]+"::true")==true) {
				EvioDataBank bank  = (EvioDataBank) event.getBank(det[idet]+"::true"); 
				for(int i=0; i < bank.rows(); i++) mc_t = (float) bank.getDouble("avgT",i);          
			}

			if(event.hasBank(det[idet]+"::dgtz")==true) {            
				EvioDataBank bank = (EvioDataBank) event.getBank(det[idet]+"::dgtz");

				for(int i = 0; i < bank.rows(); i++){
					float dum = (float)bank.getInt("TDC",i)-(float)mc_t*1000;
					if (dum<tdcmax) tdcmax=dum; //Find latest hit time
				}

				for(int i = 0; i < bank.rows(); i++){
					int is  = bank.getInt("sector",i);
					int ip  = bank.getInt("paddle",i);
					adc = bank.getInt("ADCL",i);
					tdcc = bank.getInt("TDCL",i);
					tdcf = tdcc;
					tdc[0] = (((float)tdcc-(float)mc_t*1000)-tdcmax+1340000)/1000; 
					fill(idet, is, 1, ip, adc, tdc, tdcf, tdcf); 
					adc = bank.getInt("ADCR",i);
					tdcc = bank.getInt("TDCR",i);
					tdcf = tdcc;
					tdc[0] = (((float)tdcc-(float)mc_t*1000)-tdcmax+1340000)/1000; 
					fill(idet, is, 2, ip, adc, tdc, tdcf, tdcf); 
				}                     
			}         
		}         
	}

	public void clear(int idet) {

		for (int is=0 ; is<5 ; is++) {
			int nstr = bandPix[idet].nstr[is];
			for (int il=0 ; il<2 ; il++) {
				bandPix[idet].nha[is][il] = 0;
				bandPix[idet].nht[is][il] = 0;
				for (int ip=0 ; ip<nstr ; ip++) {
					bandPix[idet].strra[is][il][ip] = 0;
					bandPix[idet].strrt[is][il][ip] = 0;
					bandPix[idet].adcr[is][il][ip]  = 0;
					bandPix[idet].tdcr[is][il][ip]  = 0;
					bandPix[idet].tf[is][il][ip]    = 0;
					bandPix[idet].ph[is][il][ip]    = 0;
				}
			}
		}

		if (app.isSingleEvent()) {
			clearHistograms();
			/*for (int is=0 ; is<5 ; is++) {
				for (int il=0 ; il<2 ; il++) {
					bandPix[idet].strips.hmap1.get("H1_a_Sevd").get(is+1,il+1,0).reset();
					bandPix[idet].strips.hmap2.get("H2_a_Sevd").get(is+1,il+1,0).reset();
					bandPix[idet].strips.hmap2.get("H2_a_Sevd").get(is+1,il+1,1).reset();
					bandPix[idet].strips.hmap2.get("H2_a_Hist").get(is+1,0,5+il+1).reset();
					bandPix[idet].strips.hmap2.get("H2_t_Sevd").get(is+1,il+1,0).reset();
				}
			}
			*/
		}   
	}


	public void fill(int idet, int is, int lr, int ip, int adc, float[] tdc, float tdcf, float adph) {

		int nstr = bandPix[idet].nstr[is-1];

		for (int ii=0; ii<tdc.length; ii++) {
			if(tdc[ii]>0&&tdc[ii]<1000){
				bandPix[idet].nht[is-1][lr-1]++; int inh = bandPix[idet].nht[is-1][lr-1];
				if (inh>nstr) inh=nstr;
				bandPix[idet].ph[is-1][lr-1][inh-1] = adph;
				bandPix[idet].tdcr[is-1][lr-1][inh-1] = (float) tdc[ii];
				bandPix[idet].strrt[is-1][lr-1][inh-1] = ip;
				bandPix[idet].ph[is-1][lr-1][inh-1] = adph;
			}
		}

		if(adc>thrcc){
			bandPix[idet].nha[is-1][lr-1]++; int inh = bandPix[idet].nha[is-1][lr-1];
			if (inh>nstr) inh=nstr;
			bandPix[idet].adcr[is-1][lr-1][inh-1] = adc;
			bandPix[idet].tf[is-1][lr-1][inh-1] = tdcf;
			bandPix[idet].strra[is-1][lr-1][inh-1] = ip;
		} 
	}

	public void processCalib() {

		IndexGenerator ig = new IndexGenerator();
		
		// We will decide now that if we don't have an FADC AND a TDC for
		// a bar, we don't give a shit about that event
		
		// Loop over all the unique bars that fired in our FADC module  -- we could do the
		// same for our TDC module, but if we don't have any FADC bar, then the event is useless.
		// Maybe we could recover the event if doesn't have TDC, but let's move on...
		System.out.println("******** PROCESSING ALL HITS IN THIS EVENT *********");
		
		for (Map.Entry<Long,List<Integer>>  entry : ltpmt.getMap().entrySet()){ 
			long hash = entry.getKey();
			int is = ig.getIndex(hash, 0); // Sector goes from 1-5
			int il = ig.getIndex(hash, 1); // Layer goes from 1-6
			int ip = ig.getIndex(hash, 2); // Paddle starts at 1

			for( int lr = 0 ; lr < 2 ; lr++) {
				// Get information for the current side:
				
				if( tdc_time.hasItem(is,il,lr,ip) ) {
					// Get TDC info
					for( int idx = 0; idx < tdc_time.getItem(is,il,lr,ip).size(); idx++) {
						float tt = tdc_time.getItem(is,il,lr,ip).get(idx);
						System.out.println("\t\tTDC info grabbed for "+is+" "+il+" "+ip+" "+lr+" : "+tt);
							// fill raw TDC histograms
						bandPix[il-1].strips.hmap2.get("H2_t_Hist").get(is,0,lr+1).fill(tt,ip);
					}
				}
			}
			
			// If I have TDCs for BOTH sides of a bar
			if( tdc_time.hasItem(is,il,0,ip) && tdc_time.hasItem(is,il,1,ip) ) {
				for( int idxL = 0; idxL < tdc_time.getItem(is,il,0,ip).size(); idxL++) {
					for( int idxR = 0; idxR < tdc_time.getItem(is,il,1,ip).size(); idxR++) {
						
						float ttL = tdc_time.getItem(is,il,0,ip).get(idxL);
						float ttR = tdc_time.getItem(is,il,1,ip).get(idxR);
							// Fill TDC L-R difference
						bandPix[il-1].strips.hmap2.get("H2_t_Hist").get(is,0,0).fill(ttL-ttR,ip);
					}
				}
				
			}
			
		}
		
		for (Map.Entry<Long,List<Integer>>  entry : lapmt.getMap().entrySet()){ 
			long hash = entry.getKey();
			int is = ig.getIndex(hash, 0); // Sector goes from 1-5
			int il = ig.getIndex(hash, 1); // Layer goes from 1-6
			int ip = ig.getIndex(hash, 2); // Paddle starts at 1
			
			for( int lr = 0 ; lr < 2 ; lr++) {
				
				if( fadc_int.hasItem(is,il,lr,ip) && fadc_time.hasItem(is,il,lr,ip)) {
					// Get FADC info
					int adcIdx = getADCidx(is,il,ip,lr);
					float ad = fadc_int.getItem(is,il,lr,ip).get(adcIdx);
					float ap = fadc_height.getItem(is,il,lr,ip).get(adcIdx);
					double at = fadc_time.getItem(is,il,lr,ip).get(adcIdx);
					System.out.println("\t\tFADC info grabbed for "+is+" "+il+" "+ip+" "+lr+" : "+ad+"/"+at);

					
					// fill raw ADC histograms
	        		// time:
	        		bandPix[il-1].strips.hmap2.get("H2_a_Hist").get(is,0,14+lr+1).fill(at, ip);
					// adc with overflow:
	        		bandPix[il-1].strips.hmap2.get("H2_a_Hist").get(is,0,3+lr+1).fill(ad,ip);
	        		if( overflow.getItem(is,il,lr,ip).get(adcIdx) == 1) {
						continue;
					}
        			// adc without overflow
	        		bandPix[il-1].strips.hmap2.get("H2_a_Hist").get(is,0,1+lr+1).fill(ad, ip);
	        		
	        		// If I also have TDC information, fill correlation plots
	        		if( tdc_time.hasItem(is,il,lr,ip)) {
	        			System.out.println("\t\t\tYAY WE HAVE BOTH FOR A SINGLE BAR");
	        			
	        			int tdcIdx = getTDCidx(is,il,ip,lr,adcIdx);
	        			float tt = tdc_time.getItem(is,il,lr,ip).get(tdcIdx);
	        			
	        			bandPix[il-1].strips.hmap2.get("H2_at_Hist").get(is,ip,1+lr+1).fill(at,tt);

	        		}
				}
				
			}
			
			
			// If I have ADCs for BOTH sides of a bar
			if(		fadc_int.hasItem(is,il,0,ip) && fadc_time.hasItem(is,il,0,ip) &&
					fadc_int.hasItem(is,il,1,ip) && fadc_time.hasItem(is,il,1,ip)	) {
				
				int adcIdxL = getADCidx(is,il,ip,0);
				int adcIdxR = getADCidx(is,il,ip,1);
				if( adcIdxL == -1 || adcIdxR == -1) continue;
				
				float ad_L = fadc_int.getItem(is,il,0,ip).get(adcIdxL);
				double at_L = fadc_time.getItem(is,il,0,ip).get(adcIdxL);
				
				float ad_R = fadc_int.getItem(is,il,1,ip).get(adcIdxR);
				double at_R = fadc_time.getItem(is,il,1,ip).get(adcIdxR);
				
					// Fill L-R time from FADC
				bandPix[il-1].strips.hmap2.get("H2_a_Hist").get(is,0,1).fill(at_L-at_R,ip);

				
			}
					
		}

	} 

	public void processSED() {

		for (int idet=0; idet<bandPix.length; idet++) {
			for (int is=0; is<5; is++) {
				for (int il=0; il<2; il++ ){;
					for (int n=0 ; n<bandPix[idet].nha[is][il] ; n++) {
						int ip=bandPix[idet].strra[is][il][n]; int ad=bandPix[idet].adcr[is][il][n];
						bandPix[idet].strips.hmap1.get("H1_a_Sevd").get(is+1,il+1,0).fill(ip,ad);
					}
					for (int n=0 ; n<bandPix[idet].nht[is][il] ; n++) {
						int ip=bandPix[idet].strrt[is][il][n]; float td=bandPix[idet].tdcr[is][il][n];
						double tdc = 0.25*(td-BANDConstants.TOFFSET[il]);
						float  wgt = bandPix[idet].ph[is][il][n];
						wgt = (wgt > 0) ? wgt:1000;
						bandPix[idet].strips.hmap2.get("H2_t_Sevd").get(is+1,il+1,0).fill((float)tdc,ip,wgt);
					}
				}
			} 
		}
	} 

	public void findPixels() {      
	}

	public void processPixels() {       
	}

	public void makeMaps(int idet) {
		//  Makes the maps for color drawing and updating color
		
		DetectorCollection<H2F> H2_a_Hist = new DetectorCollection<H2F>();
		DetectorCollection<H2F> H2_t_Hist = new DetectorCollection<H2F>();
		DetectorCollection<H1F> H1_a_Sevd = new DetectorCollection<H1F>();

		H2_a_Hist = bandPix[idet].strips.hmap2.get("H2_a_Hist");
		H2_t_Hist = bandPix[idet].strips.hmap2.get("H2_t_Hist");
		H1_a_Sevd = bandPix[idet].strips.hmap1.get("H1_a_Sevd");

		for (int is=is1;is<is2;is++) {
			for (int il=1 ; il<3 ; il++) {
				if (!app.isSingleEvent()) bandPix[idet].Lmap_a.add(is,il,0, toTreeMap(H2_a_Hist.get(is,0,5+il).projectionY().getData())); //Strip View ADC 
				if (!app.isSingleEvent()) bandPix[idet].Lmap_t.add(is,il,0, toTreeMap(H2_t_Hist.get(is,0,il).projectionY().getData())); //Strip View TDC 
				if  (app.isSingleEvent()) bandPix[idet].Lmap_a.add(is,il,0, toTreeMap(H1_a_Sevd.get(is,il,0).getData()));           
			}
		} 

		bandPix[idet].getLmapMinMax(is1,is2,1,0); 
		bandPix[idet].getLmapMinMax(is1,is2,2,0); 

	}  

	public int getADCidx(int sector, int layer, int component, int order) {
		// Loop over all ADCs to find the highest ADC pulse
		int chosenIdx =  -1;
		double highest = 0;
		for( int idx = 0; idx < fadc_int.getItem(sector,layer,order,component).size(); idx++) {
			double currADC = fadc_int.getItem(sector,layer,order,component).get(idx);
			if( currADC > highest) {
				highest = currADC;
				chosenIdx = idx;
			}
		}
		return chosenIdx;
	}
	
	public int getTDCidx(int sector, int layer, int component, int order, int ADCidx) {
		// Now with chosen idx, find the idx of TDC that matches to FADC time
		int idxTDC = -1;
		double minT = 1e5;
		for( int idx = 0; idx < tdc_time.getItem(sector,layer,order,component).size(); idx++) {
			double currTD = tdc_time.getItem(sector,layer,order,component).get(idx) - 
								fadc_time.getItem(sector,layer,order,component).get(ADCidx);
			if( Math.abs(currTD) < minT ) {
				minT = currTD;
				idxTDC = idx;
			}
		}
		return idxTDC;
	}

}

