def countVal (path , val):
    f = open(path, 'r')
    ctr = 0
    for line in f:
        num = line.split(" ", 1)[1][:-2]
        num = float(num)
        if (num == val):
            ctr += 1
    return ctr
def main():
   folder = r"C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\Aquaint\BigramIndex\50\C10\Cumulative"
   path = folder + r"\RCResults700.ret"
   val = 1
   count = countVal(path,val)
   print ("The Number Of " + str(val) + " Occurances : " + str(count))
if __name__ == '__main__':
    main()