package utilities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

public class RebalanceEr {

	public static void main(String[] args) {
		List<String>erfileList = new ArrayList<String>(); 
	 	erfileList.add("./selected/hostBilling/HospitalBillingDFGer01350.txt");
	 	Map< Double,Map<String,Double>> erList=new HashMap< Double,Map<String,Double>>();
	 	List<String> er=null;
	 	double i=0;
	 	for(String file:erfileList)
	 	{
	 		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
	 			String line;
	 			er = new ArrayList<String>();
	 			while ((line = br.readLine()) != null) {
	 				StringTokenizer st = new StringTokenizer(line);
	 				double alpha = Double.parseDouble(st.nextToken());
	 				double T0 = Double.parseDouble(st.nextToken());
	 				//double filter = Double.parseDouble(st.nextToken());
	 				double varer = Double.parseDouble(st.nextToken());
	 				varer=11.25 +((varer-13.90)*0.60/4);
	 				i++;
	 				
	 				er.add(String.format("%.3f",alpha)+" "+String.format("%.3f",T0)+" "/*+String.format("%.3f",filter)*/+" "+varer);
	 			}
	 		} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				FileWriter totalWriter  = new FileWriter(file);
				for(String s :er)
				{
        			totalWriter.write(s+"\n");
				}
				totalWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			
	 	}
		// TODO Auto-generated method stub

	}

}
