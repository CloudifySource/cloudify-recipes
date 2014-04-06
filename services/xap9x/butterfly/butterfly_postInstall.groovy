import org.cloudifysource.utilitydomain.context.ServiceContextFactory

context = ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File(context.serviceName + "-service.properties").toURL())
xapDir = "${context.serviceDirectory}/${config.installDir}/${config.xapDir}"

// If not localcloud, write JAVA_HOME and updated PATH to .bashrc
if (!context.isLocalCloud()) {
    FileWriter out = new FileWriter("${System.getenv('HOME')}/.bashrc",true);
    out.write("${System.getProperty("line.separator")}");
    out.write("export JAVA_HOME=${System.getenv('HOME')}/java");
    out.write("${System.getProperty("line.separator")}");
    out.write("export PATH=\$PATH:\$JAVA_HOME/bin");
    out.close();
}

// write CLASSPATH to .bashrc
FileWriter out = new FileWriter("${System.getenv('HOME')}/.bashrc", true);
out.write("${System.getProperty("line.separator")}");
out.write("export CLASSPATH=\"${xapDir}/lib/platform/jpa/*:${xapDir}/lib/required/*\"");
out.close();

// write imports to groovysh.profile, if .groovy folder does not exist then create it
final File file = new File("${System.getenv('HOME')}/.groovy/groovysh.profile");
final File parent_directory = file.getParentFile();

if (null != parent_directory)
{
    parent_directory.mkdirs();
}
// Start writing to groovysh.profile
BufferedReader br = new BufferedReader(new FileReader("groovysh_imports.txt"));
out = new FileWriter(file, true);
String line;
while ((line = br.readLine()) != null) {
    if ( line.trim().length() == 0 ) {
        continue;  // Skip blank lines
    }
    out.write("${System.getProperty("line.separator")}");
    out.write("import "+line);
}
out.close();
br.close();

