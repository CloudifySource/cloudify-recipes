["pwd"].execute()
userHome = System.properties["user.home"]
["cp", "-r", "scripts", "${userHome}"].execute()
["chmod", "+x", "${userHome}/scripts/*.sh"].execute()
