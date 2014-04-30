
~a = ArraySpec(\freq.asSpec, 8)


// I only want to set x and z -- this works, of course:

f = { arg w, x, y, z; "w=% x=% y=% z=%".format(w, x, y, z).postln  };
a = (x: 13, z: 99); 
f.valueWithEnvir(a); ""


format("%", thisFunction.cs)
