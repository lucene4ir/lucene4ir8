def getdoc (ln):
   parts = str(ln).split(" ",3)
   return parts[2]
   #return ln

mainPath = r'C:\Users\kkb19103\Desktop\CSV\CSV'
path1 = mainPath + r'\Per.csv'
#path2 = mainPath + r'\result200.res'  result0.3.res trec0.3.trec
#mainPath = r'C:\Users\kkb19103\Desktop\Test Res'
path2 = mainPath + r'\Backup\Per.csv'

f1 = open(path1,'r')
f2 = open(path2,'r')
i = 1
ctr = 0
for ln1 in f1:
   ln2 = f2.readline()
   if (ln1 != ln2):
  # if (ln1 != ln2):
        print ("Line Differ " + str(i))
        print('Line 1 : ' + ln1)
        print('Line 2 : ' + ln2)
        ctr += 1
   i += 1
print ('Done with ' + str(ctr) + " Differences")

