
//--tweet0000
{GlitchRHPF.ar(GbmanN.ar([2300,1150]),LFSaw.ar(Pulse.ar(4,[1,2]/8,1,LFPulse.ar(1/8)/5+1))+2)}.play//#SuperCollider

//--tweet0001
r{99.do{|i|x={Pan2.ar(SinOsc.ar(i+1,SinOsc.ar((i%9).div(3)*100+(i%9)+500),0.03),1.0.rand2)}.play;2.wait;x.release(25)}}.play//#SuperCollider

//--tweet0002
r{99.do{x={Pan2.ar(BPF.ar(Impulse.ar(18.linrand+0.5),9999.linrand,0.3.linrand,5),1.0.rand2)}.play;3.wait;x.release(9)}}.play//#SuperCollider

//--tweet0003
r{loop{x=play{t=SinOsc.ar(999.rand).abs;Formlet.ar(TDuty.ar(t,0,t),4e3.linrand,t,1-t)!2};wait(9.rand+1);x.release(39)}}.play//#SuperCollider

//--tweet0004
r{loop{z=20.rand+6;x={y=LFTri.ar(z).abs/9/z;RLPF.ar(TDuty.ar(y,0,y),z*600,0.06,9)!2}.play(s,0,z);wait(26-z);x.release}}.play//#SuperCollider

//--tweet0005
r{loop{z=60.rand+1;x={y=LFTri.ar(z).abs/z;RLPF.ar(TDuty.ar(y,0,y),z*99+y,0.01,6+y)!2}.play(s,0,z);wait(z/3);x.release}}.play//#SuperCollider

//--tweet0006
r{loop{x={GVerb.ar(MoogFF.ar(ClipNoise.ar*0.4,LFPar.kr({0.3.rand}!2,0,600,990)),9,9,1)}.play(s,0,19);3.wait;x.release}}.play//#SuperCollider

//--tweet0007
r{loop{x={BPF.ar(Pluck.ar(Crackle.ar([1.9,1.8]),Impulse.ar(5.rand+1),0.05,0.05.linrand),1200.rand)}.play(s,0,9);wait(9);x.release(69)}}.play

//--tweet0008
play{x=LFNoise1.ar(0.5!2);Formlet.ar(Crackle.ar(x.range(1.8,1.98)),TExpRand.ar(200,2e3,x).lag(2),x.range(5e-4,1e-3),0.0012)}//#SuperCollider

//--tweet0009
{|i|x=i+6.rand;Pbind(\dur,0.06,\sustain,1,\amp,0.01,\degree,Pgauss(x,sin(x+Ptime()%6/6e3)*9),\pan,Pkey(\degree)-x*9).play}!6//#SuperCollider

//--tweet0010
play{a=SinOsc;LeakDC.ar(a.ar(a.ar(0.31),a.ar(a.ar(0.21),a.ar(a.ar(0.11,a.ar(0.01)),0,a.ar([2,3],0,400))),a.ar([0.3,0.21])))}//#SuperCollider

//--tweet0011
play{f={|o,i|if(i>0,{SinOsc.ar([i,i+1e-4]**2*f.(o,i-1),f.(o,i-1)*1e-4,f.(o,i-1))},o)};f.(60,6)/60}//#SuperCollider

//--tweet0012
r{loop{Document.current.text[0..z].do{|x|z=x.ascii;play{Blip.ar(z/3,z,Line.kr(3,0,3/z,1,0,2))!2};wait(1/z)}}}.play(AppClock)//#SuperCollider

//--tweet0013
a=play{|a|Saw.ar(68,a)};fork{inf.do{|i|t="";{|j|b=cos(i*cos(j**(i/1e4)));t=t++" @"[b+1]}!68;a.set(\a,b);t.postcs;0.01.wait}}//#SuperCollider

//--tweet0014
play{a=SinOscFB;sum({|i|a.ar(a.ar(a.ar(a.ar(i+1,1/9,999),1/9,a.ar(1/9,1,1/9)),a.ar(0.1,3),i+2*999),a.ar(1/9,1/9),1/9)}!9)!2}//#SuperCollider

//--tweet0015
a=play{|b|Saw.ar*b};fork{inf.do{|i|t="";{|j|b=cos(i*sin(j+sin(i/9)/234));t=t++" @"[b+1]}!68;a.set(\b,b);t.postln;0.01.wait}}//#SuperCollider

//--tweet0016
play{b=LocalBuf(9e4,2).clear;i=Sweep.ar(BufRd.ar(2,b,Saw.ar(12,3e4,4e4)),9e4);BufWr.ar(Saw.ar([8,9]),b,i);BufRd.ar(2,b,i)/2}//#SuperCollider

