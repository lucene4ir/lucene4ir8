/*
This Class is Used to report some statistical information about a given field from a given Index
Output Field Information :
    1- Number Of Unique terms
    2- Average term length
    3- Number of Empty terms (Missing Terms )
    4- Total number of terms 
    5- Number of documents ( number of rows )
Created By : Abdulaziz AlQatan - 21/07/2019

 */

package lucene4ir;


/*import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;*/

import lucene4ir.Stats.RetrievalStatistics;

public class ReportCollectionStatisticsApp {


    public static void main (String args[])
    {
        // RunExperimentsRetrievabilityCalculatorApp Function
        // Insert Specific Field Name to iterate or EmptyString to iterate all fieldsR
       RetrievalStatistics  stats = new RetrievalStatistics() ;
       stats.calculateRetrievalStatistics("C:\\Users\\kkb19103\\Desktop\\result.res", "out" , 100);
      // rc.calculateRetrievalStatistics("out/result.res",100);
    } // End Main Function
}

/*
class IndexStatistics {

    */
/*
    General Input Parameters
    1- Field Name
     *//*

     String fldName;

    */
/*
   Output Field Properties
   1- Number Of Unique terms
   2- Average term length
   3- Number of Empty terms (Missing Terms )
   4- Total number of terms 
   5- Number of documents ( number of rows )
   *//*

    public int uniqueTermsCtr ,emptyTermsCtr , allTermsCtr , docsCtr;
    public double avgTermLength;

    // General Variables
    IndexReader reader;

    private String getFieldName(int fldID) {
        // Get Field Name by its ID
        return reader.leaves().get(0).reader().getFieldInfos().fieldInfo(fldID).name;
    } // End Function

    private void openReader(String indexName) throws IOException {
        // Open Index Reader based on the given index
        reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexName)));
    } // End Function

    private void reset() {
        // Reset All Counters
        uniqueTermsCtr = 0;
        emptyTermsCtr = 0;
        allTermsCtr = 0;
    }

    private void displayResults()
    {
        // Display Results
        String output = String.format( "Field Name : %s\n" +
                        "------------------\n" +
                        "1- Unique Terms Count = %d\n" +
                        "2- Average Term Length = %f\n" +
                        "3- Empty Terms Count = %d\n" +
                        "4- Total Terms Count = %d\n" +
                        "5- Documents Count = %d\n" ,
                fldName, uniqueTermsCtr , avgTermLength ,
                emptyTermsCtr , allTermsCtr , docsCtr);
        System.out.println(output);
    } // End Function


    private void iterateField() throws IOException {
        // Iterate through the given Field (fldName) and calculate the statistics for its terms
        String currentTerm;
        ArrayList<String> terms = new ArrayList<String>(); // Record All
        ArrayList<String> replicatedTerms = new ArrayList<String>();

        // Number Of Documents
        reset();
        docsCtr = reader.maxDoc();
        for (int i = 0; i < docsCtr; i++) {
            currentTerm = reader.document(i).get(fldName).trim();

            // Empty Terms Count
            if (currentTerm.isEmpty()) {
                emptyTermsCtr++;
                continue;
            } // end if (currentTerm.isEmpty())

            // Unique Terms Count
            if (terms.contains(currentTerm)) {
                if (!replicatedTerms.contains(currentTerm)) {
                    uniqueTermsCtr--;
                    replicatedTerms.add(currentTerm);
                } // End if (!replicateTerms.contains(currentTerm))
            } // end  if (terms.contains(currentTerm))
            else {
                uniqueTermsCtr++;
                terms.add(currentTerm);
            } // End Else

            // Total Terms
            allTermsCtr += currentTerm.split(" ").length;
        } // End For
        // Average Term Length
        avgTermLength = allTermsCtr * 1.0 / docsCtr;
    } // End Function

    public void calculateIndexStatistics(String inFieldName , String indexName)
    {
        // The RunExperimentsRetrievabilityCalculatorApp Mystro Function The Coordinate The Process of Statistics Calculation
        ArrayList<String> fieldNames = new ArrayList<String>();

        int numberOfFields;
        fldName = inFieldName;
        try {
            openReader(indexName);
            if (fldName.isEmpty())
            {
                // Iterate All Fields
                numberOfFields = reader.leaves().get(0).reader().getFieldInfos().size();
                for (int i = 0 ; i < numberOfFields ; i++)
                {
                    fldName = getFieldName(i);
                    iterateField();
                    displayResults();
                } // End For
            } // End IF
            else
            {
                // Iterate Specific Field
                iterateField();
                displayResults();
            };

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        } // End Catch
    } // End Function
} // End Function

class RetrievalStatistics
{

    */
/*
    Output Parameters From Retrieval Results File
    1- docMap  (DocID , Frequency )
    2- QueryMap QryID , Frequency --> if Frequency < c
    3- Total Number Of Queries
    4- Number Of Queries Less Than c
    5- Expected Results
    6- CurrentResults
     *//*


    public HashMap<Integer, Integer> docMap, // (DocID , Frequency )
                              qryMap; // QryID , Frequency --> if Frequency < c

    public int totalQryCtr , limitedQryCtr , lineCtr;
    // Constructor Method
    public RetrievalStatistics()
    {
        docMap = new HashMap<Integer, Integer>();
        qryMap = new HashMap<Integer, Integer>();
    }

    private void resetOutput ()
    {
        totalQryCtr =0;
        limitedQryCtr = 0;
        lineCtr = 0;
    }
    private void displayResults (String outDir , int c)
    {
        String output = String.format("Total Queries = %d\n" +
                                      "Number Of Limited Queries < c : %d\n" +
                                      "Expected Number Of Results : %d\n" +
                                      "Current Results : %d\n" ,
                                       totalQryCtr,limitedQryCtr,c * totalQryCtr , lineCtr);
        System.out.println(output);
    }


    public void calculateRetrievalStatistics(String inFile , String outDir , int c)
    {
        String line , parts[];
        int qryID , docID , rank ,  qryCtr = 1 , oldQryID = 0;

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
            displayResults(outDir,c);
        } // End Try
        catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        } // End Catch
    } // End Function

}*/
