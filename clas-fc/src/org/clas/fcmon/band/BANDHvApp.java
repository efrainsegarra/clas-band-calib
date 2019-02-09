package org.clas.fcmon.band;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.Timer;

import org.clas.fcmon.detector.view.DetectorShape2D;
import org.clas.fcmon.detector.view.EmbeddedCanvasTabbed;
import org.clas.fcmon.tools.ColorPalette;
import org.clas.fcmon.tools.FCEpics;
import org.jlab.detector.base.DetectorCollection;
import org.jlab.detector.base.DetectorDescriptor;

import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;

public class BANDHvApp extends FCEpics implements ActionListener {
    
    JTextField   newhv = new JTextField(4);
    JLabel statuslabel = new JLabel();   
    
    DetectorCollection<H1F> H1_HV = new DetectorCollection<H1F>();
    
    updateGUIAction action = new updateGUIAction();
    
    Timer timer = null;
    boolean epicsEnabled = false;
    int delay=2000;
    int nfifo=0, nmax=120;
    int isCurrentSector;
    int isCurrentLayer;
    double newHV=0;
    
    BANDHvApp(String name, String det) {
        super(name, det);
    }
    
    public void init() {
        this.is1=1;
        this.is2=2;
        setPvNames(this.detName,0);
        sectorSelected=is1;
        layerSelected=1;
        channelSelected=1;
        initHistos();
    }
    
    public void startEPICS() {
    	System.out.println("BANDHvApp: Connect to EPICS Channel Access");
    	clearMaps(); nfifo=0; 
    	createContext();
        setCaNames(this.detName,0);
        initFifos();
        this.timer = new Timer(delay,action);  
        this.timer.setDelay(delay);
        this.timer.start();       
    }
    
    public void stopEPICS() {
    	System.out.println("BANDHvApp: Connect to EPICS Channel Access");
    	epicsEnabled = false;
        if(this.timer.isRunning()) this.timer.stop();
        destroyContext();
    }   
    
    public JPanel getPanel() {        
        engineView.setLayout(new BorderLayout());
        engineView.add(getEnginePane(),BorderLayout.CENTER);
        engineView.add(getButtonPane(),BorderLayout.PAGE_END);
        return engineView;       
    }   
    
    public JSplitPane getEnginePane() {
        enginePane.setTopComponent(getEngine1DView());
        enginePane.setBottomComponent(getEngine2DView());       
        enginePane.setResizeWeight(0.2);
        return enginePane;
    }
    public JPanel getEngine1DView() {
        engine1DView.setLayout(new BorderLayout());
        engine1DCanvas = new EmbeddedCanvasTabbed("HV");
        engine1DView.add(engine1DCanvas,BorderLayout.CENTER);
        return engine1DView;
    }
    
    public JPanel getEngine2DView() {
        engine2DView.setLayout(new BorderLayout());
        engine2DCanvas = new EmbeddedCanvasTabbed("Stripcharts");
        engine2DView.add(engine2DCanvas,BorderLayout.CENTER);
        return engine2DView;        
    }   
    
    public JPanel getButtonPane() {
        buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout());
        
        JButton loadBtn = new JButton("Load HV");
        loadBtn.addActionListener(this);
        buttonPane.add(loadBtn); 

        buttonPane.add(new JLabel("New HV:"));
        newhv.setActionCommand("NEWHV"); newhv.addActionListener(this); newhv.setText("0");  
        buttonPane.add(newhv); 
        
        statuslabel = new JLabel(" ");         
        buttonPane.add(statuslabel);
        
