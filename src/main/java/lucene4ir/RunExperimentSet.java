package lucene4ir;

import com.sun.deploy.security.SelectableSecurityManager;
import lucene4ir.Stats.StatisticsRetrievabilityCalculator;
import lucene4ir.Stats.StatisticsRetrieval;
import lucene4ir.utils.CrossDirectoryClass;
import lucene4ir.utils.XMLTextParser;

import javax.xml.bind.JAXB;
import java.io.*;
import java.util.ArrayList;

public class RunExperimentSet {
    private ExperimentSetParams p;
    private String
            biFilter = "params/index/p.tokenFilterFile_Bigram.xml",
            uniFilter = "params/index/p.tokenFilterFile_Unigram.xml" ,
            combinedFilter = "params/index/p.tokenFilterFile_Combinedgram.xml" ,
            tokenFilterFile = combinedFilter;

    private void fillAutoParameters()
    {
        String qryCount , indexFolder = "";
        if (p.maxResults.equals("1000"))
        {
            p.queryFile = "out/Core17/General/Queries/50.qry";
            qryCount = "/50/C1000/";
        } // End if
        else
        {
            p.queryFile = "out/Core17/General/Queries/300K.qry";
            qryCount = "/300K/C100/";
        } // End else

        if (p.indexName.equals("Core17UnigramIndex"))
            indexFolder = "UnigramIndex";
        else  if (p.indexName.equals("Core17BigramOnlyIndex"))
            indexFolder = "BigramIndex";
        else  if (p.indexName.equals("Core17CombinedIndex"))
            indexFolder = "CombinedIndex";
        else  if (p.indexName.equals("Core17FieldedIndex"))
            indexFolder = "FieldedIndex";

        //   Sample Output
        //   out\Core17\UnigramIndex\50\C1000\CombinedgramFilter
        p.outputDir = "out/Core17/" + indexFolder + qryCount + "CombinedgramFilter";
    }

    private boolean readParamsFromFile(String paramFile) throws Exception

    {
        // Read All Parameters and check that all of them are true
        boolean valid;
            p = JAXB.unmarshal(new File(paramFile), ExperimentSetParams.class);
           /* valid =  !( p.maxResults.isEmpty() || p.indexName.isEmpty() ||
                    p.outputDir.isEmpty() || p.queryFile.isEmpty()
                    || p.tokenFilterFile.isEmpty());
            if (valid)*/
           fillAutoParameters();
            return true;
    } // End Function

    private void printOutput (String outPath , String outText) throws Exception
    {
        PrintWriter pr = new PrintWriter(outPath);
        pr.write(outText);
        pr.close();
    }
    private void runRetrievalExperiment(String b)
    {
        // Run RetrievalApp Experiment for given B Value
        String outFileName;
        FieldedRetrievalApp fRetApp;
        RetrievalApp retApp;


        XMLTextParser parser = new XMLTextParser(p.retrievalParamsFile);
        outFileName = p.outputDir + "/result" + b + ".res";
        parser.setTagValue("b", b );
        parser.setTagValue("resultFile",outFileName);
        parser.setTagValue("queryFile",p.queryFile);
        parser.setTagValue("indexName",p.indexName);
        parser.setTagValue("maxResults",p.maxResults);
        parser.save();
        if (p.indexName.contains("Fielded"))
        {
            fRetApp = new FieldedRetrievalApp(p.retrievalParamsFile);
            fRetApp.processQueryFile();
        } // End IF
        else
        {
            retApp = new RetrievalApp(p.retrievalParamsFile);
            retApp.processQueryFile();
        } // End Else
    } // End Function

    private void runRCExperiment (String b)
    {
        // Run Retrievability Calculator Experiments for given B Value
        String outFileName;

        XMLTextParser parser = new XMLTextParser(p.retrievabilityParamsFile);
        outFileName = p.outputDir + "/result" + b + ".res";
        parser.setTagValue("resFile",outFileName);
        outFileName = p.outputDir +  "/RCResults" + b + ".ret";
        parser.setTagValue("retFile",outFileName);
      //  parser.setTagValue("queryWeightFile",queryWeightFile);
        parser.setTagValue("indexName",p.indexName);
        parser.setTagValue("c",p.maxResults);
        parser.save();
        RetrievabilityCalculatorApp rcApp = new RetrievabilityCalculatorApp(p.retrievabilityParamsFile);
        rcApp.calculate();
    } // End Function

