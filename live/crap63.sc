1
b.free

//assumes hop of half fftsize, fine
b = Buffer.alloc(s,1024,1); //for sampling rates 44100 and 48000
//b = Buffer.alloc(s,2048,1); //for sampling rates 88200 and 96000

d=Buffer.read(s,"sounds/a11wlk01.wav");
//d=Buffer.read(s,"sounds/break");




(
{

var in, fft, output;

in= SoundIn.ar([0,1]); //PlayBuf.ar(1,d,BufRateScale.kr(d),1,0,1);

fft = FFT(b, in, wintype:1);
//fft = PV_MagFreeze(fft, MouseX.kr(1,-1));
//fft = PV_RectComb(fft, 8, LFTri.kr(0.097, 0, 0.4, 0.5), LFTri.kr(0.24, 0, -0.5, 0.5));
    fft = PV_RectComb(fft,  MouseX.kr(0, 32), MouseY.kr, 0.2);



//output=TPV.ar(fft, 1024, 512, 170,MouseX.kr(1,170), MouseY.kr(0.25,3),4,0.2);
output = IFFT(fft);

//Out.ar(0,Pan2.ar(output));
output * 2
}.play
)