//--tweet0017
play{b=LocalBuf(8e4,2).clear;i=Sweep.ar(BufRd.ar(2,b,Saw.ar(3.1,4e4,4e4)),8e4);BufWr.ar(Blip.ar([2,3]),b,i);BufRd.ar(2,b,i)}//#SuperCollider

//--tweet0018
play{b=LocalBuf(5e3,2).clear;i=Sweep.ar(BufRd.ar(2,b,Saw.ar(50,2e3,5e3)),6e4);BufWr.ar(Saw.ar([4,3]),b,i);BufRd.ar(2,b,i)/6}//#SuperCollider

//--tweet0019
play{b=LocalBuf(1e4,2).clear;i=Sweep.ar(BufRd.ar(2,b,Saw.ar(1,2e3,5e3)),5e5);BufWr.ar(Saw.ar([8,50]),b,i);BufRd.ar(2,b,i)/3}//#SuperCollider

//--tweet0020
play{a=LFPulse;b=(1..4);Mix(a.ar(a.ar(a.ar(a.ar(b/32)+1/8)+1*b)+(Mix(a.ar(b/64))+a.ar(4/b)*(a.ar(a.ar(b/8))*2+b))*100))/8!2}//#SuperCollider

//--tweet0021
r{{|j|a=play{sin(Decay.ar(Duty.ar(1/50,0,Dseq(flat({|i|asBinaryDigits(j+1*i)}!8),4),2),j+1*0.008))/2!2};5.12.wait}!256}.play//#SuperCollider

//--tweet0022
play{a=1/(2..5);GVerb.ar(Splay.ar(Ball.ar(LPF.ar(Impulse.ar(a),500),7-(1/a),1e-5,LFNoise2.kr(a/5,2e-4,12e-4))/2),5,0.5,0.9)}//#SuperCollider

//--tweet0023
play{Splay.ar({|i|f=i+5*99;RHPF.ar(Ringz.ar(Ball.ar(Saw.ar(i+1)>0,SinOsc.kr(0.1,0,1/5,0.3),0.05,0.02)/99,f,0.05),f,0.1)}!5)}//#SuperCollider

//--tweet0024
{|j|r{{|i|x=sin(i/5+(j*5));Ndef(i%5+(j*5),{Pan2.ar(LFCub.ar(j*2+x*40+400+i)/15,i%5/2-1)}).play;wait(x.abs+0.5)}!500}.play}!5//#SuperCollider

//--tweet0025
{|i|defer{Document.allDocuments.do{|d,c|x=sin(c+1*i/91);y=cos(c+1*i/88*x);d.bounds=Rect(a=x*160+320,b=y*120+240,a,b)}}}!555;//#SuperCollider

//--tweet0026
{CombL.ar(In.ar(8).tanh/8,1,1,8)!2}.play;Pbind(\amp,8,\dur,1/4,\degree,Pseq(List.fib(32)%(List.fib(64)%12),inf),\out,8).play//#SuperCollider

//--tweet0027
play{GVerb.ar(ceil(In ar:8*4+4)-4/10)};Pbind(\dur,2,\legato,Pgeom(0.5,1.1),\degree,Pseq(List fib:8+[[1,4]]-9,9),\out,8).play//#SuperCollider

//--tweet0028
play{MoogFF.ar(LFTri.ar(CombN.ar(Duty.ar(1/8,0,Dseq(Dshuf(List.fib(16)%8*99,8),inf)),4,4,16))/4,LFTri.kr(1/16,0,2e3,3e3))!2}//#SuperCollider

//--tweet0029
play{{|i|CombC.ar(In.ar(8),3+i,LFTri.ar(0.5,0,1,2+i),99)}!2};Pbind(\out,8,\note,Pstutter(8,Pseq(List.fib(32)%9/3,inf))).play//#SuperCollider

//--tweet0030
play{a=LFPar;GVerb.ar(VarSaw.ar(a.ar(1,0,5,a.ar([0.05,0.04],0,50,160).round(50)),0,a.ar(0.2,0,0.5,a.ar(3,0,0.2,0.5)))/8,80)}//#SuperCollider

//--tweet0031
x=0;{|i|Pbind(\dur,i+1/4,\lag,i/6/6,\octave,i+3,\legato,i+1/6,\degree,Pn(Plazy{x=x+1%6;Pseq(asDigits(x+1*142857))})).play}!6//#SuperCollider

//--tweet0032
{Splay.ar({|i|l=LFTri.ar(1/6,i/1.5,2.5,3.5).round;SinOsc.ar(142.857*l,lag(l,i-3/6),1-poll(0.142857*l,10/6,"\t\t"))}!6)}.play//#SuperCollider

