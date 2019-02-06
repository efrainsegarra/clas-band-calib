from __future__ import division
import numpy as np
import matplotlib.pyplot as plt
from scipy.optimize import curve_fit
def f(x, A, B): 
    return A*np.power(x,B)



# HVs
HV_L = np.asarray([1490,1460,1425,1385,1350,1306],dtype=float)
HV_R = np.asarray([1470,1440,1405,1365,1330,1287],dtype=float)
HV_L /= 1500.
HV_R /= 1500.


# Geometric mean of ADCs sqrt(ADCL*ADCR)
gm = np.asarray([4300,3700,3100,2700,2300,1900],dtype=float)

errors = np.ones(len(HV_L))*150.


Al,Bl = curve_fit(f, HV_L ,gm, sigma=errors)[0] 
Ar,Br = curve_fit(f, HV_R,gm, sigma=errors)[0] 
pts = np.linspace(1200,1500)
pts /= 1500.

plt.figure(1)
plt.errorbar(HV_L,gm,marker='o',color='red',label='Left PMT',yerr=errors)
plt.errorbar(HV_R,gm,marker='o',color='blue',label='Right PMT',yerr=errors)
plt.plot(pts,Al*np.power(pts,Bl),color='red')
plt.plot(pts,Ar*np.power(pts,Br),color='blue')
plt.legend(numpoints=1,loc='best')
plt.xlabel('HV [V]')
plt.ylabel(r'$\sqrt{ADC_L*ADC_R}$')

#print Al,Bl
#print Ar,Br

print "HV for PMT L to put 1MeVee at ADC 2000: ", np.power(2000./Al,1./Bl)*1500.
print "HV for PMT R to put 1MeVee at ADC 2000: ", np.power(2000./Ar,1./Br)*1500.

plt.show()