        return buttonPane;
    }
    
    private class updateGUIAction implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            fillFifos();
            fillHistos(); 
            update1DScalers(engine1DCanvas.getCanvas("HV"),1);   
       }
    } 
    
    public void initHistos() {       
        System.out.println("BANDHvApp.initHistos():");
        for (int is=is1; is<is2 ; is++) {
            for (int il=1 ; il<layMap.get(detName).length+1 ; il++){
                int nb=nlayMap.get(detName)[il-1]; int mx=nb+1;
                H1_HV.add(is, il, 0, new H1F("HV_vset"+is+"_"+il, nb,1,mx));                
                H1_HV.add(is, il, 1, new H1F("HV_vmon"+is+"_"+il, nb,1,mx));                
                H1_HV.add(is, il, 2, new H1F("HV_imon"+is+"_"+il, nb,1,mx));                
            }
        }
    }
        
    public void initFifos() {
        System.out.println("BANDHvApp.initFifos():");
        app.fifo1.clear(); app.fifo2.clear(); app.fifo3.clear(); app.fifo6.clear();
        for (int is=is1; is<is2 ; is++) {
            for (int il=1; il<layMap.get(detName).length+1 ; il++) {
                for (int ic=1; ic<nlayMap.get(detName)[il-1]+1; ic++) {
                    app.fifo1.add(is, il, ic, new LinkedList<Double>());
                    app.fifo2.add(is, il, ic, new LinkedList<Double>());
                    app.fifo3.add(is, il, ic, new LinkedList<Double>());
                    app.fifo6.add(is, il, ic, new LinkedList<Double>());
                    connectCa(0,"vset",is,il,ic);
                    connectCa(0,"vmon",is,il,ic);
                    connectCa(0,"imon",is,il,ic);
                }
            }
        }
    }
    
    public void fillFifos() {
        
        //long startTime = System.currentTimeMillis();
        nfifo++;
        for (int is=is1; is<is2 ; is++) {
            for (int il=1; il<layMap.get(detName).length+1 ; il++) {
                for (int ic=1; ic<nlayMap.get(detName)[il-1]+1; ic++) {
                    if(nfifo>nmax) {
                        app.fifo1.get(is, il, ic).removeFirst();
                        app.fifo2.get(is, il, ic).removeFirst();
                        app.fifo3.get(is, il, ic).removeFirst();
                    }
//                    System.out.println(is+" "+il+" "+ic);
                    app.fifo1.get(is, il, ic).add(getCaValue(0,"vset",is, il, ic));
                    app.fifo2.get(is, il, ic).add(getCaValue(0,"vmon",is, il, ic));
                    app.fifo3.get(is, il, ic).add(getCaValue(0,"imon",is, il, ic));
                }
            }
         }
       // System.out.println("time= "+(System.currentTimeMillis()-startTime));
        epicsEnabled = true;
        
    }

    public void fillHistos() {
        
        for (int is=is1; is<is2 ; is++) {
            for (int il=1; il<layMap.get(detName).length+1 ; il++) {
                H1_HV.get(is, il, 0).reset(); 
                H1_HV.get(is, il, 1).reset(); 
                H1_HV.get(is, il, 2).reset(); 
                for (int ic=1; ic<nlayMap.get(detName)[il-1]+1; ic++) {                    
                    H1_HV.get(is, il, 0).fill(ic,app.fifo1.get(is, il, ic).getLast());
                    H1_HV.get(is, il, 1).fill(ic,app.fifo2.get(is, il, ic).getLast());
                    H1_HV.get(is, il, 2).fill(ic,app.fifo3.get(is, il, ic).getLast());
                }
            }
        }
        
    }
    
    public void loadHV(int is1, int is2, int il1, int il2) {
        System.out.println("BANDHvApp.loadHV()");
        for (int is=is1; is<is2 ; is++) {
            for (int il=il1; il<il2 ; il++) {
                for (int ic=1; ic<nlayMap.get(detName)[il-1]+1; ic++) {
                    System.out.println("is="+is+" il="+il+" ic="+ic+" HV="+app.fifo6.get(is, il, ic).getLast());
                    putCaValue(0,"vset",is,il,ic,app.fifo6.get(is, il, ic).getLast());  
                }
            }
        }
        
    }   
    
    public void updateStatus(int is, int il, int ic) {
//      int vset = (int)getCaValue(0,"vset",is, il, ic);
//      int vmon = (int)getCaValue(0,"vmon",is, il, ic);
//      int imon = (int)getCaValue(0,"imon",is, il, ic);
      double vset = app.fifo1.get(is,il,ic).getLast();
      double vmon = app.fifo2.get(is,il,ic).getLast(); 
      double imon = app.fifo3.get(is,il,ic).getLast(); 
      this.statuslabel.setText("PMT: "+app.detectorAlias+"    Vset:"+(int)vset+"  Vmon:"+(int)vmon+"  Imon:"+(int)imon);        
    }  
    
    public void updateCanvas(DetectorDescriptor dd) {
        
    	sectorSelected  = 1;
        layerSelected   = dd.getOrder()+1+2*app.detectorIndex;
        channelSelected = BANDConstants.getBar(app.detectorIndex, dd.getSector(), dd.getComponent())+1; 
        orderSelected   = dd.getOrder();

	update1DScalers(engine1DCanvas.getCanvas("HV"),0);   
	
	if( dd.getLayer()  < 6 ){
		if (epicsEnabled) updateStatus(sectorSelected,layerSelected,channelSelected);

		isCurrentSector = sectorSelected;
		isCurrentLayer  = layerSelected;
	}


    }
    
    public void update1DScalers(EmbeddedCanvas canvas, int flag) {
        
        H1F h = new H1F();
        H1F c = new H1F();
        
        canvas.divide(4, 1);
	
	int is = sectorSelected;
        int lr = orderSelected+1;
	int ip = channelSelected-1;
	int il = app.detectorIndex+1;
	int off = 2*app.detectorIndex;
	
	if( il < 6 ){
		h = H1_HV.get(is,off+1, 0); h.setTitleX("LAY "+il+" L PMT"); h.setTitleY("VOLTS");
		h.setFillColor(33); canvas.cd(0); canvas.draw(h);
		h = H1_HV.get(is, off+2, 0); h.setTitleX("LAY "+il+" R PMT"); h.setTitleY("VOLTS");
		h.setFillColor(33); canvas.cd(1);    canvas.draw(h);

		
		c = H1_HV.get(is, lr+off, 0).histClone("Copy"); c.reset() ; 
		c.setBinContent(ip, H1_HV.get(is, lr+off, 0).getBinContent(ip));
		c.setFillColor(2);  canvas.cd(lr-1); canvas.draw(c,"same");
		
		c = H1_HV.get(is, lr+off, 2).histClone("Copy"); c.reset() ; 
		c.setBinContent(ip, H1_HV.get(is, lr+off, 2).getBinContent(ip));
		c.setFillColor(2);  canvas.cd(lr-1+2); canvas.draw(c,"same");
	}
	
        
        canvas.repaint();
    }
    
    
    public void updateDetectorView(DetectorShape2D shape) {
    	
        ColorPalette palette3 = new ColorPalette(3);
        ColorPalette palette4 = new ColorPalette(4);
                   
        ColorPalette pal = palette4;
        
        DetectorDescriptor dd = shape.getDescriptor(); 
        

	int is = sectorSelected;
        int lr = orderSelected+1;
        int ip = BANDConstants.getBar(app.detectorIndex, dd.getSector(), dd.getComponent())+1; 
	int off = 2*app.detectorIndex;
    
	if( dd.getLayer() < 6){
		float z = (float) H1_HV.get(is, lr+off, 2).getBinContent(ip) ;
		float zmin = 300 ; float zmax = 400;
		if (app.omap==3) {
			double colorfraction=(z-zmin)/(zmax-zmin);
		    app.getDetectorView().getView().zmax = zmin;
		    app.getDetectorView().getView().zmin = zmax;
		    Color col = pal.getRange(colorfraction);
		    shape.setColor(col.getRed(),col.getGreen(),col.getBlue());              
		}
	}
    } 
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        int is = sectorSelected;
        int lr = layerSelected;
        int ip = channelSelected; 
        if(e.getActionCommand().compareTo("Load HV")==0) putCaValue(0,"vset",is,lr,ip,newHV);
        if(e.getActionCommand().compareTo("NEWHV")==0)   newHV = Double.parseDouble(newhv.getText());
        
    }    
    
}