//--tweet0033
play{f=LFPar.ar(1/14).round*20+80;Splay.ar(LFPar.ar({|i|[i+1*f,i*f+(i+1/3)]}!4)>BrownNoise.ar(Pulse.ar({|i|i+1}!4,0.35))/3)}//#SuperCollider

//--tweet0034
play{x=CombN.ar(Phasor.ar(0,{|i|i+1/20}!22),0.042,0.042);y=Phasor.ar(LPF.ar(x,LFPar.ar(1/99,0,400,500)),x);Splay.ar(y)*0.25}//#SuperCollider

//--tweet0035
play{x=CombC.ar(Phasor.ar(0,{|i|i+1/4}!5),0.2,LFPar.ar(0.09,0,0.05,0.1).round(0.022));Splay.ar(Phasor.ar(BPF.ar(x,50),x)/4)}//#SuperCollider

//--tweet0036
play{Splay.ar({|i|SinOsc.ar(i+SinOsc.ar(0.01,a=pi/[2,4,8]@@i,0.1,1)*80+SinOsc.ar(i+1*1e-4+i),a,SinOsc.ar(i+1*1e-3,a)/4)}!9)}//#SuperCollider

//--tweet0037
play{a=LFCub;n=8;Splay.ar(a.ar({|i|pow(i+1,a.kr(1/n,i/n,1/n,1))}!n*150,0,a.kr({|i|pow(i+1,a.kr(i+0.5/n,i/n))}!n).max(0))/4)}//#SuperCollider

//--tweet0038
play{PingPong.ar(LocalBuf(3e4,2).clear,Ringz.ar(CuspN.ar*Impulse.kr([9,8]/12)/9,LFPar.kr(1/[3,2]).range(51,[99,17])*9),0.5)}//#SuperCollider

//--tweet0039
play{a=SinOsc;Splay.ar({|i|i=i+1;a.ar(a.ar(i)+1**a.ar(2**a.ar(i/500)*(9-i))*a.ar(9*i).exprange(90,2**a.ar(i/20)*800))}!5)/4}//#SuperCollider

//--tweet0040
a={play{|b|LFTri.ar(b+69)}}!3;fork{inf.do{|i|x=sin(sin(i/99)*i/(i%3+68))*34+34;a[i%3].set(1,x);join($@!x).postln;0.01.wait}}//#SuperCollider

//--tweet0041
play{o=SinOsc.ar(1/RunningMax.ar(Sweep.ar(LocalIn.ar(6)),Impulse.ar([1,0.749,6,12,3,4])));LocalOut.ar(o);Splay.ar(o).tanh/2}//#SuperCollider

//--tweet0042
play{c=[97,99];l=3**9;a=LocalBuf(l,2).clear;BufWr.ar(Saw.ar(c/5),a,BPF.ar(VarSaw.ar(c),98,0.1)*l);PlayBuf.ar(2,a,1/4,1,0,1)}//#SuperCollider

//--tweet0043
fork{1e4.do{|i|text(d=Document.current).size.do{|j|d.font_(Font("Arial",sin(i+j/16)*18+22),j,1)};wait(1/60)}}.play(AppClock)//#SuperCollider

//--tweet0044
play{a=SinOsc;Limiter.ar(LeakDC.ar(a.ar(0.11,BRF.ar(a.ar(a.ar(0.12).exprange(1,1e4),2pi),1/a.ar(0.13).range(1,[99,100])))))}//#SuperCollider

//--tweet0045
play{a=SinOsc;a.ar(a.ar(a.ar(0.11)),a.ar(a.ar(95*a.ar(0.01,0,1,1),0,a.ar(5e-3,0,50),100),a.ar([98,97]),pi+a.ar(5e-4))).tanh}//#SuperCollider

//--tweet0046
play{a=LFTri;GVerb.ar(Mix(Limiter.ar(BRF.ar(a.ar(50,1e-4),a.ar(a.ar([1.01,1.0111])*a.ar(8e3)*1e3+4e3,55),a.ar(0.01)*3))))/9}//#SuperCollider

//--tweet0047
play{CombN.ar(Limiter.ar(BRF.ar(LFSaw.ar(10,0,0.01),LFTri.ar([5,6]*0.1))),0.1,LFTri.kr(0.1,0,0.05,0.05).round(0.01))}//#SuperCollider#SC2012

//--tweet0048
play{a=Impulse;b=SinOsc;c=b.ar(0,BRF.ar(a.ar([7,8]),a.ar(9).lag2(1e-3),1.5,2pi));Ringz.ar(c,b.ar(0.02,0,99,150),1/9)+c*0.02}//#SuperCollider

