

(
 {

	var sig, freq, freq1, freq2, mou;
	var idx;
	var sig1, sig2;

	freq = MouseX.kr(50,2000);
	//freq = Line.kr(0,10,1);
	freq1 = freq.cpsmidi.round(12);
	freq2 = (freq.cpsmidi - 6).round(12);
	idx = IEnvGen.kr(Env([0,1,0],[0.5,0.5]), freq.cpsmidi/12 % 1);
	//[freq1, freq2, idx].poll;
	Select.kr(idx, [freq1, freq2]).poll;
	
	sig1 = SinOsc.ar(freq1.midicps);
	sig2 = SinOsc.ar(freq2.midicps);
	sig = SelectX.ar(idx, [sig1, sig2]);
	sig ! 2;
	//sig = SinOsc.ar(freq);

 }.play;


)
