/*
This Class is Used to :
        Calculate Retrievability of all documents in a given index based on :
        1- Index Name
        2- Query Weight File - list all of used queries and their weights initiated by BigramGenerator (QueryID - Query)
        3- Res File - Result File From Running RetrievalApp over the previous input queries and index Name
        4- b , c : simple numeric factors to tune the results

        Create By - ABDULAZIZ ALQATTAN - 19/06/2019
        */

package lucene4ir;

 import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlRootElement;
 import java.io.*;
 import java.nio.file.Paths;
import java.util.*;

public class RetrievabilityCalculatorApp {
    // Properties
    public RetrievabilityCalculatorParams p;

    // Private Properties
    private double totalWeights;


    // public properties
    public String retrievabilityParamFile;
    public HashMap<String, Double> docMap ;
    public HashMap<Integer, Double> qryMap;
    public double G , rSum;
    public int zeroRCtr;

    // Constructor Method
    public RetrievabilityCalculatorApp(String inputParameters) {
        retrievabilityParamFile = inputParameters;
        docMap = new HashMap<String, Double>();
        qryMap = new HashMap<Integer, Double>();

    }

    private void displayMsgThenExit(String msg)
    {
        System.out.println(msg);
        System.exit(0);
    } // End Function

    private void readParamsFromFile() {
         /*
        This function is used to :
         1- Read XML parameters file
         2- Fill these parameters into local class parameter
         */
        System.out.println("Reading Param File");
        p = JAXB.unmarshal(new File(retrievabilityParamFile), RetrievabilityCalculatorParams.class );
        if (p.indexName.toString().isEmpty())
            displayMsgThenExit("IndexName Parameter is Missing");
        System.out.println("Index: " + p.indexName);
        if (p.retFile.toString().isEmpty())
            displayMsgThenExit("Query Output Path Parameter is Missing");
        if (p.resFile.toString().isEmpty())
            displayMsgThenExit("Result File Parameter is Missing");
      /*  if (p.queryFile.toString().isEmpty())
            displayMsgThenExit("Query File Parameter is Missing");*/
        if (p.c < 1)
            p.c = 0;
    } // End Function

  /*  private void createShortQueryFile () throws Exception
    {
        Create Short Query File to be used by RetrievalAPP ( QryID - Query)
        put it in the same folder as input query file
        name it shortGram.out
        String qryFile , line , parts[];
        int slashLoc , maxLimit = 4;
        slashLoc = p.queryFile.lastIndexOf("/");
        if (slashLoc == 0)
            slashLoc = p.queryFile.lastIndexOf("\\");
        qryFile = p.queryFile.substring(0,slashLoc + 1) + "shortGram.out";
        PrintWriter pr = new PrintWriter(qryFile);
        BufferedReader br = new BufferedReader(new FileReader(p.queryFile));

        while ((line = br.readLine()) != null)
        {
            parts =  line.split(" ",maxLimit);
            line = "";
            for (int i = 0 ; i < maxLimit - 1; i++)
                line += parts[i] + " ";
            pr.write(line + "\n");
        } // End While
        br.close();
        pr.close();
    } // End Function

    */

    private void calculateG(ArrayList<Double> rValues)
    {
        /*
        Given array of R values >>> Calculate G Coefficient by :
            1- Sort r values ascendingly
            2- G = Numerator / Denominator
            3- Numerator = Σ (i = 1 to N)  ( 2 * i - N - 1) * r
            4- Deniminator = N * Σ (i = 1 to N) r
            While ,
            r = Retrievability , N = NUmber Of Documents
        */
        int N;
        double r , numerator = 0 , result = 0;

        N = rValues.size() + 1;
        // Sort input R Values Ascendingly
        Collections.sort(rValues);
        this.zeroRCtr = 0;
        for (int i = 1 ; i <= rValues.size() ; i++)
        {
            r = rValues.get(i-1);
            numerator += (2 * i - N) * r;
           // denominator += r;
            if (r == 0)
                zeroRCtr++;
        }
        result = numerator / (--N * this.rSum);
        //this.rSum = denominator;
        this.G = result;
    } // End Function


    private void displayResultsAndCalculateG() throws Exception
    {
        /*
        Display The Results as Needed For the whole Document Vector
        Sort r Values Ascendingly
        Calculate G Coefficient
        */
        String line , docID;
        Map.Entry item;
        Iterator it = docMap.entrySet().iterator();
        double r ;
        ArrayList<Double> rValues = new ArrayList<Double>();

        PrintWriter pr = new PrintWriter(p.retFile);
        while (it.hasNext()) {
            item = (Map.Entry) it.next();
            r = (double) item.getValue();
            docID =  item.getKey().toString();
            rValues.add(r);
            // Format output line
            line = String.format("%s %f\n", docID, r);
            // Display Line
            pr.print(line);
            System.out.print(line);
        } // End While

        calculateG(rValues);
        line = "The G Coefficient = " + this.G;
        System.out.println(line);
        pr.close();
    } // End Function


