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
import java.util.ArrayList;
import java.util.Collections;

public class StatisticsRetrievabilityCalculator {


    public double sum , // Sum Of r Values
                G , // G value
                avg; // Average Of r Val;ues
    public int zeroDocCtr , // Counter of Document with r = 0
               nonZeroDocCtr; // Counter of Document with r != 0


    private void resetOutput ()
    {
        sum = 0;
        zeroDocCtr = 0;
        nonZeroDocCtr = 0;
        G = 0;
        avg = 0;
    }
    public void calculateStatistics (String fileName)
    {
        String line , parts[] , output;
        double r  , numerator = 0 ;
        int N; // Total Document Count
        ArrayList<Double> rValues = new ArrayList<Double>();

        resetOutput();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));

            while((line = br.readLine()) != null)
            {
                parts = line.split(" ");
                r = Double.parseDouble(parts[1]);

                if (r == 0)
                    zeroDocCtr++;
                else
                {
                    sum += r;
                    nonZeroDocCtr++;
                } // End Else
                rValues.add(r);
            } // End While
            br.close();

            N = zeroDocCtr + nonZeroDocCtr;
            // Calculate G
            Collections.sort(rValues);
            for (int i = 1 ; i <= N ; i++)
                numerator += (2 * i - N - 1) * rValues.get(i-1);

            G = numerator / (sum * N);
            avg = sum / N;

            /*System.out.println("\nRetrievability Calculator Statistics ");
            System.out.println("---------------------------------");
            System.out.println("Document : '" + fileName + "'");
            System.out.println(String.format("G : %1.6f" , G));
            System.out.println("The Total Number Of Documents : " + N);
            System.out.println("The Number of Documents (r = 0) : " + zeroDocCtr);
            System.out.println("The Number of Documents (r != 0) : " + nonZeroDocCtr);
            System.out.println(String.format("The Sum of r Values : %1.4f" , sum));
            System.out.println(String.format("The Average of r Values : %1.4f" , avg));*/

        } catch (IOException e) {
            e.printStackTrace();
        } // End Catch
    } // End Function
    public static void main(String[] args) {
        String path;

        path = args[0];
        StatisticsRetrievabilityCalculator sts = new StatisticsRetrievabilityCalculator();
        sts.calculateStatistics(path);
    } // End Main Function
}
