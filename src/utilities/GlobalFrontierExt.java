package utilities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

public class GlobalFrontierExt {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try (BufferedReader br = new BufferedReader(new FileReader("2012.txt"))) {
		    String line;
		    List<Point1> list= new ArrayList<Point1>();
		    while ((line = br.readLine()) != null) {
		    	try {

		       StringTokenizer st1=new StringTokenizer(line);
		       double er = Double.parseDouble(st1.nextToken());
		       double size=Double.parseDouble(st1.nextToken());
		       String name=st1.nextToken();
		       String symbol=st1.nextToken();

		    
		       Point1 dp = new Point1(er,size,0,name,symbol);
		       boolean flag=false;
		       List<Point1> rm= new ArrayList<Point1>();
		       for(Point1 p:list)
		       {
		    	   if(dp.size>=p.size && dp.er>=p.er)	    	
		    		   flag=true;
		    	   if(dp.size<=p.size && dp.er<=p.er)	   
		    	   {
		    		   rm.add(p);
		    	   }    	   	  
		       }
		       if(!flag)
		       {
		    	   list.add(dp);
		    	   for(Point1 rp:rm)
			    	   list.remove(rp);
		       }
		       
		    	}catch(Exception e)
		    	{
		    		System.out.println(e);
		    	}
		    	   
		    }
		    list.sort(Comparator.comparingDouble(p -> p.size));
		    for(Point1 p:list)
		    	System.out.println(p.er+" "+p.size+" "+" "+p.name);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

}
class Point1{
	double er;
	double size;
	double privacy;
	String name;
	String symbol;
	public Point1(double er,double size,double privacy,String name,String symbol) {
		this.size=size;
		this.er=er;
		this.privacy = privacy;
		this.name=name;
		this.symbol = symbol;
	}
}
