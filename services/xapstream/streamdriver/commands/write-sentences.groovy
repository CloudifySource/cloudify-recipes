//Writes random tuples to specified stream
//If the stream doesn't exist, it is created with a default
//tuple ("f1","f2)

import org.cloudifysource.dsl.context.* 
import com.gigaspaces.streaming.client.*

//the sentences

sentences=[
"the cow jumped over the moon",
"the man went to the store and bought some candy",
"four score and seven years ago",
"how many apples can you eat",
"to be or not to be the person",
"all good men should come to the aid of their country",
"this is the end beautiful friend",
"in the long run we are all dead",
]

//list available streams
context = ServiceContextFactory.getServiceContext()
id=context.instanceId

locator=context.attributes.thisService["locator"] 
space=context.attributes.thisService["space"] 
count=Integer.valueOf(context.attributes.thisService["count"] )
streamname=context.attributes.thisService["streamname"] 

factory = new XAPStreamFactory(locator,space)
xts=null
factory.listStreams().each{
	if(it == streamname){
		xts=factory.openTupleStream(streamname)
		return;
	}
}
if(xts==null){
	xts=factory.createNewTupleStream(streamname,0,["sentence"])
}

assert (xts!=null),"stream ${streamname} not found in space ${space}"

fields=xts.getStreamConfig().getFields()

i=0
count.times{ 
	if (i>=sentences.size()) i=0
	tuple=xts.createTuple()
	tuple.setProperty(fields[0],sentences[i])
	xts.writeBatch(tuple)
	i++
}

"wrote ${count} tuples"

