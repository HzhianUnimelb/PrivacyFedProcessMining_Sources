package utilities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

public class FrontierEXT {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try (BufferedReader br = new BufferedReader(new FileReader("nasa2.txt"))) {
		    String line;
		    List<Point> list= new ArrayList<Point>();
		    while ((line = br.readLine()) != null) {
		    try {
		    		
		        StringTokenizer st = new StringTokenizer(line.substring(0,line.length()-1),"(");
		    
		        st.nextToken();
		       String candidStr =st.nextToken();
		       StringTokenizer st1=new StringTokenizer(candidStr);
		       double er = Double.parseDouble(st1.nextToken());
		       double size=Double.parseDouble(st1.nextToken());
		       
		       Point dp = new Point(er,size);
		       boolean flag=false;
		       List<Point> rm= new ArrayList<Point>();
		      
		       for(Point p:list)
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
		    	   for(Point rp:rm)
			    	   list.remove(rp);
		       }
		       
		    	
		    	   
		    }catch(Exception e)
		    {
		    	
		    }
		  }
		    list.sort(Comparator.comparingDouble(p -> p.size));
		    for(Point p:list)
		    	System.out.println(p.er+" "+p.size+" d");
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

}
class Point{
	double er;
	double size;
	public Point(double er,double size) {
		this.size=size;
		this.er=er;
	}
}
