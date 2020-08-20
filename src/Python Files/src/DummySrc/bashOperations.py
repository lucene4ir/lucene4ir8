
import os


def bashWriter():
    path = r"C:\Users\kkb19103\Desktop\bash.sh"
    line = "~/trecEval/trec_eval ~/trecEval/Qrels/trec2005.aquaint.qrels ./result%s.res > ./trec%s.trec\n"
    f = open(path,'w')
    for i in range(11):
        if (i == 10):
            b = "5000"
        else:
            b =  str((i + 1) * 100)
        bashLine = line % (b,b)
        f.write(bashLine)
    f.close()

def bashRunner():
    path = r"C:\Users\kkb19103\Desktop\bashRun.sh"
    lineFormat = "~/BiasMeasurementExperiments/%s/%sIndex/50/C1000\n"
    f = open(path, 'w')
    for corpus in ["Aquaint","Core17"]:
        for index in ["Unigram" , "Bigarm" , "Fielded" , "Combined"]:
            line = lineFormat % (corpus,index)

            # for file in os.listdir(line):
            #     file = os.path.join(line,file)
            #     if (file.endswith("")):
            #         newfile = file.replace("","")
            #         os.rename(file,newfile)


    f.close()
if __name__ == '__main__':
    bashWriter()