    private String runRetrievalStatistics (String b)
    {
        StatisticsRetrieval sts = new StatisticsRetrieval();
        String outFileName = p.outputDir + "/result" + b + ".res";
        sts.calculateStatistics(outFileName,"",100);
        return String.format("%s %d %d %d\n",b , sts.lineCtr , sts.docCtr , sts.limitedQryCtr);
    }
    
    private String runRCStatistics (String b)
    {
        StatisticsRetrievabilityCalculator sts = new StatisticsRetrievabilityCalculator();
        String outFileName = p.outputDir + "/RCResults" + b + ".ret";
        sts.calculateStatistics(outFileName);
        return String.format("%s %1.6f %d %d %1.4f %1.4f\n",b ,sts.G , sts.nonZeroDocCtr , sts.zeroDocCtr , sts.sum , sts.avg );
    }
    
    /*private void runTrecEval (String b)
    {
        String cmd = trecEval.replaceAll("0.0",b);
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("C:/Users/kkb19103/AppData/Local/Microsoft/WindowsApps/ubuntu.exe ls");

        try {
            // Runtime.getRuntime().exec(cmd).waitFor();
            builder.start();
        }
        //catch (InterruptedException e) {
        //    e.printStackTrace();
        //}
        catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
    private String getTrecEvalLine (String b)
    {
        String trecEvalLine  =
                       String.format("~/trecEval/trec_eval ~/trecEval/Results/307-690.qrels ./result%s.res > ./trec%s.trec\n" ,
                       b,b);
        return trecEvalLine;
    }

    private void printPerformanceValues() throws Exception
    {
        String inFile ,
                b,
                line,
                parts[],
                result = "";
        BufferedReader br;
        for (int i = 1 ; i < 10 ; i++)
        {
            b = "0." + i;
            inFile = p.outputDir + "/trec" + b + ".trec";
            br = new BufferedReader(new FileReader(inFile));
            result += b + "\t";
            while ((line = br.readLine()) != null)
            {
                if (line.startsWith("map"))
                {
                    parts = line.split("\t",3);
                    result += parts[2] + "\t";
                } // End if
                else if (line.startsWith("P10"))
                {
                    parts = line.split("\t",3);
                    result += parts[2] + "\n";
                    break;
                } // End else if
            } // End While
        } // End For

        if (!result.isEmpty())
        {
            printOutput(p.outputDir + "/trecValues.trec", result);
        } // End if
    } // End Function

    private void processExperimentSet () throws Exception
    {
        // This function is used to process the whole experiment Set
      //  double bv[] = {0.1 , 0.2 , 0.25 , 0.3 , 0.35 , 0.4 , 0.45 , 0.5 , 0.6 , 0.7 , 0.8 , 0.9 , 0.95 , 0.99 };
        String b , resSts = "" , retSts = "" , performanceValues = "" , bashLines = "";

            for (int i = 1; i < 10; i++) {
              //  b = String.valueOf(bv[i]);
                b = "0." + i;
                // *** Single Experiment
                runRetrievalExperiment(b);
                runRCExperiment(b);

                // *** Statistics ***
                resSts += runRetrievalStatistics(b);
                retSts += runRCStatistics(b);

                // *** Perfoemance Values ***
                bashLines += getTrecEvalLine(b);

            } // End For
            // Print output Files
            printOutput(p.outputDir + "/res.Sts" , resSts);
            printOutput(p.outputDir + "/ret.Sts" , retSts);
            printOutput(p.outputDir + "/bash.sh" , bashLines);

    } // End Function

    public static void main(String[] args) {
        // write your code here
        String experimentsPath = "params/BMExperimentSets";
        CrossDirectoryClass cr = new CrossDirectoryClass();
        ArrayList<String> fileList = cr.crossDirectory(experimentsPath,false);
        RunExperimentSet re = new RunExperimentSet();
        try {
        for (String file:fileList) {
               if (re.readParamsFromFile(file))
            {
                re.processExperimentSet();
               // re.printPerformanceValues();
                System.out.println("Experiment " + file + " is done successfully");
            }
            else
               System.out.println("Reading Parameter File " + file + " failed");

        } // End Try
        } catch (Exception e) {
            e.printStackTrace();
        } // End Catch


    } // End Function Main
} // End Class

class ExperimentSetParams  {
    public String indexName,
            maxResults,
            tokenFilterFile,
            queryFile,
            retrievalParamsFile,
            retrievabilityParamsFile,
            outputDir;
}
