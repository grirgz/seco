b = Buffer.read(s, Platform.resourceDir +/+ "sounds/a11wlk01.wav")

(
SynthDef(\buffer_osc, { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
	var ou;
	ou = SinOsc.ar(freq);
	ou = 
	ou = ou * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add;
)



(
a=Array.fib(64,0,1);b=Array.fill(size:12,function:{arg i;a[i]%8});
p=1/8;Pdef(\x,Pbind(\dur,p,\degree,Pseq(b,1,0)));
r=Array.fill(12,{arg i;Pbindf(Pdef(\x),\mtranspose,b[i]);});
Pseq(r,(1/p)).play;
)



(
c=13;a=Array.fib(64,0,1);b=Array.fill(size:c,function:{arg i;a[i]%12});
p=1/(21-c);Pdef(\x,Pbind(\dur,p,\degree,Pseq(b,1,0)));
r=Array.fill(c,{arg i;Pbindf(Pdef(\x),\mtranspose,1-b[i]);});
Pseq(r,(1/p)).play;
)

(

Pbind(
	\dur, p,


)
)



//spinback
play{a = PMOsc;b= SinOsc;c=RLPF;d=440;a.ar(c.kr(b.kr(20,0,d),b.kr(0.2,0,d)))}// #supercollider
