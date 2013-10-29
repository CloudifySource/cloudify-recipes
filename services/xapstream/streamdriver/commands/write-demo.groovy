import org.cloudifysource.dsl.context.* 
import com.gigaspaces.streaming.client.*
import com.gigaspaces.streaming.model.*

//writes moby dick, over and over

String[] lines= new File('commands/mobydick.txt').text.split("\\n")


//list available streams

context = ServiceContextFactory.getServiceContext()
id=context.instanceId

locator=context.attributes.thisService["locator"] 
space=context.attributes.thisService["space"] 

numsecs=Integer.valueOf(context.attributes.thisService["numsecs"] )//how long to write
rate=Integer.valueOf(context.attributes.thisService["rate"] ) //lines per sec

streamname=context.attributes.thisService["streamname"] 


factory = new XAPStreamFactory("jini://*/*/${space}?locators=${locator}")


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

	sleepfor=(start+1000)-System.currentTimeMillis()
	if(sleepfor>0)Thread.sleep(sleepfor)
}


