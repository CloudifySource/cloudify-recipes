/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

class ShellRuntimeException extends Exception {
    String stdout
    String stderr
    String command
    int exitValue

    def ShellRuntimeException(command, exitValue, stdout=null, stderr=null) {
        this.stdout = stdout
        this.stderr = stderr
        this.exitValue = exitValue
        this.command = command
    }

    String toString() {
        return """ShellRuntimeException: Command "${this.command}" failed with exit code ${this.exitValue}.
====== STDOUT =======
${this.stdout}
=== END OF STDOUT ===

====== STDERR =======
${this.stderr}
=== END OF STDERR ===
""".toString()
    }
}

def static sh(Map opts, ArrayList command, shellify=true) {
    sh(command, shellify, opts)
}
def static sh(Map opts, String command, shellify=true) {
    sh(command, shellify, opts)
}
def static sh(command, shellify=true, Map opts=[:]) {
    Map env = opts.env?: [:]
    println("Running \"${command}\"")
    if (shellify) {command = shellify_cmd(command)}
    def args = [command, env]
    if ("cwd" in opts) {
        args << opts.cwd
    }
    def proc = startProcess(*args)
    def stdout = ""
    def stderr = ""
    proc.inputStream.eachLine { println "STDOUT: ${it}";  stdout += "${it}\n" }
    proc.errorStream.eachLine { println "STDERR: ${it}";  stderr += "${it}\n" }
    proc.waitFor()
    println("Command finished with return code ${proc.exitValue()}")
    if ((! opts.ignore_failure?: false) && proc.exitValue() != 0) {
        throw new ShellRuntimeException(command, proc.exitValue(), stdout, stderr)
    }
    return proc.exitValue()
}

def static test(command) {
    return (sh(command, true, [ignore_failure: true]) == 0)
}

def static shellOut(command, Map env=[:]) {
    return startProcess(shellify_cmd(command), env).inputStream.text
}

def static startProcess(command, Map env=[:], String cwd) {
    startProcess(command, env, new File(cwd))
}
def static startProcess(command, Map env=[:], File cwd=null) {
    ProcessBuilder pb = new ProcessBuilder(command)
    def environment = pb.environment()
    if (!env.isEmpty()) {
        environment += env
    }
    if (!cwd.is(null)) {
        pb.directory(cwd)
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

def static sudo(command, Map opts=[:]) {
    if (System.getProperty("user.name") != "root") {
        command = "sudo ${command}"
    }
    return sh(command, true, opts)
}

def static sudo(java.util.ArrayList command, Map opts=[:]) {
    return sudo(command.join(" "), opts)
}

def static sudoShellOut(command, Map env=[:]) {
    if (System.getProperty("user.name") != "root") {
        command = "sudo ${command}"
    }
    return "\n" + shellOut(command, env)
}

def static sudoShellOut(List command, Map env=[:]) {
    return sudoShellOut(command.join(" "), env)
}


def static sudoWriteFile(fileName, content) {
    def tmpDir = new File(pathJoin(System.getProperty("java.io.tmpdir"), "shell"))
    tmpDir.mkdir()
    def tmpFile = new File(pathJoin(tmpDir.getPath(), new File(fileName).getName()))
    tmpFile.withWriter() {it.write(content)}
    return sudo("mv ${tmpFile.getPath()} ${fileName}")
}

def static pathJoin(Object... args) {
    def path_elements = args*.asType(String)
    path_elements.removeAll("")
    return path_elements.join(File.separator)
}

def static underHomeDir(inner_path) {
    return pathJoin(System.properties["user.home"], inner_path)
}

def static File getTmpDir() {
    File tempFile = File.createTempFile(new Random().nextInt().abs().toString(), "")
    if (tempFile.exists()) { tempFile.delete() }
    tempFile.mkdir()
    return tempFile
}

def static sudoReadFile(filename) {
    return shellOut("sudo cat ${filename}")
}

def static pathExists(path) {
    return new File(path).exists()
}

def static download(target, url) {
    new File(target).withOutputStream() { out ->
        out << new URL(url).openStream()
    }
}
