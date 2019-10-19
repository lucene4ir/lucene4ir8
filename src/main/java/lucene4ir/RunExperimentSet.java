package lucene4ir;

import lucene4ir.Stats.StatisticsRetrievabilityCalculator;
import lucene4ir.Stats.StatisticsRetrieval;
import lucene4ir.utils.CrossDirectoryClass;
import lucene4ir.utils.XMLTextParser;

import javax.xml.bind.JAXB;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class RunExperimentSet {
    private HashMap<String, Double> tempDocMap;
    private ExperimentSetParams p;

    private String
            biFilter = "params/index/p.tokenFilterFile_Bigram.xml",
            uniFilter = "params/index/p.tokenFilterFile_Unigram.xml" ,
            combinedFilter = "params/index/p.tokenFilterFile_Combinedgram.xml" ,
            tokenFilterFile = combinedFilter,
            retType;

    private void fillAutoParameters()
    {
        /*
        Fill Parameters Automatically
        Fill QueryFile based on Max Results
        if Max Results = 1000 - Query 50
        else Query 300K
        + fill The outputDir Automatically based on MaxResults & IndexName
         Sample OutputDir
         out\Core17\UnigramIndex\50\C1000\CombinedgramFilter
         */
        String qryCount , indexFolder = "" , C , corpus;

        retType = "";
        if (p.exType.equals("Performance"))
            p.maxResults = "1000";

        if (p.maxResults.equals("1000"))
        {
            qryCount = "50";
            C = "C1000";
        } // End if
        else
        {
            qryCount = "300K";
            C = "C100";
        } // End else

        corpus = getCorpus(p.indexName);
        p.queryFile = String.format("out/%s/Queries/%s.qry" , corpus,qryCount);

        if (p.indexName.contains("Combined"))
            indexFolder = "CombinedIndex";
        else if (p.indexName.contains("Unigram"))
            indexFolder = "UnigramIndex";
        else if (p.indexName.contains("Bigram"))
            indexFolder = "BigramIndex";
        else if (p.indexName.contains("Fielded"))
            indexFolder = "FieldedIndex";


        //   Sample Output
        //   out\Core17\UnigramIndex\50\C1000\CombinedgramFilter
        p.outputDir = String.format("out/%s/%s/%s/%s/CombinedgramFilter" ,
                corpus , indexFolder , qryCount , C);
    }
    private void readParamsFromFile(String paramFile) throws Exception
    {
        // Read All Parameters and check that all of them are true
        p = JAXB.unmarshal(new File(paramFile), ExperimentSetParams.class);
       fillAutoParameters();
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

    private String getRetTypeFolder()
    {
        /*   exType (DocumentCounter) = "/DocumentCounter";*/
        /* exType (Gravity) = "/GravityWeightB0.5C100";*/
        String result = "";
        if (retType == "Gravity")
            result =   "/GravityWeightB0.5C100";
        else
            result =   "/DocumentCounter";
        return result;
    }
    private void runRCExperiment (String b)
    {

        // Run Retrievability Calculator Experiments for given B Value
        String outFileName;
        XMLTextParser parser = new XMLTextParser(p.retrievabilityParamsFile);
        outFileName = p.outputDir + "/result" + b + ".res";
        parser.setTagValue("resFile",outFileName);
        outFileName = p.outputDir + getRetTypeFolder() +  "/RCResults" + b + ".ret";
        parser.setTagValue("retFile",outFileName);
      //  parser.setTagValue("queryWeightFile",queryWeightFile);
        parser.setTagValue("indexName",p.indexName);
        parser.setTagValue("c",p.maxResults);
        parser.setTagValue("retType",retType);
        parser.save();
        RetrievabilityCalculatorApp rcApp = new RetrievabilityCalculatorApp(p.retrievabilityParamsFile);
        rcApp.calculate();
    } // End Function

    private String runRetrievalStatistics (String b)
    {
        int maxResultsInt;
        StatisticsRetrieval sts = new StatisticsRetrieval();
        String outFileName = p.outputDir + "/result" + b + ".res";
        maxResultsInt = Integer.parseInt(p.maxResults);
        sts.calculateStatistics(outFileName,"",maxResultsInt);
        return String.format("%s %d %d %d\n",b , sts.lineCtr , sts.docCtr , sts.limitedQryCtr);
    }
    private String getCorpus (String indexName)
    {
        String result = "";

        if (!indexName.isEmpty())
            if (indexName.startsWith("Core17"))
                result = "Core17";
            else
                result = "Aquaint";
        return result;
    }

    private String runRCStatistics (String b)
    {
        StatisticsRetrievabilityCalculator sts = new StatisticsRetrievabilityCalculator();
        String outFileName = p.outputDir + getRetTypeFolder() +  "/RCResults" + b + ".ret";
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
        String qrelFile , trecEvalLine , corpus;
        corpus = getCorpus(p.indexName);
        if (corpus.equals("Core17"))
            qrelFile = "307-690.qrels";
        else
            qrelFile = "trec2005.aquaint.qrels";
        trecEvalLine  = String.format("~/trecEval/trec_eval ~/trecEval/Qrels/%s ./result%s.res > ./trec%s.trec\n" ,
                        qrelFile,b,b);
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
            printOutput(p.outputDir + "/trecValues.trec", result);

    } // End Function

    private void processExperimentSet () throws Exception
    {
        // This function is used to process the whole experiment Set
         String b , resSts = "" , retSts = "" , bashLines = "" ;

         for (int i = 1; i < 10; i++) {
              //  b = String.valueOf(bv[i]);
                b = "0." + i;

                if (retType.isEmpty())
                {
                    // ***  Retrieval Processes ***
                    // *** Single Experiment
                    runRetrievalExperiment(b);
                    // *** Statistics ***
                    resSts += runRetrievalStatistics(b);
                    // *** Perfoemance Values ***
                    bashLines += getTrecEvalLine(b);
                }
                // *** Retrievability Calculator Processes ***

                /*   retType = "/Document Counter";*/
                /* retType = "/GravityWeightB0.5C100";*/

                else if (p.maxResults.equals("100"))
                {
                    // Calculation
                    runRCExperiment(b);
                    // *** Statistics ***
                    retSts += runRCStatistics(b);
                }
            } // End For

            // Print output Files
        if (retType.isEmpty())
        {
            printOutput(p.outputDir + "/res.Sts" , resSts);
            printOutput(p.outputDir + "/bash.sh" , bashLines);
        }
        else
            printOutput(p.outputDir + getRetTypeFolder() + "/ret.Sts" , retSts);
    } // End Function

    private void runExperimentFile (String fileName)
    {
        String retTypes[] = {"" , "DocumentCounter" , "Gravity" } ;
        short beginRetType = 0;

        try {
            readParamsFromFile(fileName);
            switch (p.exType)
            {
                case "Performance":
                    printPerformanceValues();
                    break;
                case "Retrieval":
                    processExperimentSet();
                    break;
                case "DocumentCounter":
                case "Gravity":
                    retType = p.exType;
                    processExperimentSet();
                    break;
                case "All":
                    beginRetType = 1;
                    break;
                case "AllRC":
                    beginRetType = 2;
            } // End switch

            if (beginRetType > 0)
            {
                for (int i = beginRetType - 1 ; i < retTypes.length ; i++)
                {
                    retType = retTypes[i];
                    processExperimentSet();
                } // End For
            } // End if
            System.out.println("Experiment " + fileName + " is done successfully");
         } // End Try
            catch (Exception e) {
            e.printStackTrace();
        } // End Catch
    } // End Function

    private void runFileList (String folderName) {
        CrossDirectoryClass cr = new CrossDirectoryClass();
        ArrayList<String> fileList = cr.crossDirectory(folderName, false);
        RunExperimentSet re = new RunExperimentSet();
        for (String file : fileList)
            runExperimentFile(file);
    }

    private void fillParameterFile (String fileName ,  String indexName , String maxResults)
    {
        String corpus  , exType ;

        if (maxResults.equals("1000"))
            exType = "Retrieval";
        else
            exType = "All";

        corpus = getCorpus(indexName);
        XMLTextParser parser = new XMLTextParser(fileName);
        parser.setTagValue("corpus",corpus);
        parser.setTagValue("indexName",indexName);
        parser.setTagValue("maxResults",maxResults);
        parser.setTagValue("exType",exType);
        parser.save();
    } // End Function

    private void runCalculatedList ()
    {
        String paramFileName =  "params/BMExperimentSets/Experiment2.xml",
                indexName , maxResult;
       String[] indexNames = {"Core17UnigramIndex","Core17BigramIndex","Core17CombinedIndex" ,
                "AquaintBigramIndex","AquaintCombinedIndex","AquaintUnigramIndex"};
       //String maxResults[] = {"1000"};


      /* for (int i = 0 ; i < indexNames.length ; i++)
           for (int j=0 ; j < maxResults.length ; j++ )
           {
               p.indexName = indexNames[i];
               fillAutoParameters();

                  fillParameterFile(paramFileName , indexName , maxResult);
               runExperimentFile(paramFileName);
          }
        */

        runExperimentFile(paramFileName);
    }

    public static void main(String[] args)
        {
        // write your code here
        RunExperimentSet re = new RunExperimentSet();
        re.runCalculatedList();
          //  re.runExperimentFile("params\\BMExperimentSets\\Experiment2.xml");
    } // End Function Main
} // End Class

class ExperimentSetParams  {
    public String indexName,
            maxResults,
            queryFile,
            exType,
            retrievalParamsFile,
            retrievabilityParamsFile,
            outputDir;
}
