import src.classes.clsTrec as trec
import src.classes.clsCWL as cwl

def main():
    # Ubuntu Format
    resFile = r'~/Desktop/AQ-PL2-UI-50-C1000-RM3-fbdocs5-fbterms10-c0.5.res'
    gainFile = r'~/BiasMeasurementExperiments/Resources/Aquaint/Aquaint.qrel'

    output = trec.executeBash(resFile,gainFile)
   # output = cwl.executeBash(resFile,gainFile)
    print(output)
if __name__ == '__main__':
    main()