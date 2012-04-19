def static sh(command, shellify=true, env=[:]) {
	println("Running \"${command}\"")
    if (shellify) {command = shellify_cmd(command)}
    def proc = startProcess(command, env)
    proc.inputStream.eachLine { println "STDOUT: ${it}" }
    proc.errorStream.eachLine { println "STDERR: ${it}" }
	proc.waitFor()
	println("Command finished with return code ${proc.exitValue()}")
    assert proc.exitValue() == 0
}

def static shellOut(command, env=[:]) {
    return startProcess(shellify_cmd(command), env).inputStream.text
}

def static startProcess(command, env=[:]) {
	ProcessBuilder pb = new ProcessBuilder(command)
    def environment = pb.environment()
    if (!env.isEmpty()) {
        environment += env
    }
    return pb.start()
}
// overload shellify to handle different types
def static shellify_cmd(java.util.ArrayList command) {
    return ["/bin/sh", "-c", command.join(" ")]
}

def static shellify_cmd(java.lang.String command) {
    return ["/bin/sh",  "-c", command]
}

def static shellify_cmd(org.codehaus.groovy.runtime.GStringImpl command) {
    return shellify_cmd(command as java.lang.String)
}

def static sudo(command, env=[:]) {
    if (System.getProperty("user.name") != "root") {
        command = "sudo ${command}"
    }
    return sh(command, true, env)
}

def static sudo(java.util.ArrayList command, env=[:]) {
    return sudo(command.join(" "), env)
}

def static sudoWriteFile(fileName, content) {
    def tmpDir = new File(pathJoin(System.getProperty("java.io.tmpdir"), this.class.name))
    tmpDir.mkdir()
    def tmpFile = new File(pathJoin(tmpDir.getPath(), new File(fileName).getName()))
    tmpFile.withWriter() {it.write(content)}
    return sudo("mv ${tmpFile.getPath()} ${fileName}")
}

def static pathJoin(Object... args) {
    return args*.asType(String).join(File.separator)
}

def static sudoReadFile(filename) {
    return shellOut("sudo cat ${filename}")
}
