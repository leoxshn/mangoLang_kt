use std*

val badName = "semicolon"

/*[extern]
[cname: "malloc"]
fn malloc (bytes I32) Ptr<I16>*/

[entry]
fn main {

    io.println(
        unsafe {
            val s = String {
                length: 3
                chars: Ptr { ' ', ' ', ' ' }
            }

            s.chars[0] = '_'
            s.chars[1] = 'q'
            s.chars[2] = '4'
            s
        }
    )

    io.println("aa")

    io.println(
        unsafe {
            val s = String {
                length: 3
                chars: Ptr<I16> { length: 3 }
            }

            s.chars[0] = 'n'
            s.chars[1] = '_'
            s.chars[2] = '3'
            s
        }
    )
/*
    use secondFile*
    fn doit String {
        val name = getName()
        use std.string*

        if name == badName {
            io.print("Go away, ")
            io.print(name)
            io.print("!")
            io.println(" Ur not welcome here!")
        }
        else {
            io.print("Hi, ")
            io.print(name)
            io.println("!")
        }

        return name
    }
    val theGoodTheAmazing_A = doit
    val a = theGoodTheAmazing_A()
    io.print("name: ")
    io.println(a)
    io.print("length: ")
    use std.string*
    io.println(a.length.toString())
    io.print("length, but in hex: ")
    io.println(a.length.toString(16))
    io.print("a string: ")
    io.println(secondFile.astring)
    thing.something()

    // Number literals test

    io.println(10)   // 10
    io.println(0x10) // 16
    io.println(0b10) // 2
    io.println(0s10) // 6
*/
    /*
    io.println(10l)
    io.println(10.1)
    io.println(10.1f)
    io.println(.10)
    io.println(.10f)
    io.println(10f)
    io.println(10.)*/

    val testReading = read()
    io.print("-> ")
    io.println(testReading)
    io.println()
    io.println(read())

    use std.text.builder*

	val builder = StringBuilder {
	    length: 0
	    chars: Ptr<I16> { length: 4 }
	    capacity: 4
	}
	builder.appendChar('L')
    io.println(builder.toString())
}

namespace thing {
    val someRandomString = "this is a random string"

    fn something {
        io.println(someRandomString)
    }
}

/*
String* string = malloc(sizeof(String));
string->length = 0;
Int ch;
string->chars = malloc(512);
while (((ch = getchar()) != '\n') && (ch != EOF) && (string->length < 512)) {
    string->chars[string->length++] = ch;
}
return string;
*/

fn read String {
    use std.text.builder*

	val builder = StringBuilder {
	    length: 0
	    chars: Ptr<I16> { length: 512 }
	    capacity: 512
	}
	builder.appendChar('9')
	/*
    var ch = io.readChar()
    while ch != '\n' && ch != '\0' {
        builder.appendChar(ch)
        ch = io.readChar()
    }*/
    return builder.toString()
}