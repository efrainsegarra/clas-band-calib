#!/bin/sh

if (($# == 0)); then
  echo "Please pass arguments  <runnumber1> <runnumber2> <runnumber3> ..."
  exit 2
fi
i=1
for var in "$@"
do
  echo $var
  hv="../snp_tables/hvrun$var.txt"
  data="hvScan/calibOutput/run_$var-adcFit.txt"
  output="combined_run_$var.txt"
  echo $hv
  echo $data
  echo $output

  ./hvScan/calib_snp_combine/comb_calib_snp $data $hv $output

done
exit 0



#run_$run$-adcFIT.txt
