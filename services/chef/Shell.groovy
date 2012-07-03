/*******************************************************************************
* Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
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

def static sh(command, shellify=true, env=[:]) {
	println("Running \"${command}\"")
	if (shellify) {command = shellify_cmd(command)}
	def proc = startProcess(command, env)
  def stdout = ""
  def stderr = ""
  proc.inputStream.eachLine { println "STDOUT: ${it}";  stdout += "${it}\n" }
	proc.errorStream.eachLine { println "STDERR: ${it}";  stderr += "${it}\n" }
	proc.waitFor()
	println("Command finished with return code ${proc.exitValue()}")
	if (proc.exitValue() != 0) {
    throw new ShellRuntimeException(command, proc.exitValue(), stdout, stderr)
  }
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
	def tmpDir = new File(pathJoin(System.getProperty("java.io.tmpdir"), "shell"))
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
