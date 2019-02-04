from __future__ import division
import numpy as np
import matplotlib.pyplot as plt
from scipy.optimize import curve_fit
def f(x, A, B): 
    return A*x + B




# Pedestal, Sodium (2), Cobalt (1), Cesium (1)
mev = np.asarray([0.,0.341,1.062,(0.962+1.118)/2.,0.477],dtype=float)
longbar = np.asarray([0.,2200,4500,3800,2200],dtype=float)
shortbar = np.asarray([0,3200,5400,4600,2800],dtype=float)

Al,Bl = curve_fit(f, mev,longbar)[0] 
As,Bs  = curve_fit(f, mev,shortbar)[0]
print Al,Bl,As,Bs
pts = np.linspace(0,2)

plt.figure(1)
plt.scatter(mev,longbar,marker='o',color='red',label='Long Bar')
plt.plot(pts,Al*pts + Bl,color='red')
plt.legend(numpoints=1,loc='best')
plt.xlabel('MeVee')
plt.ylabel('ADC')

plt.figure(2)
plt.scatter(mev,shortbar,marker='o',color='blue',label='Short Bar')
plt.plot(pts,As*pts + Bs,color='blue')
plt.legend(numpoints=1,loc='best')
plt.xlabel('MeVee')
plt.ylabel('ADC')
plt.show()
