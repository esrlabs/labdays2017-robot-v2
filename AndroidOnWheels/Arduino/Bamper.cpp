#include "Filters.h"
#include "Bamper.h"
const int trig = 9;
const int echo_left = 6;
const int echo_mid = 5;
const int echo_righ = 3;
//const int LED = 13;

#define FILTER_FREQ 1.05
FilterOnePole filter_left( LOWPASS, FILTER_FREQ);
FilterOnePole filter_righ( LOWPASS, FILTER_FREQ);
FilterOnePole filter_mid( LOWPASS, FILTER_FREQ);

RunningStatistics filterOneLowpassStats;                    // create running statistics to smooth these values
   
void bamperSetup() {
 
  Serial.begin(115200);
  pinMode(trig, OUTPUT);
  pinMode(echo_left, INPUT);
  pinMode(echo_mid, INPUT);
  pinMode(echo_righ, INPUT);
  
  analogWrite(trig, 50);
  
}

float getCm(int sensor)
{
  float duration = pulseIn(sensor, HIGH);
  return duration / 14.5;   
}

bool bamperLoop()
{
  auto cm_left = getCm(echo_left);
  auto cm_righ= getCm(echo_righ);
  auto cm_mid = getCm(echo_mid);
  filter_righ.input( cm_righ );
  filter_left.input( cm_left );
  filter_mid.input( cm_mid );
  return ((filter_mid.output() < min_dist) || 
         (filter_left.output() < min_dist) ||
         (filter_righ.output() < min_dist));
}

void loop2()
{
  auto cm_left = getCm(echo_left);
  auto cm_righ= getCm(echo_righ);
  auto cm_mid = getCm(echo_mid);
  filter_righ.input( cm_righ );
  filter_left.input( cm_left );
  filter_mid.input( cm_mid );
  filterOneLowpassStats.input(filter_mid.output());
  Serial.print(filter_righ.output());
  Serial.print(",");
  Serial.print(filter_left.output());
  Serial.print(",");
  Serial.println(filterOneLowpassStats.mean());
}

