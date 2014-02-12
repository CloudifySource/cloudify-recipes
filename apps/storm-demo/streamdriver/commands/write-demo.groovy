import com.gigaspaces.streaming.client.*
import com.gigaspaces.streaming.model.*

log=new File("/tmp/out")
log.withWriter{writer->
writer.write( "in write-demo\n")
}

//writes moby dick, over and over

String[] lines= new File('commands/mobydick.txt').text.split("\\n")

log.withWriterAppend{writer->
writer.write( "read moby\n")
}

//list available streams
context=null
try{
context = org.cloudifysource.dsl.context.ServiceContextFactory.getServiceContext()
}
catch(e){
context = org.cloudifysource.utilitydomain.context.ServiceContextFactory.getServiceContext()
}
log.withWriterAppend{writer->
writer.write( "got context ${context}\n")
}

id=context.instanceId

locator=context.attributes.thisService["locator"] 
space=context.attributes.thisService["space"] 

numsecs=Integer.valueOf(context.attributes.thisService["numsecs"] )//how long to write
rate=Integer.valueOf(context.attributes.thisService["rate"] ) //lines per sec

streamname=context.attributes.thisService["streamname"] 

log.withWriterAppend{writer->
writer.write( "before get stream factory : space=${space} locator=${locator}\n")
}

factory = new XAPStreamFactory("jini://*/*/${space}?locators=${locator}")

log.withWriterAppend{writer->
writer.write( "got stream factory\n")
}


xts=null
factory.listStreams().each{
	if(it == streamname){
		xts=factory.getTupleStream(streamname)
		return;
	}
}
if(xts==null){
	xts=factory.createNewTupleStream(streamname,0,["sentence"])
}

assert (xts!=null),"stream ${streamname} not found in space ${space}"

fields=xts.getStreamConfig().getFields()

i=0
now=System.currentTimeMillis()
//one batch per second
while (System.currentTimeMillis()-now < numsecs*1000){ 
	start=System.currentTimeMillis()
	batch=[]
	for(j in 0..rate){
		if(i>=lines.size())i=0
		tuple=xts.createTuple()
		tuple.setProperty(fields[0],lines[i])
		batch << tuple
		i++
	}

	xts.writeBatch(batch.toArray(new XAPTuple[0]))
	println "wrote batch"

	sleepfor=(start+1000)-System.currentTimeMillis()
	if(sleepfor>0)Thread.sleep(sleepfor)
}