    private double costFunction (int rank )
    {
        double result = 1.0;
        if (rank <= p.c)
            // Gravity Exponential Function
             result = 1.0 / Math.pow(rank,p.b);
        return result;
    } // End Function

    private boolean isCumulative()
    {
        return p.b == 0;
    }

    private double calculateR (int qryID , int rank)
    {
      //  Calculate Utility/Cost Function
        double result  , weight = 1;

        // If query weight is not exist weight = 1
        if (!p.queryWeightFile.isEmpty())
            weight = qryMap.get(qryID);

        if (rank < 1)
            // If Zero Rank r = 0
            result = 0;
        // If No Cost Don't Calculate it
        else if (rank == 1 || isCumulative())
            result = weight;
        else
            result = weight * costFunction(rank);
        return result;
    } // End Function

    private void readRetrievalResultsAndCalculateR() throws Exception {
        /*Read RetrievalAPP Results File
        Calculate Retrievability for Each document Line
        Add the retrievability to docMap*/

        String line, parts[] , docid;
        int  qryid , rank ;
        double r = 1; // Default for document counter method (counting documents in .res file)
        rSum = 0;
        BufferedReader br = new BufferedReader(new FileReader(p.resFile));
        while ((line = br.readLine()) != null) {
            parts = line.split(" ", 5);
            docid = parts[2].trim();

            // Document Counter Lines
            if (!isCumulative()) {
                qryid = Integer.parseInt(parts[0]);
                rank = Integer.parseInt(parts[3]);
                r = calculateR(qryid, rank);
            } // End if
            rSum += r;
            // End Document Counter Lines
            if (docMap.containsKey(docid))
                docMap.put(docid, docMap.get(docid) + r);
            else
                System.out.println(docid);

        } // End While

        br.close();
    } // End Function

    private void initQueryMap( ) throws Exception
    {
        String line, parts[] ;
        int qryID;
        double weight;

        if (!p.queryWeightFile.isEmpty())
        {
            BufferedReader br = new BufferedReader(new FileReader(p.queryWeightFile));
            while ((line = br.readLine()) != null)
            {
                parts = line.split(" ",2);
                qryID = Integer.parseInt(parts[0]);
                weight = Double.parseDouble(parts[1]);
                qryMap.put(qryID,weight);
            } // End While
            br.close();
        } // End If

    } // End Function


    private void close()
    {
        // Close function to release memory
        docMap = null;
        qryMap = null;
    } // End Function

    public void initDocumentMap(String indexName) {

        // Initialize Document Hash MAP (docid , r = 0)
        // Local Variables
        IndexReader reader;
        long docCount;
        String docid;
        final String docIDField = Lucene4IRConstants.FIELD_DOCNUM;

        docMap = new HashMap<String, Double>();
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexName)));
        docCount = reader.maxDoc();

        for (int i = 0; i < docCount; i++) {
            docid = reader.document(i).get(docIDField);
            docMap.put(docid, 0.0);
        } // End For
        reader.close();
        } // End Try
        catch (IOException e) {
            e.printStackTrace();
        } // End Catch
    } // End Function

    public void calculate() {
        // Mystro Method that coordinate the process
        try {
            readParamsFromFile();
            if (docMap.size() < 1)
                initDocumentMap(p.indexName);
            initQueryMap();
            readRetrievalResultsAndCalculateR();
            displayResultsAndCalculateG();
            close();
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }
    } // End Function

    public HashMap<String, Double> cloneMap (String indexName)
    {
        this.initDocumentMap(indexName);
        return (HashMap<String, Double>) docMap.clone();
    }

    public void setMap (HashMap<String, Double> inMap)
    {
       docMap = (HashMap<String, Double>) inMap.clone();
    }
    public static void main(String args[]) {
        // RunExperimentsRetrievabilityCalculatorApp (Mystro Function) - Coordinate the process
        String inputParamFile;

        inputParamFile = args[0];
       // RetrievabilityCalculatorApp rc = new RetrievabilityCalculatorApp(inputParamFile);
        //rc.calculate();
    } // End Function


    @XmlRootElement(name = "RetrievabilityCalculatorParams")
    static
    public class RetrievabilityCalculatorParams {
        public String indexName , retFile ,resFile , queryWeightFile ;
        public int    c;
        public float b;

    }
}

