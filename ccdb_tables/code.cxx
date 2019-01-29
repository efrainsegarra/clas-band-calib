#include <fstream>
#include <iostream>
#include <cmath>
#include <cfloat>
#include <string>
#include <sstream>

using namespace std;

// ADC variables
int adc_ctr, adc_crateNo[1000], adc_slot[1000], adc_chan[1000], adc_sector[1000], adc_layer[1000], adc_component[1000], adc_order[1000];
string adc_crateName[1000], adc_crateType[1000], adc_sys[1000], adc_detector[1000], adc_element[1000];

// TDC variables
int tdc_ctr, tdc_crateNo[1000], tdc_slot[1000], tdc_chan[1000], tdc_sector[1000], tdc_layer[1000], tdc_component[1000], tdc_order[1000];
string tdc_crateName[1000], tdc_crateType[1000], tdc_sys[1000], tdc_detector[1000], tdc_element[1000];

// Pedestal variables
int ped_ctr;
int ped_slot[30] = {0};
double ped_val[30][30] = {{0}};

// Forward-declaring functions:
void LoadADCmap();
void LoadTDCmap();
void LoadPedest();
// ===================================================================================================
int main(int argc,char ** argv){

	cout << "*****************************************\nCreating ADC, TDC map tables for BAND\n";

	int nsb =  4;
	int nsa = 12;
	int tet = 20;
	int win_off = 1000;
	int win_siz = 500 ;

	// ---------------------------------------------
	// Loading necesary values
	cout << "-----\nLoading necesary values\n-----\n";
	LoadADCmap();
	LoadTDCmap();
	LoadPedest();

	// ---------------------------------------------
	// Table #1
	// --------------
	cout << "creating file: 'transtable.dat'" << endl;

	ofstream tab1;
	tab1.open("transtable.dat");
	tab1 << "# BAND SETUP, Crate 66, Fadcs from 3 to 8 and 10, TDC in slot 16\n";
	tab1 << "# Detector - CRATE - SLOT - CHANNEL - SECTOR - LAYER - COMPONENT - ORDER\n";
	tab1 << "# ORDER 0=ADCL, 1=ADCR, 2=TDCL, 3=TDCR\n";

	// ADC info
	for(int i = 0 ; i < adc_ctr ; i++){
		tab1 << adc_crateNo  [i] << "\t";
		tab1 << adc_slot     [i] << "\t";
		tab1 << adc_chan     [i] << "\t";
		tab1 << adc_sector   [i] << "\t";
		tab1 << adc_layer    [i] << "\t";
		tab1 << adc_component[i] << "\t";
		tab1 << adc_order    [i] << endl;
	}
	// TDC info
	for(int i = 0 ; i < tdc_ctr ; i++){
		tab1 << tdc_crateNo  [i] << "\t";
		tab1 << tdc_slot     [i] << "\t";
		tab1 << tdc_chan     [i] << "\t";
		tab1 << tdc_sector   [i] << "\t";
		tab1 << tdc_layer    [i] << "\t";
		tab1 << tdc_component[i] << "\t";
		tab1 << tdc_order    [i] << endl;
	}
	tab1.close();

	// ---------------------------------------------
	// Table #2
	// --------------
	cout << "creating file: 'fadc250table.dat'" << endl;

	ofstream tab2;
	tab2.open("fadc250table.dat");

	tab2 << "# BAND SETUP, Crate 66, Fadcs from 3 to 8 and 10\n";
	tab2 << "# CRATE - SLOT - CHANNEL - PEDESTAL - NSB - NSA - TET - WINDOW_OFF - WINDOW_SIZE\n";

	for(int i = 0 ; i < adc_ctr ; i++){
		tab2 << adc_crateNo  [i] << "\t";
		tab2 << adc_slot     [i] << "\t";
		tab2 << adc_chan     [i] << "\t";
		tab2 << ped_val[adc_slot[i]][adc_chan[i]] << "\t";
		tab2 << nsb << "\t";
		tab2 << nsa << "\t";
		tab2 << tet << "\t";
		tab2 << win_off << "\t";
		tab2 << win_siz << endl;
	}
	tab2.close();

	cout << "*****************************************" << endl;
	// ---------------------------------------------
	return 0;
}
// ===================================================================================================
void LoadPedest(){
	string temp;
	ped_ctr = 0;
	ifstream f;
	f.open("input/adcband1_ped.cnf");

	if(f.fail()){cout << "Could not find input file 'adcband1_ped.cnf'. Bailing out!" << endl; exit(0);}

	f >> temp;
	f >> temp;

	for(int i = 0 ; i < 1000 ; i++){
		f >> temp;
		f >> temp;
		if(temp=="end") break;
		ped_slot[i] = stoi(temp);

		f >> temp;
		for(int j = 0 ; j < 16 ; j++){	
			f >> ped_val[ped_slot[i]][j];
		}
	}

	f.close();
}
// ===================================================================================================
void LoadADCmap(){
	adc_ctr = 0;
	string line;
	ifstream f;
	string col1;
	f.open("input/FADC_map.dat");

	if(f.fail()){cout << "Could not find input file 'FADC_map.dat'. Bailing out!" << endl; exit(0);}

	while(!f.eof()){

		getline(f, line);
		if(line[0]=='S'){
			stringstream input(line);
			input >> col1;
			input >> adc_crateNo  [adc_ctr];
			input >> adc_crateName[adc_ctr];
			input >> adc_crateType[adc_ctr];
			input >> adc_slot     [adc_ctr];
			input >> adc_chan     [adc_ctr];
			input >> adc_sys      [adc_ctr];
			input >> adc_detector [adc_ctr];
			input >> adc_element  [adc_ctr];
			input >> adc_sector   [adc_ctr];
			input >> adc_layer    [adc_ctr];
			input >> adc_component[adc_ctr];
			input >> adc_order    [adc_ctr];

			adc_ctr++;
		}
	}
	adc_ctr--;
	f.close();
}
// ===================================================================================================
void LoadTDCmap(){
	tdc_ctr = 0;
	string line;
	ifstream f;
	string col1;
	f.open("input/TDC_map.dat");

	if(f.fail()){cout << "Could not find input file 'FADC_map.dat'. Bailing out!" << endl; exit(0);}

	while(!f.eof()){

		getline(f, line);
		if(line[0]=='S'){
			stringstream input(line);
			input >> col1;
			input >> tdc_crateNo  [tdc_ctr];
			input >> tdc_crateName[tdc_ctr];
			input >> tdc_crateType[tdc_ctr];
			input >> tdc_slot     [tdc_ctr];
			input >> tdc_chan     [tdc_ctr];
			input >> tdc_sys      [tdc_ctr];
			input >> tdc_detector [tdc_ctr];
			input >> tdc_element  [tdc_ctr];
			input >> tdc_sector   [tdc_ctr];
			input >> tdc_layer    [tdc_ctr];
			input >> tdc_component[tdc_ctr];
			input >> tdc_order    [tdc_ctr];

			tdc_ctr++;
		}
	}
	tdc_ctr--;
	f.close();
}
// ===================================================================================================
