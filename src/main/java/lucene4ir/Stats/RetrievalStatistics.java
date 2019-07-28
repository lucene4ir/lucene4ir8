/*
This Class is Used to report some statistical information about a given Lucene4IR RetrievalApp result file

 Output Parameters From Retrieval Results File
    1- docMap  (DocID , Frequency )
    2- QueryMap QryID , Frequency --> if Frequency < c
    3- Total Number Of Queries
    4- Number Of Queries Less Than c
    5- Expected Number Of Results
    6- Current Number Of Results
    7- Number of missing results
    8- Percentage of missing results


Created By : Abdulaziz AlQatan - 21/07/2019
 */

package lucene4ir.Stats;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RetrievalStatistics
{
    /*
    Output Parameters From Retrieval Results File
    1- docMap  (DocID , Frequency )
    2- QueryMap QryID , Frequency --> if Frequency < c
    3- Total Number Of Queries
    4- Number Of Queries Less Than c
    5- Expected Number Of Results
    6- Current Number Of Results
    7- Number of missing results
    8- Percentage of missing results
     */

    public HashMap<Integer, Integer> docMap, // (DocID , Frequency )
            qryMap; // QryID , Frequency --> if Frequency < c

    public int totalQryCtr , limitedQryCtr , lineCtr , expectedResults;
    public String inFile;

    private void resetOutput ()
    {
        totalQryCtr =0;
        limitedQryCtr = 0;
        lineCtr = 0;
        docMap = new HashMap<Integer, Integer>();
        qryMap = new HashMap<Integer, Integer>();
    }
    private void displayResults (String outDir) throws Exception
    {
        // Display Statistics Results

        // Display Output Parameters
        System.out.println("\nRetrieval File Statistics");
        System.out.println("-----------------------------");
        System.out.println("Retrieval File : '" + inFile + "'");
        System.out.println("Total Number of Retrieved Documents : " + docMap.size());
        System.out.println("Total Queries = " + totalQryCtr);
        System.out.println("Number Of Limited Queries < c : " + limitedQryCtr);
        System.out.println("Expected Number Of Results : " + expectedResults);
        System.out.println("Current Number Of Results : " + lineCtr);
        System.out.println("Missing Results  = " + (expectedResults - lineCtr));
        System.out.println(String.format("Missing Percentage = %%%2.2f",
                (expectedResults - lineCtr) * 100.0 /  expectedResults));

        /*
        Display Results of Document Map and Query Map
        Output Query Map
         */
        String outFileName = outDir + "/retQryMap.sts" , line;
        Iterator it = qryMap.entrySet().iterator();
        Map.Entry item;
        PrintWriter pr = new PrintWriter(new FileWriter(outFileName));
        while (it.hasNext())
        {
              item = (Map.Entry) it.next();
              line = item.getKey() + " " + item.getValue() + "\n";
              pr.write(line);
        } // End while
        pr.close();

        // output Document Map
        outFileName = outDir + "/retDocMap.sts";
        it = docMap.entrySet().iterator();
        pr = new PrintWriter(new FileWriter(outFileName));
        while (it.hasNext())
        {
            item = (Map.Entry) it.next();
            line = item.getKey() + " " + item.getValue() + "\n";
            pr.write(line);
        } // End while
        pr.close();
    } // End Function

    private void close(){
        // Close Function to release Memory
        qryMap = null;
        docMap = null;
    } // End Function

    public void calculateStatistics(String inputFile , String outDir , int c)
    {
        String line , parts[];
        int qryID , docID , rank ,  qryCtr = 1 , oldQryID = 0;

        inFile = inputFile;
        resetOutput();
        try {
            BufferedReader br = new BufferedReader(new FileReader(inFile));
            while ((line = br.readLine()) != null) {
                lineCtr++;
                parts = line.split(" ", 5);
                qryID = Integer.parseInt(parts[0]);
                docID = Integer.parseInt(parts[2]);
                rank = Integer.parseInt(parts[3]);

                // Process Document ID
                if (docMap.containsKey(docID))
                    docMap.put(docID, docMap.get(docID) + 1);
                else
                    docMap.put(docID, 1);

                // Process Query ID & Rank
                if (rank == 1)
                // new Query
                {
                    if (qryCtr < c && lineCtr > 1)
                    {
                        qryMap.put(oldQryID,qryCtr);
                        limitedQryCtr++;
                    } // End If
                    else if (qryCtr == c)
                        qryMap.put(oldQryID,c);
                    qryCtr = 1;
                    totalQryCtr++;
                } // End IF
                else
                // Repeted Query
                {
                    qryCtr++;
                    oldQryID = qryID;
                } // End Else
            } // End While
            expectedResults = c * totalQryCtr;

            displayResults(outDir);
            close();
        } // End Try
        catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        } // End Catch
    } // End Function

    public static void main(String[] args) {
        String path = "out/result.res";
        RetrievalStatistics sts = new RetrievalStatistics();
        sts.calculateStatistics(path,"out" , 100);
    } // End Main Function
} // End Class
