package utilities;
import java.io.*;

public class FileLineCopier {
    public static void main(String[] args) {
        // Specify the input and output file paths
        String inputFilePath = "BPI Challenge 2018.xes"; // Change this to your input file path
        String outputFilePath = "BPI Challenge 2018-copy.xes"; // Change this to your output file path
        int linesToCopy = 2900000; // Specify the number of lines to copy

        copyLines(inputFilePath, outputFilePath, linesToCopy);
    }

    public static void copyLines(String inputFilePath, String outputFilePath, int linesToCopy) {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;
            int currentLine = 0;

            while ((line = reader.readLine()) != null && currentLine < linesToCopy) {
                writer.write(line);
                writer.newLine(); // Write a new line in the output file
                currentLine++;
                if(currentLine>2800000)
                	if(line.trim().compareTo("</trace>")==0)
                	{
                		System.out.println(line);
                		break;
                	}
                	

            }
            writer.write("</log>");
            writer.newLine(); 
            System.out.println("Copied " + currentLine + " lines from " + inputFilePath + " to " + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}