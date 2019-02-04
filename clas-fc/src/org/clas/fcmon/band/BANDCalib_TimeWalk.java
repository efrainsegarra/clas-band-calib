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
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.fitter.DataFitter;

public class BANDCalib_TimeWalk extends FCApplication implements CalibrationConstantsListener,ChangeListener {

    
    EmbeddedCanvas c = this.getCanvas(this.getName()); 
    CalibrationConstantsView      ccview = new CalibrationConstantsView();
    ArrayList<CalibrationConstants> list = new ArrayList<CalibrationConstants>();
    CalibrationConstants calib;
    JPanel                    engineView = new JPanel();

    int is1,is2;

    
    public BANDCalib_TimeWalk(String name, BANDPixels[] bandPix) {
        super(name,bandPix);    
     }

    public void init(int is1, int is2) {
    	
    	System.out.println("\tInitializing Time Walk Calibration");
    	
        this.is1=is1;
        this.is2=is2;
        
        calib = new CalibrationConstants(3,
                "nameMe/F:nameMeToo/F");
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
         
         c.clear(); c.divide(2, 4);
         c.setAxisFontSize(12);

    //   canvas.setAxisTitleFontSize(12);
    //   canvas.setTitleFontSize(14);
    //   canvas.setStatBoxFontSize(10);
         
              
         
         H2F h;
         String alab;
         String otab[]={" L PMT "," R PMT "};
         String calTitles[]={" ADC"," TDC"};
         //String lab4[]={" ADC"," TDC"," OVERFLOW"}; 
         String tit = "SEC "+sector+" LAY "+(layer+1);
         //String tit = "SEC "+is+" LAY "+(ilm+1);

         // We will loop here for all the calibration plots we want to make for
         // selected pmt
         /*for(int iip=min;iip<max;iip++) {
             
         	alab = tit+otab[lr-1]+(iip+1)+calTitles[0];
             */
         	 alab = tit+otab[0]+(component+1)+calTitles[0];
             c.cd(0);          
             h = bandPix[layer].strips.hmap2.get("H2_x_Hist").get(sector,1,0);//.sliceY(component);
             h.setTitleX(alab); h.setTitle(""); h.setTitleY("Entries");  
             c.draw(h);
             
         	 alab = tit+otab[1]+(component+1)+calTitles[0];
             
         	 c.cd(1);          
             h = bandPix[layer].strips.hmap2.get("H2_x_Hist").get(sector,2,0);//.sliceY(component);
             h.setTitleX(alab); h.setTitle(""); h.setTitleY("Entries");  
             c.draw(h);


         /*    
         c.cd(1);
         alab = tit+otab[1]+(component+1)+calTitles[0];  


         h = bandPix[layer].strips.hmap2.get("H2_x_Hist").get(is,2,1);//.sliceY(component);
         h.setTitleX(alab); h.setTitle(""); h.setTitleY("Entries"); 
         c.draw(h,"same");


		*/
         c.repaint();
         //End of plotting
     }   
    
    
	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void constantsEvent(CalibrationConstants arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}
}
