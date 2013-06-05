//Writes random tuples to specified stream
//If the stream doesn't exist, it is created with a default
//tuple ("f1","f2)

import org.cloudifysource.dsl.context.* 
import com.gigaspaces.streaming.client.*

//list available streams
context = ServiceContextFactory.getServiceContext()
id=context.instanceId

locator=context.attributes.thisService["locator"] 
space=context.attributes.thisService["space"] 
count=Integer.valueOf(context.attributes.thisService["count"] )
streamname=context.attributes.thisService["streamname"] 

fos=new FileOutputStream("/tmp/out")
fos.write("loc=${locator} space=${space} sname=${streamname}\n".getBytes())
fos.close()

factory = new XAPStreamFactory(locator,space)
xts=null
factory.listStreams().each{
	if(it == streamname){
		xts=factory.openTupleStream(streamname)
		return;
	}
}
if(xts==null){
	xts=factory.createNewTupleStream(streamname,0,["f1","f2"])
}

assert (xts!=null),"stream ${streamname} not found in space ${space}"

fields=xts.getStreamConfig().getFields()

(0..count-1).each{ val->
	tuple=xts.createTuple()
	fields.each{
		tuple.setProperty(it,"${it}_${id}_${val}")
	}
	xts.writeBatch(tuple)
}

"wrote ${count} tuples"
