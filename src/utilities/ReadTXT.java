package utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class ReadTXT {
	
	public static void main(String[] args) {
		 	List<String>erfileList = new ArrayList<String>(); 
		 	erfileList.add("./selected/species/Sepsiseroutput00111.txt");
		 	List<String>sifileList = new ArrayList<String>(); 
		 	sifileList.add("./selected/species/Sepsissizeoutput00111.txt");
		 	double fil[]= {0.00111};
		 	int i=0;
		 	//2012-->UPPER size of model:-->121735.0 upper er 99.58177529730993
		 	//roadt-->819.0 upper er 13.363174866327311
		 	//2017--> 560237.0 upper er 115.3269128813888
		 	//2018--> 217981  and 202.98270547959467
		 	//hostB  UPPER size of model:-->7859.0 upper er 19.60054527983414
		 	//preTravel  UPPER size of model:-->2171.0 upper er 30.121197756798157
		 	//species  UPPER size of model:-->13271.0 upper er 54.30194653265722
		 	//nasa  UPPER size of model:-->37621.0 upper er 91.5942799649433
		 	//2013 26075.0 upper er 21.33535622799649
			double maxEr=51;
	 		double maxSize=75;
	 		Map< Double,Map<String,Double>> erList=new HashMap< Double,Map<String,Double>>();
	 		Map< Double,Map<String,Double>> sizeList=new HashMap< Double,Map<String,Double>>();
	 		Map<String,Double> si=null;
    		Map<String,Double> er=null;
		 	for(String file:erfileList)
		 	{
		 	
		 		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		 			BufferedReader sizebr = new BufferedReader(new FileReader(sifileList.get(i++)));
		 			String line;
		 			si = new HashMap<String, Double>();
		    		er = new HashMap<String, Double>();
		 			while ((line = br.readLine()) != null) {
		 				StringTokenizer st = new StringTokenizer(line);
		 				double alpha = Double.parseDouble(st.nextToken());
		 				double T0 = Double.parseDouble(st.nextToken());
		 				//double filter = Double.parseDouble(st.nextToken());
		 				double varer = Double.parseDouble(st.nextToken());
		 				StringTokenizer sizest= new StringTokenizer(sizebr.readLine());
		 				sizest.nextToken();
		 				//sizest.nextToken();
		 				sizest.nextToken();
		 				double varsize = Double.parseDouble(sizest.nextToken());
		 				if(maxEr<varer)
		 					maxEr=varer;
		 				if(maxSize<varsize)
		 					maxSize = varsize;
		 				si.put(String.format("%.3f",alpha)+","+String.format("%.3f",T0), varsize);
	    				er.put(String.format("%.3f",alpha)+","+String.format("%.3f",T0), varer);
	                
		 			}
		 		} catch (IOException e) {
		 			System.err.println("Error reading the file: " + e.getMessage());
		 		}
		 		sizeList.put(fil[i-1],si);
		 		erList.put(fil[i-1],er);
		 	}
		 	for(double fill:erList.keySet())
	    	{
	    		try{FileWriter totalWriter  = new FileWriter("./selected/species/Sepsistotaloutput"+fill+".txt");
	    		// UPPER size of model:-->217981.0 upper er 202.98270547959467;
	    		Map<String,Double> si1 = new HashMap<String, Double>();
	    		Map<String,Double> er1 = new HashMap<String, Double>();
	    		er=erList.get(fill);
	    		si=sizeList.get(fill);
	    		for(double k=0.02;k<=1.01;k+=0.02)
	    			for(double h=0.02;h<=1.01;h+=0.02)
	    			{
	    				double ervalue = er.get(String.format("%.3f",k)+","+String.format("%.3f",h));
	    				double sizevalue = si.get(String.format("%.3f",k)+","+String.format("%.3f",h));

	        			totalWriter.write(String.format("%.3f",k)+" "+String.format("%.3f",h)+" "+((0.1*(1 /sizevalue))+(0.9*(1 /ervalue)))+"\n");
	    			}

	    		totalWriter.close();
	    		}
	    		catch (IOException e) {
		 			System.err.println("Error reading the file: " + e.getMessage());
		 		}
	    		System.out.println(maxEr+" "+maxSize);
	    	}
		 	
		 	
		 	
	}

}
