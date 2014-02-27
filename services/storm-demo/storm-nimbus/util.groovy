// Simple file system based locking.
// Definitely doesn't work on windows

// Lock a given file.  If wait is supplied, retry failed lock attempts for "wait" seconds
static def lockFile(path,wait=0){
        def start=System.currentTimeMillis()
        def f=new File(path)
        def ret
        wait=wait*1000
        if(wait==0)wait=500
        f=new File("/tmp/${f.getAbsolutePath().replace("_","__").replace("/","_")}.lck")
        while(System.currentTimeMillis()-start < wait)
        {
                ret=f.createNewFile()
                if(ret)break
                Thread.sleep(1000)
        }
        return ret
}

static def isFileLocked(path){
        def f=new File(path)
        f=new File("/tmp/${f.getAbsolutePath().replace("_","__").replace("/","_")}.lck")
        return f.exists()
}

static def unlockFile(path){
        def f=new File(path)
        f=new File("/tmp/${f.getAbsolutePath().replace("_","__").replace("/","_")}.lck")
        f.delete()
}

