package lucene4ir;

import lucene4ir.Stats.StatisticsRetrievabilityCalculator;
import lucene4ir.Stats.StatisticsRetrieval;
import lucene4ir.parse.CSVParser;
import lucene4ir.utils.CrossDirectoryClass;
import lucene4ir.utils.XMLTextParser;

import javax.xml.bind.JAXB;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class RunExperimentSet {
    private ExperimentSetParams p;

    private String
           /* biFilter = "params/index/p.tokenFilterFile_Bigram.xml",
            uniFilter = "params/index/p.tokenFilterFile_Unigram.xml" ,
            combinedFilter = "params/index/p.tokenFilterFile_Combinedgram.xml" ,
            tokenFilterFile = combinedFilter,*/
            csvKey;
    private CSVParser csvPaser;

    private String getBashLine(String b)
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
        String qryCount , indexFolder = "" , C , corpus , csvFile ,
        outPutFolder = "C:\\Users\\kkb19103\\Desktop\\My Files 07-08-2019\\Bias Measurement Experiments";

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
        p.queryFile = String.format("%s/%s/Queries/%s.qry" , outPutFolder, corpus,qryCount);

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
        p.outputDir = String.format("%s/%s/%s/%s/%s/CombinedgramFilter" ,
                outPutFolder , corpus , indexFolder , qryCount , C);
        csvFile = outPutFolder + "/out.csv";
        csvPaser = new CSVParser();
        csvPaser.readFromFile(csvFile);

        /*CSV Key
        corpus - indexType - qryFilter - qryCount - model - maxResults - other - B*/
        csvKey = String.format("%s,%s,%s,%s,%s,%s, ,",
                corpus,indexFolder , "combinedQuery" , qryCount , "BM25" , p.maxResults);
        p.indexName = outPutFolder + "/Indexes/" + p.indexName;
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

    private String getRetTypeFolder(String retType)
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

    private String runRetrievalStatistics (String b)
    {
        int maxResultsInt;
        StatisticsRetrieval sts = new StatisticsRetrieval();
        String outFileName = p.outputDir + "/result" + b + ".res";
        maxResultsInt = Integer.parseInt(p.maxResults);
        sts.calculateStatistics(outFileName,"",maxResultsInt);
        return String.format("%d,%d,%d", sts.lineCtr , sts.docCtr , sts.limitedQryCtr);
    }
    private String runRCStatistics (String b , String retType)
    {
        StatisticsRetrievabilityCalculator sts = new StatisticsRetrievabilityCalculator();
        String outFileName = p.outputDir + getRetTypeFolder(retType) +  "/RCResults" + b + ".ret";
        sts.calculateStatistics(outFileName);
        return String.format("%1.6f,%d,%d,%1.4f,%1.4f",sts.G , sts.nonZeroDocCtr , sts.zeroDocCtr , sts.sum , sts.avg );
    }

    private String runRetrievalExperiment(String b)
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
        return runRetrievalStatistics(b);
    } // End Function

    private String runRCExperiment (String b , String retType)
    {

        // Run Retrievability Calculator Experiments for given B Value
        String outFileName ;
        XMLTextParser parser = new XMLTextParser(p.retrievabilityParamsFile);
        outFileName = p.outputDir + "/result" + b + ".res";
        parser.setTagValue("resFile",outFileName);
        outFileName = p.outputDir + getRetTypeFolder(retType) +  "/RCResults" + b + ".ret";
        parser.setTagValue("retFile",outFileName);
        parser.setTagValue("indexName",p.indexName);
        parser.setTagValue("c",p.maxResults);
        parser.setTagValue("retType",retType);
        parser.save();
        RetrievabilityCalculatorApp rcApp = new RetrievabilityCalculatorApp(p.retrievabilityParamsFile);
        rcApp.calculate();
        return runRCStatistics(b,retType);
    } // End Function
    private String getPerformanceValues(String b) throws Exception
    {
        String inFile ,
                line,
                parts[],
                result = "";
        BufferedReader br;
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
        return result;
    } // End Function
    private void runALLRC (String b)
    {
        String result;
        // Calculation
        result = runRCExperiment(b,"DocumentCounter");
        // *** Statistics ***
        csvPaser.setRetC(result);
        result = runRCExperiment(b,"Gravity");
        csvPaser.setRetG(result);
    }

    private void processExperimentSet () throws Exception
    {
        // This function is used to process the whole experiment Set
         String b , retResult , bashLines = ""  ;


         for (int i = 1; i < 10; i++) {
              //  b = String.valueOf(bv[i]);

                b = "0." + i;
                csvPaser.setKey(csvKey + b);
                csvPaser.resetCSVValues();
                switch (p.exType)
                {
                    case "All":
                        csvPaser.setRes(runRetrievalExperiment(b));
                        bashLines += getBashLine(b);
                        runALLRC(b);
                        break;
                    case "AllRC":
                        runALLRC(b);
                        break;
                    case "DocumentCounter":
                        csvPaser.setRetC(runRCExperiment(b,p.exType));
                        break;
                    case "Gravity":
                        csvPaser.setRetG(runRCExperiment(b,p.exType));
                        break;
                    case "Retrieval":
                        csvPaser.setRes(runRetrievalExperiment(b));
                        bashLines += getBashLine(b);
                        break;
                    case "Performance" :
                        csvPaser.setPerformance(getPerformanceValues(b));
                } // End Switch
                csvPaser.updateCSVLine();
            } // End For

        if (!bashLines.isEmpty())
            printOutput(p.outputDir + "/bash.sh" , bashLines);
        csvPaser.updateCSVFile();

    } // End Function

    private void runExperimentFile (String fileName)
    {
        try {
            readParamsFromFile(fileName);
            processExperimentSet();
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
