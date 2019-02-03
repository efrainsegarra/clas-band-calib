#include <iostream>
#include <cstdlib>
#include <string>
#include <cstdio>
#include <fstream>

#include "TFile.h"
#include "TTree.h"
#include "TH1.h"
#include "TH2.h"
#include "TFitResult.h"
#include "TFitResultPtr.h"
#include "TGraph.h"
#include "TGraphErrors.h"
#include "TF1.h"
#include "TCanvas.h"
#include "TImage.h"
#include "TStyle.h"

using namespace std;
void find_gain(int numRuns, TGraph * data, std::vector<double> * HVs, double &A, double &B, double &Aerr, double &Berr, double &HV_15000);
void find_size(int &step, int start, string pmt_number);


struct BarInfo{
	int id;
	int sector, layer, component, order;
	std::vector<double> hv;
	std::vector<double> means;
	std::vector<double> meanserr;
};
struct find_bar{
	int uniqueID;
	find_bar(int id): uniqueID(id){ }
	bool operator()( BarInfo const& bar) const{
		return bar.id == uniqueID;
	}
};

int main(int argc, char ** argv)
{
	if (argc != 3)
	{
		cerr << "Wrong number of arguments. Instead use:\n"
			<< "\tgainfitter /path/to/gain/test/txt /path/to/output/file/txt\n";
		return -1;
	}

	ifstream inFile;
	inFile.open( argv[1] );
	char line[256];
	inFile.getline( line, 256 );
	double hv, mean, sigma;
	double hvleft, hvright, leftMean, leftSigma, rightMean, rightSigma;
	int layer, sector, component, order;

	std::vector< BarInfo > Bars;
	std::vector< double > HVs;
	std::vector< double > HVerr;
	if (inFile.is_open() == true){
		while ( inFile >> sector >> layer >> component >> order >> hv >> mean >> sigma ) {

			if( mean < 0 || sigma < 0 ) continue;
			int ID = sector*10000 + layer*1000 + component*100 +order*10;

			std::vector< BarInfo >::iterator it;
			it = std::find_if( Bars.begin(), Bars.end(), find_bar( ID ) );
			// If I've already started saving this bar
			if( it != Bars.end() ){
				int idx = std::distance( Bars.begin() , it );
				BarInfo bar = Bars.at(idx);
				Bars.at(idx).hv.push_back( hv);
				Bars.at(idx).means.push_back( mean );
				Bars.at(idx).meanserr.push_back( sigma );
			}
			else{	// Otherwise create and add new bar
				BarInfo bar;
				bar.id = ID;
				bar.sector = sector;
				bar.layer = layer;
				bar.component = component;
				bar.order = order;
				bar.hv.push_back(hv);
				bar.means.push_back(mean);
				bar.meanserr.push_back(sigma);

				Bars.push_back( bar );
			}

			cout << ID << " " << " " << sector << " " << layer << " " << component << " " << order << " "<< hv << " " << mean <<  " " << sigma <<"\n";
		}
	}


	ofstream outFile;
	outFile.open( argv[2] );
	outFile << " Sector " << " Layer " << " Component " << " HV left " << " HV right " << std::endl;
	for( int i = 0 ; i < Bars.size() ; i++){
		BarInfo bar = Bars.at(i);

		std::vector<double> hL = bar.hv;
		std::vector<double> mL = bar.means;
		std::vector<double> mLerr = bar.meanserr;

		TGraphErrors dataL( hL.size() , &hL[0] , &mL[0] , 0 , &mLerr[0] );

		double A_l, B_l, Aerr_l, Berr_l;
		double HV_l;

		A_l = mL.at(0);

		find_gain( hL.size() , &dataL, &hL, A_l, B_l, Aerr_l, Berr_l, HV_l );

		TCanvas *c = new TCanvas;
		TString name;

		gStyle->SetOptFit(1);
		gStyle->SetStatX(0.5);
		gStyle->SetStatY(0.9);
		gStyle->SetStatW(0.2);
		gStyle->SetStatH(0.2);

		c->cd();
		name = Form("Layer %i, Sector %i, Component %i, PMT %i",bar.layer, bar.sector, bar.component,bar.order);
		dataL.SetTitle(name);
		dataL.GetXaxis()->SetTitle("HV [kV]");
		dataL.GetYaxis()->SetTitle("ADC Channel");
		dataL.GetYaxis()->SetTitleOffset(1.6);
		//	dataL.GetXaxis()->SetRangeUser(0.5,1.8);
		//	dataL.GetYaxis()->SetRangeUser(1000,30000);
		dataL.SetMarkerStyle(20);
		dataL.SetMarkerColor(2);
		dataL.Draw("AP");

		c->Update();

		name = Form("layer%i_sector%i_comp%i_ord%i",bar.layer,bar.sector,bar.component,bar.order);
		c->SaveAs("fitResults/" + name + "_gainCurve.pdf");

		outFile.precision(4);
		outFile << bar.sector << " " << bar.layer << " " << bar.component << " " << bar.order << " " << HV_l << " " << "\n";
	}

	// Clean up
	outFile.close();

	return 0;
}

void find_gain(int numRuns, TGraph * data, std::vector<double> * HVs, double &A, double &B, double &Aerr, double &Berr, double &HV_15000)
{

	TF1 myFunc("myFunc","[0]*(x^[1])", HVs->at(0) , HVs->at(numRuns-1) );

	myFunc.SetParameter(0,A);
	myFunc.SetParameter(1,7);

	TFitResultPtr fitRes = data->Fit(&myFunc,"QES","",HVs->at(0)-400.,HVs->at(numRuns-1)+400.);
	cout << "\n\tGain Functional for PMT is:\tA = " << fitRes->Parameter(0) << "\t\tB [GAIN] = " << fitRes->Parameter(1) << " with range: " << HVs->at(0)-200. << " to " << HVs->at(numRuns-1)+200. << "\n";

	A = fitRes->Parameter(0);
	Aerr = fitRes->ParError(0);
	B = fitRes->Parameter(1);
	Berr = fitRes->ParError(1);

	HV_15000 = pow(18000./A,1./B);
	cout << "\t\tHV at ADC 18000: " << HV_15000 << "\n";

}
