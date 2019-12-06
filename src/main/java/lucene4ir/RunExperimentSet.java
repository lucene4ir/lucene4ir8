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
            defaultCSVKey;
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
        String qryCount = "" , indexFolder = "" , C = "", corpus , csvFile ,
        outPutFolder = "C:\\Users\\kkb19103\\Desktop\\My Files 07-08-2019\\BiasMeasurementExperiments";

        C = "C" + p.maxResults;
        if (p.maxResults.equals("100"))
            qryCount = "300K";
        else
            qryCount = "50";

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
        corpus - indexType - qryFilter - qryCount - model - maxResults - RetrievabilityB - RetrievalCoefficient (B , mu , c) */
        defaultCSVKey = String.format("%s,%s,%s,%s,%s,%s,",
                corpus,indexFolder , "combinedQuery" , qryCount , p.model , p.maxResults);
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
        String result = "";
        if (retType == "Gravity")
            result =   "\\GravityWeightB0.5C" + p.maxResults;
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
        return String.format("%1.6f,%d,%1.4f",sts.G , sts.zeroDocCtr , sts.sum );
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

    private String getNewKey (String retB , String b)
    {
        return defaultCSVKey + retB + "," + b;
    }

    private void runALLRC (String b)
    {
        String result ;
        // Calculation

        result = runRCExperiment(b,"Cumulative");
        // *** Statistics ***
        csvPaser.setRet(result);
        csvPaser.addCSVLineToMap();

        result = runRCExperiment(b,"Gravity");
        csvPaser.setKey(getNewKey("0.5",b));
        csvPaser.setRet(result);
    }

    private String[] getbRange()
    {
        /*
        Get Coefficient Values based on input model
         */
        String[] BM25Set = {"0.1", "0.2","0.3","0.4","0.5","0.6","0.7","0.8","0.9","1.0"},
                 PL2Set = {"0.1", "0.5", "1.0", "5.0", "10", "15", "20", "50"},
                 LMDSet = {"100","200","300","400","500","600","700","800","900","1000","5000"},
                 result;
        if (p.model.equals("BM25"))
            result = BM25Set;
        else if (p.model.equals("PL2"))
            result = PL2Set;
        else result = LMDSet;
        return result;
    }

    private void processExperimentSet () throws Exception
    {
        // This function is used to process the whole experiment Set
         String b , retB  = "0", bashLines = "" , line  ;

         String bRange[] = getbRange();

         for (int i = 0; i < bRange.length; i++) {
             b = bRange[i];
         if (p.exType.equals("Gravity"))
             retB = "0.5";
        csvPaser.newLine(getNewKey(retB,b));

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
                case "Cumulative":
                case "Gravity":
                    csvPaser.setRet(runRCExperiment(b,p.exType));
                    break;

                case "Retrieval":
                    csvPaser.setRes(runRetrievalExperiment(b));
                    bashLines += getBashLine(b);
                    break;
                case "Performance" :
                    csvPaser.setPerformance(getPerformanceValues(b));
                    break;
                case "ReadSts":
                    if (p.maxResults.equals("1000"))
                        csvPaser.setPerformance(getPerformanceValues(b));
                    csvPaser.setRes(runRetrievalStatistics(b));
                    csvPaser.setRet(runRCStatistics(b,"Cumulative"));
                    csvPaser.addCSVLineToMap();
                    csvPaser.setKey(getNewKey("0.5",b));
                    csvPaser.setRet(runRCStatistics(b,"Gravity"));
            } // End Switch
            line = csvPaser.addCSVLineToMap();

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
       String maxResults[] = {"10"};


       for (int i = 0 ; i < indexNames.length ; i++)
           for (int j=0 ; j < maxResults.length ; j++ )
           {
               fillExperimentParameterFile(paramFileName , indexNames[i] , maxResults[j]);
               runExperimentFile(paramFileName);
          }

       // runExperimentFile(paramFileName);
    }

    public static void main(String[] args)
        {
        // write your code here
            String sourceFile = "params\\BMExperimentSets\\Experiment2.xml",
                    result;
        RunExperimentSet re = new RunExperimentSet();
        re.runCalculatedList();

    } // End Function Main
} // End Class

class ExperimentSetParams  {
    public String indexName,
            maxResults,
            queryFile,
            exType,
            model,
            retrievalParamsFile,
            retrievabilityParamsFile,
            outputDir;
}
