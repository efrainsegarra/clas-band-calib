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

public class BANDCalib_Source extends FCApplication implements CalibrationConstantsListener,ChangeListener {

    
    EmbeddedCanvas c = this.getCanvas(this.getName()); 
    CalibrationConstantsView      ccview = new CalibrationConstantsView();
    ArrayList<CalibrationConstants> list = new ArrayList<CalibrationConstants>();
    CalibrationConstants calib;
    JPanel                    engineView = new JPanel();
    int runno;
    int is1,is2;

    
    public BANDCalib_Source(String name, BANDPixels[] bandPix) {
        super(name,bandPix);    
     }

    public void init(int is1, int is2) {
    	
    	System.out.println("\tInitializing Speed of Light Calibration");
    	
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

    }
    
    public void fit(){ 
        
     }


    public void updateCanvas(DetectorDescriptor dd) {
        
        this.getDetIndices(dd); 
        
        int lr = dd.getOrder()+1;
        int sector = dd.getSector();
        int component = dd.getComponent();   
        int layer = ilmap;
               
        int nstr = bandPix[layer].nstr[is-1];
        int min=0, max=nstr;
        
        H1F h1c;
        
        c.clear(); c.divide(2, 2);
        c.setAxisFontSize(12);

        c.cd(0);
        h1c = bandPix[layer].strips.hmap2.get("H2_a_Hist").get(sector,0,0).sliceY(component); 
        h1c.setOptStat(Integer.parseInt("1000100")); 
        h1c.setTitleX("Sqrt(ADC_L*ADC_R)");   h1c.setFillColor(2);
        c.draw(h1c);
        
        c.cd(1);
        h1c = bandPix[layer].strips.hmap2.get("H2_a_Hist").get(sector,0,14).sliceY(component); 
        h1c.setOptStat(Integer.parseInt("1000100")); 
        h1c.setTitleX("ADC_L - ADC_R Correlation");   h1c.setFillColor(2);
        c.draw(h1c);

        
                
        c.cd(2);
        h1c = bandPix[layer].strips.hmap2.get("H2_t_Hist").get(sector,0,0).sliceY(component); 
        h1c.setOptStat(Integer.parseInt("1000100")); 
        h1c.setTitleX("TDC L-R BAR "+(component+1)+" TDIF");   h1c.setFillColor(2);
        c.draw(h1c);
        
        c.cd(3);
        h1c = bandPix[layer].strips.hmap2.get("H2_a_Hist").get(sector,0,1).sliceY(component); 
        h1c.setTitleX("FADC L-R BAR "+(component+1)+" TDIF");   h1c.setFillColor(2);
        h1c.setOptStat(Integer.parseInt("1000100")); 
        c.draw(h1c);
        
        
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
