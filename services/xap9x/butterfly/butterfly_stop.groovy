new AntBuilder().sequential {

    exec(executable:"pkill", osfamily:"unix",
            output:"pkill.${System.currentTimeMillis()}.out",
            error:"pkill.${System.currentTimeMillis()}.err"
    ){
        arg("line":"-f 'butterfly.server.py --host=0.0.0.0 --port=8080'")
    }
}

