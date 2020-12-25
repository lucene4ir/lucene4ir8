import os , src.classes.clsRetrievabilityCalculator as rc

def main():
    path = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\1st Experiment - Bigrams Influence\AllRes'
    files = os.listdir(path)
    outPath = r'C:\Users\kkb19103\Desktop\TestOut'
    for f in files:
        if (f.find('-BI-50') > 0) :
            resFile = path + '\\' + f
            outFile = outPath + '\\' + f.replace('.res','.ret')
            rc.cslRetrievabilityCalculator.calculate(resFile,0,100,outFile)
if __name__ == '__main__':
    main()