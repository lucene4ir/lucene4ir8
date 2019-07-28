/*
This Class is Used to :
        Calculate Statistics of Retrievability Calculator Result File  based on :
        1- Input Retrievability Calculator result file (DocID , r Values)

        Current results are :
        1- Number Of Documents
        2- Sum Of r Values
        3- Average Of r Values

        Create By - ABDULAZIZ ALQATTAN - 28/07/2019
        */

package lucene4ir.Stats;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class RetrievabilityCalculatorStatistics {


    public void calculateStatistics (String fileName)
    {
        String line , parts[] , output;
        double r , sum = 0 ; // Sum of r Values
        int ctr = 0; // Document Count

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));

            while((line = br.readLine()) != null)
            {
                parts = line.split(" ");
                r = Double.parseDouble(parts[1]);
                ctr ++;
                sum += r;
            } // End While

            System.out.println("\nRetrievability Calculator Statistics ");
            System.out.println("---------------------------------");
            System.out.println("Document : '" + fileName + "'");
            System.out.println("The Number Of Documents : " + ctr);
            System.out.println(String.format("The Sum of r Values : %1.4f" , sum));
            System.out.println(String.format("The Average of r Values : %1.4f" , (sum / ctr)));
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } // End Catch
    } // End Function
    public static void main(String[] args) {
        String path = "out/G/100K/RetrievabilityCalculatorList.ret";
        RetrievabilityCalculatorStatistics sts = new RetrievabilityCalculatorStatistics();
        sts.calculateStatistics(path);
    } // End Main Function
}
