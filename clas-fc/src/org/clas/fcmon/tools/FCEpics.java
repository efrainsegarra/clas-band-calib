package org.clas.fcmon.tools;

import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.clas.fcmon.band.BANDConstants;
import org.clas.fcmon.detector.view.EmbeddedCanvasTabbed;

import org.epics.ca.Channel;
import org.epics.ca.Context;
import org.epics.ca.Monitor;

import org.jlab.utils.groups.IndexedList;

public class FCEpics  {
    
    public String      appName = null;
    public String      detName = null;

    public MonitorApp      app = null;
    public DetectorMonitor mon = null;
    public Context     context = null;
    
    public Monitor<Double>     monitor = null;
    
    public JPanel                   engineView = new JPanel();    
    public JSplitPane               enginePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    public JPanel                 engine1DView = new JPanel();
    public JPanel                 engine2DView = new JPanel();
    public EmbeddedCanvasTabbed engine1DCanvas = null;
    public EmbeddedCanvasTabbed engine2DCanvas = null;
    public JPanel                   buttonPane = null;
    public JPanel                   sliderPane = null;
    
    IndexedList<String>                             map = new IndexedList<String>(4);
    TreeMap<String,IndexedList<String>>           pvMap = new TreeMap<String,IndexedList<String>>();
    TreeMap<String,IndexedList<Channel<Double>>>  caMap = new TreeMap<String,IndexedList<Channel<Double>>>();
    public TreeMap<String,String[]>              layMap = new TreeMap<String,String[]>();
    public TreeMap<String,int[]>                nlayMap = new TreeMap<String,int[]>();
   
    String ca_FC    = "scaler_calc1";
    String ca_2C24A = "hallb_IPM2C24A_CUR";
    String ca_2H01  = "hallb_IPM2H01_CUR";
    
    String   grps[] = {"HV","DISC","FADC"};
    String   band[] = {"1L","1R","2L","2R","3L","3R","4L","4R","5L","5R","6"};
    int     nband[] = {24,24,24,24,24,24,24,24,20,20,24};
 
    
    public int is1,is2;
    public int sectorSelected, layerSelected, channelSelected, orderSelected;
    public Boolean online = true;
    
	public FCEpics(String name, String det){
	    System.out.println("FCEpics: Initializing detector "+det);
	    this.appName = name;
	    this.detName = det;
        this.layMap.put("BAND",band); this.nlayMap.put("BAND", nband);
      
	}
    
	public void clearMaps() {
		System.out.println("FCEpics: Clearing Maps");
	    this.map.clear();
	    this.caMap.clear();		
	}
	
	public int createContext() {
	    if (!online) return 0;
	    this.context = new Context();
	    return 1;
	}
	
	public int destroyContext() {
        if (!online) return 0;
	    this.context.close();
	    return 1;
	}
	
    public void setApplicationClass(MonitorApp app) {
        this.app = app;
    }
    
    public void setMonitoringClass(DetectorMonitor mon) {
        this.mon = mon;
    }
    
    public String getName() {
        return this.appName;
    }
    
    public int connectCa(int grp, String action, int sector, int layer, int channel) {
        if (!online) return 0;
        try {
//        System.out.println("Connecting to grp "+grp+" sector "+sector+" layer "+layer+" channel "+channel);
        caMap.get(action).getItem(grp,sector,layer,channel).connectAsync().get(1,TimeUnit.MILLISECONDS);  //org.epics.ca
        }
        catch (InterruptedException e) {  
            return -1;
        }        
        catch (TimeoutException e) {  
            return -1;
        }        
        catch (ExecutionException e) {  
            return -1;
        }
        return 1;
        
    }
    
    public double getCaValue(int grp, String action, int sector, int layer, int channel) {
        if (!online) return 1000.0;
        try {
        CompletableFuture<Double> ffd = caMap.get(action).getItem(grp,sector,layer,channel).getAsync(); //org.epics.ca
        return ffd.get(); 
        }
        catch (InterruptedException e) {  
            return -1.0;
        }        
        catch (ExecutionException e) {  
            return -1.0;
        }   
    }
    
    public int putCaValue(int grp, String action, int sector, int layer, int channel, double value) {
        if(!online) return 0;
        caMap.get(action).getItem(grp,sector,layer,channel).putNoWait(value); //org.epics.ca  
        return 1;
    } 
    
    public void startMonitor(int grp, String action, int sector, int layer, int channel) {
        this.monitor = caMap.get(action).getItem(grp,sector,layer,channel).addValueMonitor(value->System.out.println(value));
    }
    
    public void stopMonitor(){
        this.monitor.close();
    }
    
    public void setCaNames(String det, int grp) {
        switch (grp) {
        case 0:
        setCaActionNames(det,grp,"vmon");
        setCaActionNames(det,grp,"imon");
        setCaActionNames(det,grp,"vset");     
        break;
        case 1:
        setCaActionNames(det,grp,"cTdc"); 
        break;  
        case 2: 
        setCaActionNames(det,grp,"c"); 
        break;
        case 3:
        setCaActionNames(grp,"BEAM");
        }
    }
    
