package org.clas.fcmon.jroot;

import net.blackruffy.root.*;
import static net.blackruffy.root.JRoot.*;
import static net.blackruffy.root.Pointer.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

//import static net.blackruffy.root.JRoot.newTFile;
//import static net.blackruffy.root.JRoot.TFile;
//import static net.blackruffy.root.JRoot.TTree;
//import static net.blackruffy.root.Pointer.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

//clas12rec
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioDataSync;
import org.jlab.io.evio.EvioFactory;

import static java.lang.System.*;
import static java.lang.String.format;

public class ReadPcal {
    
    public TFile     tfile = null;
    public String filePath = null;
    public String fileName = null;
    public String evioName = null;
    public String  datName = null;
    public String  pedName = null;
    
    float peds[][] = new float[68][3];
    int secmap[] = {5,6,1,2,3,4}; //module number to sector number
    int sector = 0;
    
    EvioDataSync writer = new EvioDataSync();
    
    public ReadPcal(int pedRun, int datRun, int module) {  
        
        filePath = "/Users/colesmith/rootfiles/m"+module+"/";
        fileName = filePath+"pcal_"+datRun+".root";
        evioName = filePath+"pcal_"+datRun+".evio";
         datName = filePath+"pcal_"+datRun+".dat";
         pedName = filePath+"pcpedmean"+pedRun+".vec"; 
         sector = secmap[module-1];
         
    }
    
    public TTree getTree() {    
        
        tfile = newTFile(fileName, "READ");
        TTree tree = TTree(tfile.get("h10"));   
        return tree;
        
    }
    
