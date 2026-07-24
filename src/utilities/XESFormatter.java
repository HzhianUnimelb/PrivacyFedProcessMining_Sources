package utilities;

import java.io.*;
import java.nio.file.*;
import java.util.StringTokenizer;
import java.util.regex.*;

public class XESFormatter {
    public static void main(String[] args) {
        String inputFile = "BPIC15_4.xes";
        String tempFile = "BPIC15.xes";
        Pattern pattern = Pattern.compile("(<string\\s+key=\"action_code\"\\s+value=\")([^\"]*_([A-Z]+)_[^\"]*)(\"/>)");
        System.out.println("1");
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
        	System.out.println("2");
            String line;
            while ((line = reader.readLine()) != null) {
            	System.out.println(line);
            	if (line.contains("key=\"concept:name\"")) {
                    // Extract the value attribute
                    int valueStart = line.indexOf("value=\"") + 7; // Start of the value content
                    int valueEnd = line.indexOf("\"", valueStart); // End of the value content
                    String value = line.substring(valueStart, valueEnd);

                    // Split the value by underscores and extract the desired part
                    StringTokenizer st = new StringTokenizer(value, "_");
                    if(st.hasMoreElements())
                    {
                       st.nextToken();
                       if(st.hasMoreElements())
                       {
                        String newValue = st.nextToken(); // Extract the second part (e.g., AWB45)

                        // Reconstruct the line with the modified value
                        String modifiedLine = line.substring(0, valueStart) + newValue + line.substring(valueEnd);
                        writer.write(modifiedLine+"\n");
                       }
                       else
                       	writer.write(line+"\n");
                    }
                    else
                    	writer.write(line+"\n");	
            	}
            	else
            		writer.write(line+"\n");
            }
            writer.close();
        } catch(Exception e)
        {
        	
        }
        // Safely replace the original file
    }
}