    public int setCaActionNames(int grp, String action) {
        if (!online) return 0;
        IndexedList<Channel<Double>> map = new IndexedList<Channel<Double>>(4);
        map.add(context.createChannel(ca_FC,    Double.class),grp,0,0,1); //org.epics.ca
        map.add(context.createChannel(ca_2C24A, Double.class),grp,0,0,2); //org.epics.ca
        map.add(context.createChannel(ca_2H01,  Double.class),grp,0,0,3); //org.epics.ca
        caMap.put(action,map);
    	return 1;
    }
    
    public int setCaActionNames(String det, int grp, String action) {
        
        if (!online) return 0;
        
        IndexedList<Channel<Double>> map = new IndexedList<Channel<Double>>(4);
        
        for (int is=is1; is<is2 ; is++) {
            for (int il=1; il<layMap.get(det).length+1; il++) {
                for (int ic=1; ic<nlayMap.get(det)[il-1]+1; ic++) {
                    String pv = getPvName(grp,action,is,il,ic);
                    map.add(context.createChannel(pv, Double.class),grp,is,il,ic); //org.epics.ca
                }
            }
        } 
        caMap.put(action,map);
        return 1;
    }
    
    public void setPvNames(String det, int grp) {
        switch (grp) {
            case 0:
            setPvActionNames(det,grp,"vmon");
            setPvActionNames(det,grp,"imon");
            setPvActionNames(det,grp,"vset");
            setPvActionNames(det,grp,"pwonoff"); 
            break;
            case 1:  
            setPvActionNames(det,grp,"cTdc"); break;
            case 2: 
            setPvActionNames(det,grp,"c"); break;
        }
    }
    
    public String getPvName(int grp, String action, int sector, int layer, int channel) {
        switch (grp) {
        case 0:
        switch (action) {
        case    "vmon": return (String) pvMap.get(action).getItem(grp,sector,layer,channel);
        case    "imon": return (String) pvMap.get(action).getItem(grp,sector,layer,channel); 
        case    "vset": return (String) pvMap.get(action).getItem(grp,sector,layer,channel); 
        case "pwonoff": return (String) pvMap.get(action).getItem(grp,sector,layer,channel); 
        }
        break;
        case 1:  
                        return (String) pvMap.get(action).getItem(grp,sector,layer,channel);
        case 2: 
                        return (String) pvMap.get(action).getItem(grp,sector,layer,channel);
        }
        return "Invalid action";
    }
    
    public void setPvActionNames(String det, int grp, String action) {
      
        IndexedList<String> map = new IndexedList<String>(4);
        for (int is=is1; is<is2 ; is++) {
            for (int il=1; il<layMap.get(det).length+1 ; il++) {
                for (int ic=1; ic<nlayMap.get(det)[il-1]+1; ic++) {
                    map.add(getPvString(det,grp,is,il,ic,action),grp,is,il,ic);
                }
            }
        }
        pvMap.put(action,map);
    }
    
    public String layToStr(String det, int layer) {
        return layMap.get(det)[layer-1];
    }	
    
	public String chanToStr(int channel) {
		//is the cut channel<10 correct if we need Vetos in BAND? F.H comment at 02/09/19
	    return (channel<10 ? "0"+Integer.toString(channel):Integer.toString(channel));
	}
	
	public String detAlias(String det, int layer) { 
	    return "BAND";
	}
	
	public String getPvString(String det, int grp, int sector, int layer, int channel, String action) {
		
	    String pv = "default";
	   //Group is either 0 or 2. Group = 0 is HV string and group = 2 os FADC string. Sector is not used in this function!!!
	    //Everything is done in terms of layers and channels
	  //layer = 1 & 2 is layer 1 with 24 PMTs total on L and R of a bar respectively
    	//layer = 3 & 4 is layer 2 with 24 PMTs total on L and R of a bar respectively
    	//layer = 5 & 6 is layer 3 with 24 PMTs total on L and R of a bar respectively
    	//layer = 7 & 8 is layer 4 with 24 PMTs total on L and R of a bar respectively
    	//layer = 9 & 10  is layer 5 with 20 PMTs total on  L and R of a bar respectively (missing 2 long and 2 short
	  // System.out.println(grp +" "+ sector + " " + layer + " " + channel);
	   if (det=="BAND") {
	      pv = "B_DET_"+detAlias(det,layer)+"_"+grps[grp]+"_"+BANDConstants.getHvAlias(layToStr(det,layer), channel);
	   //   System.out.println("getpv String in case BAND: " + pv);
	    }
	   else {
		   System.out.println("Error in FCEpics: getpvString(). det == BAND is not true. Check det string");
	   }
//	    System.out.println(sector+" "+layer+" "+channel+" "+pv+":"+action);
	    return pv+":"+action;
	} 
    
}
