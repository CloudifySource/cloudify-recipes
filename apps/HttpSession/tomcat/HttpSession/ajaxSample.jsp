<%-- <!doctype html public "-//w3c//dtd html 4.0 transitional//en">

<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<!-- RichFaces tag library declaration -->
<%@ taglib uri="http://richfaces.org/a4j" prefix="a4j"%>
<%@ taglib uri="http://richfaces.org/rich" prefix="rich"%>


<html>
<head>
<title>RichFaces Greeter</title>
</head>
<body>
	<f:view>
		<a4j:form>
			<rich:panel header="RichFaces Greeter" style="width: 315px">
				<h:outputText value="Your name: " />
				<h:inputText value="#{user.name}">
					<f:validateLength minimum="1" maximum="30" />
				</h:inputText>
				<a4j:commandButton value="Get greeting" reRender="greeting" />
				<h:panelGroup id="greeting">
					<h:outputText value="Hello, " rendered="#{not empty user.name}" />
					<h:outputText value="#{user.name}" />
					<h:outputText value="!" rendered="#{not empty user.name}" />
				</h:panelGroup>
			</rich:panel>
		</a4j:form>
	</f:view>
</body>
</html> --%>