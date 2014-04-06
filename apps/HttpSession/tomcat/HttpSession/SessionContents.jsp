<%@page import="java.util.*"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html" />
<title>HTTP Session Content</title>

<link type="text/css" rel="stylesheet" href="<c:url value="/style.css"/>"/>

<script type="text/javascript">
	function edit(key, value) {
		var n=document.getElementsByName('dataname');
		var v = document.getElementsByName('datavalue');
		
		n[0].value = key;
		v[0].value = value;
	}

	function remove(key) {
		var a=document.getElementsByName('dataaction');
		var n=document.getElementsByName('dataname');
		//var v = document.getElementsByName('datavalue');
		a[0].value = 'R';
		n[0].value= key;
		document.getElementById('sessionForm').submit();
	}
</script>
</head>
<body>
	<img src="giga.jpg" alt="" />
	<h3>Session Contents</h3>
	<%
		Date created = new Date(session.getCreationTime());
		Date accessed = new Date(session.getLastAccessedTime());
		String hostName = request.getServerName();
		String sessionId = session.getId();
	%>
	<br />
	<table>
		<tr>
			<td>Session creation time:</td>
			<td><%=created%></td>
		</tr>
		<tr>
			<td>Session Last access time:</td>
			<td><%=accessed%></td>
		</tr>
		<tr>
			<td>Servlet container host name:</td>
			<td><%=hostName%></td>
		</tr>
		<tr>
			<td>Session id:</td>
			<td><%=sessionId%></td>
		</tr>
	</table>
	<h3>Session Contents:</h3>
	<p />

	<form id="sessionForm" action="<%=response.encodeURL("UpdateSessionServlet")%>"
		method="post">
		Name of Session Attribute: <input type="hidden" name="dataaction" />

		<input type="text" size="20" name="dataname" /> <br /> Value of
		Session Attribute: <input type="text" size="20" name="datavalue" /> <br />
		<input type="submit" value="Update Session" />
	</form>

	<p />
	
	<table style="width:100%">
	<tr>
		<th>Key</th>
		<th>Value</th>
	</tr>
		<%
			Enumeration names = session.getAttributeNames();
			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				String value = session.getAttribute(name).toString();
		%>
		<tr>
			<td><%=name%></td>
			<td><%=value%></td>
			<td><a href="javascript:edit('<%=name%>','<%=value%>');">Edit</a>
				&nbsp;|&nbsp;<a href="javascript:remove('<%=name%>')">Delete</a></td>
		</tr>

		<%
			}
		%>
	</table>
		
</body>
</html>