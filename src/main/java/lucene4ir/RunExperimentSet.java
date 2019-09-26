package lucene4ir;

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
        String qryCount , indexFolder , C;

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
        p.queryFile = String.format("out/%s/Queries/%s.qry" , p.corpus,qryCount);
        indexFolder = p.indexName;
        p.indexName = p.corpus + p.indexName;
        //   Sample Output
        //   out\Core17\UnigramIndex\50\C1000\CombinedgramFilter
        p.outputDir = String.format("out/%s/%s/%s/%s/CombinedgramFilter" ,
                p.corpus , indexFolder , qryCount , C);
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
        StatisticsRetrieval sts = new StatisticsRetrieval();
        String outFileName = p.outputDir + "/result" + b + ".res";
        sts.calculateStatistics(outFileName,"",100);
        return String.format("%s %d %d %d\n",b , sts.lineCtr , sts.docCtr , sts.limitedQryCtr);
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
        String qrelFile , trecEvalLine;
        if (p.corpus == "Core17")
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
        {
            printOutput(p.outputDir + "/trecValues.trec", result);
        } // End if
    } // End Function

    private void processExperimentSet () throws Exception
    {
        // This function is used to process the whole experiment Set
      //  double bv[] = {0.1 , 0.2 , 0.25 , 0.3 , 0.35 , 0.4 , 0.45 , 0.5 , 0.6 , 0.7 , 0.8 , 0.9 , 0.95 , 0.99 };
        String b , resSts = "" , retSts = "" , bashLines = "" , retFolder;

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

                else
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

    public static void main(String[] args) {
        // write your code here
        String experimentsPath = "params/BMExperimentSets";
        String retTypes[] = {"" , "DocumentCounter" , "Gravity" } ;
        short beginRetType = 0;
        CrossDirectoryClass cr = new CrossDirectoryClass();
        ArrayList<String> fileList = cr.crossDirectory(experimentsPath,false);
        RunExperimentSet re = new RunExperimentSet();

        try {
        for (String file:fileList) {
            re.readParamsFromFile(file);
            switch (re.p.exType)
            {
                case "Performance":
                    re.printPerformanceValues();
                    break;
                case "Retrieval":
                    re.processExperimentSet();
                    break;
                case "DocumentCounter":
                case "Gravity":
                    re.retType = re.p.exType;
                    re.processExperimentSet();
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
                    re.retType = retTypes[i];
                    re.processExperimentSet();
                } // End For
            } // End if

            System.out.println("Experiment " + file + " is done successfully");

        } // End Try
        } catch (Exception e) {
            e.printStackTrace();
        } // End Catch

    } // End Function Main
} // End Class

class ExperimentSetParams  {
    public String indexName,
            corpus ,
            maxResults,
            tokenFilterFile,
            queryFile,
            exType,
            retrievalParamsFile,
            retrievabilityParamsFile,
            outputDir;
}
