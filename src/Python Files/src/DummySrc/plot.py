import matplotlib.pyplot as plt
def main():
    x = range(6)
    y = range(6)

    plt.subplots(2,3,figsize=(20,20))
    for i in range(6):
        plt.subplot(2,3,i+1)
        plt.plot(x,y,marker='^')
    plt.show()

if __name__ == '__main__':
    main()
