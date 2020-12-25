import json

if __name__ == '__main__':
    path = r'C:\Users\kkb19103\Desktop\TempFiles\WashingtonPost.v2\data'
    inFile = path + '\dum.txt'
    outFile = path + r'\kickerList.txt'
    f = open(inFile,'r',encoding='utf8')
    outf = open(outFile, 'w')
    outFile = path + r'\newDum.txt'
    # lines = []
    line = 'id,docid,kicker\n'
    outf.write(line)
    i = 1
    n=0
    for line in f:
        if (line != ''):
            # outf2.write(line)
            line = json.loads(line)
            docid = line['id']
            kicker = ''
            for item in line['contents']:
                if (item['type'] == 'kicker'):
                    kicker = item['content']
                    break
            if (kicker == ''):
                n+=1
            line = '%d,%s,%s\n' % (i,docid,kicker)
            outf.write(line)
            print(line)
            i+=1
    print ('Unfound Kickers : ' , str(n) )
    f.close()
    outf.close()
