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

package lucene4ir.Stats;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class IndexStatistics {

    /*
    General Input Parameters
    1- Field Name
     */
    String fldName;

    /*
   Output Field Properties
   1- Number Of Unique terms
   2- Average term length
   3- Number of Empty terms (Missing Terms )
   4- Total number of terms 
   5- Number of documents ( number of rows )
   */
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
        reader.close();
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