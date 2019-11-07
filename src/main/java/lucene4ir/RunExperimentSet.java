package lucene4ir;

import lucene4ir.Stats.StatisticsRetrievabilityCalculator;
import lucene4ir.Stats.StatisticsRetrieval;
import lucene4ir.parse.CSVParser;

import lucene4ir.utils.CrossDirectoryClass;
import lucene4ir.utils.XMLTextParser;

import javax.xml.bind.JAXB;
import java.io.*;
import java.util.ArrayList;

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
            if (indexName.contains("Core17"))
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
        outPutFolder = "C:\\Users\\kkb19103\\Desktop\\My Files 07-08-2019\\BiasMeasurementExperiments";

        if (p.maxResults.equals("1000"))
        {
            qryCount = "50";
            C = "C1000";
            // p.exType = "Retrieval";
            p.exType = "Performance";
        } // End if
        else
        {
            qryCount = "300K";
            C = "C100";
            p.exType = "All";
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
        p.outputDir = String.format("%s\\%s\\%s\\%s\\%s" ,
                outPutFolder , corpus , indexFolder , qryCount , C);
        csvFile = outPutFolder + "/out.csv";
        csvPaser = new CSVParser();
        csvPaser.readFromFile(csvFile);

        /*CSV Key
        corpus - indexType - qryFilter - qryCount - model - maxResults - other - B*/
        csvKey = String.format("%s,%s,%s,%s,%s,%s, ,",
                corpus,indexFolder , "combinedQuery" , qryCount , "PL2" , p.maxResults);
        p.indexName = outPutFolder + "\\Indexes\\" + p.indexName;
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
    private void fillExperimentParameterFile(String fileName , String indexName , String maxResults)
    {
        XMLTextParser parser = new XMLTextParser(fileName);
        parser.setTagValue("indexName",indexName);
        parser.setTagValue("maxResults",maxResults);
        parser.save();
    } // End Function

    private String getRetTypeFolder(String retType)
    {
        /*   exType (Cumulative) = "/Cumulative";*/
        /* exType (Gravity) = "/GravityWeightB0.5C100";*/
        String result = "";
        if (retType == "Gravity")
            result =   "\\GravityWeightB0.5C100";
        else
            result =   "\\Cumulative";
        return result;
    }

    private String runRetrievalStatistics (String b)
    {
        int maxResultsInt;
        StatisticsRetrieval sts = new StatisticsRetrieval();
        String outFileName = p.outputDir + "\\result" + b + ".res";
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
        parser.setTagValue("c", b );
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
        outFileName = p.outputDir + "\\result" + b + ".res";
        parser.setTagValue("resFile",outFileName);
        outFileName = p.outputDir + getRetTypeFolder(retType) +  "\\RCResults" + b + ".ret";
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
                result = "",
                keys[] = {"map","bpref","P10"};
        BufferedReader br;
        int i = 0;
        inFile = p.outputDir + "\\trec" + b + ".trec";
        br = new BufferedReader(new FileReader(inFile));

        while ((line = br.readLine()) != null && i < keys.length )
            if (line.startsWith(keys[i]))
            {
                result += "," + line.split("\t",3)[2];
                i++;
            } // End if

        return result.replaceFirst(",","");
    } // End Function
    private void runALLRC (String b)
    {
        String result;
        // Calculation
        result = runRCExperiment(b,"Cumulative");
        // *** Statistics ***
        csvPaser.appendRetCG(result);
        result = runRCExperiment(b,"Gravity");
        csvPaser.appendRetGG("0.5," + result);
    }

    private void processExperimentSet () throws Exception
    {
        // This function is used to process the whole experiment Set
         String b , retResult , bashLines = "" , line  ;

         String bRange[] = {"0.1", "0.5", "1.0", "5.0", "10", "15", "20", "50"};

         for (int i = 0; i < bRange.length; i++) {
              //  b = String.valueOf(bv[i]);

             /*if (i == 11)
                 b = "5000";
             else
              //  b = "0." + i;
                b = String.valueOf(i*100);
            //b = "5000";*/
             b = bRange[i];
            csvPaser.appendKey(csvKey + b);
            csvPaser.resetValues();
            switch (p.exType)
                {
                    case "All":
                        csvPaser.appendRes(runRetrievalExperiment(b));
                        bashLines += getBashLine(b);
                        runALLRC(b);
                        break;
                    case "AllRC":
                        runALLRC(b);
                        break;
                    case "Cumulative":
                        csvPaser.appendRetCG(runRCExperiment(b,p.exType));
                        break;
                    case "Gravity":
                        csvPaser.appendRetGG(runRCExperiment(b,p.exType));
                        break;
                    case "Retrieval":
                        csvPaser.appendRes(runRetrievalExperiment(b));
                        bashLines += getBashLine(b);
                        break;
                    case "Performance" :
                        if (p.maxResults.equals("1000"))
                            csvPaser.appendPerformance(getPerformanceValues(b));
                        break;
                    case "ReadSts":
                        csvPaser.appendRes(runRetrievalStatistics(b));
                        if (p.maxResults.equals("1000"))
                            csvPaser.appendPerformance(getPerformanceValues(b));
                        else
                        {
                            csvPaser.appendRetCG(runRCStatistics(b,"Cumulative"));
                            csvPaser.appendRetGG("0.5," + runRCStatistics(b,"Gravity"));
                        }
                } // End Switch
                line = csvPaser.updateCSVLine();
                System.out.println("Current CSV Line : " + line);
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
        String paramFileName =  "params/BMExperimentSets/Experiment2.xml";
       String[] indexNames = {
               "AquaintBigramIndex","AquaintCombinedIndex","AquaintUnigramIndex" , "AquaintFieldedIndex",
               "Core17UnigramIndex","Core17BigramIndex","Core17CombinedIndex"  , "Core17FieldedIndex"
               };
       String maxResults[] = {"1000"};


       for (int i = 0 ; i < indexNames.length ; i++)
           for (int j=0 ; j < maxResults.length ; j++ )
           {
          //     p.indexName = indexNames[i];
         //      fillAutoParameters();

               fillExperimentParameterFile(paramFileName , indexNames[i] , maxResults[j]);
               runExperimentFile(paramFileName);
          }

       // runExperimentFile(paramFileName);
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
