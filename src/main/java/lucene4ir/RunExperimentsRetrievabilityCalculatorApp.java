package lucene4ir;

import lucene4ir.utils.XMLTextParser;

public class RunExperimentsRetrievabilityCalculatorApp {


    private String retirievalParamsFile = "params/temp_retrieval_params.xml",
            retrievalOutDir = "out/G/300K/B/",
            retrievabilityParamsFile = "params/temp_retrievability_calculator_params.xml";
    private XMLTextParser retParser,
                          rcParser;
    private RetrievalApp retApp ;
    private RetrievabilityCalculatorApp rcApp ;


    private void init()
    {
        retParser = new XMLTextParser(retirievalParamsFile);
        rcParser = new XMLTextParser(retrievabilityParamsFile);
    }

    private void runAllExperiment (String b)
    {
        String outFileName;

        outFileName = retrievalOutDir + "result" + b + ".res";
        retParser.setTagValue("b", b );
        retParser.setTagValue("resultFile",outFileName);
        retParser.save();
        retApp = new RetrievalApp(retirievalParamsFile);
        retApp.processQueryFile();
        runRCExperiment(b);
    }

    private void runRCExperiment (String b)
    {
        String outFileName;
        outFileName = retrievalOutDir + "result" + b + ".res";
        rcParser.setTagValue("resFile",outFileName);
        outFileName = retrievalOutDir + "RCResults" + b + ".ret";
        rcParser.setTagValue("retFile",outFileName);
        rcParser.save();
        rcApp = new RetrievabilityCalculatorApp(retrievabilityParamsFile);
        rcApp.calculate();
    }

    public RunExperimentsRetrievabilityCalculatorApp(){
       init();
    }

    public static void main(String[] args) {
	// write your code here
        String outFileName , b;
       RunExperimentsRetrievabilityCalculatorApp re = new RunExperimentsRetrievabilityCalculatorApp();
        for (int i = 1 ; i < 3 ; i++)
        {
            b = "0." + i ;
            re.runRCExperiment(b);
        } // End For
    }
}