//--tweet0049
play{Splay.ar(SinOsc.ar(9,SinOsc.ar(midicps((Sweep.ar(0,(33..3))%128&(Sweep.ar(0,(3..9))%(LFSaw.ar(3)*9+99)))+33),0,pi)))/3}//#SuperCollider

//--tweet0050
play{a=Saw;b=(2..12);c=0.015;GVerb.ar(Splay.ar(Klank.ar(`[b*50+b,c,c],Hasher.ar(a.ar(b/4pi,a.ar(c)*b+b).ceil)))/9,5.rand+1)}//#SuperCollider

//--tweet0051
play{a=Saw;GVerb.ar(Splay.ar(BBandPass.ar(a.ar("sunday".ascii),a.ar(9/"slow".ascii)*400+500,a.ar(7/"coding".ascii)+1.1)/5))}//#SuperCollider

//--tweet0052
{Splay.ar(BLowPass.ar(Impulse.ar("sunday".ascii),LFTri.ar(3/"live".ascii)*1800+1900,LFTri.ar(4/"coding".ascii)+1.01))}.play// #SuperCollider

//--tweet0053
Pbind(\freq,Pseq("SUPERCOLLIDER".ascii,inf)*Pstutter(64,Pseq([3,4,5],inf))*[1,2.045],\dur,0.03,\amp,Pseq([0,0.1],inf)).play// #SuperCollider

//--tweet0054
play{CombN.ar(SyncSaw.ar(Saw.ar([3,4],32,64),Saw.ar([4,3],99,Duty.kr(1,0,flop(Dseq(2!6++4++3,99)*(4**(0..4))))))/9,1,1/6,2)}//#SuperCollider

//--tweet0055
play{a=Pulse;CombN.ar(Slope.ar(a.ar(a.ar([1,2]/3,1/9,50,[50,150])),a.ar([3,4],1/3)+a.ar([2,3],1/4)/10+0.005).cos/5,1,1/6,2)}//#SuperCollider

//--tweet0056
play{MantissaMask.ar(Pulse.ar(LFPulse.ar(1/8,0,0.55,15,76)+LFSaw.ar([0.1,0.11]),Saw.ar(10)),LFPar.ar(1/16,[0,0.5],3,3),0.7)}//#SuperCollider

//--tweet0057
a=GVerb;fork{loop{z=play{#b,c,d,e,f,g,h,i=(1..50).scramble;a.ar(a.ar(a.ar(a.ar(Dust.ar(1),b,c),d,e),f,g),h,i)/20};6.wait;z.release(5)}}//#sc

//--tweet0058
play{CombN.ar(SinOsc.ar(Saw.ar(3,64,99),Saw.ar([3,4],Saw.ar(1,32,128),Duty.ar(1,0,flop(Dseq([0,8,1,5])*[1,4,8]))))/9,1,1/6)}//#SuperCollider

//--tweet0059
a=LFTri;play{CombN.ar(SinOsc.ar(Saw.ar(3,128,128),Saw.ar([3,4],a.ar(a.kr(0.1,0,8,12),0,32,128)).sin)/4,1,1/6,a.kr(1/32)+1)}// #SuperCollider

//--tweet0060
a=LFSaw;play{FreeVerb.ar(CombN.ar(VarSaw.ar(a.ar([32,48],0,42*a.ar(1/[16,24]),8),0,a.ar([18,12],0,1/64,1/64)).sin/2,1,1,2))}//#SuperCollider

//--tweet0061
a=Demand;b=SinOsc;play{b.ar(a.ar(t=Saw.ar([9,9.01]),0,Dseq(0!6++500,inf)),b.ar(a.ar(t,0,Dshuf((0..7)*99,inf)).lag(0.04)))/2}//#SuperCollider

//--tweet0062
play{a=SinOsc;b=(1..9);Splay.ar(a.ar(b*55).clip(a.ar(2/b,0,0.5),a.ar(3/b,0,0.5,1))*a.ar(b*55+(4/b),0,a.ar(1/b,0,6)).tanh)/5}//#SuperCollider

//--tweet0063
format(a="c=SinOsc;play{FreeVerb.ar(c.ar(0,c.ar(Duty.ar(v=1/8,0,Dseq("+($%!96)+",inf)!2))),v,1)}",*a.ascii-96*96).interpret// #SuperCollider

//--tweet0064
format(a="play{GVerb.ar(SinOsc.ar(0,SinOsc.ar(Duty.ar(1/8,0,Dseq("+($%!16)+",inf))))/8,20,1/8)}",*a.ascii.midicps).interpret//#SuperCollider

//--tweet0065
format(a="play{SinOsc.ar(%/[%,%],LPF.ar(LFSaw.ar(Duty.ar(16/%,0,Dseq("+($%!96)+",inf)),%),%,%))}",*a.ascii).postln.interpret//#SuperCollider

//--tweet0066
tr(a="play{VarSaw.ar(Duty.ar(0.1,0,Dseq("+($%!8)+".flat.midicps,inf)!2).lag3(0.03),0,0.3)}",$%,a.ascii%64+36).post.interpret//#SuperCollider

//--tweet0067
("nj_wy_;JDRpg,_p&.,./*.*.,/*0ng'9QglMqa,_p&77)_*Quccn,_p&Q_u,_p&Y/*/,./03[(_'*2..(_'#_',r_lf-0{".ascii+2).asAscii.interpret//#SuperCollider

//--tweet0068
play{a=LocalIn.ar(2);LocalOut.ar(a=Hasher.ar(a.round(LFTri.ar(LFTri.ar(1e-4)/4+1e-3,0,LFTri.ar(1e-3)).round(2e-4))));a*0.45}//#SuperCollider

//--tweet0069
play{a=LocalIn.ar(2);LocalOut.ar(a=Hasher.ar(a.round(LFPar.ar(4e-3).round(3e-3)/3+a)));FreeVerb2.ar(a[0],a[1],0.33,1,1,0.4)}//#SuperCollider

//--tweet0070
play{a=LocalIn.ar(2);LocalOut.ar(a=Hasher.ar(a.round(SinOsc.ar(3.3e-4,a*2pi).round(5e-4))));a/3+CombN.ar(a,1,[1,0.9],3,0.4)}//#SuperCollider

//--tweet0071
play{a=LFTri;b=(2..5);Splay.ar(a.ar(abs(a.ar(b/9/9/9).round(a.ar(9-b*99,9-b/9,a.ar(b/9,b/99)))*a.ar(9,0,9-b*99,99*b),b/9)))}//#SuperCollider

//--tweet0072
play{a=Pulse;b=(1..8-1);GVerb.ar(Limiter.ar(Splay.ar(a.ar(abs(a.ar(b,1/8,8-b/8)).round(a.ar(b*8,b/8,a.ar(b))))))/8,8,1,0.8)}//#SuperCollider

//--tweet0073
play{a=Pulse;b=(1..8);CombN.ar(Splay.ar(a.ar(a.ar(b,a.ar(b/9),b*9,b*99+99),1/3,a.ar(b/9+a.ar(1,2/3,8,10)/9)).tanh),1,2/3,4)}//#SuperCollider

//--tweet0074
play{a=Pulse;BLowPass4.ar(a.ar(a.ar(2,0.2,a.ar(3,0.3)*500,[600,606]*a.ar(5))).sin,LFPar.ar(0.07)*4e3+5e3,LFPar.ar(0.1)+1.3)}//#SuperCollider

//--tweet0075
play{a=SinOsc;b=(1..16)*8;a.ar(a.ar(b).sum+[2,3]+a.ar(1/8)*99*a.ar(b/(a.ar(1/6)*2+2.05),0,4+a.ar(1/8)).reduce('bitOr'))*0.5}//#SuperCollider

//--tweet0076
play{a=SinOsc;a.ar(a.ar([1,2,4,8]/4*999).sum*50+[2,1],a.ar(1/8)*8)).tanh*a.ar(4)*6)/2}// #SuperCollider

//--tweet0077
play{a=SinOsc;b=a.ar(a.ar(1/[5,6])+[798,912],a.ar(1/16)*19+99*a.ar([9,8]),a.ar(a.ar(6)*a.ar(0.009)));a.ar([201,301],b).tanh}//#SuperCollider

//--tweet0078
play{a=GrayNoise.ar;b=(1..9);CombL.ar(a,1,b/Duty.ar(3,0,Dseq([0.5,1,2,3]*99,99)).lag3(1)).mean/2+Ringz.ar(a/99,b*99).mean!2}//#SuperCollider

//--tweet0079
play{Saw.ar((99,111..999),LFSaw.ar(1.1/(1..76))).mean.distort.distort.distort.distort.distort.distort.distort.distort*3.5!2}//#SuperCollider

//--tweet0080
play{a=SinOsc;b=a.ar(1/3);Duty.ar(SampleDur.ir,0,Dseq([0,1],inf)).bitXor(a.ar(b>0*30+60,0,a.ar(4,0,a.ar([3,2]/9,b*3,9))))/9}//#SuperCollider

//--tweet0081
fork{inf.do{t=3.0.linrand;play{{XLine.ar(1.0.rand,0.5.rand,t)}!2*SinOsc.ar(XLine.ar(999.rand+99,999.rand,t,1,0,2))};t.wait}}//#SuperCollider

//--tweet0082
play{a=LFTri.ar(1/9)*0.07+0.0708;CombN.ar(Decay2.ar(Duty.ar(Dseq([1e-4,a/2],inf),0!2,Dseq([-1,0,1,0],inf)),a/9,a)/5,1,1,12)}//#SuperCollider

//--tweet0083
play{a=LFCub;Splay.ar({|i|i=i+1;Formant.ar(*Sweep.ar(a.ar(i/[1,2,3])>a.ar(i/9,i/9,1/6,1/3),0.05)*99*i+99*i)*a.ar(0.1/i)}!6)}//#SuperCollider

//--tweet0084
play{a=Saw;Splay.ar(Formant.ar(a.ar((5,7..15)*19)*99+199,a.ar((1,3..13)*29)*199+299,a.ar((3,5..11)*a.ar(3,2,3))*299+399))/3}//#SuperCollider

//--tweet0085
play({Duty.ar(1/9600,0,Dseq((0..255).collect{|i|[1]++(1-i.asBinaryDigits.reverse)++[0]}.flat,inf),2)!2},s,0,0)// #SuperCollider talks serial

//--tweet0086
play{a=LFNoise2.kr(1/(9..17));Splay.ar(Ringz.ar(BPF.ar(Dust2.ar(a.abs*1e4),a.exprange(99,1e4),1.1-a),(9..1)*99,a+1.1,a)/5)}// #SuperCollider

//--tweet0087
play{BLowPass4.ar(Splay.ar(VarSaw.ar(200*Duty.kr(1/(1..5),0,Dseq(flat({|x|{|y|y+1/(x+1)}!8}!8),inf)))),5e3,LFTri.kr(9)+1.1)}//#SuperCollider

//--tweet0088
play{a=SinOsc;LPF.ar(LeakDC.ar(a.ar([98,99],a.ar([8,9],a.ar(1/[88,99],0,2pi),pi).lag(a.ar([9,8])),a.ar(1/[8,9])*9)%1),9e3)}// #SuperCollider

//--tweet0089
play{GVerb.ar(Splay.ar(Ringz.ar(Blip.ar(a=[4,14,5,15,6,16,8],LFNoise0.ar(4/a)*99,LFNoise1.ar(4/a).max(0)),a*99,4/a))/6,200)}//#SuperCollider

//--tweet0090
play{FreeVerb.ar(Splay.ar(BBandPass.ar(Blip.ar(b=(1..8)+1,LFTri.ar(1/b)*9e3,LFTri.ar(3/4/b).max(0)),b*999,1/b),2,3),0.3,1)}// #SuperCollider

//--tweet0091
play{a=LFPulse;Splay.ar(Pulse.ar((1..10)*a.ar(1/24+a.ar(1/3)*12,0,1/9,a.ar(1/12,0,0.5,9,48)).abs+6).reduce(\mod).softclip)}// #SuperCollider

//--tweet0092
play{Mix(Pan2.ar(Formlet.ar(Dust.ar(b=(1..8)),b*99,b/99,b/9),SinOsc.ar(b),LFSaw.ar(9.5-b,b/9,LFTri.ar(b/5)*4).max(0)).sin)}// #SuperCollider

//--tweet0093
play{x=SinOsc;a=LocalIn.ar(2);z=x.ar([3.1,4.2]+a)-Balance2.ar(a[0],a[1],x.ar(a*x.ar(a)*999));LocalOut.ar(CombN.ar(z/3));z/5}//#SuperCollider

//--tweet0094
play{a=Blip;b=LFSaw;CombN.ar(a.ar(a.ar(b.ar(1/[9,99])*1e3+4e3,b.ar(1/[23,24])*4+5,b.ar(1/[5,6])+b.ar(1/[8,9])*9)),0.3,0.3)}// #SuperCollider

//--tweet0095
{|i|a=VarSaw;b=i/8;play{Pan2.ar(a.ar(b*666+a.ar(b+0.03,b),0,b+0.06,a.ar(b+1,0,b+0.1,6+b,7+b)).sin.tanh,a.ar(b+1,b),0.2)}}!8// #SuperCollider

//--tweet0096
play{a=LFTri;b=LocalIn.ar;LocalOut.ar(c=Limiter.ar(CombC.ar(a.ar(d=b+1)*a.ar(d*999),1,a.ar((2..5)/3).mean/2+0.5,6)));c/2!2}// #SuperCollider

//--tweet0097
play{a=LFTri;b=LocalIn.ar;LocalOut.ar(c=Limiter.ar(CombC.ar(a.ar(400)*a.ar(d=b+2),1,a.ar((2..5)/d/d/d).mean*0.5+0.5)));c!2}// #SuperCollider

//--tweet0098
play{a=LFSaw;b=LocalIn.ar;LocalOut.ar(c=Limiter.ar(CombC.ar(a.ar(d=b+3.3*99)*a.ar(a.ar(d/9)*99),2,a.ar(1/d)/2+1,9)));c/2!2}// #SuperCollider

//--tweet0099
Pspawn(Pbind(\method,\par,\delta,1/8,\pattern,{Pbind(\dur,a=Pseq((1..9).sputter),\sustain,1/8/a,\degree,a,\detune,a)})).play//#SuperCollider

//--tweet0100
r{loop{x=play{a=DelayN.ar(LPF.ar(InFeedback.ar(0,2),z=1.rrand(9)*99));SinOsc.ar(z+[0,3],a*pi)/2};6.wait;x.release(9)}}.play// #SuperCollider

//--tweet0101
r{loop{x=play{c=c?1%8+1;a=DelayN.ar(InFeedback.ar(0,2),1,1);SinOsc.ar(c*99+[0,2],a[1..0])/4};wait(9-c);x.release(16)}}.play// #SuperCollider

//--tweet0102
{|i|play{a=DelayC.ar(InFeedback.ar(1-i),8,LFSaw.ar(1e-5*i+1e-4*(LFSaw.ar(0.1)>0),i,4,4));SinOsc.ar(99+[0,a/9],a*pi)/5!2}}!2// #SuperCollider

//--tweet0103
{|i|b=SinOsc;play{a=DelayC.ar(InFeedback.ar(1-i),6,b.ar(1e-3*(b.ar(1,i)),i,3,3));b.ar(45+[a/8,a/9]+b.ar(0.123),a*3)/5!2}}!2// #SuperCollider

//--tweet0104
play{a=LFCub;(50..85).midicps.clump(2).collect{|x,y|a.ar(TRand.ar(x,y,Dust.ar(b=a.ar(y/x).exprange(1,5e3))),0,b/5e3)}.mean}// #SuperCollider

//--tweet0105
play{a=SinOsc;f=InFeedback.ar(0,2);Pan2.ar(a.ar(a.ar(b=(1..9))*b+99,f/(9-b),a.ar(a.ar(b,f))).sum.sin,a.ar(a.ar(2.001)*12))}// #SuperCollider

//--tweet0106
play{a=SinOsc;b=InFeedback.ar(0,2);a.ar(9,a.ar(Pitch.kr(Balance2.ar(b[0],b[1],a.ar(12)),execFreq:99).flop[0])+a.ar(3,b,2))}// #SuperCollider

//--tweet0107
play{a=SinOsc;d=a.ar(12*a.ar(9))%1/4;c=Amplitude.ar(InFeedback.ar(0),d,d)+a.ar(d*d+[32.01,32]);BBandPass.ar(a.ar(0,c*9,c))}// #SuperCollider

//--tweet0108
play{a=SinOsc;Splay.ar({|i|j=i/700;a.ar(j,a.ar(j*2,a.ar(j*3,a.ar(j*4,a.ar(j*5,InFeedback.ar/99,2pi),2pi),2pi),2pi))}!15)/2}// #SuperCollider

//--tweet0109
play{a=LFSaw;Formant.ar(b=a.ar(a.ar(a.ar(a.ar(0.1)+1.0905*9)/99)*999)*999,c=CombN.ar(b,1,[0.1,0.11]),CombN.ar(c,1,0.19))/3}// #SuperCollider

//--tweet0110
play{Splay.ar({a={LFSaw.kr(0.05.rand2,0,1.0.rand)}!3;BLowPass4.ar(Saw.ar(a@0*250+300,a[2].max(0)),a@1*2e3+2100,0.025)}!99)}// #SuperCollider

//--tweet0111
play{a=SinOsc;Splay.ar(a.ar(PulseCount.ar(f=InFeedback.ar(0,2).sum)%999+(60,63.0005..99)*a.ar(2**f)*2+[3,4],f>0*f*9)).tanh}// #SuperCollider

//--tweet0112
r{inf.do{|i|Ndef(\,{VarSaw.ar(Duty.ar(1/12,0,Dseq((12..0)*(i%63+99)),2)*[1,1.01],0,i/9%9/9)/9}).play.spawn;wait(1/3)}}.play// #SuperCollider

//--tweet0113
{|i|play{a=Duty.ar(b=1/24,0,Dseq(Dshuf({b.linrand}!8,16+i),99));Pan2.ar(BPF.ar(Saw.ar(c=a+i+1*99,a*3),c*2,0.6)*5,i/4-1)}}!9// #SuperCollider

//--tweet0114
play{a=LFNoise1;BPF.ar(Splay.ar(SinOsc.ar(0,a.ar((999,888..111),a.ar(1/(9..1),a.ar({|i|i+1/(9-i)}!9,99))))/4),1500,a.ar+1)}// #SuperCollider

//--tweet0115
play{a=Pulse;d=Splay.ar(a.ar(Duty.ar(c=a.ar(b=(6..1),b/7.5)/8+1,0,Dseq(b*c+c.lag3(9)*66,inf))))/9;d+GVerb.ar(d.mean,25,25)}// #SuperCollider

//--tweet0116
play{BPF.ar(SinOsc.ar(Duty.ar(1/300,0,Dseq([Dseq([a=1270,b=2225],2e2),Drand([[1070,a],[2025,b]],[1e3,2e3])],inf))),1500,3)}// #SuperCollider

//--tweet0117
play{a=LFTri.ar(1/[8,7]).abs;CombC.ar(Pulse.ar(Duty.ar(a+0.1/9,0,Dseq([Dshuf((1..9)*99,7),3e3],inf)).lagud(*a/6),a),1,a,5)}// #SuperCollider

//--tweet0118
fork{999.do{|i|unixCmd("afplay -v"+5.0.rand+" -r"+(9.rand+1)+Platform.resourceDir+/+"sounds/a11wlk01.wav");wait(0.5.rand)}}// #SuperCollider

//--tweet0119
OSCFunc({|m|a.set(\f,m[4]-0.555%4)},'/');a=play{|f=55|SendPeakRMS.kr(x=SinOsc.ar(f.lag(5)*[155,555]*f,5**f),9*f,5,'/');x/5}// #SuperCollider

//--tweet0120
play{a=LFTri;CombN.ar(VarSaw.ar(Select.kr(a.kr(1/[7,8])*a.kr(1/9,0,99),(60..79).midicps),0,a.kr(1/[3,4])%1),1,1/[5,6],8)/4}// #SuperCollider

//--tweet0121
play{a=SinOsc;CombN.ar(a.ar(Select.kr(a.kr(1/[8,7])*a.kr(1/30,0,9),(56,62..98).midicps),0,a.ar(1/[4,3])),1,1/[6,5],9).tanh}// #SuperCollider

//--tweet0122
play{a=LFPar;BLowPass.ar(a.ar(Select.kr(a.kr(1/[3,4],0,64*a.kr(5)),(60..67).midicps)),a.kr(0.04)+5*500,a.kr(1/[5,6])+1.01)}// #SuperCollider

//--tweet0123
play{a=SinOsc;a.ar(a.ar(1/[8,12])>0.9+1*[400,404],InFeedback.ar([1,0]).lagud(a.ar(b=1/(1..6)).mean,a.ar(b*1.25).mean)*4pi)}// #SuperCollider

//--tweet0124
play{a=SinOsc;a.ar(a.ar(4)>0.2+1*[99,98],InFeedback.ar([1,0]).lagud(a.ar(0.1).abs/5,a.ar(a.ar(1/99)).abs)*a.ar([301,303]))}// #SuperCollider

//--tweet0125
play{a=SinOsc;a.ar(a.ar(1/[8,9])*4+[400,202],CombC.ar(InFeedback.ar([1,0]).lagud(a.ar(1/9)+1/88,a.ar(1/8)+1/99),1,0.08,9))}// #SuperCollider

//--tweet0126
play{a=SinOsc;c=HPF.ar(a.ar([1,4/3],HPF.ar((1..9).sum{|x|Pan2.ar(a.ar(1/x)>0.5,a.ar(666/x))},5)),5);GVerb.ar(c,99,9)/7+c/4}// #SuperCollider

//--tweet0127
play{a=LFTri;distort(LeakDC.ar(a.ar(LeakDC.ar((1..9).sum{|x|Pan2.ar(a.ar(1/x)>0.51,a.ar(a.ar(x+1)*9.99+1200/x))})*4e3))/9)}// #SuperCollider

//--tweet0128
play{a=LFTri;RLPF.ar(LeakDC.ar(a.ar(LeakDC.ar((1..9).sum{|x|Pan2.ar(a.ar(1/x,x/3)>0.3333,a.ar(666/x))})*999)).distort,3e3)}// #SuperCollider
