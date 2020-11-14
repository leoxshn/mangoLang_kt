use primitives*
use string*

[extern]
[cname: "putchar"]
fn print (char I16)

fn print (string String) -> {
	var i = 0
	while i < string.length {
		print(string[i])
		i += 1
	}

    [extern]
    [cname: "flushPrint"]
    fn flushPrint

	flushPrint()
}

[inline]
fn print (i Int) -> print(i.toString())

[inline]
fn print (i Bool) -> print(i.toString())



[inline]
fn println -> print('\n')

[inline]
fn println (text String) -> {
    print(text)
    println()
}

[inline]
fn println (char I16) -> {
    print(char)
    println()
}

[inline]
fn println (i Int) -> println(i.toString())

[inline]
fn println (i Bool) -> println(i.toString())



[extern]
[cname: "getchar"]
fn readChar I16

fn readln String -> {
    use text.builder*

	val builder = StringBuilder {
	    length: 0
	    chars: Ptr<I16> { length: 512 }
	    capacity: 512
	}
    var ch = readChar()
    while ch != '\n' && ch != '\0' {
        builder.appendChar(ch)
        ch = readChar()
    }
    builder.toString()
}