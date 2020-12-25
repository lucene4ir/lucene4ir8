'''
Process log Files From Running Experiments to get Log Table of Data
input Log Line :
CORE17 FbTerms = 30 , FbDocs = 10 , b = 0.75 has Started 2020-09-26 06:44:28
Output Csv Line :
corpus,fbTerms,fbDocs,startTime,endTime,Elapsed
AQUAINT,5,5,2020-09-15 17:04:00,2020-09-16 01:51:29,0 days 08:47:29.000000000
'''

import src.classes.clsRetrievabilityCalculator as rc
import pandas as pd
import io

def processLog(line):
    parts = line.split(',')
    sub = parts[0].split(' FbTerms = ')
    corpus = sub[0]
    fbterms = sub[1].strip()
    fbDocs = parts[1].replace(' FbDocs = ','').strip()
    sub=parts[2].replace(' b = 0.75 has ','').replace('\n','')
    sub = sub.split(' ')
    status = sub[0]
    time = ' '.join(sub[1:3])
    line = ','.join([corpus,fbterms,fbDocs,status,time]) + '\n'
    return line

def getLogDf(csvLines):
    fileData = io.StringIO(csvLines)
    df = pd.read_csv(fileData)
    criteria = df['status'] == 'Started'
    sdf = df[criteria]
    criteria = ~criteria
    # criteria = df['status'] == 'Finished'
    fdf = df[criteria]
    df = pd.merge(sdf,fdf,on=['corpus','fbTerms','fbDocs'])
    df.rename(columns={'time_x':'startTime', 'time_y':'endTime'},inplace=True)
    df.drop(columns=['status_x','status_y'],inplace=True)
    # df['Elapsed'] = df['endTime'] - df['startTime']
    sdf = pd.to_datetime(df['startTime'])
    fdf = pd.to_datetime(df['endTime'])
    df['Elapsed'] = fdf - sdf
    df['Elapsed'].replace('.000000000','',inplace=True)
    df.sort_values(by=['corpus','fbTerms','fbDocs'],inplace=True)
    return df

if __name__ == '__main__':
    path = r'C:\Users\kkb19103\Desktop\log.txt'
    f = open(path,'r')
    all = 'corpus,fbTerms,fbDocs,status,time\n'
    for line in f:
        all += processLog(line)
    df = getLogDf (all)
    df.to_csv(path.replace('log','outLog'),index=False)

    print(df.to_string())




