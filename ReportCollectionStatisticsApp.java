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

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ReportCollectionStatisticsApp {

   /*
    Output Field Properties
    1- Number Of Unique terms
    2- Average term length
    3- Number of Empty terms (Missing Terms )
    4- Total number of terms 
    5- Number of documents ( number of rows )
    */
   private int uniqueTermsCtr ,emptyTermsCtr , allTermsCtr , docsCtr;
   double avgTermLength;

   // input properties
    // Index Name
    // Field Name

   public String fldName , indexName;

   // Private Local Properties
   IndexReader reader;

   // Constructor Method
   public ReportCollectionStatisticsApp(String inFldName , String inIndexName)
   {
       fldName = inFldName;
       indexName = inIndexName;
   }

    private void openReader() throws IOException {
       // Open Index Reader based on the given index
        reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexName)));
    }

    private void resetCounters()
    {
        // Reset All Counters
        uniqueTermsCtr = 0;
        emptyTermsCtr = 0;
        allTermsCtr = 0;
    }
    private String getFieldName (int fldID)
    {
        // Get Field Name by its ID
        return reader.leaves().get(0).reader().getFieldInfos().fieldInfo(fldID).name;
    }

    private void iterateField() throws IOException
    {
        // Iterate through the given Field (fldName) and calculate the statistics for its terms
        String currentTerm;

        ArrayList<String> terms = new ArrayList<String>(); // Record All
        ArrayList<String> replicatedTerms = new ArrayList<String>();

        // Number Of Documents
        docsCtr = reader.maxDoc();
        resetCounters();
        for (int i = 0 ; i < docsCtr ; i++)
        {
            currentTerm = reader.document(i).get(fldName).trim();

            // Empty Terms Count
            if (currentTerm.isEmpty())
            {
                emptyTermsCtr++;
                continue;
            } // end if (currentTerm.isEmpty())

            // Unique Terms Count

            if (terms.contains(currentTerm))
            {
                if (!replicatedTerms.contains(currentTerm)) {
                    uniqueTermsCtr--;
                    replicatedTerms.add(currentTerm);
                } // End if (!replicateTerms.contains(currentTerm))
            } // end  if (terms.contains(currentTerm))
            else
                {
                    uniqueTermsCtr++;
                    terms.add(currentTerm);
                } // End Else

            // Total Terms
                allTermsCtr += currentTerm.split(" ").length;
        } // End For
        // Average Term Length
        avgTermLength = allTermsCtr * 1.0 / docsCtr;

    } // End Function

    private void displayResults ()
    {
        String output;
        output = String.format( "Field Name : %s\n" +
                                "------------------\n" +
                                "1- Unique Terms Count = %d\n" +
                               "2- Average Term Length = %f\n" +
                               "3- Empty Terms Count = %d\n" +
                               "4- Total Terms Count = %d\n" +
                               "5- Documents Count = %d\n" ,
                                fldName, uniqueTermsCtr , avgTermLength ,
                                emptyTermsCtr , allTermsCtr , docsCtr);
        System.out.println(output);
    }
   // Main (Mystro) Method
   public void calculateStatistics()
   {
       ArrayList<String> fieldNames = new ArrayList<String>();
       // The Main Mystro Function The Coordinate The Process of Statistics Calculation
       try {
            openReader();
            if (fldName.isEmpty())
                // Iterate All Fields
                for (int i = 0 ; i < 5 ; i++)
                {
                    fldName = getFieldName(i);
                    iterateField();
                    displayResults();
                }
            else
            {
                // Iterate Specific Field
                iterateField();
                displayResults();
            };

       } catch (IOException e) {
           System.out.println(" caught a " + e.getClass() +
                   "\n with message: " + e.getMessage());
       }
   }

    public static void main (String args[])
    {
        // Main Function
        // Insert Specific Field Name to iterate or EmptyString to iterate all fields
       ReportCollectionStatisticsApp rc = new ReportCollectionStatisticsApp("","smallIndex");
       rc.calculateStatistics();
    }
}
