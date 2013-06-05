import org.cloudifysource.dsl.context.* 
import com.gigaspaces.streaming.client.*

//list available streams
context = ServiceContextFactory.getServiceContext()

locator=context.attributes.thisService["locator"] 
spacename=context.attributes.thisService["spacename"] 

sb=new StringBuilder()
factory = new XAPStreamFactory(locator,spacename)
factory.listStreams().each{ sb.append(it).append("\n") }
sb.toString()
