import java.util.io.*
import org.codehaus.groovy.tools.shell.*
BufferedReader br = new BufferedReader(new InputStreamReader(System.in))

println "Welcome to XAP Demo"
println "Tutorials:"
println "1 XAP DEMO - Write/Read to/from myDataGrid space"
println "2 Open Groovy Shell"

print "Choose tutorial: "

input = br.readLine()

println "You entered: $input"

if (input == "1") {
    println "Starting the demo"
    DemoScript.run_demo()
    println "demo ended"
    println "Opening Groovy Shell"
    org.codehaus.groovy.tools.shell.Main.main()
} else if (input == "2") {
    println "Opening Groovy Shell"
    org.codehaus.groovy.tools.shell.Main.main()
} else if (input == "3") {
    try {
        print "We will now connect to the space, type the following code:\nUrlSpaceConfiguer();\n"
        input = br.readLine()
        space = Eval.me(input)
    } catch (Exception e) {
        print "Error: "
        e.printStackTrace()
    }
}