    public void getPeds() {   
              
        try{
           FileReader       file = new FileReader(pedName);
           BufferedReader reader = new BufferedReader(file);
           for (int il=0;il<3;il++){
               for (int ip=0;ip<68;ip++){
                   peds[ip][il]=Float.parseFloat(reader.readLine());      
                   System.out.println("ip,il,ped="+ip+" "+il+" "+peds[ip][il]);
               }
           }      
           reader.close();
           file.close();
        }     
        catch(FileNotFoundException ex) {
           ex.printStackTrace();            
        }     
        catch(IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("Exiting getPeds()");
        
    }
    
    public void readStream() throws IOException {   
        
        String dataFile = datName;
        DataInputStream in=null;
        try {
            in = new DataInputStream(new
                    BufferedInputStream(new FileInputStream(dataFile)));
            try {
                while (true) {
                     int npc = in.readInt();
                     for (int i=0; i<npc ; i++) {
                         int ev = in.readInt();
                       byte lay = in.readByte();
                       byte str = in.readByte();
                        int adc = in.readShort();
                       System.out.println("npc,ev,lay,str,adc="+npc+" "+ev+" "+lay+" "+str+" "+adc);
                     }
                }
            } catch (EOFException e) {
            } 
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        finally {
            in.close();
        }
        
    }
    
    public void writeStreamFromTree(TTree tree) throws IOException {   
        
        String dataFile = datName;
        DataOutputStream out = null;
        out = new DataOutputStream(new BufferedOutputStream(
                new FileOutputStream(dataFile)));
        
        getPeds();
        
        Pointer     pnpc  = allocate(4); tree.setBranchAddress("npc", pnpc);
        Pointer playerpc  = allocate(4); tree.setBranchAddress("layerpc",playerpc);
        Pointer pstrippc  = allocate(4); tree.setBranchAddress("strippc",pstrippc);
        Pointer   padcpc  = allocate(4); tree.setBranchAddress("Adcpc",padcpc);
    
        long nev = tree.getEntries();
        System.out.println("nev="+nev);
        
        for(int ev=0; ev<nev; ev++ ) {
            tree.getEntry(ev);
            int         npc =      pnpc.getIntValue();
            byte[]  layerpc =  playerpc.getByteArray(npc);
            byte[]  strippc =  pstrippc.getByteArray(npc);
            int[]     adcpc =    padcpc.getUInt16Array(npc);
            
            out.writeInt(npc);
            
            for (int i=0; i<npc ; i++) {
                out.writeInt(ev);
                out.writeByte(layerpc[i]);
                out.writeByte(strippc[i]);
                short adc = (short) (adcpc[i]-peds[strippc[i]-1][layerpc[i]-1]);
                out.writeShort(adc);
            }
        }
        System.out.println("Done");
        out.close();
          
    }
    
    public void writeBanksFromDat() throws IOException {
        
        DataInputStream in=null;
        String dataFile = datName;
        
        writer.open(evioName);
        int is=0; 
        int  s=this.sector;
        
        try {
            in = new DataInputStream(new
                    BufferedInputStream(new FileInputStream(dataFile)));
            try {
                while (true) {
                    int npc = in.readInt();
                   
                    EvioDataBank bankS = (EvioDataBank) EvioFactory.createEvioBank("PCAL::dgtz", npc);
                    for (int i=0; i<npc ; i++) {
                        int ev = in.readInt();  
                      byte lay = in.readByte(); bankS.setInt("view",i,(int)lay);
                      byte str = in.readByte(); bankS.setInt("strip",i,(int)str);
                       int adc = in.readShort();bankS.setInt("ADC",i,(int)adc);
                       is=s ; bankS.setInt("sector",i, is);
                       is=1 ; bankS.setInt("stack",i, is);
                       is=0 ; bankS.setInt("TDC",i,is);
                       is=1 ; bankS.setInt("hitn",i,is);
                    }
                    EvioDataEvent event = (EvioDataEvent) EvioFactory.createEvioEvent();
                    event.appendBank(bankS);                                
                    writer.writeEvent(event);
                }
            } catch (EOFException e) {
            } 
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        finally {
            System.out.println("Done");
            in.close();
            writer.close();
        }        
    }
    
    public void writeBanksFromTree(TTree tree) {
        
        writer.open(evioName);
        getPeds();
        
        long nev = tree.getEntries();
    
        Pointer     pnpc  = allocate(4); tree.setBranchAddress("npc", pnpc);
        Pointer psectorpc = allocate(4); tree.setBranchAddress("secpc",psectorpc);
        Pointer playerpc  = allocate(4); tree.setBranchAddress("layerpc",playerpc);
        Pointer pstrippc  = allocate(4); tree.setBranchAddress("strippc",pstrippc);
        Pointer   padcpc  = allocate(4); tree.setBranchAddress("Adcpc",padcpc);
        Pointer   ptdcpc  = allocate(4); tree.setBranchAddress("Tdcpc",ptdcpc);
    
        for( long ev=0; ev<20000; ev++ ) {
            tree.getEntry(ev);
            int         npc =      pnpc.getIntValue();
            byte[] sectorpc = psectorpc.getByteArray(npc);
            byte[]  layerpc =  playerpc.getByteArray(npc);
            byte[]  strippc =  pstrippc.getByteArray(npc);
            int[]     adcpc =    padcpc.getUInt16Array(npc);
            int[]     tdcpc =    ptdcpc.getUInt16Array(npc);
            
            EvioDataBank bankS = (EvioDataBank) EvioFactory.createEvioBank("PCAL::dgtz", npc);
            
            int is=0;
            
            for (int i=0; i<npc ; i++) {
                is=sectorpc[i]; bankS.setInt("sector",i, is);                
                is=1 ;          bankS.setInt("stack",i, is);
                is=layerpc[i];  bankS.setInt("view",i,is);
                is=strippc[i];  bankS.setInt("strip",i,is);
                is=adcpc[i];    bankS.setInt("ADC",i,is);
                is=tdcpc[i];    bankS.setInt("TDC",i,is);
                is=1;           bankS.setInt("hitn",i,is);                             
            }
            
            EvioDataEvent event = (EvioDataEvent) EvioFactory.createEvioEvent();
            event.appendBank(bankS);
                        
            writer.writeEvent(event);
 
            out.printf("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,\n",
                ev,npc,layerpc[0],layerpc[1],layerpc[2],
                strippc[0],strippc[1],strippc[2],
                  adcpc[0],adcpc[1],adcpc[2]);
                  
         }
        
         tree.delete();
         tfile.close(); 
         writer.close();

    }
  

    public static void main( String[] args ) throws Exception {
        
        ReadPcal pcal = new ReadPcal(4284,4294,5); //(pedrunno,datrunno,module)
        
        //pcal.writeBanksFromTree(pcal.getTree());
        //pcal.writeStreamFromTree(pcal.getTree());
        //pcal.readStream();
        pcal.writeBanksFromDat();
                
    }

}
